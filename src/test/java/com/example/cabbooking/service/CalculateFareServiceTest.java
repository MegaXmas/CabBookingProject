package com.example.cabbooking.service;

import com.example.cabbooking.model.Location;
import com.example.cabbooking.model.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CalculateFareServiceTest {

    private CalculateFareService calculateFareService;
    private Location newYork;
    private Location losAngeles;
    private Route route;
    private double expectedKilometers;
    private double expectedMiles;

    @Mock
    private RouteService routeServiceMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
        calculateFareService = new CalculateFareService(routeServiceMock);

        newYork = new Location("New York", 40.7128, -74.0060);
        losAngeles = new Location("Los Angeles", 34.0522, -118.2437);

        // Expected values (you calculated these)
        expectedKilometers = 3935.75;
        expectedMiles = 2445.56;

        route = new Route(newYork, losAngeles, expectedMiles);

        when(routeServiceMock.getRouteDistance(route)).thenReturn(expectedMiles);
    }

    @Test
    void calculateFare() {
        double cabFare = calculateFareService.calculateFare(route);

        assertEquals(expectedMiles, route.getDistance());
        assertEquals(cabFare, 3 + (route.getDistance() * 3));

        verify(routeServiceMock).getRouteDistance(route);
    }

    @Test
    void calculateFareRealDistanceTest() {
        double cabFare = calculateFareService.calculateFare(route);

        DistanceCalculatorService realDistanceService = new DistanceCalculatorService();
        LocationDistanceCalculatorService realLocationDistanceCalculatorService = new LocationDistanceCalculatorService(realDistanceService);
        RouteService realRouteService = new RouteService(realLocationDistanceCalculatorService);

        double distance = realRouteService.getRouteDistance(route);

        assertEquals(expectedMiles, route.getDistance());
        assertEquals(cabFare, 3 + (route.getDistance()* 3));
    }

    @Test
    void calculateFare0DistanceTest() {
        Route zeroRoute = new Route(newYork, newYork, 0.0);  // Same location

        when(routeServiceMock.getRouteDistance(zeroRoute)).thenReturn(0.0);

        double cabFare = calculateFareService.calculateFare(zeroRoute);

        assertEquals(3.0, cabFare);  // Only booking fee
        verify(routeServiceMock).getRouteDistance(zeroRoute);
    }

    @Test
    void calculateFareVerySmallDistance() {
        Route smallRoute = new Route(newYork, losAngeles, 0.1);
        when(routeServiceMock.getRouteDistance(smallRoute)).thenReturn(0.1);

        double cabFare = calculateFareService.calculateFare(smallRoute);

        assertEquals(3.3, cabFare, 0.01);  // 3 + (0.1 * 3)
    }

    @Test
    void calculateFareFormulaTest() {
        Route testRoute = new Route(newYork, losAngeles, 10.0);
        when(routeServiceMock.getRouteDistance(testRoute)).thenReturn(10.0);

        double cabFare = calculateFareService.calculateFare(testRoute);

        // Verify formula: initialFee + (distance * rate) = 3 + (10 * 3) = 33
        assertEquals(33.0, cabFare);
    }

    @Test
    void calculateFareWithDifferentRates() {
        calculateFareService.dollarsPerMile = 5;
        calculateFareService.initialBookingFee = 2;

        Route testRoute = new Route(newYork, losAngeles, 10.0);
        when(routeServiceMock.getRouteDistance(testRoute)).thenReturn(10.0);

        double cabFare = calculateFareService.calculateFare(testRoute);

        assertEquals(52.0, cabFare);  // 2 + (10 * 5)
    }
}
