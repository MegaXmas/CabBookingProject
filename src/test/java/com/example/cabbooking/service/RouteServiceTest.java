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

    // =================== SUCCESS CASE TESTS ===================

    @Test
    void createRouteTest() {
        // Arrange: Mock the distance calculation
        double expectedDistance = 2.5;
        when(locationDistanceCalculatorService.calculateDistanceUsingLocation(startLocation, endLocation))
                .thenReturn(expectedDistance);

        // Act: Call the method
        Route route = routeService.createRoute(startLocation, endLocation);

        // Assert: Check the route was created properly
        assertNotNull(route);
        assertEquals(startLocation, route.getFrom());
        assertEquals(endLocation, route.getTo());
        assertEquals(expectedDistance, route.getDistance());

        // Verify the distance calculation was called
        verify(locationDistanceCalculatorService).calculateDistanceUsingLocation(startLocation, endLocation);
    }

    @Test
    void createRouteReturnsCorrectDistanceTest() {
        // Use REAL locations with known distance
        final Location newYork = new Location("New York", 40.7128, -74.0060);
        final Location losAngeles = new Location("Los Angeles", 34.0522, -118.2437);

        // Expected values (you calculated these)
        double expectedKilometers = 3935.75;

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
        assertEquals(expectedKilometers, route.getDistance(), 1.0); // Within 1 km

        System.out.println("Expected km: " + expectedKilometers);
        System.out.println("Actual km: " + route.getDistance());
        System.out.println("Difference: " + Math.abs(expectedKilometers - route.getDistance()));
    }

    @Test
    void createRoute0DistanceTest() {
        // Same location should give 0 distance
        when(locationDistanceCalculatorService.calculateDistanceUsingLocation(startLocation, startLocation))
                .thenReturn(0.0);

        Route route = routeService.createRoute(startLocation, startLocation);

        assertNotNull(route);
        assertEquals(startLocation, route.getFrom());
        assertEquals(startLocation, route.getTo());
        assertEquals(0.0, route.getDistance());

        verify(locationDistanceCalculatorService).calculateDistanceUsingLocation(startLocation, startLocation);
    }

    @Test
    void getRouteLocationFromTest() {
        Route route = new Route(startLocation, endLocation, 5.0);

        Location result = routeService.getRouteLocationFrom(route);

        assertEquals(startLocation, result);
    }

    @Test
    void getRouteLocationToTest() {
        Route route = new Route(startLocation, endLocation, 5.0);

        Location result = routeService.getRouteLocationTo(route);

        assertEquals(endLocation, result);
    }

    @Test
    void getRouteDistanceTest() {
        Route route = new Route(startLocation, endLocation, 7.5);

        double result = routeService.getRouteDistance(route);

        assertEquals(7.5, result);
    }

    // =================== EXCEPTION TESTS ===================

    @Test
    void createRouteWithNullFromLocationShouldThrowException() {
        RouteService.InvalidRouteException exception = assertThrows(
                RouteService.InvalidRouteException.class,
                () -> routeService.createRoute(null, endLocation)
        );

        assertEquals("Starting location cannot be null", exception.getMessage());
        System.out.println("Expected exception: " + exception.getMessage());

        // Verify distance calculation was never called
        verify(locationDistanceCalculatorService, never()).calculateDistanceUsingLocation(any(), any());
    }

    @Test
    void createRouteWithNullToLocationShouldThrowException() {
        RouteService.InvalidRouteException exception = assertThrows(
                RouteService.InvalidRouteException.class,
                () -> routeService.createRoute(startLocation, null)
        );

        assertEquals("Destination location cannot be null", exception.getMessage());
        System.out.println("Expected exception: " + exception.getMessage());
    }

    @Test
    void createRouteWithEmptyLocationNameShouldThrowException() {
        Location badLocation = new Location("   ", 40.7829, -73.9654); // Empty name

        RouteService.InvalidRouteException exception = assertThrows(
                RouteService.InvalidRouteException.class,
                () -> routeService.createRoute(badLocation, endLocation)
        );

        assertEquals("Starting location must have a valid name", exception.getMessage());
        System.out.println("Expected exception: " + exception.getMessage());
    }

    @Test
    void createRouteWithCalculationErrorShouldThrowException() {
        // Mock the distance calculator to throw an exception
        when(locationDistanceCalculatorService.calculateDistanceUsingLocation(startLocation, endLocation))
                .thenThrow(new RuntimeException("GPS calculation failed"));

        RouteService.RouteCalculationException exception = assertThrows(
                RouteService.RouteCalculationException.class,
                () -> routeService.createRoute(startLocation, endLocation)
        );

        assertTrue(exception.getMessage().contains("Failed to calculate route distance"));
        assertTrue(exception.getMessage().contains("GPS calculation failed"));
        System.out.println("Expected exception: " + exception.getMessage());
    }

    @Test
    void getRouteDistanceWithNullRouteShouldThrowException() {
        RouteService.InvalidRouteException exception = assertThrows(
                RouteService.InvalidRouteException.class,
                () -> routeService.getRouteDistance(null)
        );

        assertEquals("Route cannot be null", exception.getMessage());
        System.out.println("Expected exception: " + exception.getMessage());
    }

    @Test
    void getRouteDistanceWithNegativeDistanceShouldThrowException() {
        Route badRoute = new Route(startLocation, endLocation, -10.0); // Negative distance

        RouteService.InvalidRouteException exception = assertThrows(
                RouteService.InvalidRouteException.class,
                () -> routeService.getRouteDistance(badRoute)
        );

        assertTrue(exception.getMessage().contains("Route distance cannot be negative"));
        System.out.println("Expected exception: " + exception.getMessage());
    }

    @Test
    void getRouteLocationFromWithNullRouteShouldThrowException() {
        RouteService.InvalidRouteException exception = assertThrows(
                RouteService.InvalidRouteException.class,
                () -> routeService.getRouteLocationFrom(null)
        );

        assertEquals("Route cannot be null", exception.getMessage());
        System.out.println("Expected exception: " + exception.getMessage());
    }

    @Test
    void getRouteLocationFromWithNullFromLocationShouldThrowException() {
        Route badRoute = new Route(null, endLocation, 10.0); // Null 'from' location

        RouteService.InvalidRouteException exception = assertThrows(
                RouteService.InvalidRouteException.class,
                () -> routeService.getRouteLocationFrom(badRoute)
        );

        assertEquals("Route must have valid starting and destination locations", exception.getMessage());
        System.out.println("Expected exception: " + exception.getMessage());
    }

    @Test
    void getRouteLocationToWithNullToLocationShouldThrowException() {
        Route badRoute = new Route(startLocation, null, 10.0); // Null 'to' location

        RouteService.InvalidRouteException exception = assertThrows(
                RouteService.InvalidRouteException.class,
                () -> routeService.getRouteLocationTo(badRoute)
        );

        assertEquals("Route must have valid starting and destination locations", exception.getMessage());
        System.out.println("Expected exception: " + exception.getMessage());
    }

    // =================== EDGE CASE TESTS ===================

    @Test
    void createRouteWithBoundaryLocationNames() {
        // Test with minimal valid location names
        Location loc1 = new Location("A", 40.7829, -73.9654);
        Location loc2 = new Location("B", 40.7580, -73.9855);

        when(locationDistanceCalculatorService.calculateDistanceUsingLocation(loc1, loc2))
                .thenReturn(1.0);

        Route route = routeService.createRoute(loc1, loc2);

        assertNotNull(route);
        assertEquals("A", route.getFrom().getLocationName());
        assertEquals("B", route.getTo().getLocationName());
    }

    @Test
    void getRouteDistanceWithZeroDistance() {
        Route zeroRoute = new Route(startLocation, endLocation, 0.0);

        double result = routeService.getRouteDistance(zeroRoute);

        assertEquals(0.0, result);
    }
}