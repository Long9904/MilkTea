package com.src.milkTea.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.src.milkTea.dto.request.MomoIPNRequest;
import com.src.milkTea.dto.request.MomoPaymentRequest;
import com.src.milkTea.entities.Orders;
import com.src.milkTea.entities.Payment;
import com.src.milkTea.enums.OrderStatusEnum;
import com.src.milkTea.enums.PaymentMethodEnum;
import com.src.milkTea.enums.TransactionEnum;
import com.src.milkTea.exception.NotFoundException;
import com.src.milkTea.exception.TransactionException;
import com.src.milkTea.repository.OrderRepository;
import com.src.milkTea.repository.PaymentRepository;
import com.src.milkTea.utils.MomoSignatureUtil;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
public class PaymentService {

    @Value("${momo.partnerCode}")
    private String partnerCode;

    @Value("${momo.accessKey}")
    private String accessKey;

    @Value("${momo.secretKey}")
    private String secretKey;

    @Value("${momo.endpoint}")
    private String endpoint;

    @Value("${momo.redirectUrl}")
    private String redirectUrl;

    @Value("${momo.ipnUrl}")
    private String ipnUrl;


    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public Map<String, Object> createMomoPayment(Long orderId, String paymentMethod) throws Exception {

        if (paymentMethod.equals("CASH")) {
            return Map.of("message", "Re-choosing payment method!");
            // Xử lý khi chọn phương thức thanh toán bằng tiền mặt
        }

        Orders order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found"));

        String amount = String.valueOf((int) order.getTotalPrice());

        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy'T'HHmmss");
        String formatted = now.format(formatter);

        String requestId = orderId + "ORDER" + formatted; // thay bằng custom requestId (orderId + timestamp)
        String orderInfo = "Thanh toán đơn hàng #" + requestId + " tại Milk Tea: " + amount + " VNĐ";
        String extraData = "";

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setRequestId(requestId);
        payment.setAmount(amount);
        payment.setStatus(TransactionEnum.PENDING);
        payment.setPaymentMethod(PaymentMethodEnum.MOMO);

        paymentRepository.save(payment);

        String customOrderId = order.getId().toString() + "T" + formatted;

        String rawSignature = "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + customOrderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=captureWallet";
        String signature = "";

        try {
            // Tạo chữ ký bằng HMAC SHA256
            signature = MomoSignatureUtil.signSHA256(rawSignature, secretKey);
        } catch (Exception e) {
            throw new TransactionException("Error while signing: " + e.getMessage());
        } // Exception này có thể do không tìm thấy class MomoSignatureUtil hoặc do lỗi trong quá trình ký

        MomoPaymentRequest request = new MomoPaymentRequest(partnerCode,
                accessKey,
                requestId,
                amount,
                customOrderId,
                orderInfo,
                redirectUrl,
                ipnUrl,
                extraData,
                "captureWallet",
                signature, "vi");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MomoPaymentRequest> entity = new HttpEntity<>(request, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);

        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(response.getBody(), new TypeReference<>() {
        });
    }


    public void processMomoIPN(MomoIPNRequest request) {
        String customOrderId = request.getOrderId();  // Lấy customOrderId (ví dụ: "123T270425T122804")
        String orderId = customOrderId.split("T")[0];  // "123"
        String resultCode = request.getResultCode();

        Payment transaction = paymentRepository.findByOrderId(Long.parseLong(orderId)).orElseThrow(() -> new NotFoundException("Order not found"));

        if ("0".equals(resultCode)) {
            transaction.setStatus(TransactionEnum.SUCCESS);
            // Thực hiện các hành động cần thiết sau khi thanh toán thành công
            // Update order status
            System.out.println("Payment successful for order ID: " + orderId);
            Orders order = orderRepository.findById(Long.valueOf(orderId)).orElseThrow(() -> new NotFoundException("Order not found"));
            order.setStatus(OrderStatusEnum.PAID);
            orderRepository.save(order);
            System.out.println("Order status updated to PAID for order ID: " + orderId);
        } else {
            transaction.setStatus(TransactionEnum.FAILED);
        }

        transaction.setMessage(request.getMessage());
        paymentRepository.save(transaction);
    }

    public void updatePaymentStatus(String orderId, String status) {
        Payment payment = paymentRepository.findByOrderId(Long.parseLong(orderId)).orElseThrow(() -> new NotFoundException("Order not found"));
        if (status.equals("SUCCESS")) {
            payment.setStatus(TransactionEnum.SUCCESS);
        } else if (status.equals("FAILED")) {
            payment.setStatus(TransactionEnum.FAILED);
        } else {
            throw new TransactionException("Invalid status");
        }
        paymentRepository.save(payment);
    }

    public Map<String, Object> reMomoPayment(Long orderId) throws Exception {

        Orders order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found"));
        if (order.getStatus() == OrderStatusEnum.PAID) {
            return Map.of("message", "Order has been paid!");
        }

        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(() -> new NotFoundException("Payment not found"));
        if (payment.getStatus() == TransactionEnum.SUCCESS) {
            return Map.of("message", "Order has been paid!");
        }
        String amount = String.valueOf((int) order.getTotalPrice());

        LocalDateTime now = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyy'T'HHmmss");
        String formatted = now.format(formatter);

        String requestId = orderId + "ORDER" + formatted; // thay bằng custom requestId (orderId + timestamp)
        String orderInfo = "Thanh toán đơn hàng #" + requestId + " tại Milk Tea: " + amount + " VNĐ";
        String extraData = "";

        payment.setRequestId(requestId); // new requestId
        payment.setAmount(amount);
        payment.setStatus(TransactionEnum.PENDING);//
        payment.setPaymentMethod(PaymentMethodEnum.MOMO);

        paymentRepository.save(payment);

        String customOrderId = order.getId().toString() + "T" + formatted;

        String rawSignature = "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + customOrderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=captureWallet";
        String signature = "";

        try {
            // Tạo chữ ký bằng HMAC SHA256
            signature = MomoSignatureUtil.signSHA256(rawSignature, secretKey);
        } catch (Exception e) {
            throw new TransactionException("Error while signing: " + e.getMessage());
        } // Exception này có thể do không tìm thấy class MomoSignatureUtil hoặc do lỗi trong quá trình ký

        MomoPaymentRequest request = new MomoPaymentRequest(partnerCode,
                accessKey,
                requestId,
                amount,
                customOrderId,
                orderInfo,
                redirectUrl,
                ipnUrl,
                extraData,
                "captureWallet",
                signature, "vi");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MomoPaymentRequest> entity = new HttpEntity<>(request, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);

        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(response.getBody(), new TypeReference<>() {
        });
    }

    public Map<String, Object> paymentWithCash(Long orderId, String paymentMethod) {
        Orders order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found"));
        if (order.getStatus() == OrderStatusEnum.PAID) {
            return Map.of("message", "Order has been paid!");
        }

        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(() -> new NotFoundException("Payment not found"));
        if (payment.getStatus() == TransactionEnum.SUCCESS) {
            return Map.of("message", "Order has been paid!");
        }
        payment.setStatus(TransactionEnum.SUCCESS);
        payment.setPaymentMethod(PaymentMethodEnum.CASH);
        paymentRepository.save(payment);

        order.setStatus(OrderStatusEnum.PAID);
        orderRepository.save(order);

        return Map.of("message", "Payment successful! Please wait for the order to be processed.");
    }
}
