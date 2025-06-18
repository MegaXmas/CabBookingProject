package com.example.cabbooking.service;

import com.example.cabbooking.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocationServiceTest {

    private LocationService locationService;

    @BeforeEach
    void setUp() {
        locationService = new LocationService();
    }

    // =================== TESTING SUCCESSFUL OPERATIONS ===================

    @Test
    public void testCreateLocationSuccess() {
        // Test the happy path - everything should work normally
        Location location = locationService.createLocation("Central Park", 40.7829, -73.9654);

        // Verify the location was created correctly
        assertNotNull(location);
        assertEquals("Central Park", location.getLocationName());
        assertEquals(40.7829, location.getLatitude());
        assertEquals(-73.9654, location.getLongitude());

        // Verify it was added to the service's internal list
        assertEquals(1, locationService.getAllLocations().size());
        assertTrue(locationService.getAllLocations().contains(location));
    }

    @Test
    public void testUpdateLocationSuccess() {
        // First create a location to update
        Location originalLocation = locationService.createLocation("Times Square", 40.7580, -73.9855);

        // Now update it
        Location updatedLocation = locationService.updateLocation(originalLocation, "New Times Square", 40.7600, -73.9800);

        // Verify the update worked
        assertNotNull(updatedLocation);
        assertEquals("New Times Square", updatedLocation.getLocationName());
        assertEquals(40.7600, updatedLocation.getLatitude());
        assertEquals(-73.9800, updatedLocation.getLongitude());

        // Verify it's the same object that was updated (not a new one created)
        assertSame(originalLocation, updatedLocation);

        // Verify the list still has only one location
        assertEquals(1, locationService.getAllLocations().size());
    }

    // =================== TESTING EXCEPTION SCENARIOS ===================

    @Test
    public void testCreateLocationWithNullName() {
        // This is where we test that our exception handling works correctly
        // We expect an IllegalArgumentException to be thrown
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> locationService.createLocation(null, 40.7829, -73.9654)
        );

        // Verify the exception has the right message
        assertEquals("Location name cannot be null or empty", exception.getMessage());

        // Verify no location was created (the list should still be empty)
        assertEquals(0, locationService.getAllLocations().size());
        System.out.println(exception.getMessage());
    }

    @Test
    public void testCreateLocationWithEmptyName() {
        // Test with empty string (different from null)
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> locationService.createLocation("   ", 40.7829, -73.9654) // Just spaces
        );

        assertEquals("Location name cannot be null or empty", exception.getMessage());
        assertEquals(0, locationService.getAllLocations().size());
        System.out.println(exception.getMessage());
    }

    @Test
    public void testCreateLocationWithInvalidLatitude() {
        // Test latitude that's too high
        LocationService.InvalidCoordinateException exception = assertThrows(
                LocationService.InvalidCoordinateException.class,
                () -> locationService.createLocation("Invalid Location", 95.0, -73.9654)
        );

        assertTrue(exception.getMessage().contains("Latitude must be between -90 and 90"));
        assertEquals(0, locationService.getAllLocations().size());
        System.out.println(exception.getMessage());
    }

    @Test
    public void testCreateLocationWithInvalidLongitude() {
        // Test longitude that's too low
        LocationService.InvalidCoordinateException exception = assertThrows(
                LocationService.InvalidCoordinateException.class,
                () -> locationService.createLocation("Invalid Location", 40.7829, -200.0)
        );

        assertTrue(exception.getMessage().contains("Longitude must be between -180 and 180"));
        assertEquals(0, locationService.getAllLocations().size());
        System.out.println(exception.getMessage());
    }

    @Test
    public void testCreateLocationWithNaNCoordinates() {
        // Test with Not-a-Number values
        LocationService.InvalidCoordinateException exception = assertThrows(
                LocationService.InvalidCoordinateException.class,
                () -> locationService.createLocation("NaN Location", Double.NaN, -73.9654)
        );

        assertEquals("Coordinates cannot be NaN", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    public void testCreateLocationWithInfiniteCoordinates() {
        // Test with infinite values
        LocationService.InvalidCoordinateException exception = assertThrows(
                LocationService.InvalidCoordinateException.class,
                () -> locationService.createLocation("Infinite Location", 40.7829, Double.POSITIVE_INFINITY)
        );

        assertEquals("Coordinates cannot be infinite", exception.getMessage());
        System.out.println(exception.getMessage());
    }
    //^^Test found a bug in exceptions being thrown, invalid coordinates of infinity were being caught by the wrong exception message^^

    // =================== TESTING UPDATE EXCEPTION SCENARIOS ===================

    @Test
    public void testUpdateLocationWithNullLocation() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> locationService.updateLocation(null, "New Name", 40.7829, -73.9654)
        );

        assertEquals("Location cannot be null", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    public void testUpdateLocationThatDoesNotExist() {
        // Create a location but don't add it to the service
        Location orphanLocation = new Location("Orphan", 40.7829, -73.9654);

        LocationService.LocationNotFoundException exception = assertThrows(
                LocationService.LocationNotFoundException.class,
                () -> locationService.updateLocation(orphanLocation, "New Name", 40.7800, -73.9600)
        );

        assertEquals("Location does not exist in the system", exception.getMessage());
        System.out.println(exception.getMessage());
    }

    @Test
    public void testUpdateLocationWithInvalidNewCoordinates() {
        // First create a valid location
        Location location = locationService.createLocation("Valid Location", 40.7829, -73.9654);

        // Now try to update it with invalid coordinates
        LocationService.InvalidCoordinateException exception = assertThrows(
                LocationService.InvalidCoordinateException.class,
                () -> locationService.updateLocation(location, "Updated Name", 95.0, -73.9654)
        );

        assertTrue(exception.getMessage().contains("Latitude must be between -90 and 90"));
        System.out.println(exception.getMessage());

        // Verify the original location wasn't changed
        assertEquals("Valid Location", location.getLocationName());
        assertEquals(40.7829, location.getLatitude());
    }

    // =================== TESTING EDGE CASES ===================

    @Test
    public void testCreateLocationWithBoundaryCoordinates() {
        // Test exactly at the boundaries (should work)
        Location northPole = locationService.createLocation("North Pole", 90.0, 0.0);
        Location southPole = locationService.createLocation("South Pole", -90.0, 0.0);
        Location dateLine = locationService.createLocation("Date Line", 0.0, 180.0);

        assertNotNull(northPole);
        assertNotNull(southPole);
        assertNotNull(dateLine);
        assertEquals(3, locationService.getAllLocations().size());
    }

    @Test
    public void testMultipleLocationOperations() {
        // Test a sequence of operations to ensure the service maintains state correctly
        Location loc1 = locationService.createLocation("Location 1", 40.7829, -73.9654);
        Location loc2 = locationService.createLocation("Location 2", 34.0522, -118.2437);

        assertEquals(2, locationService.getAllLocations().size());

        // Update the first location
        locationService.updateLocation(loc1, "Updated Location 1", 40.7800, -73.9600);

        // Verify both locations are still there and the update worked
        assertEquals(2, locationService.getAllLocations().size());
        assertEquals("Updated Location 1", loc1.getLocationName());
        assertEquals("Location 2", loc2.getLocationName()); // This shouldn't have changed
    }
}