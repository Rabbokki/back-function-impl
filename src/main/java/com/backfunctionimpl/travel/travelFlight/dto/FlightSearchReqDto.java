package com.backfunctionimpl.travel.travelFlight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
public class FlightSearchReqDto implements Serializable {
    private static final long serialVersionUID = 1L;

    @NotBlank(message = "Origin is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Origin must be a 3-letter IATA code")
    @JsonProperty("origin")
    private String origin;

    @NotBlank(message = "Destination is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Destination must be a 3-letter IATA code")
    @JsonProperty("destination")
    private String destination;

    @NotBlank(message = "Departure date is required")
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Departure date must be in YYYY-MM-DD format")
    @JsonProperty("departureDate")
    private String departureDate;

    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Return date must be in YYYY-MM-DD format")
    @JsonProperty("returnDate")
    private String returnDate;

    @JsonProperty("realTime")
    private boolean realTime;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FlightSearchReqDto that = (FlightSearchReqDto) o;
        return realTime == that.realTime &&
                Objects.equals(origin, that.origin) &&
                Objects.equals(destination, that.destination) &&
                Objects.equals(departureDate, that.departureDate) &&
                Objects.equals(returnDate, that.returnDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(origin, destination, departureDate, returnDate, realTime);
    }
}