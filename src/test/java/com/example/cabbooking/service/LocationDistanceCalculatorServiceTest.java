package com.example.cabbooking.service;

import org.junit.jupiter.api.Test;
import com.example.cabbooking.model.Location;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocationDistanceCalculatorServiceTest {

    private final Location newYork = new Location("New York", 40.7128, -74.0060);
    private final Location losAngeles = new Location("Los Angeles", 34.0522, -118.2437);

    private LocationDistanceCalculatorService locationDistanceCalculatorService;

    @Test
    void testCalculateDistanceInKilometersUsingLocation() {

        double result = locationDistanceCalculatorService.calculateDistanceUsingLocation(newYork, losAngeles);
        System.out.println("Distance: " + result + " km");

        // This should return the actual calculated distance
        assertEquals(3935.75, result, 0.1); // Allow small margin for precision
    }

    @Test
    void testPrintDistanceReport() {


        // When (this will print to console)
        locationDistanceCalculatorService.printDistanceReport(newYork, losAngeles);


        // You could also capture System.out if needed
    }
}