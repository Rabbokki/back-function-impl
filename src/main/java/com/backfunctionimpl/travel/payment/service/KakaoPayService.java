package com.backfunctionimpl.travel.payment.service;

import com.backfunctionimpl.global.error.CustomException;
import com.backfunctionimpl.global.error.ErrorCode;
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
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;

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

    @Value("${kakaopay.secret-key}")
    private String secretKey;

    @Value("${kakaopay.ready-url}")
    private String readyUrl;

    @Value("${kakaopay.approve-api-url}")
    private String approveApiUrl;

    @Value("${kakaopay.cancel-api-url}")
    private String cancelApiUrl;

    @Value("${kakaopay.order-api-url}")
    private String orderApiUrl;

    @Value("${kakaopay.approval-url}")
    private String approvalUrl;

    @Value("${kakaopay.cancel-url}")
    private String cancelRedirectUrl;

    @Value("${kakaopay.fail-url}")
    private String failUrl;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private String normalizePartnerUserId(String userId) {
        if (userId == null || userId.trim().isEmpty() || !EMAIL_PATTERN.matcher(userId).matches()) {
            String normalized = "user_" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 20) + "@example.com";
            log.warn("유효하지 않은 partner_user_id: {}. 대체 값 사용: {}", userId, normalized);
            return normalized;
        }
        String sanitized = userId.replaceAll("[^a-zA-Z0-9@._-]", "").substring(0, Math.min(userId.length(), 100));
        if (!EMAIL_PATTERN.matcher(sanitized).matches()) {
            String normalized = "user_" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 20) + "@example.com";
            log.warn("정규화 후에도 유효하지 않은 partner_user_id: {}. 대체 값 사용: {}", sanitized, normalized);
            return normalized;
        }
        return sanitized;
    }

    @Transactional
    public PaymentResponse preparePayment(PaymentRequest request) {
        log.info("카카오페이 결제 준비 시작: flightId={}", request.getFlightId());
        log.debug("사용 중인 CID: {}, Secret Key: {}", cid, secretKey);

        try {
            // RestTemplate 메시지 컨버터 설정
            boolean hasJsonConverter = false;
            for (HttpMessageConverter<?> converter : restTemplate.getMessageConverters()) {
                if (converter instanceof MappingJackson2HttpMessageConverter) {
                    hasJsonConverter = true;
                    break;
                }
            }
            if (!hasJsonConverter) {
                restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
            }
            restTemplate.getMessageConverters().add(new StringHttpMessageConverter(StandardCharsets.UTF_8));

            // DNS 확인
            try {
                InetAddress address = InetAddress.getByName("open-api.kakaopay.com");
                log.info("DNS 확인 성공: open-api.kakaopay.com -> {}", address.getHostAddress());
            } catch (UnknownHostException e) {
                log.error("DNS 확인 실패: open-api.kakaopay.com", e);
            }

            // 요청 파라미터 검증
            if (request.getFlightId() == null || request.getFlightId().trim().isEmpty()) {
                log.error("flightId가 누락되었습니다.");
                return new PaymentResponse(false, "항공편 ID가 누락되었습니다.", null, null, null);
            }
            if (request.getPassengerCount() <= 0) {
                log.error("잘못된 탑승객 수: passengerCount={}", request.getPassengerCount());
                return new PaymentResponse(false, "탑승객 수는 1명 이상이어야 합니다.", null, null, null);
            }
            if (request.getTotalPrice() == null || request.getTotalPrice().intValue() <= 0) {
                log.error("잘못된 결제 금액: totalPrice={}", request.getTotalPrice());
                return new PaymentResponse(false, "결제 금액이 유효하지 않습니다.", null, null, null);
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
            String partnerUserId = normalizePartnerUserId(
                    request.getContact() != null ? request.getContact().getEmail() : null
            );
            String itemName = request.getItemName() != null ? request.getItemName() : "AirTicket_" + request.getPassengerCount();
            log.debug("파트너 정보: partnerOrderId={}, partnerUserId={}, itemName={}", partnerOrderId, partnerUserId, itemName);

            // 카카오페이 결제 준비 요청 (JSON 형식)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "SECRET_KEY " + secretKey);

            Map<String, Object> params = new HashMap<>();
            params.put("cid", cid);
            params.put("partner_order_id", partnerOrderId);
            params.put("partner_user_id", partnerUserId);
            params.put("item_name", itemName);
            params.put("quantity", request.getPassengerCount());
            params.put("total_amount", request.getTotalPrice().intValue());
            params.put("tax_free_amount", 0);
            params.put("approval_url", approvalUrl);
            params.put("cancel_url", cancelRedirectUrl);
            params.put("fail_url", failUrl);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(params, headers);
            log.info("카카오페이 결제 준비 요청: URL={}, Headers={}, Params={}", readyUrl, headers, params);

            // 카카오페이 API 호출
            Map<String, Object> response;
            try {
                response = restTemplate.postForObject(readyUrl, requestEntity, Map.class);
                log.debug("카카오페이 API 응답 본문: {}", response);
            } catch (HttpClientErrorException e) {
                log.error("카카오페이 API 호출 실패: 상태 코드={}, 응답 본문={}, 헤더={}",
                        e.getStatusCode(), e.getResponseBodyAsString(), e.getResponseHeaders());
                String errorMessage = String.format("카카오페이 결제 준비 중 오류: %s - %s", e.getStatusText(), e.getResponseBodyAsString());
                return new PaymentResponse(false, errorMessage, null, null, null);
            } catch (ResourceAccessException e) {
                if (e.getCause() instanceof UnknownHostException) {
                    log.error("카카오페이 서버에 연결할 수 없습니다: 호스트={}, 원인={}", readyUrl, e.getMessage());
                    return new PaymentResponse(false, "카카오페이 서버에 연결할 수 없습니다: DNS 확인 실패", null, null, null);
                }
                log.error("카카오페이 API 호출 중 네트워크 오류: {}", e.getMessage());
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
            payment.setContactEmail(request.getContact() != null ? request.getContact().getEmail() : null);
            payment.setContactPhone(request.getContact() != null ? request.getContact().getPhone() : null);
            paymentRepository.save(payment);
            log.info("결제 정보 저장 완료: paymentId={}", payment.getId());

            // 응답 생성
            PaymentResponse paymentResponse = new PaymentResponse(true, null, tid, qrCodeUrl, null);
            paymentResponse.setPartnerOrderId(partnerOrderId);
            paymentResponse.setPartnerUserId(partnerUserId);
            log.info("카카오페이 결제 준비 성공: response={}", paymentResponse);
            return paymentResponse;

        } catch (Exception e) {
            log.error("카카오페이 결제 준비 중 예외 발생: message={}, stackTrace={}", e.getMessage(), e.getStackTrace());
            return new PaymentResponse(false, "카카오페이 결제 준비 중 오류: " + e.getMessage(), null, null, null);
        }
    }

    @Transactional
    public PaymentResponse approvePayment(String tid, String pgToken, String partnerOrderId, String partnerUserId) {
        try {
            if (tid == null || tid.trim().isEmpty()) {
                log.error("tid가 누락되었습니다.");
                return new PaymentResponse(false, "결제 고유번호(tid)가 누락되었습니다.", null, null, null);
            }
            if (pgToken == null || pgToken.trim().isEmpty()) {
                log.error("pgToken이 누락되었습니다.");
                return new PaymentResponse(false, "결제 인증 토큰(pgToken)이 누락되었습니다.", null, null, null);
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "SECRET_KEY " + secretKey);

            Map<String, Object> params = new HashMap<>();
            params.put("cid", cid);
            params.put("tid", tid);
            params.put("partner_order_id", partnerOrderId);
            params.put("partner_user_id", normalizePartnerUserId(partnerUserId));
            params.put("pg_token", pgToken);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(params, headers);
            log.info("카카오페이 결제 승인 요청: URL={}, Headers={}, Params={}", approveApiUrl, headers, params);

            Map<String, Object> response;
            try {
                response = restTemplate.postForObject(approveApiUrl, requestEntity, Map.class);
                log.debug("카카오페이 결제 승인 응답 본문: {}", response);
            } catch (HttpClientErrorException e) {
                log.error("카카오페이 결제 승인 실패: 상태 코드={}, 응답 본문={}, 헤더={}",
                        e.getStatusCode(), e.getResponseBodyAsString(), e.getResponseHeaders());
                String errorMessage = String.format("카카오페이 결제 승인 중 오류: %s - %s", e.getStatusText(), e.getResponseBodyAsString());
                return new PaymentResponse(false, errorMessage, null, null, null);
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

            return new PaymentResponse(true, "결제가 완료되었습니다.", tid, null, "SUCCESS");
        } catch (Exception e) {
            log.error("카카오페이 결제 승인 중 오류: {}", e.getMessage(), e);
            return new PaymentResponse(false, "결제 승인 중 오류가 발생했습니다: " + e.getMessage(), null, null, null);
        }
    }

    @Transactional(readOnly = true)
    public PaymentResponse checkPaymentStatus(String tid) {
        try {
            if (tid == null || tid.trim().isEmpty()) {
                log.error("tid가 누락되었습니다.");
                return new PaymentResponse(false, "결제 고유번호(tid)가 누락되었습니다.", tid, null, null);
            }

            // payment 테이블에서 tid 조회
            Payment payment = paymentRepository.findByTid(tid)
                    .orElseThrow(() -> {
                        log.error("결제 정보를 찾을 수 없습니다: tid={}", tid);
                        return new CustomException(ErrorCode.PAYMENT_NOT_FOUND, "결제 정보를 찾을 수 없습니다: tid=" + tid);
                    });

            // 샌드박스 테스트를 위해 상태를 SUCCESS로 가정
            log.info("샌드박스 테스트: tid={}에 대해 SUCCESS 상태 반환", tid);
            payment.setStatus("SUCCESS");
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);
            return new PaymentResponse(true, "결제 상태: SUCCESS", tid, null, "SUCCESS");

            // 실제 프로덕션 코드 (주석 처리)
        /*
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "SECRET_KEY " + secretKey);

        Map<String, Object> params = new HashMap<>();
        params.put("cid", cid);
        params.put("tid", tid);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(params, headers);
        log.info("카카오페이 결제 상태 확인 요청: URL={}, Headers={}, Params={}", orderApiUrl, headers, params);

        Map<String, Object> response = restTemplate.postForObject(orderApiUrl, requestEntity, Map.class);
        log.debug("카카오페이 결제 상태 확인 응답 본문: {}", response);

        if (response == null || !response.containsKey("status")) {
            log.error("카카오페이 결제 상태 확인 응답이 유효하지 않습니다: {}", response);
            return new PaymentResponse(false, "결제 상태 확인 응답이 유효하지 않습니다.", tid, null, null);
        }

        String status = (String) response.get("status");
        log.info("결제 상태 확인 성공: tid={}, status={}", tid, status);
        return new PaymentResponse(true, "결제 상태: " + status, tid, null, status);
        */
        } catch (CustomException e) {
            log.error("결제 상태 확인 중 사용자 정의 예외: tid={}, message={}", tid, e.getMessage());
            return new PaymentResponse(false, e.getMessage(), tid, null, null);
        } catch (Exception e) {
            log.error("결제 상태 확인 중 예외 발생: tid={}, message={}", tid, e.getMessage(), e);
            return new PaymentResponse(false, "결제 상태 확인 중 오류: " + e.getMessage(), tid, null, null);
        }
    }
}