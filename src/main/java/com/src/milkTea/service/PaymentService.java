package com.src.milkTea.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.src.milkTea.dto.request.MomoIPNRequest;
import com.src.milkTea.dto.request.MomoPaymentRequest;
import com.src.milkTea.entities.CashDrawer;
import com.src.milkTea.entities.Orders;
import com.src.milkTea.entities.Payment;
import com.src.milkTea.enums.OrderStatusEnum;
import com.src.milkTea.enums.PaymentMethodEnum;
import com.src.milkTea.enums.TransactionEnum;
import com.src.milkTea.exception.CashDrawerException;
import com.src.milkTea.exception.NotFoundException;
import com.src.milkTea.exception.OrderException;
import com.src.milkTea.exception.TransactionException;
import com.src.milkTea.repository.CashDrawerRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

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

    @Autowired
    private CashDrawerRepository cashDrawerRepository;

    @Transactional
    public Map<String, Object> createMomoPayment(Long orderId, String paymentMethod, Long promotionId) throws Exception {

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
        // Xử lí promotion cho payment
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
            Orders order = orderRepository.findById(Long.valueOf(orderId)).orElseThrow(() -> new NotFoundException("Order not found"));
            order.setStatus(OrderStatusEnum.PREPARING);
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

    /**
     * Xử lý thanh toán bằng tiền mặt
     * @param orderId ID của đơn hàng cần thanh toán
     * @return Thông báo kết quả thanh toán
     * @throws NotFoundException nếu không tìm thấy đơn hàng
     * @throws TransactionException nếu đơn hàng không ở trạng thái hợp lệ
     */
    @Transactional
    public Map<String, Object> paymentWithCash(Long orderId) {
        // 1. Kiểm tra két tiền
        CashDrawer drawer = cashDrawerRepository.findByDateAndIsOpenTrue(LocalDate.now())
            .orElseThrow(() -> new CashDrawerException("Cash drawer is not open"));


        // 2. Kiểm tra và lấy thông tin đơn hàng
        Orders order = orderRepository.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order not found"));

        if (order.getStatus() != OrderStatusEnum.PENDING) {
            throw new OrderException("Order status must be PENDING to proceed with cash payment");
        }

        // 3. Tính toán số tiền
        double orderAmount = order.getTotalPrice();


        // 5. Tạo payment mới
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(String.valueOf((int)orderAmount));
        payment.setStatus(TransactionEnum.SUCCESS);
        payment.setPaymentMethod(PaymentMethodEnum.CASH);
        payment.setCashDrawer(drawer);
        payment.setMessage("Thanh toán tiền mặt thành công");
        paymentRepository.save(payment);

        // 6. Cập nhật số dư két
        drawer.setCurrentBalance(drawer.getCurrentBalance() + orderAmount);
        cashDrawerRepository.save(drawer);

        // 7. Cập nhật trạng thái đơn hàng
        order.setStatus(OrderStatusEnum.PREPARING);
        orderRepository.save(order);

        // 8. Trả về kết quả
        return Map.of(
            "message", "Thanh toán thành công, vui lòng chờ nhận hàng!",
            "orderId", orderId,
            "orderAmount", orderAmount,
            "drawerBalance", drawer.getCurrentBalance(),
            "status", "SUCCESS"
        );
    }

    /**
     * Hủy thanh toán Momo và chuyển sang thanh toán tiền mặt
     * @param orderId ID của đơn hàng cần chuyển đổi
     * @return Kết quả thanh toán tiền mặt
     */
    @Transactional
    public Map<String, Object> switchToPaymentWithCash(Long orderId) {
        // Kiểm tra và lấy thông tin đơn hàng
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        // Kiểm tra trạng thái đơn hàng
        if (order.getStatus() == OrderStatusEnum.PAID) {
            return Map.of("message", "Order has been paid!");
        }

        // Kiểm tra payment hiện tại
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Payment information not found"));

        // Chỉ cho phép chuyển đổi nếu đang thanh toán Momo và trong trạng thái PENDING
        if (payment.getPaymentMethod() != PaymentMethodEnum.MOMO || payment.getStatus() != TransactionEnum.PENDING) {
            throw new TransactionException("Cannot switch payment method in current status");
        }

        // Đánh dấu payment Momo là FAILED và chuyển sang CASH
        payment.setPaymentMethod(PaymentMethodEnum.CASH);
        payment.setStatus(TransactionEnum.SUCCESS);
        payment.setMessage("User cancelled Momo payment and switched to cash payment");
        paymentRepository.save(payment);

        // Cập nhật trạng thái đơn hàng
        order.setStatus(OrderStatusEnum.PAID);
        orderRepository.save(order);

        return Map.of(
            "message", "Successfully switched to cash payment!",
            "orderId", orderId,
            "amount", order.getTotalPrice(),
            "status", "SUCCESS"
        );
    }
}
