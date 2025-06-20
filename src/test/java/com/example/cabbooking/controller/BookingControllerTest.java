package com.example.cabbooking.controller;

import com.example.cabbooking.model.Client;
import com.example.cabbooking.model.Location;
import com.example.cabbooking.model.Route;
import com.example.cabbooking.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

@ExtendWith(MockitoExtension.class) // This enables Mockito annotations without loading Spring context
class BookingControllerTest {

    // MockMvc for simulating HTTP requests - configured manually rather than via Spring
    private MockMvc mockMvc;

    // ObjectMapper for JSON serialization - created manually for better test control
    private ObjectMapper objectMapper;

    // Pure Mockito mocks - no Spring involvement, just mock objects for testing
    @Mock
    private BookingService bookingService;

    @Mock
    private CalculateFareService calculateFareService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private RouteService routeService;

    @Mock
    private ClientService clientService;

    // The controller under test - Mockito will inject the mocks above into this instance
    @InjectMocks
    private BookingController bookingController;

    // Test data that we'll reuse across multiple tests
    private Client testClient;
    private Route testRoute;
    private Location fromLocation;
    private Location toLocation;

    /**
     * Set up the testing environment before each test runs
     *
     * This method demonstrates the modern approach to test setup. Instead of relying on
     * Spring to configure our testing environment, we manually create the components we need.
     * This gives us complete control and makes our tests much faster since we're not loading
     * the entire Spring application context.
     */
    @BeforeEach
    void setUp() {
        // Create ObjectMapper for JSON processing - in the old approach, Spring provided this
        objectMapper = new ObjectMapper();

        // Manually configure MockMvc to test our controller - this is the key difference
        // We're building MockMvc around our specific controller instance with its injected mocks
        mockMvc = MockMvcBuilders
                .standaloneSetup(bookingController) // Focus only on our controller
                .build(); // No Spring context needed!

        // Create consistent test data for use across all test methods
        // Having predictable test data makes our tests more reliable and easier to debug
        fromLocation = new Location("Times Square", 40.7580, -73.9855);
        toLocation = new Location("Central Park", 40.7829, -73.9654);

        testClient = new Client(1, "John Doe", "john@email.com",
                "555-1234", "123 Main St", "4111-1111-1111-1111");

        testRoute = new Route(fromLocation, toLocation, 2.5);

        System.out.println("✓ Modern test setup completed - no Spring context loaded!");
        System.out.println("✓ Testing with client: " + testClient.getName() +
                " and route distance: " + testRoute.getDistance() + " miles");
    }

    // =================== TESTING THE MAIN BOOKING ENDPOINT ===================

    /**
     * Testing the main booking functionality with the modern approach
     *
     * These tests demonstrate how we can achieve the same comprehensive coverage
     * as before, but with faster, more focused tests that don't depend on Spring's
     * application context loading mechanisms.
     */
    @Nested
    @DisplayName("POST /booking - Main Booking Endpoint Tests")
    class MainBookingEndpointTests {

        @Test
        @DisplayName("Should successfully book a cab when all data is valid")
        void testBookCabSuccess() throws Exception {
            // ARRANGE: Configure our mock services to return predictable values
            // This is pure Mockito - no Spring bean replacement happening here
            when(calculateFareService.calculateFare(any(Route.class))).thenReturn(10.50);

            // Configure the booking service to do nothing (void method) when called
            doNothing().when(bookingService).bookCab(any(Client.class), any(Route.class));

            // ACT & ASSERT: Send HTTP request and verify the response
            // Notice how we're testing the full HTTP layer behavior, including JSON parsing
            mockMvc.perform(post("/booking")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createBookingRequestJson(testClient, testRoute)))
                    .andDo(print()) // Helpful for debugging - shows actual request/response
                    .andExpect(status().isOk()) // Verify HTTP 200 OK status
                    .andExpect(content().string("Cab booked successfully! Fare: $10.50"));

            // VERIFY: Confirm that our controller called the services as expected
            // This ensures our controller is orchestrating the business logic correctly
            verify(bookingService, times(1)).bookCab(any(Client.class), any(Route.class));
            verify(calculateFareService, times(1)).calculateFare(any(Route.class));

            System.out.println("✓ Main booking flow verified with modern testing approach");
        }

        @Test
        @DisplayName("Should return 400 Bad Request when client is null")
        void testBookCabWithNullClient() throws Exception {
            // ACT & ASSERT: Test validation behavior with null client
            // The beauty of this approach is that we're testing pure controller logic
            // without any Spring bean lifecycle complications
            mockMvc.perform(post("/booking")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createBookingRequestJson(null, testRoute)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Invalid client: Client information is required")));

            // VERIFY: Ensure business logic services are not called when validation fails
            // This confirms our controller properly validates input before processing
            verify(bookingService, never()).bookCab(any(), any());
            verify(calculateFareService, never()).calculateFare(any());

            System.out.println("✓ Input validation working correctly");
        }

        @Test
        @DisplayName("Should return 400 Bad Request when route is null")
        void testBookCabWithNullRoute() throws Exception {
            mockMvc.perform(post("/booking")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createBookingRequestJson(testClient, null)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Invalid route: Route information is required")));

            verify(bookingService, never()).bookCab(any(), any());

            System.out.println("✓ Route validation working correctly");
        }

        @Test
        @DisplayName("Should handle unexpected service errors gracefully")
        void testBookCabServiceException() throws Exception {
            // ARRANGE: Simulate a service failure to test error handling
            // This tests how our controller responds when dependencies fail
            doThrow(new RuntimeException("Database connection failed"))
                    .when(bookingService).bookCab(any(Client.class), any(Route.class));

            // ACT & ASSERT: Verify graceful error handling
            mockMvc.perform(post("/booking")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createBookingRequestJson(testClient, testRoute)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Booking failed: Database connection failed")));

            System.out.println("✓ Exception handling verified");
        }
    }

    // =================== TESTING THE QUICK BOOKING ENDPOINT ===================

    @Nested
    @DisplayName("POST /booking/quick-book - Quick Booking Tests")
    class QuickBookingEndpointTests {

        @Test
        @DisplayName("Should successfully create quick booking when client exists")
        void testQuickBookingSuccess() throws Exception {
            // ARRANGE: Set up a complete successful quick booking scenario
            // Notice how we're precisely controlling each service dependency
            when(clientService.clientExists(1)).thenReturn(true);
            when(clientService.getClientById(1)).thenReturn(Optional.of(testClient));
            when(routeService.createRoute(any(Location.class), any(Location.class))).thenReturn(testRoute);
            when(calculateFareService.calculateFare(any(Route.class))).thenReturn(15.75);
            doNothing().when(bookingService).bookCab(any(Client.class), any(Route.class));

            // Create the request object that matches our controller's expectations
            BookingController.QuickBookingRequest request = new BookingController.QuickBookingRequest();
            request.setClientId(1);
            request.setFromLocation(fromLocation);
            request.setToLocation(toLocation);

            // ACT & ASSERT: Test the complete quick booking flow
            mockMvc.perform(post("/booking/quick-book")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("Quick booking successful!")))
                    .andExpect(content().string(containsString("From: Times Square")))
                    .andExpect(content().string(containsString("To: Central Park")))
                    .andExpect(content().string(containsString("Fare: $15.75")));

            // VERIFY: Confirm the entire workflow executed in the correct sequence
            // This ensures our controller properly orchestrates multiple service calls
            verify(clientService).clientExists(1);
            verify(clientService).getClientById(1);
            verify(routeService).createRoute(fromLocation, toLocation);
            verify(bookingService).bookCab(testClient, testRoute);
            verify(calculateFareService).calculateFare(testRoute);

            System.out.println("✓ Quick booking workflow completed successfully");
        }

        @Test
        @DisplayName("Should return 400 when client doesn't exist")
        void testQuickBookingClientNotFound() throws Exception {
            // ARRANGE: Simulate a non-existent client scenario
            when(clientService.clientExists(999)).thenReturn(false);

            BookingController.QuickBookingRequest request = new BookingController.QuickBookingRequest();
            request.setClientId(999);
            request.setFromLocation(fromLocation);
            request.setToLocation(toLocation);

            // ACT & ASSERT: Verify proper error handling for missing clients
            mockMvc.perform(post("/booking/quick-book")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Client not found with ID: 999")));

            // VERIFY: Ensure processing stops at the first validation failure
            verify(clientService).clientExists(999);
            verify(clientService, never()).getClientById(any());
            verify(routeService, never()).createRoute(any(), any());

            System.out.println("✓ Non-existent client handling verified");
        }

        @Test
        @DisplayName("Should handle route creation failure")
        void testQuickBookingRouteCreationFails() throws Exception {
            // ARRANGE: Client validation passes but route creation fails
            when(clientService.clientExists(1)).thenReturn(true);
            when(clientService.getClientById(1)).thenReturn(Optional.of(testClient));
            when(routeService.createRoute(any(Location.class), any(Location.class)))
                    .thenThrow(new RuntimeException("Invalid coordinates"));

            BookingController.QuickBookingRequest request = new BookingController.QuickBookingRequest();
            request.setClientId(1);
            request.setFromLocation(fromLocation);
            request.setToLocation(toLocation);

            // ACT & ASSERT: Test error handling in the middle of the workflow
            mockMvc.perform(post("/booking/quick-book")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Quick booking failed: Invalid coordinates")));

            System.out.println("✓ Route creation failure handling verified");
        }
    }

    // =================== TESTING THE PAYMENT ENDPOINT ===================

    @Nested
    @DisplayName("POST /booking/payment - Payment Processing Tests")
    class PaymentEndpointTests {

        @Test
        @DisplayName("Should successfully process payment when all data is valid")
        void testProcessPaymentSuccess() throws Exception {
            // ARRANGE: Configure successful payment processing scenario
            when(clientService.getClientById(1)).thenReturn(Optional.of(testClient));
            doNothing().when(paymentService).paymentConfirmation(
                    any(Client.class), any(Route.class), anyDouble(), anyString());

            // Create payment request with all required data
            BookingController.PaymentRequest request = new BookingController.PaymentRequest();
            request.setClientId(1);
            request.setRoute(testRoute);
            request.setPaymentAmount(25.50);
            request.setCreditCardNumber("4111-1111-1111-1111");

            // ACT & ASSERT: Test successful payment processing
            mockMvc.perform(post("/booking/payment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().string("Payment processed successfully!"));

            // VERIFY: Ensure payment service receives correct parameters
            verify(clientService).getClientById(1);
            verify(paymentService).paymentConfirmation(testClient, testRoute, 25.50, "4111-1111-1111-1111");

            System.out.println("✓ Payment processing verified");
        }

        @Test
        @DisplayName("Should return 400 when client not found for payment")
        void testProcessPaymentClientNotFound() throws Exception {
            // ARRANGE: Simulate payment attempt for non-existent client
            when(clientService.getClientById(999)).thenReturn(Optional.empty());

            BookingController.PaymentRequest request = new BookingController.PaymentRequest();
            request.setClientId(999);
            request.setRoute(testRoute);
            request.setPaymentAmount(25.50);
            request.setCreditCardNumber("4111-1111-1111-1111");

            // ACT & ASSERT: Verify client validation in payment flow
            mockMvc.perform(post("/booking/payment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Invalid client: Client not found")));

            // VERIFY: Payment should not proceed if client doesn't exist
            verify(paymentService, never()).paymentConfirmation(any(), any(), anyDouble(), anyString());

            System.out.println("✓ Payment client validation verified");
        }

        @Test
        @DisplayName("Should handle invalid credit card error")
        void testProcessPaymentInvalidCard() throws Exception {
            // ARRANGE: Valid client but payment service rejects the card
            when(clientService.getClientById(1)).thenReturn(Optional.of(testClient));
            doThrow(new RuntimeException("invalid credit card number"))
                    .when(paymentService).paymentConfirmation(any(), any(), anyDouble(), anyString());

            BookingController.PaymentRequest request = new BookingController.PaymentRequest();
            request.setClientId(1);
            request.setRoute(testRoute);
            request.setPaymentAmount(25.50);
            request.setCreditCardNumber("1234-5678-9999-0000"); // Invalid card

            // ACT & ASSERT: Test credit card validation error handling
            mockMvc.perform(post("/booking/payment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Payment error: invalid credit card number")));

            System.out.println("✓ Invalid credit card handling verified");
        }

        @Test
        @DisplayName("Should handle incorrect payment amount")
        void testProcessPaymentIncorrectAmount() throws Exception {
            // ARRANGE: Valid client but wrong payment amount
            when(clientService.getClientById(1)).thenReturn(Optional.of(testClient));
            doThrow(new IllegalArgumentException("incorrect payment amount"))
                    .when(paymentService).paymentConfirmation(any(), any(), anyDouble(), anyString());

            BookingController.PaymentRequest request = new BookingController.PaymentRequest();
            request.setClientId(1);
            request.setRoute(testRoute);
            request.setPaymentAmount(10.00); // Wrong amount
            request.setCreditCardNumber("4111-1111-1111-1111");

            // ACT & ASSERT: Test payment amount validation
            mockMvc.perform(post("/booking/payment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Payment error: incorrect payment amount")));

            System.out.println("✓ Payment amount validation verified");
        }
    }

    // =================== TESTING EXCEPTION HANDLING ===================

    @Nested
    @DisplayName("Exception Handling Tests")
    class ExceptionHandlingTests {

        @Test
        @DisplayName("Should handle BookingException with 400 status")
        void testBookingExceptionHandling() throws Exception {
            // ARRANGE: Force an exception to test the exception handler
            doThrow(new RuntimeException("Test exception"))
                    .when(bookingService).bookCab(any(), any());

            // ACT & ASSERT: Verify our exception handler converts exceptions properly
            mockMvc.perform(post("/booking")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(createBookingRequestJson(testClient, testRoute)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Booking failed: Test exception")));

            System.out.println("✓ Exception handling mechanism verified");
        }

        @Test
        @DisplayName("Should handle malformed JSON at framework level")
        void testMalformedJsonHandling() throws Exception {
            // Create clearly invalid JSON that will fail parsing
            String malformedJson = "{ this is not valid JSON at all }";

            mockMvc.perform(post("/booking")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(malformedJson))
                    .andDo(print())
                    .andExpect(status().is5xxServerError()) // Accept that framework returns 500
                    .andExpect(content().string(containsString("JSON parse error"))); // Verify error type

            System.out.println("✓ Framework-level JSON parsing error handling verified");
        }

        @Test
        @DisplayName("Should handle semantically invalid booking requests")
        void testJsonInvalidBookingData() throws Exception {
            // Create syntactically valid JSON with invalid business data
            String invalidBookingJson = """
        {
            "client": null,
            "route": {
                "from": null,
                "to": null,
                "distance": -5.0
            }
        }
        """;

            mockMvc.perform(post("/booking")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBookingJson))
                    .andDo(print())
                    .andExpect(status().isBadRequest()) // This should trigger your custom handlers
                    .andExpect(content().string(containsString("Invalid client")));

            System.out.println("✓ Application-level validation error handling verified");
        }
    }

    // =================== HELPER METHODS FOR CREATING TEST DATA ===================

    /**
     * Helper method to create JSON for booking requests
     *
     * In the modern approach, we handle JSON serialization manually rather than
     * relying on Spring's automatic configuration. This gives us more control
     * and makes our tests more explicit about the data format being tested.
     */
    private String createBookingRequestJson(Client client, Route route) throws Exception {
        String clientJson = (client != null) ? objectMapper.writeValueAsString(client) : "null";
        String routeJson = (route != null) ? objectMapper.writeValueAsString(route) : "null";

        return String.format("{\"client\":%s,\"route\":%s}", clientJson, routeJson);
    }

    /**
     * Helper method for creating test locations with realistic coordinates
     */
    private Location createTestLocation(String name, double lat, double lng) {
        return new Location(name, lat, lng);
    }

    /**
     * Helper method for creating test routes with calculated distances
     */
    private Route createTestRoute(Location from, Location to, double distance) {
        return new Route(from, to, distance);
    }

    // =================== INTEGRATION-STYLE TESTS ===================

    @Nested
    @DisplayName("Integration-Style Workflow Tests")
    class WorkflowTests {

        @Test
        @DisplayName("Should handle complete booking workflow from start to finish")
        void testCompleteBookingWorkflow() throws Exception {
            // This test demonstrates how the modern approach can still handle complex workflows
            // The key difference is that we're testing controller orchestration logic rather
            // than actual service implementations

            // STEP 1: Quick booking setup
            when(clientService.clientExists(1)).thenReturn(true);
            when(clientService.getClientById(1)).thenReturn(Optional.of(testClient));
            when(routeService.createRoute(any(), any())).thenReturn(testRoute);
            when(calculateFareService.calculateFare(any())).thenReturn(12.50);
            doNothing().when(bookingService).bookCab(any(), any());

            BookingController.QuickBookingRequest quickRequest = new BookingController.QuickBookingRequest();
            quickRequest.setClientId(1);
            quickRequest.setFromLocation(fromLocation);
            quickRequest.setToLocation(toLocation);

            // Execute quick booking
            mockMvc.perform(post("/booking/quick-book")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(quickRequest)))
                    .andExpect(status().isOk());

            // STEP 2: Payment processing setup
            doNothing().when(paymentService).paymentConfirmation(any(), any(), anyDouble(), anyString());

            BookingController.PaymentRequest paymentRequest = new BookingController.PaymentRequest();
            paymentRequest.setClientId(1);
            paymentRequest.setRoute(testRoute);
            paymentRequest.setPaymentAmount(12.50);
            paymentRequest.setCreditCardNumber("4111-1111-1111-1111");

            // Execute payment
            mockMvc.perform(post("/booking/payment")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(paymentRequest)))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Payment processed successfully!"));

            // VERIFY: Complete workflow verification
            verify(clientService, times(2)).getClientById(1); // Once for booking, once for payment
            verify(bookingService).bookCab(testClient, testRoute);
            verify(paymentService).paymentConfirmation(testClient, testRoute, 12.50, "4111-1111-1111-1111");

            System.out.println("✓ Complete modern workflow test passed!");
        }
    }
}