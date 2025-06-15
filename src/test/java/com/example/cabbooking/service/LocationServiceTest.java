package com.example.cabbooking.service;

import com.example.cabbooking.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocationServiceTest {

    private LocationService locationService;
    private Location testLocation;

    @BeforeEach
    void setUp() {
        locationService = new LocationService();

        testLocation = new Location();
        testLocation.setLocationName("Twin Peaks");
        testLocation.setLatitude(34.2389);
        testLocation.setLongitude(-117.2336);
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
    }

    @Test
    public void testCreateLocationWithNaNCoordinates() {
        // Test with Not-a-Number values
        LocationService.InvalidCoordinateException exception = assertThrows(
                LocationService.InvalidCoordinateException.class,
                () -> locationService.createLocation("NaN Location", Double.NaN, -73.9654)
        );

        assertEquals("Coordinates cannot be NaN", exception.getMessage());
    }

    @Test
    public void testCreateLocationWithInfiniteCoordinates() {
        // Test with infinite values
        LocationService.InvalidCoordinateException exception = assertThrows(
                LocationService.InvalidCoordinateException.class,
                () -> locationService.createLocation("Infinite Location", 40.7829, Double.POSITIVE_INFINITY)
        );

        assertEquals("Coordinates cannot be infinite", exception.getMessage());
    }
    //Test found a bug in exceptions being thrown, invalid coordinates of infinity were being caught by the wrong exception message

}