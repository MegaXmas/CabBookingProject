package com.example.cabbooking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.example.cabbooking.model.Location;

import static org.junit.jupiter.api.Assertions.*;

class LocationDistanceCalculatorServiceTest {

    private final Location newYork = new Location("New York", 40.7128, -74.0060);
    private final Location losAngeles = new Location("Los Angeles", 34.0522, -118.2437);

    private LocationDistanceCalculatorService locationDistanceCalculatorService;

    @BeforeEach
    void setUp() {
        locationDistanceCalculatorService = new LocationDistanceCalculatorService(new DistanceCalculatorService());
    }

    // =================== SUCCESS CASE TESTS ===================

    @Test
    void testCalculateDistanceInKilometersUsingLocation() {
        double result = locationDistanceCalculatorService.calculateDistanceUsingLocation(newYork, losAngeles);

        // This should return the actual calculated distance in kilometers
        assertEquals(3935.75, result, 1.0); // Allow small margin for precision
        System.out.println("Distance: " + result + " km");
    }

    @Test
    void testCalculateDistanceUsingLocation_SameLocation() {
        double result = locationDistanceCalculatorService.calculateDistanceUsingLocation(newYork, newYork);

        // Distance should be 0
        assertEquals(0.0, result, 0.001);
        System.out.println("Distance: " + result + " km");
    }

    @Test
    void testPrintDistanceReport() {
        double milesResult = locationDistanceCalculatorService.printDistanceReport(newYork, losAngeles);

        // Verify it returns miles (should be around 2445)
        assertTrue(milesResult > 2400 && milesResult < 2500);
        System.out.println("Returned miles: " + milesResult);
    }

    @Test
    void testPrintDistanceReport_VeryShortDistance() {
        Location loc1 = new Location("Close 1", 40.7128, -74.0060);
        Location loc2 = new Location("Close 2", 40.7130, -74.0062); // ~200 meters

        // This will print to console - verify it doesn't crash
        assertDoesNotThrow(() -> {
            double miles = locationDistanceCalculatorService.printDistanceReport(loc1, loc2);
            assertTrue(miles >= 0); // Should be positive
        }, "Print report should not throw exception");
    }

    @Test
    void testPrintDistanceReport_VeryLongDistance() {
        Location sydney = new Location("Sydney", -33.8688, 151.2093);

        // This will print to console - verify it doesn't crash
        assertDoesNotThrow(() -> {
            double miles = locationDistanceCalculatorService.printDistanceReport(newYork, sydney);
            assertTrue(miles > 0); // Should be positive and large
        }, "Print report should not throw exception for long distances");
    }

    // =================== EXCEPTION TESTS ===================

    @Test
    void testCalculateDistanceWithNullFirstLocation() {
        LocationDistanceCalculatorService.InvalidLocationException exception = assertThrows(
                LocationDistanceCalculatorService.InvalidLocationException.class,
                () -> locationDistanceCalculatorService.calculateDistanceUsingLocation(null, losAngeles)
        );

        assertEquals("First location cannot be null", exception.getMessage());
        System.out.println("Expected exception: " + exception.getMessage());    }

    @Test
    void testCalculateDistanceWithNullSecondLocation() {
        LocationDistanceCalculatorService.InvalidLocationException exception = assertThrows(
                LocationDistanceCalculatorService.InvalidLocationException.class,
                () -> locationDistanceCalculatorService.calculateDistanceUsingLocation(newYork, null)
        );

        assertEquals("Second location cannot be null", exception.getMessage());
        System.out.println("Expected exception: " + exception.getMessage());
    }

    @Test
    void testCalculateDistanceWithEmptyLocationName() {
        Location badLocation = new Location("   ", 40.7128, -74.0060); // Empty name

        LocationDistanceCalculatorService.InvalidLocationException exception = assertThrows(
                LocationDistanceCalculatorService.InvalidLocationException.class,
                () -> locationDistanceCalculatorService.calculateDistanceUsingLocation(badLocation, losAngeles)
        );

        assertEquals("First location must have a valid name", exception.getMessage());
        System.out.println("Expected exception: " + exception.getMessage());
    }

    @Test
    void testCalculateDistanceWithInvalidLatitude() {
        Location badLocation = new Location("Invalid", 95.0, -74.0060); // Latitude > 90

        LocationDistanceCalculatorService.InvalidLocationException exception = assertThrows(
                LocationDistanceCalculatorService.InvalidLocationException.class,
                () -> locationDistanceCalculatorService.calculateDistanceUsingLocation(badLocation, losAngeles)
        );

        assertTrue(exception.getMessage().contains("invalid latitude"));
        assertTrue(exception.getMessage().contains("95.0"));
        System.out.println("Expected exception: " + exception.getMessage());
    }

    @Test
    void testCalculateDistanceWithInvalidLongitude() {
        Location badLocation = new Location("Invalid", 40.7128, -200.0); // Longitude < -180

        LocationDistanceCalculatorService.InvalidLocationException exception = assertThrows(
                LocationDistanceCalculatorService.InvalidLocationException.class,
                () -> locationDistanceCalculatorService.calculateDistanceUsingLocation(badLocation, losAngeles)
        );

        assertTrue(exception.getMessage().contains("invalid longitude"));
        assertTrue(exception.getMessage().contains("-200.0"));
        System.out.println("Expected exception: " + exception.getMessage());
    }

    @Test
    void testCalculateDistanceWithNaNCoordinates() {
        Location badLocation = new Location("NaN Location", Double.NaN, -74.0060);

        LocationDistanceCalculatorService.InvalidLocationException exception = assertThrows(
                LocationDistanceCalculatorService.InvalidLocationException.class,
                () -> locationDistanceCalculatorService.calculateDistanceUsingLocation(badLocation, losAngeles)
        );

        assertTrue(exception.getMessage().contains("invalid coordinates (NaN)"));
        System.out.println("Expected exception: " + exception.getMessage());
    }

    @Test
    void testCalculateDistanceWithInfiniteCoordinates() {
        Location badLocation = new Location("Infinite Location", 40.7128, Double.POSITIVE_INFINITY);

        LocationDistanceCalculatorService.InvalidLocationException exception = assertThrows(
                LocationDistanceCalculatorService.InvalidLocationException.class,
                () -> locationDistanceCalculatorService.calculateDistanceUsingLocation(badLocation, losAngeles)
        );

        assertTrue(exception.getMessage().contains("invalid coordinates (infinite)"));
        System.out.println("Expected exception: " + exception.getMessage());
    }

    @Test
    void testPrintDistanceReportWithNullLocation() {
        LocationDistanceCalculatorService.InvalidLocationException exception = assertThrows(
                LocationDistanceCalculatorService.InvalidLocationException.class,
                () -> locationDistanceCalculatorService.printDistanceReport(null, losAngeles)
        );

        assertEquals("First location cannot be null", exception.getMessage());
        System.out.println("Expected exception: " + exception.getMessage());
    }

    @Test
    void testPrintDistanceReportWithInvalidLocation() {
        Location badLocation = new Location("Bad", 95.0, -74.0060); // Invalid latitude

        LocationDistanceCalculatorService.InvalidLocationException exception = assertThrows(
                LocationDistanceCalculatorService.InvalidLocationException.class,
                () -> locationDistanceCalculatorService.printDistanceReport(badLocation, losAngeles)
        );

        assertTrue(exception.getMessage().contains("invalid latitude"));
        System.out.println("Expected exception: " + exception.getMessage());
    }

    // =================== BOUNDARY TESTS ===================

    @Test
    void testCalculateDistanceWithBoundaryCoordinates() {
        // Test with valid boundary coordinates
        Location northPole = new Location("North Pole", 90.0, 0.0);
        Location southPole = new Location("South Pole", -90.0, 0.0);

        // Should not throw exception
        double distance = locationDistanceCalculatorService.calculateDistanceUsingLocation(northPole, southPole);

        assertTrue(distance > 0); // Should be a large positive number
        System.out.println("North to South Pole distance: " + distance + " km");
    }

    @Test
    void testCalculateDistanceWithDateLineLocations() {
        Location loc1 = new Location("Date Line East", 0.0, 179.0);
        Location loc2 = new Location("Date Line West", 0.0, -179.0);

        // Should not throw exception
        double distance = locationDistanceCalculatorService.calculateDistanceUsingLocation(loc1, loc2);

        assertTrue(distance > 0);
        System.out.println("Across date line distance: " + distance + " km");
    }
}