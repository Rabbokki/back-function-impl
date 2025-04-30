package com.backfunctionimpl.travel.travelFlight.controller;

import com.backfunctionimpl.global.dto.ResponseDto;
import com.backfunctionimpl.travel.travelFlight.dto.FlightSearchReqDto;
import com.backfunctionimpl.travel.travelFlight.dto.FlightSearchResDto;
import com.backfunctionimpl.travel.travelFlight.service.FlightSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {
    private final FlightSearchService flightSearchService;

    @PostMapping("/search")
    public ResponseDto<FlightSearchResDto> searchFlights(
            @RequestBody FlightSearchReqDto reqDto){
        FlightSearchResDto resDto = flightSearchService.searchFlights(reqDto);
        return ResponseDto.success(resDto);
    }
}
