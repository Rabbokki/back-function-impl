package com.backfunctionimpl.travel.travelFlight.dto;

import lombok.*;

import java.io.Serializable;
import java.util.List;

@Data
public class FlightSearchResDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<FlightInfo> flights;

    public FlightSearchResDto() {}

    public FlightSearchResDto(List<FlightInfo> flights) {
        this.flights = flights;
    }

    public List<FlightInfo> getFlights() {
        return flights;
    }

    public void setFlights(List<FlightInfo> flights) {
        this.flights = flights;
    }

    public static class FlightInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        private String price;

        public FlightInfo() {}

        public FlightInfo(String price) {
            this.price = price;
        }

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }
    }
}