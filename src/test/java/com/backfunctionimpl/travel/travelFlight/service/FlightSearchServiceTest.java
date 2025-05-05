//package com.backfunctionimpl.travel.travelFlight.service;
//
//import com.backfunctionimpl.account.repository.RefreshTokenRepository;
//import com.backfunctionimpl.global.security.user.UserDetailsServiceImpl;
//import com.backfunctionimpl.travel.travelFlight.dto.FlightInfo;
//import com.backfunctionimpl.travel.travelFlight.dto.FlightSearchReqDto;
//import com.backfunctionimpl.travel.travelFlight.dto.FlightSearchResDto;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.security.core.userdetails.User;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.util.Collections;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.when;
//
//@SpringBootTest
//@ActiveProfiles("test")
//class FlightSearchServiceTest {
//
//    @Autowired
//    private FlightSearchService flightSearchService;
//
//    @MockBean
//    private UserDetailsServiceImpl userDetailsService;
//
//    @MockBean
//    private RefreshTokenRepository refreshTokenRepository;
//
//    @BeforeEach
//    public void setUp() {
//        // Mock UserDetailsServiceImpl
//        UserDetails userDetails = new User("test@example.com", "password", Collections.emptyList());
//        when(userDetailsService.loadUserByUsername(anyString())).thenReturn(userDetails);
//
//        // Mock RefreshTokenRepository
//        when(refreshTokenRepository.findByAccountEmail(anyString())).thenReturn(Optional.empty());
//    }
//
//    @Test
//    void searchFlights() {
//        // Given
//        FlightSearchReqDto reqDto = new FlightSearchReqDto();
//        reqDto.setOrigin("seoul");
//        reqDto.setDestination("tokyo");
//        reqDto.setDepartureDate("2025-06-01");
//        reqDto.setRealTime(true);
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
//    }
//}
