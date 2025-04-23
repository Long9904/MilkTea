package com.src.milkTea.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MomoPaymentRequest {
    private String partnerCode;
    private String accessKey;
    private String requestId;
    private String amount;
    private String orderId; // Order ID from Order table
    private String orderInfo;
    private String redirectUrl;
    private String ipnUrl;
    private String extraData;
    private String requestType;
    private String signature;
    private String lang;
}
