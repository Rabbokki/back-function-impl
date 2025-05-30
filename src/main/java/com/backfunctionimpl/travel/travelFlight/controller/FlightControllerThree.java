package com.backfunctionimpl.travel.travelFlight.controller;

import com.backfunctionimpl.global.dto.ResponseDto;
import com.backfunctionimpl.global.error.CustomException;
import com.backfunctionimpl.global.error.ErrorCode;
import com.backfunctionimpl.global.security.user.UserDetailsImpl;
import com.backfunctionimpl.travel.config.AmadeusClient;
import com.backfunctionimpl.travel.travelFlight.data.MockFlightData;
import com.backfunctionimpl.travel.travelFlight.dto.*;
import com.backfunctionimpl.travel.travelFlight.service.FlightSearchServiceThree;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.internal.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Slf4j
public class FlightControllerThree {
    private final FlightSearchServiceThree flightSearchService;
    private final AmadeusClient amadeusClient;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FlightSearchResDto> searchFlights(@RequestBody FlightSearchReqDto reqDto) {
        log.info("항공편 검색 요청: {}", reqDto);
        try {
            FlightSearchResDto resDto = flightSearchService.searchFlights(reqDto);

            if (reqDto.isRealTime() &&
                    reqDto.getReturnDate() != null &&
                    !reqDto.getReturnDate().isEmpty()) {
                boolean hasReturn = resDto.getFlights().stream()
                        .anyMatch(f -> f.getReturnDepartureTime() != null && f.getReturnArrivalTime() != null);
                if (!hasReturn) {
                    log.warn("귀국 여정 없음! 반환된 항공편 수: {}", resDto.getFlights().size());
                }
            }

            return ResponseEntity.ok(resDto);
        } catch (CustomException e) {
            log.error("항공편 검색 오류: {}", e.getMessage());
            return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                    .body(new FlightSearchResDto(false, List.of()));
        } catch (Exception e) {
            log.error("항공편 검색 중 예기치 않은 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new FlightSearchResDto(false, List.of()));
        }
    }

    @PostMapping("/save")
    public ResponseEntity<ResponseDto<Long>> saveFlight(@Valid @RequestBody FlightSearchReqDto reqDto, @RequestParam("travelPlanId") Long travelPlanId ) {
        log.info("항공편 저장 요청: {}, travelPlanId: {}", reqDto, travelPlanId);
        try {
            Long travelFlightId = flightSearchService.saveFlight(reqDto, travelPlanId);
            log.info("항공편 저장 성공, TravelFlight ID: {}", travelFlightId);
            return ResponseEntity.ok(ResponseDto.success(travelFlightId));
        } catch (CustomException e) {
            log.error("항공편 저장 오류: {}", e.getMessage());
            return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                    .body(ResponseDto.fail(e.getErrorCode().getCode(), e.getErrorCode().getMessage()));
        } catch (Exception e) {
            log.error("항공편 저장 중 예기치 않은 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.fail(ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                            ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
        }
    }

    @PostMapping("/clear-cache")
    public ResponseEntity<ResponseDto<String>> clearFlightCache(@Valid @RequestBody FlightSearchReqDto reqDto) {
        log.info("항공편 캐시 삭제 요청: {}", reqDto);
        try {
            flightSearchService.clearFlightCache(reqDto);
            log.info("항공편 캐시 삭제 성공: {}", reqDto);
            return ResponseEntity.ok(ResponseDto.success("캐시 삭제 완료"));
        } catch (Exception e) {
            log.error("항공편 캐시 삭제 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.fail(ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                            ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
        }
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<Map<String, Object>> autocomplete(@RequestParam("term") String term) {
        log.info("자동완성 요청, 검색어: {}", term);
        Map<String, Object> response = new HashMap<>();
        try {
            JsonNode locations = MockFlightData.getLocations(term);
            List<Map<String, Object>> data = new ArrayList<>();
            Set<String> seenIataCodes = new HashSet<>();
            for (JsonNode location : locations) {
                String iataCode = location.path("iataCode").asText();
                if (seenIataCodes.contains(iataCode)) {
                    log.debug("중복된 IATA 코드 제외: {}", iataCode);
                    continue;
                }
                seenIataCodes.add(iataCode);
                Map<String, Object> item = new HashMap<>();
                item.put("detailedName", location.path("detailedName").asText());
                item.put("iataCode", iataCode);
                item.put("subType", location.path("subType").asText());
                data.add(item);
            }
            response.put("success", true);
            response.put("data", data);
            response.put("error", null);
            log.info("Autocomplete response, result count: {}", data.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("자동완성 오류: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("data", null);
            response.put("error", Map.of("message", e.getMessage()));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/detail/{flightId}")
    public ResponseEntity<ResponseDto<FlightInfo>> getFlightDetail(@PathVariable String flightId) {
        log.info("항공편 상세 조회 요청: flightId = {}", flightId);
        try {
            FlightInfo flightInfo = flightSearchService.getFlightDetail(flightId);
            if (flightInfo == null) {
                log.warn("항공편을 찾을 수 없음: flightId = {}", flightId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ResponseDto.fail(ErrorCode.FLIGHT_NOT_FOUND.getCode(),
                                ErrorCode.FLIGHT_NOT_FOUND.getMessage()));
            }
            log.info("항공편 상세 조회 성공: flightId = {}", flightId);
            return ResponseEntity.ok(ResponseDto.success(flightInfo));
        } catch (CustomException e) {
            log.error("항공편 상세 조회 오류: {}", e.getMessage());
            return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                    .body(ResponseDto.fail(e.getErrorCode().getCode(), e.getErrorCode().getMessage()));
        } catch (Exception e) {
            log.error("항공편 상세 조회 중 예기치 않은 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.fail(ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                            ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
        }
    }

    @GetMapping("/detail/by-travel-flight/{travelFlightId}")
    public ResponseEntity<ResponseDto<FlightInfo>> getFlightDetailByTravelFlightId(@PathVariable("travelFlightId") Long travelFlightId) {
        log.info("항공편 상세 조회 요청 (TravelFlight ID): travelFlightId = {}", travelFlightId);
        try {
            FlightInfo flightInfo = flightSearchService.getFlightDetailByTravelFlightId(travelFlightId);
            if (flightInfo == null) {
                log.warn("항공편을 찾을 수 없음: travelFlightId = {}", travelFlightId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ResponseDto.fail(ErrorCode.FLIGHT_NOT_FOUND.getCode(),
                                ErrorCode.FLIGHT_NOT_FOUND.getMessage()));
            }
            log.info("항공편 상세 조회 성공: travelFlightId = {}", travelFlightId);
            return ResponseEntity.ok(ResponseDto.success(flightInfo));
        } catch (CustomException e) {
            log.error("항공편 상세 조회 오류: {}", e.getMessage());
            return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                    .body(ResponseDto.fail(e.getErrorCode().getCode(), e.getErrorCode().getMessage()));
        } catch (Exception e) {
            log.error("항공편 상세 조회 중 예기치 않은 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.fail(ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                            ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
        }
    }

    @PostMapping("/book")
    public ResponseEntity<ResponseDto<?>> bookFlight(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody AccountFlightRequestDto requestDto) {

        try {
            if (userDetails == null || userDetails.getAccount() == null) {
                return ResponseEntity.badRequest()
                        .body(ResponseDto.fail("AUTH_ERROR", "로그인이 필요합니다."));
            }
            Long accountId = userDetails.getAccount().getId();
            flightSearchService.saveBooking(accountId, requestDto);
            String complete = "예약이 완료되었습니다.";

            return ResponseEntity.ok(ResponseDto.success(complete));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ResponseDto.fail("BOOKING_ERROR", "예약 처리 중 오류 발생: " + e.getMessage()));
        }
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<ResponseDto<List<AccountFlightResponseDto>>> getMyBookings(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        try {
            if (userDetails == null || userDetails.getAccount() == null) {
                return ResponseEntity.badRequest()
                        .body(ResponseDto.fail("AUTH_ERROR", "로그인이 필요합니다."));
            }
            Long accountId = userDetails.getAccount().getId();
            List<AccountFlightResponseDto> bookings = flightSearchService.getUserBookings(accountId);
            return ResponseEntity.ok(ResponseDto.success(bookings));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ResponseDto.fail("FETCH_ERROR", "예약 목록 조회 실패: " + e.getMessage()));
        }
    }
}
