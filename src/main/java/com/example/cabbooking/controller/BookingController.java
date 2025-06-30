package com.example.cabbooking.controller;

import com.example.cabbooking.model.Client;
import com.example.cabbooking.model.Location;
import com.example.cabbooking.model.Route;
import com.example.cabbooking.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/booking")
public class BookingController {

    //=============SERVICES==============
    private final BookingService bookingService;
    private final CalculateFareService calculateFareService;
    private final LocationService locationService;
    private final RouteService routeService;

    @Autowired
    public BookingController(BookingService bookingService,
                             CalculateFareService calculateFareService,
                             LocationService locationService,
                             RouteService routeService) {
        this.bookingService = bookingService;
        this.calculateFareService = calculateFareService;
        this.locationService = locationService;
        this.routeService = routeService;
    }

    /**
     * Sets up all the Washington DC locations on start-up
     */
    @PostConstruct
    public void initializeLocations() {
        System.out.println("Setting up Washington DC locations for the booking system...");
        locationService.initializeWashingtonDCLocations();
        System.out.println("Location setup complete! Ready for bookings.");
    }

    /**
     * Booking endpoint
     */
    @PostMapping
    public void bookCab(@RequestBody Client client, Route route) {
        bookingService.bookCab(client, route);
        calculateFareService.calculateFare(route);
    }

    /**
     * Web-based fare calculation endpoint for HTML form
     * @param request the JSON delivered from the HTML request
     * @return HashMap of the booking request send from the HTML
     */
    @PostMapping("/calculate-fare")
    public ResponseEntity<Map<String, Object>> calculateWebBookingFare(@RequestBody WebBookingRequest request) {
        try {
            System.out.println("Web booking request received:");
            System.out.println("  From: " + request.getPickupLocation());
            System.out.println("  To: " + request.getDropoffLocation());

            // Step 1: Find the actual Location objects that match what the user selected
            Location pickupLocationObj = locationService.findLocationByName(request.getPickupLocation());
            Location dropoffLocationObj = locationService.findLocationByName(request.getDropoffLocation());

            // Validate that we found both locations
            if (pickupLocationObj == null) {
                System.out.println("ERROR: Could not find pickup location: " + request.getPickupLocation());
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Pickup location not found: " + request.getPickupLocation()));
            }

            if (dropoffLocationObj == null) {
                System.out.println("ERROR: Could not find dropoff location: " + request.getDropoffLocation());
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("Drop-off location not found: " + request.getDropoffLocation()));
            }

            System.out.println("✓ Both locations found successfully");

            // Step 2: Create a Route object using RouteService
            Route route = routeService.createRoute(pickupLocationObj, dropoffLocationObj);
            System.out.println("✓ Route created - Distance: " + String.format("%.2f", route.getDistance()) + " km");

            // Step 3: Calculate the fare using CalculateFareService
            double fareAmount = calculateFareService.calculateFare(route);
            System.out.println("✓ Fare calculated: $" + String.format("%.2f", fareAmount));

            // Step 4: Package everything into a response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("pickupLocation", request.getPickupLocation());
            response.put("dropoffLocation", request.getDropoffLocation());
            response.put("distance", route.getDistance());
            response.put("fareAmount", fareAmount);
            response.put("message", "Fare calculated successfully using your booking services!");

            System.out.println("✓ Web booking calculation complete");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("ERROR in web booking calculation: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("Error calculating fare: " + e.getMessage()));
        }
    }

    /**
     * API endpoint to get all available locations
     * @return List of location objects
     */
    @GetMapping("/locations")
    public ResponseEntity<List<Location>> getAllLocations() {
        try {
            List<Location> locations = locationService.getAllLocations();
            System.out.println("Sending " + locations.size() + " locations to web client");
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            System.out.println("Error getting locations for web client: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Helper method to create consistent error responses for the web client
     * @param errorMessage Error message to be created
     * @return Generated error message
     */
    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", errorMessage);
        return errorResponse;
    }

    /**
     * Test booking endpoint that accepts a pre-made client and route information
     * This allows the frontend to send a complete booking without requiring user registration
     * @param request the JSON delivered from the HTML request
     * @return HashMap of the booking request send from the HTML
     */
    @PostMapping("/test-booking")
    public ResponseEntity<Map<String, Object>> testBooking(@RequestBody TestBookingRequest request) {
        try {
            System.out.println("Test booking request received for: " + request.getClient().getName());

            // STEP 1: Create Client object from the JSON test data sent by frontend
            Client client = new Client(
                    request.getClient().getId(),
                    request.getClient().getName(),
                    request.getClient().getEmail(),
                    request.getClient().getPhone(),
                    request.getClient().getAddress(),
                    request.getClient().getCredit_card()
            );

            // STEP 2: Find locations and create route
            Location pickupLocationObj = locationService.findLocationByName(request.getPickupLocation());
            Location dropoffLocationObj = locationService.findLocationByName(request.getDropoffLocation());

            if (pickupLocationObj == null || dropoffLocationObj == null) {
                return ResponseEntity.badRequest()
                        .body(createErrorResponse("One or both locations not found"));
            }

            Route route = routeService.createRoute(pickupLocationObj, dropoffLocationObj);
            double fareAmount = calculateFareService.calculateFare(route);

            // STEP 3: call bookingService.bookCab() with both objects
            bookingService.bookCab(client, route);

            // STEP 4: Return the response
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("pickupLocation", request.getPickupLocation());
            response.put("dropoffLocation", request.getDropoffLocation());
            response.put("distance", route.getDistance());
            response.put("fareAmount", fareAmount);
            response.put("clientName", client.getName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("Test booking error: " + e.getMessage()));
        }
    }

    /**
     * Simple data class to receive the test booking request
     */
    public static class TestBookingRequest {
        private TestClient client;
        private String pickupLocation;
        private String dropoffLocation;

        // ============TestBookingRequest constructors, getters, and setters==============
        public TestBookingRequest() {}

        public TestClient getClient() { return client; }
        public void setClient(TestClient client) { this.client = client; }

        public String getPickupLocation() { return pickupLocation; }
        public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

        public String getDropoffLocation() { return dropoffLocation; }
        public void setDropoffLocation(String dropoffLocation) { this.dropoffLocation = dropoffLocation; }

        /**
         * Inner class to match the test client structure from frontend
         */
        public static class TestClient {
            private Integer id;
            private String name;
            private String email;
            private String phone;
            private String address;
            private String credit_card;

            // ==============Test Client default constructor and getters/setters=============
            public TestClient() {}

            public Integer getId() { return id; }
            public void setId(Integer id) { this.id = id; }

            public String getName() { return name; }
            public void setName(String name) { this.name = name; }

            public String getEmail() { return email; }
            public void setEmail(String email) { this.email = email; }

            public String getPhone() { return phone; }
            public void setPhone(String phone) { this.phone = phone; }

            public String getAddress() { return address; }
            public void setAddress(String address) { this.address = address; }

            public String getCredit_card() { return credit_card; }
            public void setCredit_card(String credit_card) { this.credit_card = credit_card; }
        }

        @Override
        public String toString() {
            return "TestBookingRequest{" +
                    "client=" + client +
                    ", pickupLocation='" + pickupLocation + '\'' +
                    ", dropoffLocation='" + dropoffLocation + '\'' +
                    '}';
        }
    }

    /**
     * Inner class to represent the booking request coming from the HTML form
     * and methods to covert JSON
     */
    public static class WebBookingRequest {
        private String pickupLocation;
        private String dropoffLocation;

        //=============WebBookingRequest constructors, getters, and setters===========
        public WebBookingRequest() {}

        public WebBookingRequest(String pickupLocation, String dropoffLocation) {
            this.pickupLocation = pickupLocation;
            this.dropoffLocation = dropoffLocation;
        }

        public String getPickupLocation() {return pickupLocation;}
        public void setPickupLocation(String pickupLocation) {this.pickupLocation = pickupLocation;}

        public String getDropoffLocation() {return dropoffLocation;}
        public void setDropoffLocation(String dropoffLocation) {this.dropoffLocation = dropoffLocation;}

        @Override
        public String toString() {
            return "WebBookingRequest{" +
                    "pickupLocation='" + pickupLocation + '\'' +
                    ", dropoffLocation='" + dropoffLocation + '\'' +
                    '}';
        }
    }
}