package com.example.cabbooking.service;

import com.example.cabbooking.model.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LocationServiceTest {

    private LocationService locationService;

    @BeforeEach
    void setUp() {
        locationService = new LocationService();
    }

    // === TESTING THE WASHINGTON DC INITIALIZATION ===

    @Test
    public void testInitializeWashingtonDCLocations_LoadsAllExpectedLocations() {
        // ACT: Initialize Washington DC locations
        locationService.initializeWashingtonDCLocations();

        // ASSERT: Verify that all expected locations were created
        List<Location> allLocations = locationService.getAllLocations();

        assertEquals(15, allLocations.size(), "Should have exactly 15 Washington DC locations");

        // Verify some key locations are present by checking they can be found by name
        // This tests both the initialization and the search functionality together
        assertNotNull(locationService.findLocationByName("The White House"),
                "The White House should be in the system");
        assertNotNull(locationService.findLocationByName("Ronald Reagan Washington National Airport"),
                "Reagan Airport should be in the system");
        assertNotNull(locationService.findLocationByName("Smithsonian National Museum of Natural History"),
                "Natural History Museum should be in the system");
        assertNotNull(locationService.findLocationByName("Pentagon"),
                "Pentagon should be in the system");

        System.out.println("✓ All 15 Washington DC locations loaded successfully");
    }

    @Test
    public void testInitializeWashingtonDCLocations_LocationsHaveRealisticCoordinates() {
        // ACT: Initialize locations
        locationService.initializeWashingtonDCLocations();

        // ASSERT: Check that all locations have coordinates that make sense for Washington DC area
        List<Location> locations = locationService.getAllLocations();

        for (Location location : locations) {
            // Washington DC area latitude should be roughly between 38.8 and 39.0
            assertTrue(location.getLatitude() >= 38.8 && location.getLatitude() <= 39.0,
                    "Location " + location.getLocationName() + " has unrealistic latitude: " + location.getLatitude());

            // Washington DC area longitude should be roughly between -77.6 and -76.9
            assertTrue(location.getLongitude() >= -77.6 && location.getLongitude() <= -76.9,
                    "Location " + location.getLocationName() + " has unrealistic longitude: " + location.getLongitude());
        }

        System.out.println("✓ All locations have realistic Washington DC area coordinates");
    }

    @Test
    public void testInitializeWashingtonDCLocations_MultipleCallsDontCreateDuplicates() {
        // ACT: Call initialization multiple times
        locationService.initializeWashingtonDCLocations();
        int firstCallCount = locationService.getAllLocations().size();

        locationService.initializeWashingtonDCLocations();
        int secondCallCount = locationService.getAllLocations().size();

        locationService.initializeWashingtonDCLocations();
        int thirdCallCount = locationService.getAllLocations().size();

        // ASSERT: Each call should result in the same number of locations (no duplicates)
        assertEquals(firstCallCount, secondCallCount,
                "Second initialization should not create duplicate locations");
        assertEquals(secondCallCount, thirdCallCount,
                "Third initialization should not create duplicate locations");
        assertEquals(15, thirdCallCount,
                "Should always have exactly 15 locations regardless of how many times we initialize");

        System.out.println("✓ Multiple initialization calls handled correctly without duplicates");
    }

    // === TESTING THE FIND LOCATION BY NAME FUNCTIONALITY ===

    @Test
    public void testFindLocationByName_FindsExactMatches() {
        // ARRANGE: Set up the location system
        locationService.initializeWashingtonDCLocations();

        // ACT & ASSERT: Test finding several different types of locations

        // Test finding a government location
        Location whiteHouse = locationService.findLocationByName("The White House");
        assertNotNull(whiteHouse, "Should find The White House");
        assertEquals("The White House", whiteHouse.getLocationName());
        assertEquals(38.8977, whiteHouse.getLatitude(), 0.1); // Small tolerance for floating point comparison
        assertEquals(-77.0365, whiteHouse.getLongitude(), 0.1);

        // Test finding a transportation location
        Location unionStation = locationService.findLocationByName("Union Station");
        assertNotNull(unionStation, "Should find Union Station");
        assertEquals("Union Station", unionStation.getLocationName());

        // Test finding a museum location
        Location naturalHistory = locationService.findLocationByName("Smithsonian National Museum of Natural History");
        assertNotNull(naturalHistory, "Should find Natural History Museum");
        assertEquals("Smithsonian National Museum of Natural History", naturalHistory.getLocationName());

        System.out.println("✓ Successfully found locations by exact name match");
    }

    @Test
    public void testFindLocationByName_ReturnsNullForNonexistentLocations() {
        // ARRANGE: Set up the location system
        locationService.initializeWashingtonDCLocations();

        // ACT & ASSERT: Test searching for locations that don't exist

        assertNull(locationService.findLocationByName("Nonexistent Place"),
                "Should return null for location that doesn't exist");

        assertNull(locationService.findLocationByName("The Blue House"),
                "Should return null for similar but incorrect name");

        assertNull(locationService.findLocationByName("white house"),
                "Should return null for incorrect case (search is case-sensitive)");

        System.out.println("✓ Correctly returns null for nonexistent locations");
    }

    @Test
    public void testFindLocationByName_HandlesInvalidInput() {
        // ARRANGE: Set up the location system
        locationService.initializeWashingtonDCLocations();

        // ACT & ASSERT: Test with various invalid inputs

        assertNull(locationService.findLocationByName(null),
                "Should handle null input gracefully");

        assertNull(locationService.findLocationByName(""),
                "Should handle empty string gracefully");

        assertNull(locationService.findLocationByName("   "),
                "Should handle whitespace-only string gracefully");

        System.out.println("✓ Handles invalid input parameters correctly");
    }

    @Test
    public void testFindLocationByName_WorksWithEmptyLocationList() {
        // Note: Deliberately don't call initializeWashingtonDCLocations()

        // ACT & ASSERT: Search in empty location system
        assertNull(locationService.findLocationByName("The White House"),
                "Should return null when no locations are loaded");

        assertEquals(0, locationService.getAllLocations().size(),
                "Location list should be empty initially");

        System.out.println("✓ Search works correctly even with empty location system");
    }

    // === TESTING THE ORIGINAL LOCATION SERVICE FUNCTIONALITY ===

    @Test
    public void testCreateLocation_StillWorksAfterDCInitialization() {
        // ARRANGE: Initialize DC locations first
        locationService.initializeWashingtonDCLocations();
        int dcLocationCount = locationService.getAllLocations().size();

        // ACT: Create a new custom location
        Location customLocation = locationService.createLocation("Custom Test Location", 40.7128, -74.0060);

        // ASSERT: Verify the new location was added to the existing ones
        assertNotNull(customLocation, "Should successfully create custom location");
        assertEquals("Custom Test Location", customLocation.getLocationName());
        assertEquals(dcLocationCount + 1, locationService.getAllLocations().size(),
                "Should have DC locations plus the new custom location");

        // Verify both DC locations and custom locations can be found
        assertNotNull(locationService.findLocationByName("The White House"),
                "Should still find DC locations");
        assertNotNull(locationService.findLocationByName("Custom Test Location"),
                "Should find newly created custom location");

        System.out.println("✓ Original functionality works alongside new DC initialization");
    }

    @Test
    public void testCreateLocation_ExceptionHandlingStillWorks() {
        // Test that creating locations with invalid data still throws appropriate exceptions

        // Test null name
        assertThrows(IllegalArgumentException.class, () -> {
            locationService.createLocation(null, 40.7128, -74.0060);
        }, "Should throw exception for null name");

        // Test empty name
        assertThrows(IllegalArgumentException.class, () -> {
            locationService.createLocation("   ", 40.7128, -74.0060);
        }, "Should throw exception for empty name");

        // Test invalid coordinates
        assertThrows(LocationService.InvalidCoordinateException.class, () -> {
            locationService.createLocation("Invalid Location", 95.0, -74.0060);
        }, "Should throw exception for invalid latitude");

        System.out.println("✓ Exception handling continues to work correctly");
    }

    // === INTEGRATION-STYLE TESTS ===

    @Test
    public void testRealisticWebApplicationWorkflow() {
        // STEP 1: Initialize locations
        locationService.initializeWashingtonDCLocations();
        System.out.println("Application started, locations loaded");

        // STEP 2: User selects pickup location
        String userPickupChoice = "The White House";
        Location pickupLocation = locationService.findLocationByName(userPickupChoice);
        assertNotNull(pickupLocation, "User's pickup choice should be found");
        System.out.println("Found pickup location: " + pickupLocation.getLocationName());

        // STEP 3: User selects dropoff location
        String userDropoffChoice = "Lincoln Memorial";
        Location dropoffLocation = locationService.findLocationByName(userDropoffChoice);
        assertNotNull(dropoffLocation, "User's dropoff choice should be found");
        System.out.println("Found dropoff location: " + dropoffLocation.getLocationName());

        // STEP 4: Verify both locations have realistic coordinates for route calculation
        assertTrue(pickupLocation.getLatitude() != 0.0, "Pickup location should have real coordinates");
        assertTrue(pickupLocation.getLongitude() != 0.0, "Pickup location should have real coordinates");
        assertTrue(dropoffLocation.getLatitude() != 0.0, "Dropoff location should have real coordinates");
        assertTrue(dropoffLocation.getLongitude() != 0.0, "Dropoff location should have real coordinates");

        // STEP 5: Verify locations are different
        assertNotEquals(pickupLocation.getLatitude(), dropoffLocation.getLatitude(),
                "Pickup and dropoff should have different coordinates");
        assertNotEquals(pickupLocation.getLongitude(), dropoffLocation.getLongitude(),
                "Pickup and dropoff should have different coordinates");

        System.out.println("✓ Complete web application workflow simulation successful");
    }

    @Test
    public void testPerformanceWithFullLocationSet() {
        // ARRANGE: Initialize all locations
        locationService.initializeWashingtonDCLocations();

        // ACT: Time how long it takes to search for all locations
        long startTime = System.nanoTime();

        // Search for each location to simulate heavy usage
        List<Location> allLocations = locationService.getAllLocations();
        for (Location location : allLocations) {
            Location found = locationService.findLocationByName(location.getLocationName());
            assertNotNull(found, "Should find location: " + location.getLocationName());
        }

        long endTime = System.nanoTime();
        long durationMicroseconds = (endTime - startTime) / 1000;

        // ASSERT: Search operations should be fast (under 1 millisecond for 14 locations)
        assertTrue(durationMicroseconds < 1000,
                "Searching all locations should be fast, took: " + durationMicroseconds + " microseconds");

        System.out.println("✓ Performance test passed - searched " + allLocations.size() +
                " locations in " + durationMicroseconds + " microseconds");
    }

    @Test
    public void testWashingtonDCLocationCompleteness() {
        // ARRANGE: Initialize locations
        locationService.initializeWashingtonDCLocations();

        // Define the categories and locations we expect
        String[] transportationLocations = {
                "Ronald Reagan Washington National Airport",
                "Washington Dulles International Airport",
                "Union Station",
                "Metro Center Station"
        };

        String[] governmentLocations = {
                "The White House",
                "U.S. Capitol Building",
                "Lincoln Memorial",
                "Washington Monument",
                "Jefferson Memorial",
                "Supreme Court",
                "Pentagon"
        };

        String[] museumLocations = {
                "Smithsonian National Museum of Natural History",
                "National Air and Space Museum",
                "Kennedy Center",
                "National Gallery of Art"
        };

        // ACT & ASSERT: Verify all expected locations are present

        // Check transportation locations
        for (String locationName : transportationLocations) {
            assertNotNull(locationService.findLocationByName(locationName),
                    "Transportation location missing: " + locationName);
        }
        System.out.println("✓ All " + transportationLocations.length + " transportation locations found");

        // Check government locations
        for (String locationName : governmentLocations) {
            assertNotNull(locationService.findLocationByName(locationName),
                    "Government location missing: " + locationName);
        }
        System.out.println("✓ All " + governmentLocations.length + " government locations found");

        // Check museum locations
        for (String locationName : museumLocations) {
            assertNotNull(locationService.findLocationByName(locationName),
                    "Museum location missing: " + locationName);
        }
        System.out.println("✓ All " + museumLocations.length + " museum locations found");

        // Verify total count matches our expectations
        int expectedTotal = transportationLocations.length + governmentLocations.length + museumLocations.length;
        assertEquals(expectedTotal, locationService.getAllLocations().size(),
                "Total location count should match sum of all categories");

        System.out.println("✓ Location dataset completeness verified: " + expectedTotal + " total locations");
    }

    // === EDGE CASE AND STRESS TESTING ===

    @Test
    public void testLocationSearchAccuracy() {
        // ARRANGE: Initialize locations
        locationService.initializeWashingtonDCLocations();

        // ACT & ASSERT: Test that we get exact matches even with similar names

        // Test locations with "National" in the name
        Location nationalAirSpace = locationService.findLocationByName("National Air and Space Museum");
        Location nationalGallery = locationService.findLocationByName("National Gallery of Art");
        Location nationalHistory = locationService.findLocationByName("Smithsonian National Museum of Natural History");

        assertNotNull(nationalAirSpace, "Should find Air and Space Museum");
        assertNotNull(nationalGallery, "Should find National Gallery");
        assertNotNull(nationalHistory, "Should find Natural History Museum");

        // Verify they are different objects
        assertNotEquals(nationalAirSpace.getLocationName(), nationalGallery.getLocationName());
        assertNotEquals(nationalAirSpace.getLocationName(), nationalHistory.getLocationName());
        assertNotEquals(nationalGallery.getLocationName(), nationalHistory.getLocationName());

        // Test locations with "Washington" in the name
        Location washingtonMonument = locationService.findLocationByName("Washington Monument");
        Location dullesAirport = locationService.findLocationByName("Washington Dulles International Airport");

        assertNotNull(washingtonMonument, "Should find Washington Monument");
        assertNotNull(dullesAirport, "Should find Dulles Airport");
        assertNotEquals(washingtonMonument.getLocationName(), dullesAirport.getLocationName());

        System.out.println("✓ Location search accuracy verified for similar names");
    }

    @Test
    public void testCommonUserMistakes() {
        // ARRANGE: Initialize locations
        locationService.initializeWashingtonDCLocations();

        // ACT & ASSERT: Test common user input mistakes

        // Case sensitivity errors
        assertNull(locationService.findLocationByName("the white house"),
                "Search should be case-sensitive");
        assertNull(locationService.findLocationByName("THE WHITE HOUSE"),
                "Search should be case-sensitive");

        // Partial name errors
        assertNull(locationService.findLocationByName("White House"),
                "Should require exact name match");
        assertNull(locationService.findLocationByName("Pentagon Building"),
                "Should require exact name match");

        // Common abbreviation errors
        assertNull(locationService.findLocationByName("Reagan Airport"),
                "Should require full official name");
        assertNull(locationService.findLocationByName("Dulles"),
                "Should require full official name");

        System.out.println("✓ Confirmed that search requires exact name matches");
    }

    @Test
    public void testDataIntegrity() {
        // ARRANGE: Initialize locations
        locationService.initializeWashingtonDCLocations();

        // ACT & ASSERT: Verify data integrity

        List<Location> allLocations = locationService.getAllLocations();

        // Check that no location has null or empty names
        for (Location location : allLocations) {
            assertNotNull(location.getLocationName(), "Location name should not be null");
            assertFalse(location.getLocationName().trim().isEmpty(),
                    "Location name should not be empty");
        }

        // Check that no two locations have identical names
        for (int i = 0; i < allLocations.size(); i++) {
            for (int j = i + 1; j < allLocations.size(); j++) {
                assertNotEquals(allLocations.get(i).getLocationName(),
                        allLocations.get(j).getLocationName(),
                        "No two locations should have the same name");
            }
        }

        // Check that all locations have valid coordinates (not zero)
        for (Location location : allLocations) {
            assertNotEquals(0.0, location.getLatitude(),
                    "Location should have real latitude: " + location.getLocationName());
            assertNotEquals(0.0, location.getLongitude(),
                    "Location should have real longitude: " + location.getLocationName());
        }

        System.out.println("✓ Data integrity verified for all " + allLocations.size() + " locations");
    }
}