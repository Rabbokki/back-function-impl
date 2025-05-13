package com.backfunctionimpl.travel.travelFlight.data;

import com.backfunctionimpl.travel.travelFlight.dto.FlightInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MockFlightData {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Map<String, JsonNode> LOCATIONS = new HashMap<>();
    private static final Random random = new Random();
    private static final Map<String, String> aircraftMap = new HashMap<>();
    private static final Map<String, String> koreanToEnglishCityMap = new HashMap<>();
    private static final Map<String, List<String>> countryCodeToAirline = new HashMap<>();
    private static final List<String> globalAirlines = Arrays.asList(
            "Qatar Airways|QR|787", "Lufthansa|LH|350", "Delta Air Lines|DL|767", "United Airlines|UA|787"
    );

    static {
        try {
            // aircraftMap 초기화
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

            // 한글-영어 도시명 매핑
            koreanToEnglishCityMap.put("서울", "Seoul");
            koreanToEnglishCityMap.put("인천", "Seoul");
            koreanToEnglishCityMap.put("도쿄", "Tokyo");
            koreanToEnglishCityMap.put("파리", "Paris");
            koreanToEnglishCityMap.put("뉴욕", "New York");
            koreanToEnglishCityMap.put("런던", "London");
            koreanToEnglishCityMap.put("싱가포르", "Singapore");
            koreanToEnglishCityMap.put("방콕", "Bangkok");
            koreanToEnglishCityMap.put("홍콩", "Hong Kong");
            koreanToEnglishCityMap.put("로스앤젤레스", "Los Angeles");
            koreanToEnglishCityMap.put("시드니", "Sydney");
            koreanToEnglishCityMap.put("두바이", "Dubai");
            koreanToEnglishCityMap.put("상하이", "Shanghai");
            koreanToEnglishCityMap.put("프랑크푸르트", "Frankfurt");
            koreanToEnglishCityMap.put("암스테르담", "Amsterdam");
            koreanToEnglishCityMap.put("마드리드", "Madrid");
            koreanToEnglishCityMap.put("로마", "Rome");

            // 국가 코드별 주요 항공사
            countryCodeToAirline.put("KR", Arrays.asList("대한항공|KE|777", "아시아나항공|OZ|330"));
            countryCodeToAirline.put("JP", Arrays.asList("Japan Airlines|JL|737", "All Nippon Airways|NH|787"));
            countryCodeToAirline.put("FR", Arrays.asList("Air France|AF|350"));
            countryCodeToAirline.put("US", Arrays.asList("American Airlines|AA|777", "Delta Air Lines|DL|767"));
            countryCodeToAirline.put("GB", Arrays.asList("British Airways|BA|787"));
            countryCodeToAirline.put("SG", Arrays.asList("Singapore Airlines|SQ|380"));
            countryCodeToAirline.put("TH", Arrays.asList("Thai Airways|TG|787"));
            countryCodeToAirline.put("HK", Arrays.asList("Cathay Pacific|CX|350"));
            countryCodeToAirline.put("AU", Arrays.asList("Qantas|QF|330"));
            countryCodeToAirline.put("AE", Arrays.asList("Emirates|EK|380"));
            countryCodeToAirline.put("CN", Arrays.asList("China Eastern|MU|330", "Shanghai Airlines|FM|737"));
            countryCodeToAirline.put("DE", Arrays.asList("Lufthansa|LH|350"));
            countryCodeToAirline.put("NL", Arrays.asList("KLM|KL|737"));
            countryCodeToAirline.put("ES", Arrays.asList("Iberia|IB|320"));
            countryCodeToAirline.put("IT", Arrays.asList("Alitalia|AZ|320"));

            // 도시/공항 목록
            // 서울 (ICN)
            ArrayNode seoulLocations = mapper.createArrayNode();
            ObjectNode icnAirport = mapper.createObjectNode();
            icnAirport.put("type", "location");
            icnAirport.put("subType", "AIRPORT");
            icnAirport.put("detailedName", "Seoul/KR: Incheon");
            icnAirport.put("iataCode", "ICN");
            seoulLocations.add(icnAirport);
            LOCATIONS.put("Seoul", seoulLocations);

            // 파리 (CDG)
            ArrayNode parisLocations = mapper.createArrayNode();
            ObjectNode cdgAirport = mapper.createObjectNode();
            cdgAirport.put("type", "location");
            cdgAirport.put("subType", "AIRPORT");
            cdgAirport.put("detailedName", "Paris/FR: Charles de Gaulle");
            cdgAirport.put("iataCode", "CDG");
            parisLocations.add(cdgAirport);
            LOCATIONS.put("Paris", parisLocations);

            // 도쿄 (NRT, HND)
            ArrayNode tokyoLocations = mapper.createArrayNode();
            ObjectNode nrtAirport = mapper.createObjectNode();
            nrtAirport.put("type", "location");
            nrtAirport.put("subType", "AIRPORT");
            nrtAirport.put("detailedName", "Tokyo/JP: Narita");
            nrtAirport.put("iataCode", "NRT");
            tokyoLocations.add(nrtAirport);
            ObjectNode hndAirport = mapper.createObjectNode();
            hndAirport.put("type", "location");
            hndAirport.put("subType", "AIRPORT");
            hndAirport.put("detailedName", "Tokyo/JP: Haneda");
            hndAirport.put("iataCode", "HND");
            tokyoLocations.add(hndAirport);
            LOCATIONS.put("Tokyo", tokyoLocations);

            // 홍콩 (HKG)
            ArrayNode hongKongLocations = mapper.createArrayNode();
            ObjectNode hkgAirport = mapper.createObjectNode();
            hkgAirport.put("type", "location");
            hkgAirport.put("subType", "AIRPORT");
            hkgAirport.put("detailedName", "Hong Kong/HK: Hong Kong International");
            hkgAirport.put("iataCode", "HKG");
            hongKongLocations.add(hkgAirport);
            LOCATIONS.put("Hong Kong", hongKongLocations);

            // 뉴욕 (JFK)
            ArrayNode newYorkLocations = mapper.createArrayNode();
            ObjectNode jfkAirport = mapper.createObjectNode();
            jfkAirport.put("type", "location");
            jfkAirport.put("subType", "AIRPORT");
            jfkAirport.put("detailedName", "New York/US: John F. Kennedy");
            jfkAirport.put("iataCode", "JFK");
            newYorkLocations.add(jfkAirport);
            LOCATIONS.put("New York", newYorkLocations);

            // 런던 (LHR)
            ArrayNode londonLocations = mapper.createArrayNode();
            ObjectNode lhrAirport = mapper.createObjectNode();
            lhrAirport.put("type", "location");
            lhrAirport.put("subType", "AIRPORT");
            lhrAirport.put("detailedName", "London/GB: Heathrow");
            lhrAirport.put("iataCode", "LHR");
            londonLocations.add(lhrAirport);
            LOCATIONS.put("London", londonLocations);

            // 싱가포르 (SIN)
            ArrayNode singaporeLocations = mapper.createArrayNode();
            ObjectNode sinAirport = mapper.createObjectNode();
            sinAirport.put("type", "location");
            sinAirport.put("subType", "AIRPORT");
            sinAirport.put("detailedName", "Singapore/SG: Changi");
            sinAirport.put("iataCode", "SIN");
            singaporeLocations.add(sinAirport);
            LOCATIONS.put("Singapore", singaporeLocations);

            // 방콕 (BKK)
            ArrayNode bangkokLocations = mapper.createArrayNode();
            ObjectNode bkkAirport = mapper.createObjectNode();
            bkkAirport.put("type", "location");
            bkkAirport.put("subType", "AIRPORT");
            bkkAirport.put("detailedName", "Bangkok/TH: Suvarnabhumi");
            bkkAirport.put("iataCode", "BKK");
            bangkokLocations.add(bkkAirport);
            LOCATIONS.put("Bangkok", bangkokLocations);

            // 로스앤젤레스 (LAX)
            ArrayNode losAngelesLocations = mapper.createArrayNode();
            ObjectNode laxAirport = mapper.createObjectNode();
            laxAirport.put("type", "location");
            laxAirport.put("subType", "AIRPORT");
            laxAirport.put("detailedName", "Los Angeles/US: Los Angeles International");
            laxAirport.put("iataCode", "LAX");
            losAngelesLocations.add(laxAirport);
            LOCATIONS.put("Los Angeles", losAngelesLocations);

            // 시드니 (SYD)
            ArrayNode sydneyLocations = mapper.createArrayNode();
            ObjectNode sydAirport = mapper.createObjectNode();
            sydAirport.put("type", "location");
            sydAirport.put("subType", "AIRPORT");
            sydAirport.put("detailedName", "Sydney/AU: Sydney Kingsford Smith");
            sydAirport.put("iataCode", "SYD");
            sydneyLocations.add(sydAirport);
            LOCATIONS.put("Sydney", sydneyLocations);

            // 두바이 (DXB)
            ArrayNode dubaiLocations = mapper.createArrayNode();
            ObjectNode dxbAirport = mapper.createObjectNode();
            dxbAirport.put("type", "location");
            dxbAirport.put("subType", "AIRPORT");
            dxbAirport.put("detailedName", "Dubai/AE: Dubai International");
            dxbAirport.put("iataCode", "DXB");
            dubaiLocations.add(dxbAirport);
            LOCATIONS.put("Dubai", dubaiLocations);

            // 상하이 (PVG)
            ArrayNode shanghaiLocations = mapper.createArrayNode();
            ObjectNode pvgAirport = mapper.createObjectNode();
            pvgAirport.put("type", "location");
            pvgAirport.put("subType", "AIRPORT");
            pvgAirport.put("detailedName", "Shanghai/CN: Pudong");
            pvgAirport.put("iataCode", "PVG");
            shanghaiLocations.add(pvgAirport);
            LOCATIONS.put("Shanghai", shanghaiLocations);

            // 프랑크푸르트 (FRA)
            ArrayNode frankfurtLocations = mapper.createArrayNode();
            ObjectNode fraAirport = mapper.createObjectNode();
            fraAirport.put("type", "location");
            fraAirport.put("subType", "AIRPORT");
            fraAirport.put("detailedName", "Frankfurt/DE: Frankfurt Main");
            fraAirport.put("iataCode", "FRA");
            frankfurtLocations.add(fraAirport);
            LOCATIONS.put("Frankfurt", frankfurtLocations);

            // 암스테르담 (AMS)
            ArrayNode amsterdamLocations = mapper.createArrayNode();
            ObjectNode amsAirport = mapper.createObjectNode();
            amsAirport.put("type", "location");
            amsAirport.put("subType", "AIRPORT");
            amsAirport.put("detailedName", "Amsterdam/NL: Schiphol");
            amsAirport.put("iataCode", "AMS");
            amsterdamLocations.add(amsAirport);
            LOCATIONS.put("Amsterdam", amsterdamLocations);

            // 마드리드 (MAD)
            ArrayNode madridLocations = mapper.createArrayNode();
            ObjectNode madAirport = mapper.createObjectNode();
            madAirport.put("type", "location");
            madAirport.put("subType", "AIRPORT");
            madAirport.put("detailedName", "Madrid/ES: Adolfo Suárez Madrid–Barajas");
            madAirport.put("iataCode", "MAD");
            madridLocations.add(madAirport);
            LOCATIONS.put("Madrid", madridLocations);

            // 로마 (FCO)
            ArrayNode romeLocations = mapper.createArrayNode();
            ObjectNode fcoAirport = mapper.createObjectNode();
            fcoAirport.put("type", "location");
            fcoAirport.put("subType", "AIRPORT");
            fcoAirport.put("detailedName", "Rome/IT: Leonardo da Vinci–Fiumicino");
            fcoAirport.put("iataCode", "FCO");
            romeLocations.add(fcoAirport);
            LOCATIONS.put("Rome", romeLocations);

        } catch (Exception e) {
            System.err.println("MockFlightData initialization failed: " + e.getMessage());
            throw new RuntimeException("Failed to initialize MockFlightData", e);
        }
    }

    private static FlightInfo createFlight(String id, String departureAirport, String arrivalAirport, String carrier,
                                           String carrierCode, String flightNumber, String departureTime, int hours,
                                           int price, String aircraftCode) {
        if (aircraftMap == null) {
            throw new IllegalStateException("aircraftMap is not initialized");
        }

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

        try {
            LocalDateTime departureDateTime = LocalDateTime.parse("2025-01-01T" + departureTime);
            flight.setDepartureTime(departureDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            LocalDateTime arrivalDateTime = departureDateTime.plusHours(hours);
            flight.setArrivalTime(arrivalDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            // 시간대 설정
            flight.setDepartureTimeZone(getTimeZone(departureAirport));
            flight.setArrivalTimeZone(getTimeZone(arrivalAirport));
        } catch (Exception e) {
            System.err.println("Failed to parse date in createFlight: id=" + id + ", departureTime=" + departureTime);
            throw new RuntimeException("Invalid date format in createFlight", e);
        }

        return flight;
    }

    private static String getTimeZone(String iataCode) {
        Map<String, String> airportTimeZones = new HashMap<>();
        airportTimeZones.put("ICN", "Asia/Seoul");
        airportTimeZones.put("CDG", "Europe/Paris");
        airportTimeZones.put("LHR", "Europe/London");
        airportTimeZones.put("JFK", "America/New_York");
        airportTimeZones.put("NRT", "Asia/Tokyo");
        airportTimeZones.put("HND", "Asia/Tokyo");
        airportTimeZones.put("HKG", "Asia/Hong_Kong");
        airportTimeZones.put("SIN", "Asia/Singapore");
        airportTimeZones.put("BKK", "Asia/Bangkok");
        airportTimeZones.put("LAX", "America/Los_Angeles");
        airportTimeZones.put("SYD", "Australia/Sydney");
        airportTimeZones.put("DXB", "Asia/Dubai");
        airportTimeZones.put("PVG", "Asia/Shanghai");
        airportTimeZones.put("FRA", "Europe/Berlin");
        airportTimeZones.put("AMS", "Europe/Amsterdam");
        airportTimeZones.put("MAD", "Europe/Madrid");
        airportTimeZones.put("FCO", "Europe/Rome");
        return airportTimeZones.getOrDefault(iataCode, "UTC");
    }

    private static int estimateFlightDuration(String origin, String destination) {
        Map<String, Integer> durationMap = new HashMap<>();
        durationMap.put("ICN-CDG", 12);
        durationMap.put("CDG-ICN", 12);
        durationMap.put("ICN-NRT", 2);
        durationMap.put("NRT-ICN", 2);
        durationMap.put("ICN-HND", 2);
        durationMap.put("HND-ICN", 2);
        durationMap.put("ICN-HKG", 3);
        durationMap.put("HKG-ICN", 3);
        durationMap.put("ICN-JFK", 14);
        durationMap.put("JFK-ICN", 14);
        durationMap.put("ICN-LHR", 12);
        durationMap.put("LHR-ICN", 12);
        durationMap.put("ICN-SIN", 6);
        durationMap.put("SIN-ICN", 6);
        durationMap.put("ICN-BKK", 5);
        durationMap.put("BKK-ICN", 5);
        durationMap.put("ICN-LAX", 11);
        durationMap.put("LAX-ICN", 11);
        durationMap.put("ICN-SYD", 10);
        durationMap.put("SYD-ICN", 10);
        durationMap.put("ICN-DXB", 9);
        durationMap.put("DXB-ICN", 9);
        durationMap.put("ICN-PVG", 2);
        durationMap.put("PVG-ICN", 2);
        durationMap.put("ICN-FRA", 12);
        durationMap.put("FRA-ICN", 12);
        durationMap.put("ICN-AMS", 12);
        durationMap.put("AMS-ICN", 12);
        durationMap.put("ICN-MAD", 13);
        durationMap.put("MAD-ICN", 13);
        durationMap.put("ICN-FCO", 12);
        durationMap.put("FCO-ICN", 12);

        String key = origin + "-" + destination;
        String reverseKey = destination + "-" + origin;
        if (durationMap.containsKey(key)) {
            return durationMap.get(key);
        } else if (durationMap.containsKey(reverseKey)) {
            return durationMap.get(reverseKey);
        }
        String originCountry = getCountryCode(origin);
        String destCountry = getCountryCode(destination);
        if (originCountry != null && destCountry != null && originCountry.equals(destCountry)) {
            return 3; // 같은 대륙 내
        }
        return 10; // 대륙 간
    }

    private static String getCountryCode(String iataCode) {
        for (JsonNode locations : LOCATIONS.values()) {
            for (JsonNode location : locations) {
                if (iataCode.equals(location.get("iataCode").asText())) {
                    String detailedName = location.get("detailedName").asText();
                    int slashIndex = detailedName.indexOf('/');
                    int colonIndex = detailedName.indexOf(':');
                    if (slashIndex != -1 && colonIndex != -1 && slashIndex + 2 <= colonIndex - 1) {
                        return detailedName.substring(slashIndex + 1, colonIndex);
                    }
                }
            }
        }
        System.err.println("Country code not found for IATA: " + iataCode);
        return null;
    }

    private static List<FlightInfo> generateFlights(String origin, String destination, String departureDate) {
        System.out.println("Generating flights for " + origin + "-" + destination);
        List<FlightInfo> flights = new ArrayList<>();
        int duration = estimateFlightDuration(origin, destination);
        System.out.println("Flight duration: " + duration + " hours");
        int basePrice;
        String aircraftCode;

        // 가격 및 기종 설정
        if (duration <= 3) {
            basePrice = 150000 + random.nextInt(150000); // 단거리
            aircraftCode = random.nextBoolean() ? "320" : "737";
        } else if (duration <= 7) {
            basePrice = 300000 + random.nextInt(300000); // 중거리
            aircraftCode = random.nextBoolean() ? "330" : "777";
        } else {
            basePrice = 800000 + random.nextInt(700000); // 장거리
            aircraftCode = random.nextBoolean() ? "350" : "787";
        }

        // 항공사 선택
        List<String> airlines = new ArrayList<>();
        String originCountry = getCountryCode(origin);
        String destCountry = getCountryCode(destination);
        if (originCountry != null && countryCodeToAirline.containsKey(originCountry)) {
            airlines.addAll(countryCodeToAirline.get(originCountry));
        }
        if (destCountry != null && countryCodeToAirline.containsKey(destCountry)) {
            airlines.addAll(countryCodeToAirline.get(destCountry));
        }
        Collections.shuffle(globalAirlines);
        airlines.addAll(globalAirlines);
        System.out.println("Selected airlines: " + airlines);

        // 출발 시간
        String[] departureTimes = {"08:00:00", "10:00:00", "12:00:00", "14:00:00", "16:00:00", "18:00:00", "20:00:00"};
        int flightCount = Math.min(airlines.size() * 2, departureTimes.length * 2); // 최대 14개 항공편

        for (int i = 0; i < flightCount; i++) {
            String airline = airlines.get(i % airlines.size());
            String[] airlineData = airline.split("\\|");
            if (airlineData.length < 2) {
                System.err.println("Invalid airline data: " + airline + ", skipping");
                continue;
            }
            String carrier = airlineData[0];
            String carrierCode = airlineData[1];
            String flightAircraft = airlineData.length > 2 ? airlineData[2] : aircraftCode;
            String flightNumber = carrierCode + (100 + random.nextInt(900));
            String departureTime = departureTimes[i % departureTimes.length];
            String flightId = "FL" + String.format("%03d", (i + 1)) + "-" + origin + "-" + destination;

            FlightInfo flight = createFlight(
                    flightId,
                    origin,
                    destination,
                    carrier,
                    carrierCode,
                    flightNumber,
                    departureTime,
                    duration,
                    basePrice + (i * 50000), // 가격 다양화
                    flightAircraft
            );
            flights.add(flight);
            System.out.println("Generated flight: id=" + flightId + ", carrier=" + carrier + ", departure=" + departureTime);
        }

        System.out.println("Generated flights count: " + flights.size());
        return flights;
    }

    public static JsonNode getLocations(String term) {
        String decodedTerm;
        try {
            decodedTerm = URLDecoder.decode(term, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            System.err.println("Failed to decode term: " + term);
            decodedTerm = term;
        }

        String normalizedTerm = decodedTerm.trim().toLowerCase();
        System.out.println("Original term: " + term + ", Decoded term: " + decodedTerm + ", Normalized term: " + normalizedTerm);

        String searchKey = koreanToEnglishCityMap.entrySet().stream()
                .filter(entry -> normalizedTerm.equals(entry.getKey().toLowerCase()) || normalizedTerm.contains(entry.getKey().toLowerCase()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(normalizedTerm);

        System.out.println("Search key: " + searchKey);

        List<JsonNode> matchingLocations = new ArrayList<>();
        for (Map.Entry<String, JsonNode> entry : LOCATIONS.entrySet()) {
            String cityName = entry.getKey();
            JsonNode locations = entry.getValue();
            System.out.println("Comparing cityName: " + cityName + " with searchKey: " + searchKey);
            if (cityName.toLowerCase().equals(searchKey.toLowerCase()) || locations.toString().toLowerCase().contains(searchKey.toLowerCase())) {
                matchingLocations.add(locations);
            }
        }

        ArrayNode result = mapper.createArrayNode();
        matchingLocations.forEach(node -> result.addAll((ArrayNode) node));
        System.out.println("Matching locations count: " + result.size() + ", locations: " + result.toString());
        return result;
    }

    public static List<FlightInfo> getFlights(String origin, String destination, String departureDate, String returnDate) {
        try {
            List<FlightInfo> results = generateFlights(origin, destination, departureDate);

            for (FlightInfo flight : results) {
                try {
                    LocalDateTime baseTime = LocalDateTime.parse(departureDate + "T" + flight.getDepartureTime().split("T")[1]);
                    flight.setDepartureTime(baseTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    long hours = Long.parseLong(flight.getDuration().replace("PT", "").replace("H", ""));
                    flight.setArrivalTime(baseTime.plusHours(hours).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    // 시간대 재설정
                    flight.setDepartureTimeZone(getTimeZone(flight.getDepartureAirport()));
                    flight.setArrivalTimeZone(getTimeZone(flight.getArrivalAirport()));
                } catch (Exception e) {
                    System.err.println("Failed to parse flight dates: id=" + flight.getId());
                    continue;
                }
            }

            if (returnDate != null && !returnDate.isEmpty()) {
                List<FlightInfo> returnFlights = generateFlights(destination, origin, returnDate);
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
                        combined.setDepartureTimeZone(outbound.getDepartureTimeZone());
                        combined.setArrivalTimeZone(outbound.getArrivalTimeZone());

                        try {
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
                            combined.setReturnDepartureTimeZone(inbound.getDepartureTimeZone());
                            combined.setReturnArrivalTimeZone(inbound.getArrivalTimeZone());
                        } catch (Exception e) {
                            System.err.println("Failed to parse return flight dates: id=" + inbound.getId());
                            continue;
                        }

                        combinedFlights.add(combined);
                    }
                }
                System.out.println("  Combined flights count: " + combinedFlights.size());
                return combinedFlights;
            }

            System.out.println("Outbound flights count: " + results.size());
            return results;
        } catch (Exception e) {
            System.err.println("Error generating flights for " + origin + "-" + destination + ": " + e.getMessage());
            return Collections.emptyList();
        }
    }
}