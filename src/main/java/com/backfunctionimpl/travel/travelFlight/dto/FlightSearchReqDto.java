package com.backfunctionimpl.travel.travelFlight.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Data
public class FlightSearchReqDto {
    private String origin;
    private String destination;
    private String departureDate;
    private boolean realTime; // 실시간 요청 플래그

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlightSearchReqDto that = (FlightSearchReqDto) o;
        return Objects.equals(origin, that.origin) &&
                Objects.equals(destination, that.destination) &&
                Objects.equals(departureDate, that.departureDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, destination, departureDate);
    }
}
