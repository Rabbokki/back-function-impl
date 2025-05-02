package com.backfunctionimpl.travel.travelFlight.dto;

import com.backfunctionimpl.global.error.ErrorCode;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
public class FlightSearchResDto {
    private boolean success;
    private FlightSearchData data;
    private ErrorCode errorCode;
}