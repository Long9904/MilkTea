package com.src.milkTea.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.src.milkTea.dto.request.MomoIPNRequest;
import com.src.milkTea.dto.request.MomoPaymentRequest;
import com.src.milkTea.entities.MomoTransaction;
import com.src.milkTea.enums.TransactionEnum;
import com.src.milkTea.exception.NotFoundException;
import com.src.milkTea.exception.TransactionException;
import com.src.milkTea.repository.MomoTransactionRepository;
import com.src.milkTea.utils.MomoSignatureUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Service
public class MomoPaymentService {

    @Value("${momo.partnerCode}")
    private String partnerCode;

    @Value("${momo.accessKey}")
    private String accessKey;

    @Value("${momo.secretKey}")
    private String secretKey;

    @Value("${momo.endpoint}")
    private String endpoint;

//    @Value("${momo.redirectUrl}")
    private String redirectUrl;

//    @Value("${momo.ipnUrl}")
    private String ipnUrl;


    @Autowired
    private MomoTransactionRepository momoTransactionRepository;


    public Map<String, Object> createMomoPayment(String amount) throws Exception {
        String orderId = UUID.randomUUID().toString(); // thay bằng orderId thực tế từ table Order
        String requestId = UUID.randomUUID().toString(); // thay bằng custom requestId (orderId + timestamp)
        String orderInfo = "Thanh toán đơn hàng #" + orderId;
        String extraData = "";

        MomoTransaction transaction = new MomoTransaction();
        transaction.setOrderId(orderId);
        transaction.setRequestId(requestId);
        transaction.setAmount(amount);
        transaction.setStatus(TransactionEnum.PENDING);

        momoTransactionRepository.save(transaction);

        String rawSignature = "accessKey=" + accessKey +
                "&amount=" + amount +
                "&extraData=" + extraData +
                "&ipnUrl=" + ipnUrl +
                "&orderId=" + orderId +
                "&orderInfo=" + orderInfo +
                "&partnerCode=" + partnerCode +
                "&redirectUrl=" + redirectUrl +
                "&requestId=" + requestId +
                "&requestType=captureWallet";
        String signature ="";
        try {
            // Tạo chữ ký bằng HMAC SHA256
          signature = MomoSignatureUtil.signSHA256(rawSignature, secretKey);
        } catch (Exception e) {
            throw new TransactionException("Error while signing: " + e.getMessage());
        } // Exception này có thể do không tìm thấy class MomoSignatureUtil hoặc do lỗi trong quá trình ký

        MomoPaymentRequest request = new MomoPaymentRequest(
                partnerCode, accessKey, requestId, amount,
                orderId, orderInfo, redirectUrl, ipnUrl,
                extraData, "captureWallet", signature, "vi"
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<MomoPaymentRequest> entity = new HttpEntity<>(request, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(endpoint, entity, String.class);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = mapper.readValue(response.getBody(), new TypeReference<>() {});

        return result;
    }


    public void processMomoIPN(MomoIPNRequest request) {
        String orderId = request.getOrderId();
        String resultCode = request.getResultCode();

        MomoTransaction transaction = momoTransactionRepository.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if ("0".equals(resultCode)) {
            transaction.setStatus(TransactionEnum.SUCCESS);
            // Thực hiện các hành động cần thiết sau khi thanh toán thành công
            // Update order status
        } else {
            transaction.setStatus(TransactionEnum.FAILED);
        }

        transaction.setMessage(request.getMessage());
        momoTransactionRepository.save(transaction);
    }
}
