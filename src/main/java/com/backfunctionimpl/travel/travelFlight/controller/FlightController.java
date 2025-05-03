package com.backfunctionimpl.travel.travelFlight.controller;

import com.backfunctionimpl.global.dto.ResponseDto;
import com.backfunctionimpl.global.error.CustomException;
import com.backfunctionimpl.global.error.ErrorCode;
import com.backfunctionimpl.travel.travelFlight.dto.FlightSearchReqDto;
import com.backfunctionimpl.travel.travelFlight.dto.FlightSearchResDto;
import com.backfunctionimpl.travel.travelFlight.service.FlightSearchService;
import com.fasterxml.jackson.core.JsonParseException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Slf4j
public class FlightController {
    private final FlightSearchService flightSearchService;

    @PostMapping("/search")
    public ResponseEntity<ResponseDto<FlightSearchResDto>> searchFlights(@Valid @RequestBody FlightSearchReqDto reqDto) {
        log.info("Received flight search request: {}", reqDto);
        try {
            FlightSearchResDto response = flightSearchService.searchFlights(reqDto);
            log.info("Returning {} flights for request: {}", response.getData().getFlights().size(), reqDto);
            return ResponseEntity.ok(ResponseDto.success(response));
        } catch (CustomException e) {
            log.error("Flight search error: {}", e.getMessage());
            return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                    .body(ResponseDto.fail(e.getErrorCode().getCode(), e.getErrorCode().getMessage()));
        } catch (HttpMessageNotReadableException e) {
            log.error("JSON parse error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseDto.fail(ErrorCode.INVALID_JSON.getCode(), ErrorCode.INVALID_JSON.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during flight search: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.fail(ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                            ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
        }
    }

    @PostMapping("/save")
    public ResponseEntity<ResponseDto<Long>> saveFlight(@Valid @RequestBody FlightSearchReqDto reqDto, @RequestParam Long travelPlanId) {
        log.info("Saving flight for request: {}, travelPlanId: {}", reqDto, travelPlanId);
        try {
            Long flightId = flightSearchService.saveFlight(reqDto, travelPlanId);
            log.info("Flight saved successfully with ID: {}", flightId);
            return ResponseEntity.ok(ResponseDto.success(flightId));
        } catch (CustomException e) {
            log.error("Flight save error: {}", e.getMessage());
            return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                    .body(ResponseDto.fail(e.getErrorCode().getCode(), e.getErrorCode().getMessage()));
        } catch (HttpMessageNotReadableException e) {
            log.error("JSON parse error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseDto.fail(ErrorCode.INVALID_JSON.getCode(), ErrorCode.INVALID_JSON.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during flight save: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.fail(ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                            ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
        }
    }

    @PostMapping("/clear-cache")
    public ResponseEntity<ResponseDto<String>> clearFlightCache(@Valid @RequestBody FlightSearchReqDto reqDto) {
        log.info("Clearing flight cache for request: {}", reqDto);
        try {
            flightSearchService.clearFlightCache(reqDto);
            log.info("Flight cache cleared successfully for request: {}", reqDto);
            return ResponseEntity.ok(ResponseDto.success("Cache cleared successfully"));
        } catch (Exception e) {
            log.error("Error clearing flight cache: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ResponseDto.fail(ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                            ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
        }
    }
}