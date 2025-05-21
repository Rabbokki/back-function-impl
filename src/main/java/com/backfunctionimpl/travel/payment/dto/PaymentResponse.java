package com.backfunctionimpl.travel.payment.dto;

import lombok.Builder;
import lombok.Data;

@Data
public class PaymentResponse {
    private boolean success;
    private String message;
    private String tid;
    private String qrCodeUrl;
    private String status;
    private String partnerOrderId;
    private String partnerUserId;

    public PaymentResponse(boolean success, String message, String tid, String qrCodeUrl, String status) {
        this.success = success;
        this.message = message;
        this.tid = tid;
        this.qrCodeUrl = qrCodeUrl;
        this.status = status;
    }
}