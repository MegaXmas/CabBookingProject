package com.example.cabbooking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.example.cabbooking.model.Location;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class LocationDistanceCalculatorServiceTest {

    private final Location newYork = new Location("New York", 40.7128, -74.0060);
    private final Location losAngeles = new Location("Los Angeles", 34.0522, -118.2437);

    private LocationDistanceCalculatorService locationDistanceCalculatorService;

    @BeforeEach
    void setUp() {
        locationDistanceCalculatorService = new LocationDistanceCalculatorService(new DistanceCalculatorService());
    }

    @Test
    void testCalculateDistanceInKilometersUsingLocation() {

        double result = locationDistanceCalculatorService.calculateDistanceUsingLocation(newYork, losAngeles);
        System.out.println("Distance: " + result + " miles");

        // This should return the actual calculated distance
        assertEquals(3935.75, result, 0.1); // Allow small margin for precision
    }

    @Test
    void testCalculateDistanceUsingLocation_SameLocation() {

        double result = locationDistanceCalculatorService.calculateDistanceUsingLocation(newYork, newYork);

        // Then - Distance should be 0
        assertEquals(0.0, result, 0.001);
        System.out.println("Distance: " + result + " miles");
    }

    @Test
    void testPrintDistanceReport() {

        // When - This will print to console (you can see the output)
        locationDistanceCalculatorService.printDistanceReport(newYork, losAngeles);
    }

    @Test
    void testPrintDistanceReport_VeryShortDistance() {
        Location loc1 = new Location("Close 1", 40.7128, -74.0060);
        Location loc2 = new Location("Close 2", 40.7130, -74.0062); // ~200 meters

        // This will print to console - verify it doesn't crash
        assertDoesNotThrow(() -> {
            locationDistanceCalculatorService.printDistanceReport(loc1, loc2);
        }, "Print report should not throw exception");
    }

    @Test
    void testPrintDistanceReport_VeryLongDistance() {
        Location newYork = new Location("New York", 40.7128, -74.0060);
        Location sydney = new Location("Sydney", -33.8688, 151.2093);

        // This will print to console - verify it doesn't crash
        assertDoesNotThrow(() -> {
            locationDistanceCalculatorService.printDistanceReport(newYork, sydney);
        }, "Print report should not throw exception for long distances");
    }
}