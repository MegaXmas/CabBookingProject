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

    //-----------CreateLocation Tests----------------
    @Test
    void testCreateLocation() {
        Location result = locationService.createLocation("Downtown", 40.7, -74.0);
        assertEquals("Downtown", result.getLocationName());
        assertEquals(40.7, result.getLatitude());
        assertEquals(-74.0, result.getLongitude());
    }

    @Test
    void testCreateLocationWithInvalidValues() {
        // Test with special double values
        Location result1 = locationService.createLocation(null, Double.NaN, Double.NEGATIVE_INFINITY);
        Location result2 = locationService.createLocation("", Double.POSITIVE_INFINITY, -999.999);

        // Verify both locations were created
        assertNotNull(result1);
        assertNotNull(result2);
        assertEquals(2, locationService.getAllLocations().size());

        // Check if your service handles these appropriately
        assertNull(result1.getLocationName());
        assertTrue(Double.isNaN(result1.getLatitude()));
        assertTrue(Double.isInfinite(result1.getLongitude()));

        assertEquals("", result2.getLocationName());
        assertTrue(Double.isInfinite(result2.getLatitude()));
        assertEquals(-999.999, result2.getLongitude());
    }

    @Test
    void testCreateLocationWithBoundaryValues() {
        // Test extreme but valid coordinates
        Location result = locationService.createLocation("Extreme", -90.0, 180.0);

        assertNotNull(result);
        assertEquals(-90.0, result.getLatitude());   // Valid latitude range: -90 to 90
        assertEquals(180.0, result.getLongitude());  // Valid longitude range: -180 to 180
    }

    @Test
    void testUpdateLocation() {


    }
}