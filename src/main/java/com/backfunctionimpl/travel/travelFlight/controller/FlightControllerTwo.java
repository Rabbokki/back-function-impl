package com.backfunctionimpl.travel.travelFlight.controller;

import com.amadeus.exceptions.ResponseException;
import com.amadeus.resources.Location;
import com.backfunctionimpl.global.dto.ResponseDto;
import com.backfunctionimpl.global.error.CustomException;
import com.backfunctionimpl.global.error.ErrorCode;
import com.backfunctionimpl.travel.config.AmadeusClient;
import com.backfunctionimpl.travel.travelFlight.dto.FlightSearchReqDto;
import com.backfunctionimpl.travel.travelFlight.dto.FlightSearchResDto;
import com.backfunctionimpl.travel.travelFlight.dto.LocationDTO;
import com.backfunctionimpl.travel.travelFlight.service.FlightSearchServiceTwo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Slf4j
public class FlightControllerTwo {
    private final FlightSearchServiceTwo flightSearchService;
    private final AmadeusClient amadeusClient;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FlightSearchResDto> searchFlights(@RequestBody FlightSearchReqDto reqDto) {
        log.info("항공편 검색 요청: {}", reqDto);

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
    }

    @PostMapping("/save")
    public ResponseEntity<ResponseDto<Long>> saveFlight(@Valid @RequestBody FlightSearchReqDto reqDto, @RequestParam Long travelPlanId) {
        log.info("항공편 저장 요청: {}, travelPlanId: {}", reqDto, travelPlanId);
        try {
            Long flightId = flightSearchService.saveFlight(reqDto, travelPlanId);
            log.info("항공편 저장 성공, ID: {}", flightId);
            return ResponseEntity.ok(ResponseDto.success(flightId));
        } catch (CustomException e) {
            log.error("항공편 저장 오류: {}", e.getMessage());
            return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                    .body(ResponseDto.fail(e.getErrorCode().getCode(), e.getErrorCode().getMessage()));
        } catch (HttpMessageNotReadableException e) {
            log.error("JSON 파싱 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseDto.fail(ErrorCode.INVALID_JSON.getCode(), ErrorCode.INVALID_JSON.getMessage()));
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
    public ResponseEntity<Map<String, Object>> autocomplete(@RequestParam String term) {
        log.info("자동완성 요청, 검색어: {}", term);
        Map<String, Object> response = new HashMap<>();
        try {
            JsonNode locations = amadeusClient.searchLocations(term);
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
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Amadeus 자동완성 오류: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("data", null);
            response.put("error", Map.of("message", e.getMessage()));
            return ResponseEntity.badRequest().body(response);
        }
    }
}