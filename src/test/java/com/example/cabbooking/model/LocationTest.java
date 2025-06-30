package com.example.cabbooking.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class LocationTest {

    // Test the empty constructor creates an object with default values
    @Test
    public void testDefaultConstructor() {
        Location location = new Location();

        // Check that all fields start with their default values
        assertEquals(0.0, location.getLatitude());
        assertEquals(0.0, location.getLongitude());
        assertNull(location.getLocationName());
    }

    // Test the full constructor sets all values correctly
    @Test
    public void testFullConstructor() {
        Location location = new Location("Central Park", 40.7829, -73.9654);

        // Verify each field was set correctly
        assertEquals("Central Park", location.getLocationName());
        assertEquals(40.7829, location.getLatitude());
        assertEquals(-73.9654, location.getLongitude());
    }

    // Test latitude getter and setter
    @Test
    public void testLatitudeGetterAndSetter() {
        Location location = new Location();

        // Set a value using the setter
        location.setLatitude(34.0522);

        // Check that the getter returns the same value
        assertEquals(34.0522, location.getLatitude());
    }

    // Test longitude getter and setter
    @Test
    public void testLongitudeGetterAndSetter() {
        Location location = new Location();

        location.setLongitude(-118.2437);
        assertEquals(-118.2437, location.getLongitude());
    }

    // Test location name getter and setter
    @Test
    public void testLocationNameGetterAndSetter() {
        Location location = new Location();

        location.setLocationName("Los Angeles");
        assertEquals("Los Angeles", location.getLocationName());
    }

    // Test that toString contains all the expected information
    @Test
    public void testToString() {
        Location location = new Location("Times Square", 40.7580, -73.9855);

        String result = location.toString();

        // Check that the string contains all the key information
        assertTrue(result.contains("latitude=40.7580"));
        assertTrue(result.contains("longitude=-73.9855"));
        assertTrue(result.contains("name='Times Square'"));
    }

    // Test edge case: negative coordinates (valid for southern/western locations)
    @Test
    public void testNegativeCoordinates() {
        Location location = new Location("Sydney Opera House", -33.8568, 151.2153);

        assertEquals(-33.8568, location.getLatitude());
        assertEquals(151.2153, location.getLongitude());
        assertEquals("Sydney Opera House", location.getLocationName());
    }

    // Test edge case: zero coordinates (valid location)
    @Test
    public void testZeroCoordinates() {
        Location location = new Location();

        location.setLatitude(0.0);
        location.setLongitude(0.0);
        location.setLocationName("Null Island");

        assertEquals(0.0, location.getLatitude());
        assertEquals(0.0, location.getLongitude());
        assertEquals("Null Island", location.getLocationName());
    }

    // Test edge case: setting null location name
    @Test
    public void testNullLocationName() {
        Location location = new Location("Test Location", 45.0, 90.0);

        // Set location name to null - this should work without errors
        location.setLocationName(null);

        assertNull(location.getLocationName());
        // Coordinates should remain unchanged
        assertEquals(45.0, location.getLatitude());
        assertEquals(90.0, location.getLongitude());
    }

    // Test with extreme coordinate values (valid but unusual)
    @Test
    public void testExtremeCoordinates() {
        Location location = new Location();

        // Test maximum valid latitude and longitude
        location.setLatitude(90.0);    // North Pole
        location.setLongitude(180.0);  // International Date Line

        assertEquals(90.0, location.getLatitude());
        assertEquals(180.0, location.getLongitude());

        // Test minimum valid latitude and longitude
        location.setLatitude(-90.0);   // South Pole
        location.setLongitude(-180.0); // International Date Line (other side)

        assertEquals(-90.0, location.getLatitude());
        assertEquals(-180.0, location.getLongitude());
    }

    // Test that two locations with same values are equal
    @Test
    public void testEqualsWithSameValues() {
        Location location1 = new Location("Central Park", 40.7829, -73.9654);
        Location location2 = new Location("Central Park", 40.7829, -73.9654);

        // They should be equal even though they're different objects
        assertEquals(location1, location2);
        assertTrue(location1.equals(location2));
    }

    // Test that two locations with different values are not equal
    @Test
    public void testEqualsWithDifferentValues() {
        Location location1 = new Location("Central Park", 40.7829, -73.9654);
        Location location2 = new Location("Times Square", 40.7580, -73.9855);

        // They should NOT be equal
        assertNotEquals(location1, location2);
        assertFalse(location1.equals(location2));
    }

    // Test equals with null
    @Test
    public void testEqualsWithNull() {
        Location location = new Location("Central Park", 40.7829, -73.9654);

        // Should not be equal to null
        assertFalse(location.equals(null));
    }

    // Test that equal objects have the same hash code
    @Test
    public void testHashCodeConsistency() {
        Location location1 = new Location("Central Park", 40.7829, -73.9654);
        Location location2 = new Location("Central Park", 40.7829, -73.9654);

        // If objects are equal, their hash codes must be equal
        assertEquals(location1.hashCode(), location2.hashCode());
    }

    // Test that different objects usually have different hash codes
    @Test
    public void testHashCodeDifferent() {
        Location location1 = new Location("Central Park", 40.7829, -73.9654);
        Location location2 = new Location("Times Square", 40.7580, -73.9855);

        // Different objects should usually have different hash codes
        assertNotEquals(location1.hashCode(), location2.hashCode());
    }
}