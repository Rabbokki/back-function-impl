package com.backfunctionimpl.travel.payment.controller;

import com.backfunctionimpl.global.security.user.UserDetailsImpl;
import com.backfunctionimpl.travel.payment.dto.PaymentRequest;
import com.backfunctionimpl.travel.payment.dto.PaymentResponse;
import com.backfunctionimpl.travel.payment.service.KakaoPayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final KakaoPayService kakaoPayService;

    /**
     * 카카오페이 결제 준비 요청 처리
     * @param request 결제 요청 데이터
     * @return 결제 준비 응답
     */
    @PostMapping("/ready")
    public ResponseEntity<PaymentResponse> prepareKakaoPay(@RequestBody PaymentRequest request,
                                                           @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("카카오페이 결제 준비 요청: flightId={}", request.getFlightId());
        if (userDetails == null || userDetails.getAccount() == null) {
            return ResponseEntity.badRequest()
                    .body(new PaymentResponse(false, "로그인이 필요합니다.", null, null, null));
        }
        PaymentResponse response = kakaoPayService.preparePayment(request);
        return ResponseEntity.ok(response);
    }

    /**
     * 카카오페이 결제 상태 확인
     * @param tid 결제 고유 번호
     * @return 결제 상태 응답
     */
    @GetMapping("/status/{tid}")
    public ResponseEntity<PaymentResponse> checkKakaoPayStatus(@PathVariable("tid") String tid) {
        log.info("카카오페이 결제 상태 확인 요청: tid={}", tid);
        PaymentResponse response = kakaoPayService.checkPaymentStatus(tid);
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.BAD_REQUEST)
                .body(response);
    }

    /**
     * 결제 성공 리다이렉트 처리
     * @param pgToken 결제 인증 토큰
     * @param tid 결제 고유 번호
     * @return 프론트엔드 마이페이지로 리다이렉트
     */
    @GetMapping("/success")
    public ResponseEntity<Void> handlePaymentSuccess(@RequestParam("pg_token") String pgToken,
                                                     @RequestParam("tid") String tid) {
        log.info("결제 성공 리다이렉트: tid={}, pgToken={}", tid, pgToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("http://localhost:3000/mypage?tid=" + tid));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    /**
     * 결제 취소 리다이렉트 처리
     * @return 프론트엔드 취소 페이지로 리다이렉트
     */
    @GetMapping("/cancel")
    public ResponseEntity<Void> handlePaymentCancel() {
        log.info("결제 취소 리다이렉트");
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("http://localhost:3000/payment/cancel"));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    /**
     * 결제 실패 리다이렉트 처리
     * @return 프론트엔드 실패 페이지로 리다이렉트
     */
    @GetMapping("/fail")
    public ResponseEntity<Void> handlePaymentFail() {
        log.info("결제 실패 리다이렉트");
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("http://localhost:3000/payment/fail"));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}