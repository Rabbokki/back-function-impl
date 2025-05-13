package com.backfunctionimpl.travel.travelFlight.data;

import com.backfunctionimpl.global.error.CustomException;
import com.backfunctionimpl.global.error.ErrorCode;
import com.backfunctionimpl.travel.travelFlight.dto.FlightInfo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class MockFlightData {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Map<String, JsonNode> LOCATIONS = new HashMap<>();
    private static final Map<String, String> IATA_TO_CITY = new HashMap<>(); // IATA 코드 -> 도시명 매핑
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

            // 도시/공항 목록 및 IATA 코드 매핑
            ArrayNode seoulLocations = mapper.createArrayNode();
            ObjectNode icnAirport = mapper.createObjectNode();
            icnAirport.put("type", "location");
            icnAirport.put("subType", "AIRPORT");
            icnAirport.put("detailedName", "Seoul/KR: Incheon");
            icnAirport.put("iataCode", "ICN");
            seoulLocations.add(icnAirport);
            LOCATIONS.put("Seoul", seoulLocations);
            IATA_TO_CITY.put("ICN", "Seoul");

            ArrayNode parisLocations = mapper.createArrayNode();
            ObjectNode cdgAirport = mapper.createObjectNode();
            cdgAirport.put("type", "location");
            cdgAirport.put("subType", "AIRPORT");
            cdgAirport.put("detailedName", "Paris/FR: Charles de Gaulle");
            cdgAirport.put("iataCode", "CDG");
            parisLocations.add(cdgAirport);
            LOCATIONS.put("Paris", parisLocations);
            IATA_TO_CITY.put("CDG", "Paris");

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
            IATA_TO_CITY.put("NRT", "Tokyo");
            IATA_TO_CITY.put("HND", "Tokyo");

            ArrayNode hongKongLocations = mapper.createArrayNode();
            ObjectNode hkgAirport = mapper.createObjectNode();
            hkgAirport.put("type", "location");
            hkgAirport.put("subType", "AIRPORT");
            hkgAirport.put("detailedName", "Hong Kong/HK: Hong Kong International");
            hkgAirport.put("iataCode", "HKG");
            hongKongLocations.add(hkgAirport);
            LOCATIONS.put("Hong Kong", hongKongLocations);
            IATA_TO_CITY.put("HKG", "Hong Kong");

            ArrayNode newYorkLocations = mapper.createArrayNode();
            ObjectNode jfkAirport = mapper.createObjectNode();
            jfkAirport.put("type", "location");
            jfkAirport.put("subType", "AIRPORT");
            jfkAirport.put("detailedName", "New York/US: John F. Kennedy");
            jfkAirport.put("iataCode", "JFK");
            newYorkLocations.add(jfkAirport);
            LOCATIONS.put("New York", newYorkLocations);
            IATA_TO_CITY.put("JFK", "New York");

            ArrayNode londonLocations = mapper.createArrayNode();
            ObjectNode lhrAirport = mapper.createObjectNode();
            lhrAirport.put("type", "location");
            lhrAirport.put("subType", "AIRPORT");
            lhrAirport.put("detailedName", "London/GB: Heathrow");
            lhrAirport.put("iataCode", "LHR");
            londonLocations.add(lhrAirport);
            LOCATIONS.put("London", londonLocations);
            IATA_TO_CITY.put("LHR", "London");

            ArrayNode singaporeLocations = mapper.createArrayNode();
            ObjectNode sinAirport = mapper.createObjectNode();
            sinAirport.put("type", "location");
            sinAirport.put("subType", "AIRPORT");
            sinAirport.put("detailedName", "Singapore/SG: Changi");
            sinAirport.put("iataCode", "SIN");
            singaporeLocations.add(sinAirport);
            LOCATIONS.put("Singapore", singaporeLocations);
            IATA_TO_CITY.put("SIN", "Singapore");

            ArrayNode bangkokLocations = mapper.createArrayNode();
            ObjectNode bkkAirport = mapper.createObjectNode();
            bkkAirport.put("type", "location");
            bkkAirport.put("subType", "AIRPORT");
            bkkAirport.put("detailedName", "Bangkok/TH: Suvarnabhumi");
            bkkAirport.put("iataCode", "BKK");
            bangkokLocations.add(bkkAirport);
            LOCATIONS.put("Bangkok", bangkokLocations);
            IATA_TO_CITY.put("BKK", "Bangkok");

            ArrayNode losAngelesLocations = mapper.createArrayNode();
            ObjectNode laxAirport = mapper.createObjectNode();
            laxAirport.put("type", "location");
            laxAirport.put("subType", "AIRPORT");
            laxAirport.put("detailedName", "Los Angeles/US: Los Angeles International");
            laxAirport.put("iataCode", "LAX");
            losAngelesLocations.add(laxAirport);
            LOCATIONS.put("Los Angeles", losAngelesLocations);
            IATA_TO_CITY.put("LAX", "Los Angeles");

            ArrayNode sydneyLocations = mapper.createArrayNode();
            ObjectNode sydAirport = mapper.createObjectNode();
            sydAirport.put("type", "location");
            sydAirport.put("subType", "AIRPORT");
            sydAirport.put("detailedName", "Sydney/AU: Sydney Kingsford Smith");
            sydAirport.put("iataCode", "SYD");
            sydneyLocations.add(sydAirport);
            LOCATIONS.put("Sydney", sydneyLocations);
            IATA_TO_CITY.put("SYD", "Sydney");

            ArrayNode dubaiLocations = mapper.createArrayNode();
            ObjectNode dxbAirport = mapper.createObjectNode();
            dxbAirport.put("type", "location");
            dxbAirport.put("subType", "AIRPORT");
            dxbAirport.put("detailedName", "Dubai/AE: Dubai International");
            dxbAirport.put("iataCode", "DXB");
            dubaiLocations.add(dxbAirport);
            LOCATIONS.put("Dubai", dubaiLocations);
            IATA_TO_CITY.put("DXB", "Dubai");

            ArrayNode shanghaiLocations = mapper.createArrayNode();
            ObjectNode pvgAirport = mapper.createObjectNode();
            pvgAirport.put("type", "location");
            pvgAirport.put("subType", "AIRPORT");
            pvgAirport.put("detailedName", "Shanghai/CN: Pudong");
            pvgAirport.put("iataCode", "PVG");
            shanghaiLocations.add(pvgAirport);
            LOCATIONS.put("Shanghai", shanghaiLocations);
            IATA_TO_CITY.put("PVG", "Shanghai");

            ArrayNode frankfurtLocations = mapper.createArrayNode();
            ObjectNode fraAirport = mapper.createObjectNode();
            fraAirport.put("type", "location");
            fraAirport.put("subType", "AIRPORT");
            fraAirport.put("detailedName", "Frankfurt/DE: Frankfurt Main");
            fraAirport.put("iataCode", "FRA");
            frankfurtLocations.add(fraAirport);
            LOCATIONS.put("Frankfurt", frankfurtLocations);
            IATA_TO_CITY.put("FRA", "Frankfurt");

            ArrayNode amsterdamLocations = mapper.createArrayNode();
            ObjectNode amsAirport = mapper.createObjectNode();
            amsAirport.put("type", "location");
            amsAirport.put("subType", "AIRPORT");
            amsAirport.put("detailedName", "Amsterdam/NL: Schiphol");
            amsAirport.put("iataCode", "AMS");
            amsterdamLocations.add(amsAirport);
            LOCATIONS.put("Amsterdam", amsterdamLocations);
            IATA_TO_CITY.put("AMS", "Amsterdam");

            ArrayNode madridLocations = mapper.createArrayNode();
            ObjectNode madAirport = mapper.createObjectNode();
            madAirport.put("type", "location");
            madAirport.put("subType", "AIRPORT");
            madAirport.put("detailedName", "Madrid/ES: Adolfo Suárez Madrid–Barajas");
            madAirport.put("iataCode", "MAD");
            madridLocations.add(madAirport);
            LOCATIONS.put("Madrid", madridLocations);
            IATA_TO_CITY.put("MAD", "Madrid");

            ArrayNode romeLocations = mapper.createArrayNode();
            ObjectNode fcoAirport = mapper.createObjectNode();
            fcoAirport.put("type", "location");
            fcoAirport.put("subType", "AIRPORT");
            fcoAirport.put("detailedName", "Rome/IT: Leonardo da Vinci–Fiumicino");
            fcoAirport.put("iataCode", "FCO");
            romeLocations.add(fcoAirport);
            LOCATIONS.put("Rome", romeLocations);
            IATA_TO_CITY.put("FCO", "Rome");

        } catch (Exception e) {
            System.err.println("MockFlightData initialization failed: " + e.getMessage());
            throw new RuntimeException("Failed to initialize MockFlightData", e);
        }
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
        System.out.println("getLocations 호출: Original term=" + term + ", Decoded term=" + decodedTerm + ", Normalized term=" + normalizedTerm);

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
        long startTime = System.currentTimeMillis();
        System.out.println("MockFlightData.getFlights 호출: origin=" + origin + ", destination=" + destination +
                ", departureDate=" + departureDate + ", returnDate=" + returnDate);

        // IATA 코드 또는 도시명 처리
        String decodedOrigin = decodeCity(origin);
        String decodedDestination = decodeCity(destination);
        String englishOrigin = resolveCity(decodedOrigin);
        String englishDestination = resolveCity(decodedDestination);

        JsonNode originLocations = LOCATIONS.get(englishOrigin);
        JsonNode destinationLocations = LOCATIONS.get(englishDestination);

        if (originLocations == null || destinationLocations == null) {
            System.out.println("출발지 또는 도착지 공항 정보 없음: origin=" + englishOrigin + ", destination=" + englishDestination);
            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH, "지원되지 않는 출발지 또는 도착지입니다.");
        }

        List<String> originAirports = new ArrayList<>();
        originLocations.forEach(node -> originAirports.add(node.get("iataCode").asText()));
        List<String> destinationAirports = new ArrayList<>();
        destinationLocations.forEach(node -> destinationAirports.add(node.get("iataCode").asText()));

        if (originAirports.isEmpty() || destinationAirports.isEmpty()) {
            System.out.println("공항 코드 목록 비어 있음: originAirports=" + originAirports + ", destinationAirports=" + destinationAirports);
            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH, "공항 정보를 찾을 수 없습니다.");
        }

        LocalDate parsedDepartureDate;
        LocalDate parsedReturnDate = null;
        try {
            parsedDepartureDate = LocalDate.parse(departureDate);
        } catch (DateTimeParseException e) {
            System.out.println("출발일 파싱 실패: " + departureDate);
            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH, "유효하지 않은 출발일 형식입니다.");
        }
        boolean isRoundTrip = returnDate != null && !returnDate.isEmpty();
        if (isRoundTrip) {
            try {
                parsedReturnDate = LocalDate.parse(returnDate);
                if (parsedReturnDate.isBefore(parsedDepartureDate)) {
                    System.out.println("귀국일이 출발일보다 빠름: returnDate=" + returnDate + ", departureDate=" + departureDate);
                    throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH, "귀국일은 출발일 이후여야 합니다.");
                }
            } catch (DateTimeParseException e) {
                System.out.println("귀국일 파싱 실패: " + returnDate);
                throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH, "유효하지 않은 귀국일 형식입니다.");
            }
        }

        List<FlightInfo> flights = generateFlights(
                originAirports, destinationAirports, parsedDepartureDate, parsedReturnDate, isRoundTrip);

        System.out.println("생성된 항공편 수: " + flights.size() + ", 소요 시간: " + (System.currentTimeMillis() - startTime) + "ms");
        return flights;
    }

    private static String resolveCity(String input) {
        // IATA 코드인 경우 도시명으로 변환
        if (IATA_TO_CITY.containsKey(input)) {
            return IATA_TO_CITY.get(input);
        }
        // 한글 도시명인 경우 영어로 변환
        String englishCity = koreanToEnglishCityMap.getOrDefault(input, input);
        // LOCATIONS 맵에 존재하는지 확인
        return LOCATIONS.containsKey(englishCity) ? englishCity : input;
    }

    private static List<FlightInfo> generateFlights(List<String> originAirports, List<String> destinationAirports,
                                                    LocalDate departureDate, LocalDate returnDate, boolean isRoundTrip) {
        List<FlightInfo> flights = new ArrayList<>();
        int maxFlights = 10;

        String originCountry = originAirports.get(0).equals("ICN") ? "KR" : destinationAirports.get(0).equals("CDG") ? "FR" : "US";
        String destinationCountry = destinationAirports.get(0).equals("CDG") ? "FR" : originAirports.get(0).equals("ICN") ? "KR" : "US";
        List<String> availableAirlines = new ArrayList<>();
        availableAirlines.addAll(countryCodeToAirline.getOrDefault(originCountry, new ArrayList<>()));
        availableAirlines.addAll(countryCodeToAirline.getOrDefault(destinationCountry, new ArrayList<>()));
        availableAirlines.addAll(globalAirlines);
        if (availableAirlines.isEmpty()) {
            availableAirlines.add("대한항공|KE|777");
            availableAirlines.add("Air France|AF|350");
        }

        List<Integer> departureHours = Arrays.asList(6, 8, 10, 12, 14, 16, 18, 20, 22);
        int baseDurationHours = estimateFlightDuration(originAirports.get(0), destinationAirports.get(0));
        int basePrice = calculateBasePrice(baseDurationHours);

        for (int i = 0; i < maxFlights && flights.size() < maxFlights; i++) {
            String originAirport = originAirports.get(random.nextInt(originAirports.size()));
            String destinationAirport = destinationAirports.get(random.nextInt(destinationAirports.size()));
            String airlineData = availableAirlines.get(random.nextInt(availableAirlines.size()));
            String[] airlineParts = airlineData.split("\\|");
            String carrier = airlineParts[0];
            String carrierCode = airlineParts[1];
            String aircraftCode = airlineParts[2];

            int hour = departureHours.get(random.nextInt(departureHours.size()));
            LocalDateTime departureTime = departureDate.atTime(hour, random.nextInt(60));
            int durationHours = baseDurationHours + random.nextInt(3) - 1;
            LocalDateTime arrivalTime = departureTime.plusHours(durationHours).plusMinutes(random.nextInt(60));
            int price = basePrice + random.nextInt(200000) - 100000;
            if (price < 200000) price = 200000;

            String flightId = UUID.randomUUID().toString();
            String flightNumber = carrierCode + (100 + random.nextInt(900));

            FlightInfo flight = createFlight(
                    flightId, originAirport, destinationAirport, carrier, carrierCode, flightNumber,
                    departureTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), durationHours, price, aircraftCode);

            if (isRoundTrip && returnDate != null) {
                String returnAirlineData = availableAirlines.get(random.nextInt(availableAirlines.size()));
                String[] returnAirlineParts = returnAirlineData.split("\\|");
                String returnCarrier = returnAirlineParts[0];
                String returnCarrierCode = returnAirlineParts[1];
                String returnAircraftCode = returnAirlineParts[2];
                String returnFlightNumber = returnCarrierCode + (100 + random.nextInt(900));

                int returnHour = departureHours.get(random.nextInt(departureHours.size()));
                LocalDateTime returnDepartureTime = returnDate.atTime(returnHour, random.nextInt(60));
                int returnDurationHours = baseDurationHours + random.nextInt(3) - 1;
                LocalDateTime returnArrivalTime = returnDepartureTime.plusHours(returnDurationHours).plusMinutes(random.nextInt(60));
                int returnPrice = price + random.nextInt(100000) - 50000;

                flight.setReturnDepartureAirport(destinationAirport);
                flight.setReturnArrivalAirport(originAirport);
                flight.setReturnDepartureTime(returnDepartureTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                flight.setReturnArrivalTime(returnArrivalTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                flight.setReturnDuration(String.format("PT%dH", returnDurationHours));
                flight.setReturnCarrier(returnCarrier);
                flight.setReturnCarrierCode(returnCarrierCode);
                flight.setReturnFlightNumber(returnFlightNumber);
                flight.setPrice(String.valueOf(price + returnPrice));
            }

            flights.add(flight);
        }

        return flights.stream()
                .sorted(Comparator.comparingDouble(f -> Double.parseDouble(f.getPrice())))
                .limit(maxFlights)
                .collect(Collectors.toList());
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
            LocalDateTime departureDateTime = LocalDateTime.parse(departureTime);
            flight.setDepartureTime(departureDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            LocalDateTime arrivalDateTime = departureDateTime.plusHours(hours).plusMinutes(random.nextInt(60));
            flight.setArrivalTime(arrivalDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            flight.setDepartureTimeZone(getTimeZone(departureAirport));
            flight.setArrivalTimeZone(getTimeZone(arrivalAirport));
        } catch (DateTimeParseException e) {
            System.out.println("날짜 파싱 오류: departureTime=" + departureTime);
            throw new CustomException(ErrorCode.INVALID_FLIGHT_SEARCH, "유효하지 않은 날짜 형식입니다.");
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

    private static String decodeCity(String city) {
        try {
            return URLDecoder.decode(city, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            System.out.println("도시명 디코딩 실패: " + city);
            return city;
        }
    }

    private static int estimateFlightDuration(String origin, String destination) {
        Map<String, Integer> durationMap = new HashMap<>();
        durationMap.put("ICN-CDG", 12);
        durationMap.put("CDG-ICN", 11);
        durationMap.put("ICN-NRT", 2);
        durationMap.put("NRT-ICN", 2);
        durationMap.put("ICN-HND", 2);
        durationMap.put("HND-ICN", 2);
        durationMap.put("ICN-HKG", 3);
        durationMap.put("HKG-ICN", 3);
        durationMap.put("ICN-JFK", 14);
        durationMap.put("JFK-ICN", 15);
        durationMap.put("ICN-LHR", 12);
        durationMap.put("LHR-ICN", 11);
        durationMap.put("ICN-SIN", 6);
        durationMap.put("SIN-ICN", 6);
        durationMap.put("ICN-BKK", 5);
        durationMap.put("BKK-ICN", 5);
        durationMap.put("ICN-LAX", 11);
        durationMap.put("LAX-ICN", 12);
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
        return durationMap.getOrDefault(key, 8);
    }

    private static int calculateBasePrice(int durationHours) {
        return 200000 + (durationHours * 50000);
    }
}