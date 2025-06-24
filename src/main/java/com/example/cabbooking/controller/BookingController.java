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

    // Your existing services
    private final BookingService bookingService;
    private final CalculateFareService calculateFareService;

    // Adding the new services we need for location handling
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

    // This method runs automatically when the application starts
    // It sets up all the Washington DC locations so they're ready to use
    @PostConstruct
    public void initializeLocations() {
        System.out.println("Setting up Washington DC locations for the booking system...");
        locationService.initializeWashingtonDCLocations();
        System.out.println("Location setup complete! Ready for bookings.");
    }

    // Your existing booking endpoint (keeping this exactly as it was)
    @PostMapping
    public void bookCab(@RequestBody Client client, Route route) {
        bookingService.bookCab(client, route);
        calculateFareService.calculateFare(route);
    }

    // NEW: Web-based fare calculation endpoint for your HTML form
    // This is what gets called when someone submits the booking form
    @PostMapping("/calculate-fare")
    public ResponseEntity<Map<String, Object>> calculateWebBookingFare(@RequestBody WebBookingRequest request) {
        try {
            System.out.println("Web booking request received:");
            System.out.println("  From: " + request.getPickupLocation());
            System.out.println("  To: " + request.getDropoffLocation());

            // Step 1: Find the actual Location objects that match what the user selected
            // Think of this like looking up addresses in a phone book
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

            // Step 2: Create a Route object using your existing RouteService
            // This calculates the distance between the two locations
            Route route = routeService.createRoute(pickupLocationObj, dropoffLocationObj);
            System.out.println("✓ Route created - Distance: " + String.format("%.2f", route.getDistance()) + " km");

            // Step 3: Calculate the fare using your existing CalculateFareService
            // This applies your business rules: $3 base fee + $3 per mile
            double fareAmount = calculateFareService.calculateFare(route);
            System.out.println("✓ Fare calculated: $" + String.format("%.2f", fareAmount));

            // Step 4: Package everything into a response for the web page
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
            e.printStackTrace(); // This helps with debugging
            return ResponseEntity.internalServerError()
                    .body(createErrorResponse("Error calculating fare: " + e.getMessage()));
        }
    }

    // API endpoint to get all available locations
    // This could be used to dynamically populate your dropdown menus
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

    // Helper method to create consistent error responses for the web client
    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", errorMessage);
        return errorResponse;
    }

    // Inner class to represent the booking request coming from your HTML form
    // This is like a container that holds the data sent from the web page
    public static class WebBookingRequest {
        private String pickupLocation;
        private String dropoffLocation;

        public WebBookingRequest() {}

        public WebBookingRequest(String pickupLocation, String dropoffLocation) {
            this.pickupLocation = pickupLocation;
            this.dropoffLocation = dropoffLocation;
        }

        // Getters and setters (Spring uses these to populate the object from JSON)
        public String getPickupLocation() {
            return pickupLocation;
        }

        public void setPickupLocation(String pickupLocation) {
            this.pickupLocation = pickupLocation;
        }

        public String getDropoffLocation() {
            return dropoffLocation;
        }

        public void setDropoffLocation(String dropoffLocation) {
            this.dropoffLocation = dropoffLocation;
        }

        @Override
        public String toString() {
            return "WebBookingRequest{" +
                    "pickupLocation='" + pickupLocation + '\'' +
                    ", dropoffLocation='" + dropoffLocation + '\'' +
                    '}';
        }
    }
}