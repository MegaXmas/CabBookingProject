package com.example.cabbooking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.cabbooking.model.Location;

@Service
public class LocationDistanceCalculatorService {

    private final DistanceCalculatorService distanceCalculatorService;

    // Custom exceptions for distance calculation problems
    public static class InvalidLocationException extends RuntimeException {
        public InvalidLocationException(String message) {
            super(message);
        }
    }

    public static class DistanceCalculationException extends RuntimeException {
        public DistanceCalculationException(String message) {
            super(message);
        }
    }

    @Autowired
    public LocationDistanceCalculatorService(DistanceCalculatorService distanceCalculatorService) {
        this.distanceCalculatorService = distanceCalculatorService;
    }

    /**
     * method used to take the lat and long of two Location objects and
     * give those parameters to DistanceCalculatorService.calculateDistance(),
     * which will then calculate the distance between Location 1 and Location 2
     * @param loc1 Location 1
     * @param loc2 Location 2
     * @return Distance between Location 1 and Location 2 in kilometers
     */
    public double calculateDistanceUsingLocation(Location loc1, Location loc2) {
        // Validate inputs
        validateLocations(loc1, loc2);

        try {
            double kilometers = DistanceCalculatorService.calculateDistance(
                    loc1.getLatitude(), loc1.getLongitude(),
                    loc2.getLatitude(), loc2.getLongitude()
            );

            // Validate result
            if (kilometers < 0) {
                throw new DistanceCalculationException("Distance calculation returned negative value: " + kilometers);
            }

            if (Double.isNaN(kilometers) || Double.isInfinite(kilometers)) {
                throw new DistanceCalculationException("Distance calculation returned invalid value: " + kilometers);
            }

            return kilometers;

        } catch (Exception e) {
            if (e instanceof InvalidLocationException || e instanceof DistanceCalculationException) {
                throw e;
            }
            throw new DistanceCalculationException("Failed to calculate distance: " + e.getMessage());
        }
    }

    /**
     * print a distance report between two Location objects and
     * convert the distance from km to miles
     * @param from Location 1 (initial location)
     * @param to Location 2 (destination location)
     * @return Distance between Location 1 and Location 2 in miles
     */
    public double printDistanceReport(Location from, Location to) {
        // Validate inputs
        validateLocations(from, to);

        try {
            double km = calculateDistanceUsingLocation(from, to);
            double miles = distanceCalculatorService.kmToMiles(km);

            System.out.println("=== Distance Report ===");
            System.out.println("From: " + from);
            System.out.println("To: " + to);
            System.out.printf("Distance: %.2f km (%.2f miles)%n", km, miles);
            System.out.println("=======================");

            return miles;

        } catch (Exception e) {
            if (e instanceof InvalidLocationException || e instanceof DistanceCalculationException) {
                throw e; // Re-throw our custom exceptions
            }
            throw new DistanceCalculationException("Failed to generate distance report: " + e.getMessage());
        }
    }

    /**
     * Helper method to validate that Location objects are not null
     * @param loc1 Location 1
     * @param loc2 Location 2
     */
    private void validateLocations(Location loc1, Location loc2) {
        if (loc1 == null) {
            throw new InvalidLocationException("First location cannot be null");
        }

        if (loc2 == null) {
            throw new InvalidLocationException("Second location cannot be null");
        }

        // Validate location data
        validateLocationData(loc1, "First location");
        validateLocationData(loc2, "Second location");
    }

    /**
     * Helper method to validate that Location objects have compatible parameter values
     * @param location Location to be validated
     * @param locationDescription Description of the Location object
     */
    private void validateLocationData(Location location, String locationDescription) {
        if (location.getLocationName() == null || location.getLocationName().trim().isEmpty()) {
            throw new InvalidLocationException(locationDescription + " must have a valid name");
        }

        double lat = location.getLatitude();
        double lng = location.getLongitude();

        if (Double.isNaN(lat) || Double.isNaN(lng)) {
            throw new InvalidLocationException(locationDescription + " has invalid coordinates (NaN)");
        }

        if (Double.isInfinite(lat) || Double.isInfinite(lng)) {
            throw new InvalidLocationException(locationDescription + " has invalid coordinates (infinite)");
        }

        if (lat < -90.0 || lat > 90.0) {
            throw new InvalidLocationException(locationDescription + " has invalid latitude: " + lat + " (must be between -90 and 90)");
        }

        if (lng < -180.0 || lng > 180.0) {
            throw new InvalidLocationException(locationDescription + " has invalid longitude: " + lng + " (must be between -180 and 180)");
        }
    }
}