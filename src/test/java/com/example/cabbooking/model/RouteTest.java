package com.example.cabbooking.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

public class RouteTest {

    private Location startLocation;
    private Location endLocation;

    // Set up test locations before each test
    @BeforeEach
    public void setUp() {
        startLocation = new Location("Central Park", 40.7829, -73.9654);
        endLocation = new Location("Times Square", 40.7580, -73.9855);
    }

    // Test the empty constructor creates an object with default values
    @Test
    public void testDefaultConstructor() {
        Route route = new Route();

        // Check that all fields start with their default values
        assertNull(route.getFrom());
        assertNull(route.getTo());
        assertEquals(0.0, route.getDistance());
    }

    // Test the full constructor sets all values correctly
    @Test
    public void testFullConstructor() {
        Route route = new Route(startLocation, endLocation, 2.5);

        // Verify each field was set correctly
        assertEquals(startLocation, route.getFrom());
        assertEquals(endLocation, route.getTo());
        assertEquals(2.5, route.getDistance());
    }

    // Test that setters and getters work properly for 'from' location
    @Test
    public void testFromGetterAndSetter() {
        Route route = new Route();

        // Set a value using the setter
        route.setFrom(startLocation);

        // Check that the getter returns the same value
        assertEquals(startLocation, route.getFrom());
    }

    // Test 'to' location getter and setter
    @Test
    public void testToGetterAndSetter() {
        Route route = new Route();

        route.setTo(endLocation);
        assertEquals(endLocation, route.getTo());
    }

    // Test distance getter and setter
    @Test
    public void testDistanceGetterAndSetter() {
        Route route = new Route();

        route.setDistance(15.7);
        assertEquals(15.7, route.getDistance());
    }

    // Test that route works with the same location for start and end
    @Test
    public void testSameStartAndEndLocation() {
        Route route = new Route(startLocation, startLocation, 0.0);

        assertEquals(startLocation, route.getFrom());
        assertEquals(startLocation, route.getTo());
        assertEquals(0.0, route.getDistance());
    }

    // Test edge case: zero distance
    @Test
    public void testZeroDistance() {
        Route route = new Route();

        route.setDistance(0.0);
        assertEquals(0.0, route.getDistance());
    }

    // Test edge case: very long distance
    @Test
    public void testLongDistance() {
        Route route = new Route();

        route.setDistance(1000.5);
        assertEquals(1000.5, route.getDistance());
    }

    // Test edge case: setting locations to null
    @Test
    public void testNullLocations() {
        Route route = new Route(startLocation, endLocation, 5.0);

        // Set locations to null - this should work without errors
        route.setFrom(null);
        route.setTo(null);

        assertNull(route.getFrom());
        assertNull(route.getTo());
        // Distance should remain unchanged
        assertEquals(5.0, route.getDistance());
    }

    // Test that we can modify location objects after setting them in route
    @Test
    public void testLocationObjectModification() {
        Location modifiableLocation = new Location("Test Location", 10.0, 20.0);
        Route route = new Route();

        route.setFrom(modifiableLocation);

        // Modify the location object
        modifiableLocation.setLocationName("Modified Location");

        // The route should reference the same object, so it reflects the change
        assertEquals("Modified Location", route.getFrom().getLocationName());
    }

    // Test creating multiple routes with different combinations
    @Test
    public void testMultipleRoutes() {
        Location airport = new Location("JFK Airport", 40.6413, -73.7781);

        Route route1 = new Route(startLocation, endLocation, 2.5);
        Route route2 = new Route(endLocation, airport, 15.2);

        // Verify both routes are independent
        assertNotEquals(route1.getFrom(), route2.getFrom());
        assertNotEquals(route1.getTo(), route2.getTo());
        assertNotEquals(route1.getDistance(), route2.getDistance());

        // But route1's 'to' should equal route2's 'from'
        assertEquals(route1.getTo(), route2.getFrom());
    }

    // Test with very precise distance values
    @Test
    public void testPreciseDistance() {
        Route route = new Route();

        route.setDistance(2.123456789);
        assertEquals(2.123456789, route.getDistance());
    }

    @Test
    public void testEqualsSameObject() {
        Route route = new Route(startLocation, endLocation, 0.0);
        assertTrue(route.equals(route));
    }

    @Test
    void testEqualsWithSameValues() {
        Route route1 = new Route(startLocation, endLocation, 2.5);
        Route route2 = new Route(startLocation, endLocation, 2.5);

        assertTrue(route1.equals(route2));
        System.out.println(route1 + " " + route2);
    }

    @Test
    void testEqualsWithDifferentFromAndToValues() {
        //Should be false

        Location location1 = new Location("location1", 10.0, 20.0);
        Location location2 = new Location("location2", 10.0, 20.0);

        Route route1 = new Route(location1, location2, 2.5);
        Route route2 = new Route(location2, location1, 2.5);

        assertFalse(route1.equals(route2));
        System.out.println(route1 + " " + route2);
    }

    @Test
    void testEqualsWithDifferentDistanceValues() {
        //should be false

        Route route1 = new Route(startLocation, endLocation, 2.5);
        Route route2 = new Route(endLocation, startLocation, 23);

        assertFalse(route1.equals(route2));
        System.out.println(route1 + " " + route2);
    }

    @Test
    void testEqualsWithNaNDistanceValues() {
        Route route1 = new Route(startLocation, endLocation, Double.NaN);
        Route route2 = new Route(startLocation, endLocation, Double.NaN);

        assertTrue(route1.equals(route2));
    }

    @Test
    void testEqualsWithInfiniteDistanceValues() {
        Route route1 = new Route(startLocation, endLocation, Double.POSITIVE_INFINITY);
        Route route2 = new Route(startLocation, endLocation, Double.POSITIVE_INFINITY);

        assertTrue(route1.equals(route2));

        Route route3 = new Route(startLocation, endLocation, Double.NEGATIVE_INFINITY);
        Route route4 = new Route(startLocation, endLocation, Double.NEGATIVE_INFINITY);

        assertTrue(route3.equals(route4));

        assertFalse(route1.equals(route3));
    }

    @Test
    void testEqualsWithNullValues() {
        Route route1 = new Route(null, null, 2.5);
        Route route2 = new Route(null, null, 2.5);

        assertTrue(route1.equals(route2));
        System.out.println(route1 + " " + route2);
    }

    @Test
    void testEqualsTransitive() {
        Route route1 = new Route(startLocation, endLocation, 2.5);
        Route route2 = new Route(startLocation, endLocation, 2.5);
        Route route3 = new Route(startLocation, endLocation, 2.5);

        assertTrue(route1.equals(route2));
        assertTrue(route1.equals(route3));
        assertTrue(route2.equals(route3));
        System.out.println(route1 + " " + route2 + " " + route3);
    }

    @Test
    void testHashCodeEqualObjectsSameHashCode() {
        Route route1 = new Route(startLocation, endLocation, 2.5);
        Route route2 = new Route(startLocation, endLocation, 2.5);

        assertEquals(route1.hashCode(), route2.hashCode());
    }

    @Test
    void testHashCodeDifferentObjectsDifferentHashCode() {
        Route route1 = new Route(startLocation, endLocation, 2.5);
        Route route2 = new Route(startLocation, endLocation, 2);

        assertNotEquals(route1.hashCode(), route2.hashCode());
    }

    @Test
    void testHashCodeWithNullValues() {
        Route route1 = new Route(null, null, 2.5);
        Route route2 = new Route(null, null, 2.5);

        assertEquals(route1.hashCode(), route2.hashCode());
    }

    @Test
    void testHashCodeConsistency() {
        Route route1 = new Route(startLocation, endLocation, 2.5);

        int hash1 = route1.hashCode();
        int hash2 = route1.hashCode();

        assertEquals(hash1, hash2);
    }
}