package com.example.cabbooking.service;

import com.example.cabbooking.model.Location;
import com.example.cabbooking.model.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CalculateFareServiceTest {

    private CalculateFareService calculateFareService;
    private Location newYork;
    private Location losAngeles;
    private Route route;
    private double expectedMiles;

    @Mock
    private RouteService routeServiceMock;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        calculateFareService = new CalculateFareService(routeServiceMock);

        newYork = new Location("New York", 40.7128, -74.0060);
        losAngeles = new Location("Los Angeles", 34.0522, -118.2437);

        expectedMiles = 2445.56;

        route = new Route(newYork, losAngeles, expectedMiles);

        when(routeServiceMock.getRouteDistance(route)).thenReturn(expectedMiles);
    }

    // =================== SUCCESS CASE TESTS ===================

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
        assertEquals(cabFare, 3 + (route.getDistance() * 3));
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
    void calculateFareVerySmallDistanceTest() {
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
    void calculateFareWithDifferentRatesTest() {
        // ✅ Use the new setter methods
        calculateFareService.setDollarsPerMile(5);
        calculateFareService.setInitialBookingFee(2);

        Route testRoute = new Route(newYork, losAngeles, 10.0);
        when(routeServiceMock.getRouteDistance(testRoute)).thenReturn(10.0);

        double cabFare = calculateFareService.calculateFare(testRoute);

        assertEquals(52.0, cabFare);  // 2 + (10 * 5)
    }

    @Test
    void calculateFareLargeDistanceTest() {
        Route largeRoute = new Route(newYork, losAngeles, 1000.0);
        when(routeServiceMock.getRouteDistance(largeRoute)).thenReturn(1000.0);

        double cabFare = calculateFareService.calculateFare(largeRoute);

        assertEquals(3003.0, cabFare);  // 3 + (1000 * 3)
    }

    // =================== EXCEPTION TESTS ===================

    @Test
    void calculateFareWithNullRouteShouldThrowException() {
        CalculateFareService.InvalidFareParametersException exception = assertThrows(
                CalculateFareService.InvalidFareParametersException.class,
                () -> calculateFareService.calculateFare(null)
        );

        assertEquals("Route cannot be null", exception.getMessage());

        // Verify route service was never called
        verify(routeServiceMock, never()).getRouteDistance(any());
    }

    @Test
    void calculateFareWithRouteHavingNullLocationsShouldThrowException() {
        Route badRoute = new Route(null, null, 10.0);

        CalculateFareService.InvalidFareParametersException exception = assertThrows(
                CalculateFareService.InvalidFareParametersException.class,
                () -> calculateFareService.calculateFare(badRoute)
        );

        assertEquals("Route must have valid starting and destination locations", exception.getMessage());
    }

    @Test
    void calculateFareWithNegativeDistanceShouldThrowException() {
        when(routeServiceMock.getRouteDistance(route)).thenReturn(-10.0);

        CalculateFareService.FareCalculationException exception = assertThrows(
                CalculateFareService.FareCalculationException.class,
                () -> calculateFareService.calculateFare(route)
        );

        assertTrue(exception.getMessage().contains("Distance cannot be negative"));
    }

    @Test
    void calculateFareWithNegativeDollarsPerMileShouldThrowException() {
        CalculateFareService.InvalidFareParametersException exception = assertThrows(
                CalculateFareService.InvalidFareParametersException.class,
                () -> calculateFareService.setDollarsPerMile(-5)
        );

        assertTrue(exception.getMessage().contains("Dollars per mile cannot be negative"));
    }

    @Test
    void calculateFareWithNegativeBookingFeeShouldThrowException() {
        CalculateFareService.InvalidFareParametersException exception = assertThrows(
                CalculateFareService.InvalidFareParametersException.class,
                () -> calculateFareService.setInitialBookingFee(-2)
        );

        assertTrue(exception.getMessage().contains("Initial booking fee cannot be negative"));
    }

    @Test
    void calculateFareWithBothRatesZeroShouldThrowException() {
        calculateFareService.setDollarsPerMile(0);
        calculateFareService.setInitialBookingFee(0);

        when(routeServiceMock.getRouteDistance(route)).thenReturn(10.0);

        CalculateFareService.InvalidFareParametersException exception = assertThrows(
                CalculateFareService.InvalidFareParametersException.class,
                () -> calculateFareService.calculateFare(route)
        );

        assertEquals("Both rate and booking fee cannot be zero", exception.getMessage());
    }

    @Test
    void calculateFareWhenRouteServiceThrowsExceptionShouldThrowFareCalculationException() {
        when(routeServiceMock.getRouteDistance(route))
                .thenThrow(new RouteService.InvalidRouteException("Route is invalid"));

        CalculateFareService.FareCalculationException exception = assertThrows(
                CalculateFareService.FareCalculationException.class,
                () -> calculateFareService.calculateFare(route)
        );

        assertTrue(exception.getMessage().contains("Cannot calculate fare due to invalid route"));
        assertTrue(exception.getMessage().contains("Route is invalid"));
    }

    // =================== SETTER/GETTER TESTS ===================

    @Test
    void testSettersAndGetters() {
        calculateFareService.setDollarsPerMile(7);
        calculateFareService.setInitialBookingFee(5);

        assertEquals(7, calculateFareService.getDollarsPerMile());
        assertEquals(5, calculateFareService.getInitialBookingFee());
    }

    @Test
    void testValidZeroRates() {
        // Test that having one rate as zero is okay (but not both)
        calculateFareService.setDollarsPerMile(0);
        calculateFareService.setInitialBookingFee(5);

        when(routeServiceMock.getRouteDistance(route)).thenReturn(10.0);

        double cabFare = calculateFareService.calculateFare(route);
        assertEquals(5.0, cabFare);  // 5 + (10 * 0) = 5
    }

    @Test
    void testValidZeroBookingFee() {
        calculateFareService.setDollarsPerMile(4);
        calculateFareService.setInitialBookingFee(0);

        when(routeServiceMock.getRouteDistance(route)).thenReturn(10.0);

        double cabFare = calculateFareService.calculateFare(route);
        assertEquals(40.0, cabFare);  // 0 + (10 * 4) = 40
    }

    // =================== EDGE CASE TESTS ===================

    @Test
    void calculateFareWithVeryLargeDistance() {
        Route hugeRoute = new Route(newYork, losAngeles, 999999.0);
        when(routeServiceMock.getRouteDistance(hugeRoute)).thenReturn(999999.0);

        double cabFare = calculateFareService.calculateFare(hugeRoute);

        assertEquals(3 + (999999.0 * 3), cabFare);
        assertTrue(cabFare > 2999999.0); // Should be a very large positive number
    }

    @Test
    void calculateFareWithHighRates() {
        calculateFareService.setDollarsPerMile(100);
        calculateFareService.setInitialBookingFee(50);

        when(routeServiceMock.getRouteDistance(route)).thenReturn(5.0);

        double cabFare = calculateFareService.calculateFare(route);
        assertEquals(550.0, cabFare);  // 50 + (5 * 100)
    }
}