package com.backfunctionimpl.travel.travelFlight.dto;

import com.backfunctionimpl.global.error.ErrorCode;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
public class FlightSearchResDto {
    private boolean success;
    private FlightSearchData data;
    private Error error;

    public FlightSearchResDto() {
        this.success = false;
    }

    public FlightSearchResDto(List<FlightInfo> flights) {
        this.success = true;
        this.data = new FlightSearchData(flights);
    }

    @Data
    public static class FlightSearchData {
        private List<FlightInfo> flights;

        public FlightSearchData(List<FlightInfo> flights) {
            this.flights = flights;
        }
    }

    @Data
    public static class Error {
        private String code;
        private String message;

        public Error(String code, String message) {
            this.code = code;
            this.message = message;
        }
    }
}