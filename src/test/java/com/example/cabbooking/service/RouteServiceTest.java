package com.example.cabbooking.service;

import com.example.cabbooking.model.Location;
import com.example.cabbooking.model.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RouteServiceTest {

    @Mock
    private LocationDistanceCalculatorService locationDistanceCalculatorService;

    private RouteService routeService;
    private Location startLocation;
    private Location endLocation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        routeService = new RouteService(locationDistanceCalculatorService);

        startLocation = new Location("Central Park", 40.7829, -73.9654);
        endLocation = new Location("Times Square", 40.7580, -73.9855);

        // Set up default mock behavior
        when(locationDistanceCalculatorService.calculateDistanceUsingLocation(any(Location.class), any(Location.class)))
                .thenReturn(2.5);
    }

    @Test
    void createRouteTest() {
        // Arrange: Mock both methods
        double expectedKilometers = 4.0;
        double expectedMiles = 2.485; // What we want the route distance to be

        when(locationDistanceCalculatorService.calculateDistanceUsingLocation(startLocation, endLocation))
                .thenReturn(expectedKilometers);

        // Now printDistanceReport returns miles!
        when(locationDistanceCalculatorService.printDistanceReport(startLocation, endLocation))
                .thenReturn(expectedMiles);

        // Act: Call the method
        Route route = routeService.createRoute(startLocation, endLocation);

        // Assert: Distance should be the miles value from printDistanceReport
        assertNotNull(route);
        assertEquals(startLocation, route.getFrom());
        assertEquals(endLocation, route.getTo());
        assertEquals(expectedMiles, route.getDistance()); // Should work now!

        // Verify both methods were called
        verify(locationDistanceCalculatorService).printDistanceReport(startLocation, endLocation);
    }

    @Test
    void createRouteReturnsCorrectDistanceTest() {
        // Use REAL locations with known distance
        final Location newYork = new Location("New York", 40.7128, -74.0060);
        final Location losAngeles = new Location("Los Angeles", 34.0522, -118.2437);

        // Expected values (you calculated these)
        double expectedKilometers = 3935.75;
        double expectedMiles = 2445.56;

        // Create a REAL LocationDistanceCalculatorService (no mocking!)
        DistanceCalculatorService realDistanceService = new DistanceCalculatorService();
        LocationDistanceCalculatorService realLocationService = new LocationDistanceCalculatorService(realDistanceService);
        RouteService realRouteService = new RouteService(realLocationService);

        // Act: Call the method with real services
        Route route = realRouteService.createRoute(newYork, losAngeles);

        // Assert: Check the actual calculations work
        assertNotNull(route);
        assertEquals(newYork, route.getFrom());
        assertEquals(losAngeles, route.getTo());

        // Allow some tolerance for floating point precision
        assertEquals(expectedMiles, route.getDistance(), 0.1); // Within 0.1 miles

        System.out.println("Expected miles: " + expectedMiles);
        System.out.println("Actual miles: " + route.getDistance());
        System.out.println("Difference: " + Math.abs(expectedMiles - route.getDistance()));
    }

    @Test
    void getRouteLocationFromTest() {
        Route route = new Route(startLocation, endLocation, 5.0);

        Location result = routeService.getRouteLocationFrom(route);

        assertEquals(startLocation, result);
        System.out.println(result);
    }

    @Test
    void getRouteLocationToTest() {
        Route route = new Route(startLocation, endLocation, 5.0);

        Location result = routeService.getRouteLocationTo(route);

        assertEquals(endLocation, result);
        System.out.println(result);
    }

    @Test
    void getRouteDistanceTest() {
        Route route = new Route(startLocation, endLocation, 7.5);

        double result = routeService.getRouteDistance(route);

        assertEquals(7.5, result);
    }
}