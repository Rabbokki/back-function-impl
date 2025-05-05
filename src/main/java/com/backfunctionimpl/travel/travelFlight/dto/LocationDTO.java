package com.backfunctionimpl.travel.travelFlight.dto;

public class LocationDTO {
    private final String detailedName;
    private final String iataCode;

    public LocationDTO(String detailedName, String iataCode) {
        this.detailedName = detailedName;
        this.iataCode = iataCode;
    }

    public String getDetailedName() {
        return detailedName;
    }

    public String getIataCode() {
        return iataCode;
    }
}
