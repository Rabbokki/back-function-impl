package com.backfunctionimpl.travel.travelFlight.data;

import com.backfunctionimpl.travel.travelFlight.dto.FlightInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MockFlightData {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Map<String, JsonNode> LOCATIONS = new HashMap<>();
    private static final Map<String, List<FlightInfo>> FLIGHTS = new HashMap<>();
    private static final Random random = new Random();

    static {
        // 도시/공항 목록 (HARDCODED_LOCATIONS와 유사)
        ArrayNode seoulLocations = mapper.createArrayNode();
        ObjectNode icnAirport = mapper.createObjectNode();
        icnAirport.put("type", "location");
        icnAirport.put("subType", "AIRPORT");
        icnAirport.put("detailedName", "Seoul/KR: Incheon");
        icnAirport.put("iataCode", "ICN");
        seoulLocations.add(icnAirport);
        LOCATIONS.put("Seoul", seoulLocations);

        ArrayNode tokyoLocations = mapper.createArrayNode();
        ObjectNode nrtAirport = mapper.createObjectNode();
        nrtAirport.put("type", "location");
        nrtAirport.put("subType", "AIRPORT");
        nrtAirport.put("detailedName", "Tokyo/JP: Narita");
        nrtAirport.put("iataCode", "NRT");
        tokyoLocations.add(nrtAirport);
        LOCATIONS.put("Tokyo", tokyoLocations);

        ArrayNode parisLocations = mapper.createArrayNode();
        ObjectNode cdgAirport = mapper.createObjectNode();
        cdgAirport.put("type", "location");
        cdgAirport.put("subType", "AIRPORT");
        cdgAirport.put("detailedName", "Paris/FR: Charles de Gaulle");
        cdgAirport.put("iataCode", "CDG");
        parisLocations.add(cdgAirport);
        LOCATIONS.put("Paris", parisLocations);

        ArrayNode nyLocations = mapper.createArrayNode();
        ObjectNode jfkAirport = mapper.createObjectNode();
        jfkAirport.put("type", "location");
        jfkAirport.put("subType", "AIRPORT");
        jfkAirport.put("detailedName", "New York/US: John F. Kennedy");
        jfkAirport.put("iataCode", "JFK");
        nyLocations.add(jfkAirport);
        LOCATIONS.put("New York", nyLocations);

        ArrayNode londonLocations = mapper.createArrayNode();
        ObjectNode lhrAirport = mapper.createObjectNode();
        lhrAirport.put("type", "location");
        lhrAirport.put("subType", "AIRPORT");
        lhrAirport.put("detailedName", "London/GB: Heathrow");
        lhrAirport.put("iataCode", "LHR");
        londonLocations.add(lhrAirport);
        LOCATIONS.put("London", londonLocations);

        ArrayNode singaporeLocations = mapper.createArrayNode();
        ObjectNode sinAirport = mapper.createObjectNode();
        sinAirport.put("type", "location");
        sinAirport.put("subType", "AIRPORT");
        sinAirport.put("detailedName", "Singapore/SG: Changi");
        sinAirport.put("iataCode", "SIN");
        singaporeLocations.add(sinAirport);
        LOCATIONS.put("Singapore", singaporeLocations);

        // 항공편 데이터
        // CDG -> JFK
        List<FlightInfo> cdgToJfkFlights = new ArrayList<>();
        cdgToJfkFlights.add(createFlight("FL001", "CDG", "JFK", "Air France", "AF", "AF123", "08:00:00", 7, 500000, "350"));
        cdgToJfkFlights.add(createFlight("FL002", "CDG", "JFK", "Air France", "AF", "AF124", "12:00:00", 7, 550000, "350"));
        cdgToJfkFlights.add(createFlight("FL003", "CDG", "JFK", "Delta Air Lines", "DL", "DL456", "18:00:00", 7, 600000, "767"));
        FLIGHTS.put("CDG-JFK", cdgToJfkFlights);

        // JFK -> CDG
        List<FlightInfo> jfkToCdgFlights = new ArrayList<>();
        jfkToCdgFlights.add(createFlight("FL004", "JFK", "CDG", "Air France", "AF", "AF125", "09:00:00", 7, 520000, "350"));
        jfkToCdgFlights.add(createFlight("FL005", "JFK", "CDG", "Air France", "AF", "AF126", "15:00:00", 7, 570000, "350"));
        jfkToCdgFlights.add(createFlight("FL006", "JFK", "CDG", "Delta Air Lines", "DL", "DL457", "21:00:00", 7, 610000, "767"));
        FLIGHTS.put("JFK-CDG", jfkToCdgFlights);

        // LHR -> SIN
        List<FlightInfo> lhrToSinFlights = new ArrayList<>();
        lhrToSinFlights.add(createFlight("FL007", "LHR", "SIN", "British Airways", "BA", "BA789", "10:00:00", 13, 800000, "787"));
        lhrToSinFlights.add(createFlight("FL008", "LHR", "SIN", "British Airways", "BA", "BA790", "16:00:00", 13, 850000, "787"));
        FLIGHTS.put("LHR-SIN", lhrToSinFlights);

        // SIN -> LHR
        List<FlightInfo> sinToLhrFlights = new ArrayList<>();
        sinToLhrFlights.add(createFlight("FL009", "SIN", "LHR", "British Airways", "BA", "BA791", "11:00:00", 13, 820000, "787"));
        sinToLhrFlights.add(createFlight("FL010", "SIN", "LHR", "British Airways", "BA", "BA792", "17:00:00", 13, 870000, "787"));
        FLIGHTS.put("SIN-LHR", sinToLhrFlights);

        // ICN -> NRT
        List<FlightInfo> icnToNrtFlights = new ArrayList<>();
        icnToNrtFlights.add(createFlight("FL011", "ICN", "NRT", "대한항공", "KE", "KE701", "09:00:00", 2, 200000, "737"));
        icnToNrtFlights.add(createFlight("FL012", "ICN", "NRT", "아시아나항공", "OZ", "OZ101", "14:00:00", 2, 220000, "320"));
        FLIGHTS.put("ICN-NRT", icnToNrtFlights);

        // NRT -> ICN
        List<FlightInfo> nrtToIcnFlights = new ArrayList<>();
        nrtToIcnFlights.add(createFlight("FL013", "NRT", "ICN", "대한항공", "KE", "KE702", "10:00:00", 2, 210000, "737"));
        nrtToIcnFlights.add(createFlight("FL014", "NRT", "ICN", "아시아나항공", "OZ", "OZ102", "15:00:00", 2, 230000, "320"));
        FLIGHTS.put("NRT-ICN", nrtToIcnFlights);
    }

    private static FlightInfo createFlight(String id, String departureAirport, String arrivalAirport, String carrier,
                                           String carrierCode, String flightNumber, String departureTime, int hours,
                                           int price, String aircraftCode) {
        FlightInfo flight = new FlightInfo();
        flight.setId(id);
        flight.setDepartureAirport(departureAirport);
        flight.setArrivalAirport(arrivalAirport);
        flight.setCarrier(carrier);
        flight.setCarrierCode(carrierCode);
        flight.setFlightNumber(flightNumber);
        flight.setAircraft(aircraftMap.getOrDefault(aircraftCode, "Unknown Aircraft"));
        flight.setPrice(String.valueOf(price));
        flight.setCurrency("KRW");
        flight.setCabinBaggage("Weight: 20kg");
        flight.setNumberOfBookableSeats(50);
        flight.setDuration(String.format("PT%dH", hours));
        // 날짜는 요청 시 조정
        flight.setDepartureTime(departureTime);
        flight.setArrivalTime(LocalDateTime.parse("2025-01-01T" + departureTime)
                .plusHours(hours)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return flight;
    }

    public static JsonNode getLocations(String term) {
        String normalizedTerm = term.trim().toLowerCase();
        List<JsonNode> matchingLocations = new ArrayList<>();
        for (Map.Entry<String, JsonNode> entry : LOCATIONS.entrySet()) {
            if (entry.getKey().toLowerCase().contains(normalizedTerm)) {
                matchingLocations.add(entry.getValue());
            }
        }
        ArrayNode result = mapper.createArrayNode();
        matchingLocations.forEach(node -> result.addAll((ArrayNode) node));
        return result;
    }

    public static List<FlightInfo> getFlights(String origin, String destination, String departureDate, String returnDate) {
        List<FlightInfo> results = new ArrayList<>();
        String routeKey = origin + "-" + destination;
        List<FlightInfo> outboundFlights = FLIGHTS.getOrDefault(routeKey, Collections.emptyList());

        for (FlightInfo flight : outboundFlights) {
            FlightInfo copy = new FlightInfo();
            copy.setId(flight.getId() + "-" + UUID.randomUUID().toString().substring(0, 8));
            copy.setDepartureAirport(flight.getDepartureAirport());
            copy.setArrivalAirport(flight.getArrivalAirport());
            copy.setCarrier(flight.getCarrier());
            copy.setCarrierCode(flight.getCarrierCode());
            copy.setFlightNumber(flight.getFlightNumber());
            copy.setAircraft(flight.getAircraft());
            copy.setPrice(String.valueOf(Integer.parseInt(flight.getPrice()) + random.nextInt(50000)));
            copy.setCurrency(flight.getCurrency());
            copy.setCabinBaggage(flight.getCabinBaggage());
            copy.setNumberOfBookableSeats(flight.getNumberOfBookableSeats());
            copy.setDuration(flight.getDuration());

            // 출발 날짜 조정
            LocalDateTime baseTime = LocalDateTime.parse(departureDate + "T" + flight.getDepartureTime().split("T")[1]);
            copy.setDepartureTime(baseTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            copy.setArrivalTime(baseTime.plusHours(Long.parseLong(flight.getDuration().replace("PT", "").replace("H", "")))
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            results.add(copy);
        }

        if (returnDate != null && !returnDate.isEmpty()) {
            String returnRouteKey = destination + "-" + origin;
            List<FlightInfo> returnFlights = FLIGHTS.getOrDefault(returnRouteKey, Collections.emptyList());
            List<FlightInfo> combinedFlights = new ArrayList<>();

            for (FlightInfo outbound : results) {
                for (FlightInfo inbound : returnFlights) {
                    FlightInfo combined = new FlightInfo();
                    combined.setId(outbound.getId() + "-RT");
                    combined.setDepartureAirport(outbound.getDepartureAirport());
                    combined.setArrivalAirport(outbound.getArrivalAirport());
                    combined.setCarrier(outbound.getCarrier());
                    combined.setCarrierCode(outbound.getCarrierCode());
                    combined.setFlightNumber(outbound.getFlightNumber());
                    combined.setAircraft(outbound.getAircraft());
                    combined.setPrice(String.valueOf(Integer.parseInt(outbound.getPrice()) + Integer.parseInt(inbound.getPrice())));
                    combined.setCurrency(outbound.getCurrency());
                    combined.setCabinBaggage(outbound.getCabinBaggage());
                    combined.setNumberOfBookableSeats(Math.min(outbound.getNumberOfBookableSeats(), inbound.getNumberOfBookableSeats()));
                    combined.setDuration(outbound.getDuration());
                    combined.setDepartureTime(outbound.getDepartureTime());
                    combined.setArrivalTime(outbound.getArrivalTime());

                    // 귀국 여정
                    LocalDateTime returnBaseTime = LocalDateTime.parse(returnDate + "T" + inbound.getDepartureTime().split("T")[1]);
                    combined.setReturnDepartureAirport(inbound.getDepartureAirport());
                    combined.setReturnArrivalAirport(inbound.getArrivalAirport());
                    combined.setReturnCarrier(inbound.getCarrier());
                    combined.setReturnCarrierCode(inbound.getCarrierCode());
                    combined.setReturnFlightNumber(inbound.getFlightNumber());
                    combined.setReturnDuration(inbound.getDuration());
                    combined.setReturnDepartureTime(returnBaseTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    combined.setReturnArrivalTime(returnBaseTime.plusHours(Long.parseLong(inbound.getDuration().replace("PT", "").replace("H", "")))
                            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

                    combinedFlights.add(combined);
                }
            }
            return combinedFlights;
        }

        return results;
    }

    private static final Map<String, String> aircraftMap = new HashMap<>();
    static {
        aircraftMap.put("320", "Airbus A320");
        aircraftMap.put("321", "Airbus A321");
        aircraftMap.put("330", "Airbus A330");
        aircraftMap.put("350", "Airbus A350");
        aircraftMap.put("380", "Airbus A380");
        aircraftMap.put("737", "Boeing 737");
        aircraftMap.put("747", "Boeing 747");
        aircraftMap.put("757", "Boeing 757");
        aircraftMap.put("767", "Boeing 767");
        aircraftMap.put("777", "Boeing 777");
        aircraftMap.put("787", "Boeing 787");
    }
}
