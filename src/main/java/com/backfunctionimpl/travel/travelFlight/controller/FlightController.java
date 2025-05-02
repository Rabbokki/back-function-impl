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
    public ResponseEntity<FlightSearchResDto> searchFlights(@Valid @RequestBody FlightSearchReqDto reqDto) {
        try {
            FlightSearchResDto response = flightSearchService.searchFlights(reqDto);
            return ResponseEntity.ok(response);
        } catch (JsonParseException e) {
            FlightSearchResDto errorResponse = new FlightSearchResDto();
            errorResponse.setSuccess(false);
            errorResponse.setErrorCode(ErrorCode.INVALID_MESSAGE);
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            FlightSearchResDto errorResponse = new FlightSearchResDto();
            errorResponse.setSuccess(false);
            errorResponse.setErrorCode(ErrorCode.INVALID_MESSAGE);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @PostMapping("/save")
    public ResponseDto<Long> saveFlight(@Valid @RequestBody FlightSearchReqDto reqDto, @RequestParam Long travelPlanId) {
        try {
            Long flightId = flightSearchService.saveFlight(reqDto, travelPlanId);
            return ResponseDto.success(flightId);
        } catch (CustomException e) {
            return ResponseDto.fail(e.getErrorCode().getCode(), e.getMessage());
        } catch (HttpMessageNotReadableException e) {
            log.error("JSON parse error: {}", e.getMessage());
            return ResponseDto.fail(ErrorCode.INVALID_FLIGHT_SEARCH.getCode(), "Invalid JSON format in request body");
        } catch (Exception e) {
            log.error("Flight save error: {}", e.getMessage());
            return ResponseDto.fail(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "Internal server error");
        }
    }

    @PostMapping("/clear-cache")
    public ResponseDto<String> clearFlightCache(@Valid @RequestBody FlightSearchReqDto reqDto) {
        flightSearchService.clearFlightCache(reqDto);
        return ResponseDto.success("Cache cleared successfully");
    }
}
