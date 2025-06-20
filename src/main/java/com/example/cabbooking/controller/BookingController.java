package com.example.cabbooking.controller;

import com.example.cabbooking.model.Client;
import com.example.cabbooking.model.Location;
import com.example.cabbooking.model.Route;
import com.example.cabbooking.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;
    private final CalculateFareService calculateFareService;
    private final PaymentService paymentService;
    private final RouteService routeService;
    private final ClientService clientService;

    @Autowired
    public BookingController(BookingService bookingService,
                             CalculateFareService calculateFareService,
                             PaymentService paymentService,
                             RouteService routeService,
                             ClientService clientService) {
        this.bookingService = bookingService;
        this.calculateFareService = calculateFareService;
        this.paymentService = paymentService;
        this.routeService = routeService;
        this.clientService = clientService;
    }

    // =================== CUSTOM EXCEPTIONS ===================

    /**
     * Exception thrown when booking-related errors occur
     */
    public static class BookingException extends RuntimeException {
        public BookingException(String message) {
            super(message);
        }
    }

    /**
     * Exception thrown when client validation fails
     */
    public static class InvalidClientException extends BookingException {
        public InvalidClientException(String message) {
            super("Invalid client: " + message);
        }
    }

    /**
     * Exception thrown when route validation fails
     */
    public static class InvalidRouteException extends BookingException {
        public InvalidRouteException(String message) {
            super("Invalid route: " + message);
        }
    }

    /**
     * Exception thrown when payment processing fails
     */
    public static class PaymentProcessingException extends BookingException {
        public PaymentProcessingException(String message) {
            super("Payment error: " + message);
        }
    }

    // =================== EXCEPTION HANDLER ===================

    /**
     * Handles all booking-related exceptions and returns appropriate HTTP responses
     */
    @ExceptionHandler(BookingException.class)
    public ResponseEntity<String> handleBookingException(BookingException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Handles general exceptions that might occur
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("An unexpected error occurred: " + ex.getMessage());
    }

    // =================== BOOKING ENDPOINTS ===================

    /**
     * Books a cab for a client with proper validation and error handling
     * @param request The client requesting the booking
     * @return Response with booking confirmation and fare information
     */
    @PostMapping
    public ResponseEntity<String> bookCab(@RequestBody BookingRequest request) {
        Client client = request.getClient();
        Route route = request.getRoute();

        try {
            // Validate inputs first
            if (client == null) {
                throw new InvalidClientException("Client information is required");
            }
            if (route == null) {
                throw new InvalidRouteException("Route information is required");
            }

            // Do the booking
            bookingService.bookCab(client, route);
            double fare = calculateFareService.calculateFare(route);

            String message = String.format("Cab booked successfully! Fare: $%.2f", fare);
            return ResponseEntity.ok(message);

        } catch (Exception ex) {
            throw new BookingException("Booking failed: " + ex.getMessage());
        }
    }

    /**
     * Creates a route and books a cab in one step
     * @param request Contains client info, start location, end location
     * @return Response with booking confirmation
     */
    @PostMapping("/quick-book")
    public ResponseEntity<String> quickBookCab(@RequestBody QuickBookingRequest request) {
        try {
            // Validate client exists
            if (!clientService.clientExists(request.getClientId())) {
                throw new InvalidClientException("Client not found with ID: " + request.getClientId());
            }

            // Get client details
            Client client = clientService.getClientById(request.getClientId())
                    .orElseThrow(() -> new InvalidClientException("Could not retrieve client"));

            // Create route from locations
            Route route = routeService.createRoute(request.getFromLocation(), request.getToLocation());

            // Book the cab
            bookingService.bookCab(client, route);

            // Calculate fare
            double fare = calculateFareService.calculateFare(route);

            String message = String.format("Quick booking successful! From: %s To: %s Fare: $%.2f",
                    request.getFromLocation().getLocationName(),
                    request.getToLocation().getLocationName(),
                    fare);
            return ResponseEntity.ok(message);

        } catch (BookingException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BookingException("Quick booking failed: " + ex.getMessage());
        }
    }

    /**
     * Processes payment for a booking
     * @param request Payment details
     * @return Payment confirmation
     */
    @PostMapping("/payment")
    public ResponseEntity<String> processPayment(@RequestBody PaymentRequest request) {
        try {
            // Validate client
            Client client = clientService.getClientById(request.getClientId())
                    .orElseThrow(() -> new InvalidClientException("Client not found"));

            // Process payment using PaymentService
            paymentService.paymentConfirmation(client, request.getRoute(),
                    request.getPaymentAmount(), request.getCreditCardNumber());

            return ResponseEntity.ok("Payment processed successfully!");

        } catch (RuntimeException ex) {
            // PaymentService throws RuntimeException for invalid card or amount
            throw new PaymentProcessingException(ex.getMessage());
        } catch (Exception ex) {
            throw new PaymentProcessingException("Payment processing failed: " + ex.getMessage());
        }
    }

    // =================== HELPER METHODS ===================

    /**
     * Validates a booking request to ensure all required data is present and valid
     */
    private void validateBookingRequest(BookingRequest request) {
        if (request == null) {
            throw new BookingException("Booking request cannot be null");
        }

        // Validate client
        if (request.getClient() == null) {
            throw new InvalidClientException("Client information is required");
        }

        if (request.getClient().getName() == null || request.getClient().getName().trim().isEmpty()) {
            throw new InvalidClientException("Client name is required");
        }

        // Validate route
        if (request.getRoute() == null) {
            throw new InvalidRouteException("Route information is required");
        }

        if (request.getRoute().getFrom() == null || request.getRoute().getTo() == null) {
            throw new InvalidRouteException("Both start and end locations are required");
        }

        if (request.getRoute().getDistance() <= 0) {
            throw new InvalidRouteException("Route distance must be greater than 0");
        }
    }

    // =================== REQUEST/RESPONSE CLASSES ===================

    /**
     * Request object for standard booking
     */
    public static class BookingRequest {
        private Client client;
        private Route route;

        // Constructors, getters, and setters
        public BookingRequest() {}

        public BookingRequest(Client client, Route route) {
            this.client = client;
            this.route = route;
        }

        public Client getClient() { return client; }
        public void setClient(Client client) { this.client = client; }
        public Route getRoute() { return route; }
        public void setRoute(Route route) { this.route = route; }
    }

    /**
     * Request object for quick booking with locations
     */
    public static class QuickBookingRequest {
        private Integer clientId;
        private Location fromLocation;
        private Location toLocation;

        // Constructors, getters, and setters
        public QuickBookingRequest() {}

        public Integer getClientId() { return clientId; }
        public void setClientId(Integer clientId) { this.clientId = clientId; }
        public Location getFromLocation() { return fromLocation; }
        public void setFromLocation(Location fromLocation) { this.fromLocation = fromLocation; }
        public Location getToLocation() { return toLocation; }
        public void setToLocation(Location toLocation) { this.toLocation = toLocation; }
    }

    /**
     * Request object for payment processing
     */
    public static class PaymentRequest {
        private Integer clientId;
        private Route route;
        private double paymentAmount;
        private String creditCardNumber;

        public PaymentRequest() {}

        // Getters and setters
        public Integer getClientId() { return clientId; }
        public void setClientId(Integer clientId) { this.clientId = clientId; }
        public Route getRoute() { return route; }
        public void setRoute(Route route) { this.route = route; }
        public double getPaymentAmount() { return paymentAmount; }
        public void setPaymentAmount(double paymentAmount) { this.paymentAmount = paymentAmount; }
        public String getCreditCardNumber() { return creditCardNumber; }
        public void setCreditCardNumber(String creditCardNumber) { this.creditCardNumber = creditCardNumber; }
    }
}