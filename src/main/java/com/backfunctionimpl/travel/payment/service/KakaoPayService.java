package com.backfunctionimpl.travel.payment.service;

import com.backfunctionimpl.travel.payment.dto.PaymentRequest;
import com.backfunctionimpl.travel.payment.dto.PaymentResponse;
import com.backfunctionimpl.travel.payment.entity.Payment;
import com.backfunctionimpl.travel.payment.repository.PaymentRepository;
import com.backfunctionimpl.travel.travelFlight.entity.TravelFlight;
import com.backfunctionimpl.travel.travelFlight.repository.TravelFlightRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoPayService {

    private final PaymentRepository paymentRepository;
    private final TravelFlightRepository travelFlightRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kakaopay.cid}")
    private String cid;

    @Value("${kakaopay.admin-key}")
    private String adminKey;

    @Value("${kakaopay.api.base-url}")
    private String kakaoPayBaseUrl;

    @Value("${kakaopay.approval-url}")
    private String approvalUrl;

    @Value("${kakaopay.cancel-url}")
    private String cancelUrl;

    @Value("${kakaopay.fail-url}")
    private String failUrl;

    @Value("${kakaopay.api.ready-url}")
    private String readyUrl;

    @Transactional
    public PaymentResponse preparePayment(PaymentRequest request) {
        log.info("카카오페이 결제 준비 시작: flightId={}", request.getFlightId());
        log.debug("사용 중인 CID: {}, Admin Key: {}", cid, adminKey);
        try {
            // DNS 확인 디버깅
            try {
                InetAddress address = InetAddress.getByName("kapi.kakao.com");
                log.info("DNS 확인 성공: kapi.kakao.com -> {}", address.getHostAddress());
            } catch (UnknownHostException e) {
                log.error("DNS 확인 실패: kapi.kakao.com", e);
            }

            // 항공편 조회
            Optional<TravelFlight> flightOptional = travelFlightRepository.findByFlightId(request.getFlightId());
            if (flightOptional.isEmpty()) {
                log.error("항공편을 찾을 수 없습니다: flightId={}", request.getFlightId());
                return new PaymentResponse(false, "항공편을 찾을 수 없습니다.", null, null, null);
            }
            TravelFlight flight = flightOptional.get();
            log.debug("항공편 조회 성공: flight={}", flight);

            // 파트너 정보 생성
            String partnerOrderId = "ORDER_" + request.getFlightId() + "_" + System.currentTimeMillis();
            String partnerUserId = request.getContact().getEmail() != null
                    ? request.getContact().getEmail()
                    : "user_" + System.currentTimeMillis();
            log.debug("파트너 정보: partnerOrderId={}, partnerUserId={}", partnerOrderId, partnerUserId);

            // 카카오페이 결제 준비 요청
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "KakaoAK " + adminKey);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("cid", cid);
            params.add("partner_order_id", partnerOrderId);
            params.add("partner_user_id", partnerUserId);
            params.add("item_name", "항공권 (" + request.getPassengerCount() + "명)");
            params.add("quantity", String.valueOf(request.getPassengerCount()));
            params.add("total_amount", String.valueOf(request.getTotalPrice().intValue()));
            params.add("tax_free_amount", "0");
            params.add("approval_url", approvalUrl);
            params.add("cancel_url", cancelUrl);
            params.add("fail_url", failUrl);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
            log.info("카카오페이 결제 준비 요청: URL={}, Headers={}, Params={}", readyUrl, headers, params);

            // 카카오페이 API 호출
            Map<String, Object> response;
            try {
                response = restTemplate.postForObject(readyUrl, requestEntity, Map.class);
            } catch (HttpClientErrorException e) {
                log.error("카카오페이 API 호출 실패: 상태 코드={}, 응답 본문={}, 헤더={}",
                        e.getStatusCode(), e.getResponseBodyAsString(), e.getResponseHeaders(), e);
                return new PaymentResponse(false, "카카오페이 결제 준비 중 오류: " + e.getStatusText() + " - " + e.getResponseBodyAsString(), null, null, null);
            } catch (ResourceAccessException e) {
                if (e.getCause() instanceof UnknownHostException) {
                    log.error("카카오페이 서버에 연결할 수 없습니다: 호스트={}, 원인={}", readyUrl, e.getMessage(), e);
                    return new PaymentResponse(false, "카카오페이 서버에 연결할 수 없습니다: DNS 확인 실패", null, null, null);
                }
                log.error("카카오페이 API 호출 중 네트워크 오류: {}", e.getMessage(), e);
                return new PaymentResponse(false, "카카오페이 서버에 연결할 수 없습니다: " + e.getMessage(), null, null, null);
            }

            log.info("카카오페이 결제 준비 응답: {}", response);
            if (response == null || !response.containsKey("tid")) {
                log.error("카카오페이 결제 준비 응답이 유효하지 않습니다: response={}", response);
                return new PaymentResponse(false, "카카오페이 결제 준비 응답이 유효하지 않습니다.", null, null, null);
            }

            String tid = (String) response.get("tid");
            String qrCodeUrl = (String) response.get("next_redirect_mobile_url");
            log.debug("결제 준비 완료: tid={}, qrCodeUrl={}", tid, qrCodeUrl);

            // 결제 정보 저장
            Payment payment = new Payment();
            payment.setFlight(flight);
            payment.setTid(tid);
            payment.setPartnerOrderId(partnerOrderId);
            payment.setPartnerUserId(partnerUserId);
            payment.setStatus("PENDING");
            payment.setTotalAmount(request.getTotalPrice());
            payment.setPassengerCount(request.getPassengerCount());
            payment.setSeats(objectMapper.writeValueAsString(request.getSelectedSeats()));
            payment.setContactEmail(request.getContact().getEmail());
            payment.setContactPhone(request.getContact().getPhone());
            paymentRepository.save(payment);
            log.info("결제 정보 저장 완료: paymentId={}", payment.getId());

            // 응답 생성
            PaymentResponse paymentResponse = new PaymentResponse(true, null, tid, qrCodeUrl, null);
            paymentResponse.setPartnerOrderId(partnerOrderId);
            paymentResponse.setPartnerUserId(partnerUserId);
            log.info("카카오페이 결제 준비 성공: response={}", paymentResponse);
            return paymentResponse;

        } catch (Exception e) {
            log.error("카카오페이 결제 준비 중 오류: message={}, stackTrace={}", e.getMessage(), e.getStackTrace());
            return new PaymentResponse(false, "카카오페이 결제 준비 중 오류: " + e.getMessage(), null, null, null);
        }
    }

    @Transactional
    public PaymentResponse approvePayment(String tid, String pgToken, String partnerOrderId, String partnerUserId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "KakaoAK " + adminKey);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("cid", cid);
            params.add("tid", tid);
            params.add("partner_order_id", partnerOrderId);
            params.add("partner_user_id", partnerUserId);
            params.add("pg_token", pgToken);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
            log.info("카카오페이 결제 승인 요청: URL={}, Headers={}, Params={}",
                    kakaoPayBaseUrl + "/v1/payment/approve", headers, params);
            Map<String, Object> response;
            try {
                response = restTemplate.postForObject(kakaoPayBaseUrl + "/v1/payment/approve", requestEntity, Map.class);
            } catch (HttpClientErrorException e) {
                log.error("카카오페이 결제 승인 실패: 상태 코드={}, 응답 본문={}, 헤더={}",
                        e.getStatusCode(), e.getResponseBodyAsString(), e.getResponseHeaders(), e);
                return new PaymentResponse(false, "카카오페이 결제 승인 중 오류: " + e.getStatusText() + " - " + e.getResponseBodyAsString(), null, null, null);
            }

            log.info("카카오페이 결제 승인 응답: {}", response);
            if (response == null || !response.containsKey("tid")) {
                log.error("카카오페이 결제 승인 응답이 유효하지 않습니다: {}", response);
                return new PaymentResponse(false, "카카오페이 결제 승인에 실패했습니다.", null, null, null);
            }

            Optional<Payment> paymentOptional = paymentRepository.findByTid(tid);
            if (paymentOptional.isEmpty()) {
                log.error("결제 정보를 찾을 수 없습니다: tid={}", tid);
                return new PaymentResponse(false, "결제 정보를 찾을 수 없습니다.", null, null, null);
            }

            Payment payment = paymentOptional.get();
            payment.setStatus("SUCCESS");
            paymentRepository.save(payment);

            return new PaymentResponse(true, "결제가 완료되었습니다.", tid, null, null);
        } catch (Exception e) {
            log.error("카카오페이 결제 승인 중 오류: {}", e.getMessage(), e);
            return new PaymentResponse(false, "결제 승인 중 오류가 발생했습니다: " + e.getMessage(), null, null, null);
        }
    }

    public PaymentResponse checkPaymentStatus(String tid) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.set("Authorization", "KakaoAK " + adminKey);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("cid", cid);
            params.add("tid", tid);

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
            log.info("카카오페이 결제 상태 확인 요청: URL={}, Headers={}, Params={}",
                    kakaoPayBaseUrl + "/v1/payment/status", headers, params);
            Map<String, Object> response;
            try {
                response = restTemplate.postForObject(kakaoPayBaseUrl + "/v1/payment/status", requestEntity, Map.class);
            } catch (HttpClientErrorException e) {
                log.error("카카오페이 결제 상태 확인 실패: 상태 코드={}, 응답 본문={}, 헤더={}",
                        e.getStatusCode(), e.getResponseBodyAsString(), e.getResponseHeaders(), e);
                return new PaymentResponse(false, "카카오페이 결제 상태 확인 중 오류: " + e.getStatusText() + " - " + e.getResponseBodyAsString(), null, null, null);
            }

            log.info("카카오페이 결제 상태 확인 응답: {}", response);
            if (response == null || !response.containsKey("status")) {
                log.error("카카오페이 결제 상태 확인 응답이 유효하지 않습니다: {}", response);
                return new PaymentResponse(false, "결제 상태 확인에 실패했습니다.", null, null, null);
            }

            String status = (String) response.get("status");
            return new PaymentResponse(true, "결제 상태: " + status, tid, null, status);
        } catch (Exception e) {
            log.error("결제 상태 확인 중 오류: {}", e.getMessage(), e);
            return new PaymentResponse(false, "결제 상태 확인 중 오류가 발생했습니다: " + e.getMessage(), null, null, null);
        }
    }
}