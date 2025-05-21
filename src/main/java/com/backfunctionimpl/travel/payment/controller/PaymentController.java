package com.backfunctionimpl.travel.payment.controller;

import com.backfunctionimpl.travel.payment.dto.PaymentRequest;
import com.backfunctionimpl.travel.payment.dto.PaymentResponse;
import com.backfunctionimpl.travel.payment.service.KakaoPayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/kakaopay")
@RequiredArgsConstructor
public class PaymentController {

    private final KakaoPayService kakaoPayService;

    @PostMapping("/ready")
    public ResponseEntity<PaymentResponse> preparePayment(@RequestBody PaymentRequest request) {
        PaymentResponse response = kakaoPayService.preparePayment(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/approve")
    public ResponseEntity<PaymentResponse> approvePayment(
            @RequestParam String tid,
            @RequestParam String pg_token,
            @RequestParam String partner_order_id,
            @RequestParam String partner_user_id) {
        PaymentResponse response = kakaoPayService.approvePayment(tid, pg_token, partner_order_id, partner_user_id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{tid}")
    public ResponseEntity<PaymentResponse> checkPaymentStatus(@PathVariable String tid) {
        PaymentResponse response = kakaoPayService.checkPaymentStatus(tid);
        return ResponseEntity.ok(response);
    }
}
