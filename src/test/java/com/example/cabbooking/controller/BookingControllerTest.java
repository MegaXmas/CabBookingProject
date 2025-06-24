package com.example.cabbooking.controller;

import com.example.cabbooking.model.Client;
import com.example.cabbooking.model.Location;
import com.example.cabbooking.model.Route;
import com.example.cabbooking.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * This test class demonstrates several important testing concepts:
 * 1. Unit Testing: We test each method in isolation
 * 2. Mocking: We create fake versions of dependencies so we can control their behavior
 * 3. Test Data Setup: We create consistent test data that represents real scenarios
 * 4. Assertion Testing: We verify that our code produces the expected results
 */
public class BookingControllerTest {

    // Mock objects are like stunt doubles for our real services
    // They look like the real thing, but we can control exactly what they do
    @Mock
    private BookingService bookingService;

    @Mock
    private CalculateFareService calculateFareService;

    @Mock
    private LocationService locationService;

    @Mock
    private RouteService routeService;

    // The actual controller we're testing - this is the "actor" in our test "movie"
    private BookingController bookingController;

    // Test data that represents realistic scenarios
    private Location whiteHouse;
    private Location lincolnMemorial;
    private Route testRoute;
    private Client testClient;

    /**
     * This method runs before each test, setting up our "test stage"
     * Think of it like setting up props before each scene in a play
     */
    @BeforeEach
    public void setUp() {
        // Initialize our mocks (creates the stunt doubles)
        MockitoAnnotations.openMocks(this);

        // Create our controller with the mock dependencies
        // This is like assembling our test subject with controlled parts
        bookingController = new BookingController(
                bookingService,
                calculateFareService,
                locationService,
                routeService
        );

        // Set up realistic test data
        // These represent real locations someone might actually select
        whiteHouse = new Location("The White House", 38.8977, -77.0365);
        lincolnMemorial = new Location("Lincoln Memorial", 38.8893, -77.0502);

        // Create a route between these locations (distance would be calculated in real service)
        testRoute = new Route(whiteHouse, lincolnMemorial, 2.5); // 2.5 km distance

        // Create a test client for the original booking method
        testClient = new Client(1, "John Doe", "john@example.com",
                "555-1234", "123 Main St", "4111-1111-1111-1111");
    }

    // === TESTING THE NEW WEB BOOKING FUNCTIONALITY ===

    /**
     * This tests the happy path - when everything works perfectly
     * This is like testing that a well-rehearsed dance routine goes smoothly
     */
    @Test
    public void testCalculateWebBookingFare_Success() {

        when(locationService.findLocationByName("The White House")).thenReturn(whiteHouse);
        when(locationService.findLocationByName("Lincoln Memorial")).thenReturn(lincolnMemorial);

        // Mock the route service to return our test route
        when(routeService.createRoute(whiteHouse, lincolnMemorial)).thenReturn(testRoute);

        // Mock the fare calculation ($3 base + $3/mile)
        // Distance is 2.5 km = ~1.55 miles, so fare should be $3 + (1.55 * 3) = ~$7.65
        when(calculateFareService.calculateFare(testRoute)).thenReturn(7.65);

        // Create the request that would come from the web form
        BookingController.WebBookingRequest request = new BookingController.WebBookingRequest(
                "The White House", "Lincoln Memorial"
        );

        // ACT: Call the method we're testing
        ResponseEntity<Map<String, Object>> response = bookingController.calculateWebBookingFare(request);

        // ASSERT: Verify everything worked as expected

        // Check that we got a successful HTTP response
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // Get the response body to examine the details
        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);

        // Verify the response contains all the expected information
        assertTrue((Boolean) responseBody.get("success"));
        assertEquals("The White House", responseBody.get("pickupLocation"));
        assertEquals("Lincoln Memorial", responseBody.get("dropoffLocation"));
        assertEquals(2.5, responseBody.get("distance"));
        assertEquals(7.65, responseBody.get("fareAmount"));

        // VERIFY: Make sure our services were called correctly
        // This ensures the controller is orchestrating the services properly
        verify(locationService).findLocationByName("The White House");
        verify(locationService).findLocationByName("Lincoln Memorial");
        verify(routeService).createRoute(whiteHouse, lincolnMemorial);
        verify(calculateFareService).calculateFare(testRoute);

        System.out.println(responseBody);
    }

    /**
     * This tests what happens when a location isn't found
     * This is like testing what happens when an actor forgets their lines
     */
    @Test
    public void testCalculateWebBookingFare_PickupLocationNotFound() {
        // ARRANGE: Set up a scenario where the pickup location doesn't exist
        when(locationService.findLocationByName("Nonexistent Place")).thenReturn(null);
        when(locationService.findLocationByName("Lincoln Memorial")).thenReturn(lincolnMemorial);

        BookingController.WebBookingRequest request = new BookingController.WebBookingRequest(
                "Nonexistent Place", "Lincoln Memorial"
        );

        // ACT: Try to calculate fare with invalid pickup location
        ResponseEntity<Map<String, Object>> response = bookingController.calculateWebBookingFare(request);

        // ASSERT: Verify we get appropriate error response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertFalse((Boolean) responseBody.get("success"));
        assertTrue(((String) responseBody.get("error")).contains("Pickup location not found"));

        // VERIFY: Ensure we didn't try to create routes with invalid data
        verify(routeService, never()).createRoute(any(), any());
        verify(calculateFareService, never()).calculateFare(any());
    }

    /**
     * This tests what happens when the dropoff location isn't found
     */
    @Test
    public void testCalculateWebBookingFare_DropoffLocationNotFound() {
        // ARRANGE: Valid pickup, invalid dropoff
        when(locationService.findLocationByName("The White House")).thenReturn(whiteHouse);
        when(locationService.findLocationByName("Nonexistent Place")).thenReturn(null);

        BookingController.WebBookingRequest request = new BookingController.WebBookingRequest(
                "The White House", "Nonexistent Place"
        );

        // ACT & ASSERT: Similar to above test but for dropoff location
        ResponseEntity<Map<String, Object>> response = bookingController.calculateWebBookingFare(request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Map<String, Object> responseBody = response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertTrue(((String) responseBody.get("error")).contains("Drop-off location not found"));

        System.out.println(responseBody);
    }

    /**
     * This tests what happens when the fare calculation service throws an exception
     * This simulates unexpected errors, like a database connection failing
     */
    @Test
    public void testCalculateWebBookingFare_ServiceException() {
        // ARRANGE: Set up scenario where everything looks good but fare calculation fails
        when(locationService.findLocationByName("The White House")).thenReturn(whiteHouse);
        when(locationService.findLocationByName("Lincoln Memorial")).thenReturn(lincolnMemorial);
        when(routeService.createRoute(whiteHouse, lincolnMemorial)).thenReturn(testRoute);

        // Simulate the fare service throwing an exception
        when(calculateFareService.calculateFare(testRoute))
                .thenThrow(new RuntimeException("Fare calculation system temporarily unavailable"));

        BookingController.WebBookingRequest request = new BookingController.WebBookingRequest(
                "The White House", "Lincoln Memorial"
        );

        // ACT: Try to calculate fare when service fails
        ResponseEntity<Map<String, Object>> response = bookingController.calculateWebBookingFare(request);

        // ASSERT: Verify we handle the error gracefully
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Map<String, Object> responseBody = response.getBody();
        assertFalse((Boolean) responseBody.get("success"));
        assertTrue(((String) responseBody.get("error")).contains("Error calculating fare"));

        System.out.println(responseBody);
    }

    // === TESTING THE GET LOCATIONS FUNCTIONALITY ===

    /**
     * This tests the endpoint that returns all available locations
     * This would be used if you wanted to dynamically populate your dropdown menus
     */
    @Test
    public void testGetAllLocations_Success() {
        // ARRANGE: Create a list of locations that the service would return
        List<Location> expectedLocations = Arrays.asList(whiteHouse, lincolnMemorial);
        when(locationService.getAllLocations()).thenReturn(expectedLocations);

        // ACT: Call the get locations endpoint
        ResponseEntity<List<Location>> response = bookingController.getAllLocations();

        // ASSERT: Verify we get the expected locations
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<Location> responseBody = response.getBody();
        assertNotNull(responseBody);
        assertEquals(2, responseBody.size());
        assertTrue(responseBody.contains(whiteHouse));
        assertTrue(responseBody.contains(lincolnMemorial));

        // VERIFY: Ensure the service was called
        verify(locationService).getAllLocations();

        System.out.println(responseBody);
    }

    /**
     * This tests what happens when the location service fails
     */
    @Test
    public void testGetAllLocations_ServiceException() {
        // ARRANGE: Simulate the location service throwing an exception
        when(locationService.getAllLocations())
                .thenThrow(new RuntimeException("Database connection failed"));

        // ACT: Try to get locations when service fails
        ResponseEntity<List<Location>> response = bookingController.getAllLocations();

        // ASSERT: Verify we handle the error appropriately
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    // === TESTING THE ORIGINAL BOOKING FUNCTIONALITY ===

    /**
     * This tests your original bookCab method to ensure it still works
     * This demonstrates backward compatibility - new features don't break old ones
     */
    @Test
    public void testBookCab_OriginalFunctionality() {
        // ARRANGE: Set up the original booking scenario
        // We don't need to mock return values here because this method doesn't return anything

        // ACT: Call the original booking method
        // This should work exactly as it did before our changes
        assertDoesNotThrow(() -> {
            bookingController.bookCab(testClient, testRoute);
        });

        // VERIFY: Ensure both services were called as expected
        verify(bookingService).bookCab(testClient, testRoute);
        verify(calculateFareService).calculateFare(testRoute);
    }

    // === TESTING EDGE CASES AND DATA VALIDATION ===

    /**
     * This tests the WebBookingRequest class to ensure it handles data correctly
     * Testing data classes might seem boring, but they're crucial for web applications
     */
    @Test
    public void testWebBookingRequest_DataHandling() {
        // Test the default constructor
        BookingController.WebBookingRequest emptyRequest = new BookingController.WebBookingRequest();
        assertNull(emptyRequest.getPickupLocation());
        assertNull(emptyRequest.getDropoffLocation());

        // Test the parameterized constructor
        BookingController.WebBookingRequest filledRequest = new BookingController.WebBookingRequest(
                "Test Pickup", "Test Dropoff"
        );
        assertEquals("Test Pickup", filledRequest.getPickupLocation());
        assertEquals("Test Dropoff", filledRequest.getDropoffLocation());

        // Test the setters
        emptyRequest.setPickupLocation("New Pickup");
        emptyRequest.setDropoffLocation("New Dropoff");
        assertEquals("New Pickup", emptyRequest.getPickupLocation());
        assertEquals("New Dropoff", emptyRequest.getDropoffLocation());

        // Test toString (useful for debugging and logging)
        String stringRepresentation = filledRequest.toString();
        assertTrue(stringRepresentation.contains("Test Pickup"));
        assertTrue(stringRepresentation.contains("Test Dropoff"));
    }

    /**
     * This tests various edge cases that might occur in real usage
     * Edge cases are the unusual scenarios that often cause bugs
     */
    @Test
    public void testCalculateWebBookingFare_EdgeCases() {
        // Test with locations that have the same name (should work fine)
        Location duplicateNameLocation = new Location("Union Station", 38.8973, -77.0065);
        when(locationService.findLocationByName("Union Station")).thenReturn(duplicateNameLocation);

        Route sameLocationRoute = new Route(duplicateNameLocation, duplicateNameLocation, 0.0);
        when(routeService.createRoute(duplicateNameLocation, duplicateNameLocation)).thenReturn(sameLocationRoute);
        when(calculateFareService.calculateFare(sameLocationRoute)).thenReturn(3.0); // Just the base fee

        BookingController.WebBookingRequest sameLocationRequest = new BookingController.WebBookingRequest(
                "Union Station", "Union Station"
        );

        ResponseEntity<Map<String, Object>> response = bookingController.calculateWebBookingFare(sameLocationRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> responseBody = response.getBody();
        assertTrue((Boolean) responseBody.get("success"));
        assertEquals(0.0, responseBody.get("distance")); // No distance for same location
        assertEquals(3.0, responseBody.get("fareAmount")); // Just the base booking fee
    }
}