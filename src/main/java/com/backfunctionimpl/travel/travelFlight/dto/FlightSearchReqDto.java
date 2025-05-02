package com.backfunctionimpl.travel.travelFlight.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.Objects;

@Data
public class FlightSearchReqDto {
    @NotBlank(message = "Origin city is required")
    private String origin;

    @NotBlank(message = "Destination city is required")
    private String destination;

    @NotBlank(message = "Departure date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Departure date must be in YYYY-MM-DD format")
    private String departureDate;

    private boolean realTime;

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