//package com.backfunctionimpl.travel.travelFlight.service;
//
//
//
//import com.backfunctionimpl.travel.travelFlight.dto.FlightInfo;
//import com.backfunctionimpl.travel.travelFlight.dto.FlightSearchReqDto;
//import com.backfunctionimpl.travel.travelFlight.dto.FlightSearchResDto;
//import com.backfunctionimpl.travel.travelFlight.repository.TravelFlightRepository;
//import com.backfunctionimpl.travel.travelPlan.repository.TravelPlanRepository;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//
//import java.lang.reflect.Field;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@ActiveProfiles("test")
//public class FlightSearchServiceMockTest {
//
//    @Mock
//    private WebClient webClient;
//
//    @Mock
//    private WebClient.Builder webClientBuilder;
//
//    @Mock
//    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
//
//    @Mock
//    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
//
//    @Mock
//    private WebClient.RequestHeadersSpec requestHeadersSpec;
//
//    @Mock
//    private WebClient.ResponseSpec responseSpec;
//
//    @Mock
//    private TravelFlightRepository travelFlightRepository;
//
//    @Mock
//    private TravelPlanRepository travelPlanRepository;
//
//    @InjectMocks
//    private FlightSearchService flightSearchService;
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    @BeforeEach
//    public void setUp() throws NoSuchFieldException, IllegalAccessException {
//        // Inject clientId and clientSecret using reflection
//        Field clientIdField = FlightSearchService.class.getDeclaredField("clientId");
//        clientIdField.setAccessible(true);
//        clientIdField.set(flightSearchService, "XgZnpBbz894SWxSCiQldacuFptPRGZGz");
//
//        Field clientSecretField = FlightSearchService.class.getDeclaredField("clientSecret");
//        clientSecretField.setAccessible(true);
//        clientSecretField.set(flightSearchService, "28SLF2h9A5oduVQa");
//
//        // Mock WebClient.Builder
//        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
//        when(webClientBuilder.build()).thenReturn(webClient);
//    }
//
//    @Test
//    public void testSearchFlights() throws Exception {
//        // Given
//        FlightSearchReqDto reqDto = new FlightSearchReqDto();
//        reqDto.setOrigin("seoul");
//        reqDto.setDestination("tokyo");
//        reqDto.setDepartureDate("2025-06-01");
//        reqDto.setRealTime(true);
//
//        // Mock Amadeus token response
//        String tokenJson = "{\"access_token\": \"mockToken\", \"expires_in\": 1799}";
//        JsonNode tokenResponse = objectMapper.readTree(tokenJson);
//
//        // Mock Amadeus flight search response
//        String flightJson = "{\"data\": [{\"id\": \"1\", \"price\": {\"total\": \"100.00\", \"currency\": \"KRW\"}, " +
//                "\"numberOfBookableSeats\": 9, \"itineraries\": [{\"duration\": \"PT2H30M\", \"segments\": [" +
//                "{\"carrierCode\": \"KE\", \"number\": \"701\", \"departure\": {\"iataCode\": \"ICN\", \"at\": \"2025-06-01T08:00:00\"}, " +
//                "\"arrival\": {\"iataCode\": \"NRT\", \"at\": \"2025-06-01T10:30:00\"}, \"aircraft\": {\"code\": \"789\"}}]}], " +
//                "\"travelerPricings\": [{\"includedCabinBags\": {\"quantity\": 1, \"weight\": 7}}]}]}";
//        JsonNode flightResponse = objectMapper.readTree(flightJson);
//
//        // Mock WebClient for token request
//        when(webClient.post()).thenReturn(requestBodyUriSpec);
//        when(requestBodyUriSpec.uri("/v1/security/oauth2/token")).thenReturn(requestBodyUriSpec);
//        when(requestBodyUriSpec.contentType(MediaType.APPLICATION_FORM_URLENCODED)).thenReturn(requestBodyUriSpec);
//        when(requestBodyUriSpec.bodyValue(anyString())).thenReturn(requestHeadersSpec);
//        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
//        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
//        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(tokenResponse));
//
//        // Mock WebClient for flight search
//        when(webClient.get()).thenReturn(requestHeadersUriSpec);
//        when(requestHeadersUriSpec.uri(any(java.util.function.Function.class))).thenReturn(requestHeadersSpec);
//        when(requestHeadersSpec.header(anyString(), anyString())).thenReturn(requestHeadersSpec);
//        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
//        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
//        when(responseSpec.bodyToMono(JsonNode.class)).thenReturn(Mono.just(flightResponse));
//
//        // When
//        FlightSearchResDto result = flightSearchService.searchFlights(reqDto);
//
//        // Then
//        assertNotNull(result);
//        assertTrue(result.isSuccess());
//        assertNotNull(result.getData());
//        assertFalse(result.getData().getFlights().isEmpty(), "Flights list should not be empty");
//
//        FlightInfo flightInfo = result.getData().getFlights().get(0);
//        assertNotNull(flightInfo.getId());
//        assertNotNull(flightInfo.getPrice());
//        assertNotNull(flightInfo.getDepartureTime());
//        assertNotNull(flightInfo.getArrivalTime());
//        assertEquals("ICN", flightInfo.getDepartureAirport());
//        assertEquals("NRT", flightInfo.getArrivalAirport());
//        assertTrue(flightInfo.getNumberOfBookableSeats() >= 0);
//        assertNotNull(flightInfo.getCabinBaggage());
//        assertEquals("대한항공", flightInfo.getCarrier());
//        assertEquals("701", flightInfo.getFlightNumber());
//        assertEquals("PT2H30M", flightInfo.getDuration());
//        assertEquals("Boeing 787-9", flightInfo.getAircraft());
//        assertEquals("Quantity: 1, Weight: 7kg", flightInfo.getCabinBaggage());
//    }
//}
