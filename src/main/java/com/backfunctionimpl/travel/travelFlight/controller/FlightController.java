package com.backfunctionimpl.travel.travelFlight.controller;

import com.backfunctionimpl.global.dto.ResponseDto;
import com.backfunctionimpl.global.error.CustomException;
import com.backfunctionimpl.global.error.ErrorCode;
import com.backfunctionimpl.travel.travelFlight.dto.FlightSearchReqDto;
import com.backfunctionimpl.travel.travelFlight.dto.FlightSearchResDto;
import com.backfunctionimpl.travel.travelFlight.service.FlightSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
@Slf4j
public class FlightController {
    private final FlightSearchService flightSearchService;

    @PostMapping("/search")
    public ResponseDto<FlightSearchResDto> searchFlights(@RequestBody FlightSearchReqDto reqDto) {
        try {
            FlightSearchResDto result = flightSearchService.searchFlights(reqDto);
            return ResponseDto.success(result);
        } catch (CustomException e) {
            return ResponseDto.fail(e.getErrorCode().getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("Flight search error: {}", e.getMessage());
            return ResponseDto.fail(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), "서버 오류: " + e.getMessage());
        }
    }

    @PostMapping("/clear-cache")
    public ResponseDto<String> clearFlightCache(@RequestBody FlightSearchReqDto reqDto) {
        flightSearchService.clearFlightCache(reqDto);
        return ResponseDto.success("Cache cleared successfully");
    }
}
