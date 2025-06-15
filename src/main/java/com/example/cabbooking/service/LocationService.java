package com.example.cabbooking.service;

import com.example.cabbooking.model.Location;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class LocationService {

    private final List<Location> locations = new ArrayList<>();

    // Custom exception for location-related problems
    public class LocationNotFoundException extends RuntimeException {
        public LocationNotFoundException(String message) {
            super(message);
        }
    }

    public class InvalidCoordinateException extends RuntimeException {
        public InvalidCoordinateException(String message) {
            super(message);
        }
    }

    public Location createLocation(String name, double lat, double lng) {

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Location name cannot be null or empty");
        }

        if (!validateCoordinates(lat, lng)) {
            throw new IllegalArgumentException("Invalid coordinates: latitude must be between -90 and 90, longitude between -180 and 180");
        }

        Location location = new Location();
        location.setLocationName(name);
        location.setLatitude(lat);
        location.setLongitude(lng);
        locations.add(location);

        System.out.println("Location successfully created");
        System.out.println(location);

        return location;
    }

    public Location updateLocation(Location location, String newName, double newLat, double newLng) {
        // Input validation with specific exceptions
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null");
        }

        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Location name cannot be null or empty");
        }

        if (!validateCoordinates(newLat, newLng)) {
            throw new IllegalArgumentException("Invalid coordinates: latitude must be between -90 and 90, longitude between -180 and 180");
        }

        // Business logic validation
        if (!locations.contains(location)) {
            throw new LocationNotFoundException("Location does not exist in the system");
        }

        // If we get here, everything is valid
        location.setLocationName(newName);
        location.setLatitude(newLat);
        location.setLongitude(newLng);

        // Optional: informational logging
        System.out.println("Location updated successfully: " + location.getLocationName());

        return location;
    }

    // Helper method to validate coordinates
    private boolean validateCoordinates(double latitude, double longitude) {
        if (latitude < -90.0 || latitude > 90.0 && Double.isFinite(latitude)) {
            throw new InvalidCoordinateException("Latitude must be between -90 and 90, got: " + latitude);
        }

        if (longitude < -180.0 || longitude > 180.0 && Double.isFinite(longitude)) {
            throw new InvalidCoordinateException("Longitude must be between -180 and 180, got: " + longitude);
        }

        if (Double.isNaN(latitude) || Double.isNaN(longitude)) {
            throw new InvalidCoordinateException("Coordinates cannot be NaN");
        }

        if (Double.isInfinite(latitude) || Double.isInfinite(longitude)) {
            throw new InvalidCoordinateException("Coordinates cannot be infinite");
        }

        return true;
    }

    public void printLocationInfo(Location location) {
        System.out.println("Name: " + location.getLocationName());
        System.out.println("Lat: " + location.getLatitude());
        System.out.println("Lng: " + location.getLongitude());
    }

    public List<Location> getAllLocations() {
        return locations;
    }
}