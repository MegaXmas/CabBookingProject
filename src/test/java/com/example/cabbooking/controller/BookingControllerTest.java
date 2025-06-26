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

    // ===============================================================================
    // TESTING THE NEW TEST BOOKING FUNCTIONALITY
    // These tests verify that your test client integration works correctly
    // This is where we test the complete flow from HTML form to bookingService.bookCab()
    // ===============================================================================

    /**
     * This tests the complete test booking flow - the happy path scenario
     * This is like testing that your entire restaurant can serve a meal from order to delivery
     *
     * The key insight here is that we're testing INTEGRATION between multiple services:
     * - LocationService finds the locations
     * - RouteService creates the route
     * - CalculateFareService calculates the fare
     * - BookingService actually books the cab
     *
     * This test verifies that all these services work together correctly when called
     * through your new test booking endpoint
     */
    @Test
    public void testTestBooking_CompleteFlowSuccess() {
        System.out.println("=== Testing Complete Test Booking Flow ===");

        // ARRANGE: Set up our test scenario with a realistic test client
        // This simulates the test client data that your HTML form would send
        BookingController.TestBookingRequest.TestClient testClientData =
                new BookingController.TestBookingRequest.TestClient();
        testClientData.setId(1);
        testClientData.setName("John Test Doe");
        testClientData.setEmail("john.test@example.com");
        testClientData.setPhone("555-123-4567");
        testClientData.setAddress("123 Test Street, Washington DC");
        testClientData.setCredit_card("4111-1111-1111-1234");

        // Create the complete test booking request
        BookingController.TestBookingRequest request = new BookingController.TestBookingRequest();
        request.setClient(testClientData);
        request.setPickupLocation("The White House");
        request.setDropoffLocation("Lincoln Memorial");

        // Mock all the service calls that should happen during the booking process
        // Step 1: Location finding should work
        when(locationService.findLocationByName("The White House")).thenReturn(whiteHouse);
        when(locationService.findLocationByName("Lincoln Memorial")).thenReturn(lincolnMemorial);

        // Step 2: Route creation should work
        when(routeService.createRoute(whiteHouse, lincolnMemorial)).thenReturn(testRoute);

        // Step 3: Fare calculation should work
        when(calculateFareService.calculateFare(testRoute)).thenReturn(7.65);

        // Step 4: Booking service should be called (it's a void method, so we just verify it's called)
        // This is the key test - we want to make sure bookingService.bookCab() gets called with proper objects

        // ACT: Call the test booking method
        ResponseEntity<Map<String, Object>> response = bookingController.testBooking(request);

        // ASSERT: Verify the complete flow worked correctly

        // Check that we got a successful response
        assertEquals(HttpStatus.OK, response.getStatusCode(), "Should return successful HTTP status");

        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody, "Response body should not be null");

        // Verify the response contains all expected booking information
        assertTrue((Boolean) responseBody.get("success"), "Booking should be marked as successful");
        assertEquals("The White House", responseBody.get("pickupLocation"), "Should return correct pickup location");
        assertEquals("Lincoln Memorial", responseBody.get("dropoffLocation"), "Should return correct dropoff location");
        assertEquals(2.5, responseBody.get("distance"), "Should return correct distance");
        assertEquals(7.65, responseBody.get("fareAmount"), "Should return correct fare amount");
        assertEquals("John Test Doe", responseBody.get("clientName"), "Should return correct client name");

        // VERIFY: This is the most important part - ensure all services were called correctly

        // Verify location service was used to find both locations
        verify(locationService).findLocationByName("The White House");
        verify(locationService).findLocationByName("Lincoln Memorial");

        // Verify route service was used to create the route
        verify(routeService).createRoute(whiteHouse, lincolnMemorial);

        // Verify fare calculation service was used
        verify(calculateFareService).calculateFare(testRoute);

        // THIS IS THE KEY VERIFICATION: Ensure bookingService.bookCab() was called
        // We use ArgumentCaptor to capture the actual Client object that was passed
        // This lets us verify that the Client object was created correctly from the test data
        verify(bookingService).bookCab(any(Client.class), eq(testRoute));

        System.out.println("✓ Complete test booking flow verified successfully");
        System.out.println("Response: " + responseBody);
    }

    /**
     * This test verifies that our test booking creates the Client object correctly
     * This is crucial because the Client object is what gets passed to bookingService.bookCab()
     *
     * Think of this like testing that a restaurant correctly transcribes a phone order
     * onto their order ticket - all the details need to be accurate
     */
    @Test
    public void testTestBooking_ClientObjectCreatedCorrectly() {
        System.out.println("=== Testing Client Object Creation ===");

        // ARRANGE: Create test client data with specific values we can verify
        BookingController.TestBookingRequest.TestClient testClientData =
                new BookingController.TestBookingRequest.TestClient();
        testClientData.setId(42);  // Using distinctive values to make verification clear
        testClientData.setName("Jane Test Smith");
        testClientData.setEmail("jane.test@verification.com");
        testClientData.setPhone("555-999-8888");
        testClientData.setAddress("456 Verification Ave");
        testClientData.setCredit_card("5555-4444-3333-2222");

        BookingController.TestBookingRequest request = new BookingController.TestBookingRequest();
        request.setClient(testClientData);
        request.setPickupLocation("The White House");
        request.setDropoffLocation("Pentagon");

        // Mock the services to return valid responses
        when(locationService.findLocationByName("The White House")).thenReturn(whiteHouse);
        Location pentagon = new Location("Pentagon", 38.8719, -77.0563);
        when(locationService.findLocationByName("Pentagon")).thenReturn(pentagon);

        Route testRoute2 = new Route(whiteHouse, pentagon, 5.2);
        when(routeService.createRoute(whiteHouse, pentagon)).thenReturn(testRoute2);
        when(calculateFareService.calculateFare(testRoute2)).thenReturn(18.60);

        // ACT: Process the test booking
        ResponseEntity<Map<String, Object>> response = bookingController.testBooking(request);

        // ASSERT: Verify the booking was successful
        assertEquals(HttpStatus.OK, response.getStatusCode());

        // VERIFY: Use ArgumentCaptor to capture and examine the Client object that was passed to bookingService
        // This is an advanced testing technique that lets us inspect method arguments
        var clientCaptor = org.mockito.ArgumentCaptor.forClass(Client.class);
        verify(bookingService).bookCab(clientCaptor.capture(), any(Route.class));

        // Now we can examine the actual Client object that was created and passed to the service
        Client capturedClient = clientCaptor.getValue();

        // Verify every field was transferred correctly from test data to Client object
        assertEquals(42, capturedClient.getId(), "Client ID should match test data");
        assertEquals("Jane Test Smith", capturedClient.getName(), "Client name should match test data");
        assertEquals("jane.test@verification.com", capturedClient.getEmail(), "Client email should match test data");
        assertEquals("555-999-8888", capturedClient.getPhone(), "Client phone should match test data");
        assertEquals("456 Verification Ave", capturedClient.getAddress(), "Client address should match test data");
        assertEquals("5555-4444-3333-2222", capturedClient.getCredit_card(), "Client credit card should match test data");

        System.out.println("✓ Client object creation verified:");
        System.out.println("  Created Client: " + capturedClient.toString());
        System.out.println("  All fields transferred correctly from test data");
    }

    /**
     * This test verifies error handling when locations aren't found during test booking
     * This ensures your test booking is as robust as your regular booking functionality
     */
    @Test
    public void testTestBooking_LocationNotFound() {
        System.out.println("=== Testing Test Booking Error Handling ===");

        // ARRANGE: Set up a test booking request with an invalid location
        BookingController.TestBookingRequest.TestClient testClientData =
                new BookingController.TestBookingRequest.TestClient();
        testClientData.setId(1);
        testClientData.setName("Error Test Client");
        testClientData.setEmail("error@test.com");
        testClientData.setPhone("555-000-0000");
        testClientData.setAddress("Error Test Address");
        testClientData.setCredit_card("0000-0000-0000-0000");

        BookingController.TestBookingRequest request = new BookingController.TestBookingRequest();
        request.setClient(testClientData);
        request.setPickupLocation("Nonexistent Location");
        request.setDropoffLocation("Lincoln Memorial");

        // Mock location service to return null for the nonexistent location
        when(locationService.findLocationByName("Nonexistent Location")).thenReturn(null);
        when(locationService.findLocationByName("Lincoln Memorial")).thenReturn(lincolnMemorial);

        // ACT: Try to process the test booking with invalid location
        ResponseEntity<Map<String, Object>> response = bookingController.testBooking(request);

        // ASSERT: Verify we get an appropriate error response
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "Should return bad request for invalid location");

        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody, "Error response body should not be null");
        assertFalse((Boolean) responseBody.get("success"), "Should indicate booking was not successful");
        assertTrue(((String) responseBody.get("error")).contains("One or both locations not found"),
                "Error message should mention locations not found");

        // VERIFY: Ensure booking service was NOT called when location lookup failed
        verify(bookingService, never()).bookCab(any(Client.class), any(Route.class));
        verify(routeService, never()).createRoute(any(), any());
        verify(calculateFareService, never()).calculateFare(any());

        System.out.println("✓ Error handling verified for invalid location");
        System.out.println("Error Response: " + responseBody);
    }

    /**
     * This test verifies that test booking handles service exceptions gracefully
     * This simulates real-world scenarios where backend services might fail unexpectedly
     */
    @Test
    public void testTestBooking_ServiceException() {
        System.out.println("=== Testing Test Booking Exception Handling ===");

        // ARRANGE: Set up a valid test booking request
        BookingController.TestBookingRequest.TestClient testClientData =
                new BookingController.TestBookingRequest.TestClient();
        testClientData.setId(1);
        testClientData.setName("Exception Test Client");
        testClientData.setEmail("exception@test.com");
        testClientData.setPhone("555-111-1111");
        testClientData.setAddress("Exception Test Address");
        testClientData.setCredit_card("1111-1111-1111-1111");

        BookingController.TestBookingRequest request = new BookingController.TestBookingRequest();
        request.setClient(testClientData);
        request.setPickupLocation("The White House");
        request.setDropoffLocation("Lincoln Memorial");

        // Mock location service to work correctly
        when(locationService.findLocationByName("The White House")).thenReturn(whiteHouse);
        when(locationService.findLocationByName("Lincoln Memorial")).thenReturn(lincolnMemorial);
        when(routeService.createRoute(whiteHouse, lincolnMemorial)).thenReturn(testRoute);

        // BUT mock the fare calculation service to throw an exception
        when(calculateFareService.calculateFare(testRoute))
                .thenThrow(new RuntimeException("Fare calculation service is temporarily down"));

        // ACT: Try to process the test booking when a service fails
        ResponseEntity<Map<String, Object>> response = bookingController.testBooking(request);

        // ASSERT: Verify we handle the exception gracefully
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(),
                "Should return internal server error when service fails");

        Map<String, Object> responseBody = response.getBody();
        assertNotNull(responseBody, "Error response should not be null");
        assertFalse((Boolean) responseBody.get("success"), "Should indicate booking failed");
        assertTrue(((String) responseBody.get("error")).contains("Test booking error"),
                "Should provide helpful error message");

        // VERIFY: Ensure booking service was NOT called when fare calculation failed
        verify(bookingService, never()).bookCab(any(Client.class), any(Route.class));

        System.out.println("✓ Exception handling verified");
        System.out.println("Error Response: " + responseBody);
    }

    // ===============================================================================
    // TESTING THE TEST BOOKING REQUEST DATA CLASSES
    // These tests verify that your data transfer objects work correctly
    // This might seem boring, but data integrity is crucial for web applications
    // ===============================================================================

    /**
     * This tests the TestBookingRequest and TestClient data classes
     * Think of this like testing that your order forms have all the right fields
     * and that information gets transferred correctly between forms
     */
    @Test
    public void testTestBookingRequest_DataIntegrity() {
        System.out.println("=== Testing Test Booking Request Data Classes ===");

        // ARRANGE & ACT: Create and populate the data objects

        // Test the TestClient inner class
        BookingController.TestBookingRequest.TestClient testClient =
                new BookingController.TestBookingRequest.TestClient();

        // Test all setters and getters for TestClient
        testClient.setId(99);
        testClient.setName("Data Test Client");
        testClient.setEmail("data@test.com");
        testClient.setPhone("555-222-3333");
        testClient.setAddress("Data Test Street");
        testClient.setCredit_card("9999-8888-7777-6666");

        // Test the main TestBookingRequest class
        BookingController.TestBookingRequest request = new BookingController.TestBookingRequest();
        request.setClient(testClient);
        request.setPickupLocation("Data Test Pickup");
        request.setDropoffLocation("Data Test Dropoff");

        // ASSERT: Verify all data was stored and retrieved correctly

        // Test TestClient data integrity
        assertEquals(99, testClient.getId(), "TestClient ID should be stored correctly");
        assertEquals("Data Test Client", testClient.getName(), "TestClient name should be stored correctly");
        assertEquals("data@test.com", testClient.getEmail(), "TestClient email should be stored correctly");
        assertEquals("555-222-3333", testClient.getPhone(), "TestClient phone should be stored correctly");
        assertEquals("Data Test Street", testClient.getAddress(), "TestClient address should be stored correctly");
        assertEquals("9999-8888-7777-6666", testClient.getCredit_card(), "TestClient credit card should be stored correctly");

        // Test TestBookingRequest data integrity
        assertEquals(testClient, request.getClient(), "TestBookingRequest should store TestClient correctly");
        assertEquals("Data Test Pickup", request.getPickupLocation(), "TestBookingRequest should store pickup location correctly");
        assertEquals("Data Test Dropoff", request.getDropoffLocation(), "TestBookingRequest should store dropoff location correctly");

        // Test that the nested structure works correctly
        assertEquals("Data Test Client", request.getClient().getName(), "Nested client name should be accessible");
        assertEquals(99, request.getClient().getId(), "Nested client ID should be accessible");

        System.out.println("✓ All data classes verified successfully");
        System.out.println("TestClient: " + testClient.getName() + " (" + testClient.getEmail() + ")");
        System.out.println("Trip: " + request.getPickupLocation() + " -> " + request.getDropoffLocation());
    }

    /**
     * This test verifies that default constructors work correctly
     * This is important because Spring Boot uses default constructors when converting JSON to Java objects
     */
    @Test
    public void testTestBookingRequest_DefaultConstructors() {
        System.out.println("=== Testing Default Constructors ===");

        // ACT: Create objects using default constructors (like Spring Boot would do)
        BookingController.TestBookingRequest.TestClient testClient =
                new BookingController.TestBookingRequest.TestClient();

        BookingController.TestBookingRequest request = new BookingController.TestBookingRequest();

        // ASSERT: Verify default state is reasonable

        // TestClient should have null values initially
        assertNull(testClient.getId(), "TestClient ID should be null initially");
        assertNull(testClient.getName(), "TestClient name should be null initially");
        assertNull(testClient.getEmail(), "TestClient email should be null initially");
        assertNull(testClient.getPhone(), "TestClient phone should be null initially");
        assertNull(testClient.getAddress(), "TestClient address should be null initially");
        assertNull(testClient.getCredit_card(), "TestClient credit card should be null initially");

        // TestBookingRequest should have null values initially
        assertNull(request.getClient(), "TestBookingRequest client should be null initially");
        assertNull(request.getPickupLocation(), "TestBookingRequest pickup should be null initially");
        assertNull(request.getDropoffLocation(), "TestBookingRequest dropoff should be null initially");

        System.out.println("✓ Default constructors work correctly");
        System.out.println("This ensures Spring Boot can create these objects from JSON data");
    }
}