package com.backfunctionimpl.travel.travelFlight.dto;

import com.backfunctionimpl.global.error.ErrorCode;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlightSearchResDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean success;
    private List<FlightInfo> flights;
}