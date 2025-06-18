package com.example.cabbooking.service;

import com.example.cabbooking.model.Client;
import com.example.cabbooking.model.Location;
import com.example.cabbooking.model.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    private final RouteService routeService;
    private final LocationDistanceCalculatorService locationDistanceCalculatorService;

    // ✅ Custom exceptions for booking-related problems
    public static class InvalidBookingException extends RuntimeException {
        public InvalidBookingException(String message) {
            super(message);
        }
    }

    public static class BookingProcessException extends RuntimeException {
        public BookingProcessException(String message) {
            super(message);
        }
    }

    @Autowired
    public BookingService(RouteService routeService, LocationDistanceCalculatorService locationDistanceCalculatorService) {
        this.routeService = routeService;
        this.locationDistanceCalculatorService = locationDistanceCalculatorService;
    }

    public void bookCab(Client client, Route route) {
        // ✅ Validate inputs
        validateBookingInputs(client, route);

        try {
            Location from = routeService.getRouteLocationFrom(route);
            Location to = routeService.getRouteLocationTo(route);

            // ✅ Additional validation after getting locations
            if (from.equals(to)) {
                throw new InvalidBookingException("Cannot book cab for same pickup and destination location: " + from.getLocationName());
            }

            System.out.println("Booking cab from " + from + " to " + to);
            System.out.println("Booking cab for client " + client.getName() + " (ID: " + client.getId() + ")");

            // ✅ Validate distance calculation
            double calculatedDistance = locationDistanceCalculatorService.calculateDistanceUsingLocation(from, to);
            double routeDistance = routeService.getRouteDistance(route);

            // Check if distances are reasonably close (allow 10% variance for rounding)
            if (Math.abs(calculatedDistance - routeDistance) > (calculatedDistance * 0.1)) {
                System.out.println("Warning: Route distance (" + routeDistance + ") differs significantly from calculated distance (" + calculatedDistance + ")");
            }

            locationDistanceCalculatorService.printDistanceReport(from, to);

            System.out.println("✓ Cab booking initiated successfully for " + client.getName());

        } catch (RouteService.InvalidRouteException e) {
            throw new BookingProcessException("Cannot book cab due to invalid route: " + e.getMessage());
        } catch (LocationDistanceCalculatorService.InvalidLocationException e) {
            throw new BookingProcessException("Cannot book cab due to invalid location: " + e.getMessage());
        } catch (Exception e) {
            if (e instanceof InvalidBookingException || e instanceof BookingProcessException) {
                throw e; // Re-throw our custom exceptions
            }
            throw new BookingProcessException("Failed to book cab: " + e.getMessage());
        }
    }

    public void finishBookingCab(Client client, Route route) {
        // ✅ Validate inputs
        validateBookingInputs(client, route);

        try {
            Location from = routeService.getRouteLocationFrom(route);
            Location to = routeService.getRouteLocationTo(route);

            System.out.println("Your cab from " + from.getLocationName() + " to " + to.getLocationName() + " is booked!");
            System.out.println("Thank you " + client.getName() + "! We hope you enjoy your ride!");
            System.out.println("✓ Booking completed successfully");

        } catch (RouteService.InvalidRouteException e) {
            throw new BookingProcessException("Cannot finish booking due to invalid route: " + e.getMessage());
        } catch (Exception e) {
            if (e instanceof InvalidBookingException || e instanceof BookingProcessException) {
                throw e; // Re-throw our custom exceptions
            }
            throw new BookingProcessException("Failed to finish booking: " + e.getMessage());
        }
    }

    // ✅ Helper method to validate booking inputs
    private void validateBookingInputs(Client client, Route route) {
        if (client == null) {
            throw new InvalidBookingException("Client cannot be null");
        }

        if (route == null) {
            throw new InvalidBookingException("Route cannot be null");
        }

        // Validate client data
        validateClientData(client);

        // Validate route data
        if (route.getFrom() == null || route.getTo() == null) {
            throw new InvalidBookingException("Route must have valid starting and destination locations");
        }

        if (route.getDistance() < 0) {
            throw new InvalidBookingException("Route distance cannot be negative: " + route.getDistance());
        }
    }

    // ✅ Helper method to validate client data
    private void validateClientData(Client client) {
        if (client.getId() == null || client.getId() <= 0) {
            throw new InvalidBookingException("Client must have a valid ID");
        }

        if (client.getName() == null || client.getName().trim().isEmpty()) {
            throw new InvalidBookingException("Client must have a valid name");
        }

        if (client.getEmail() == null || client.getEmail().trim().isEmpty()) {
            throw new InvalidBookingException("Client must have a valid email");
        }

        // Basic email validation
        if (!client.getEmail().contains("@")) {
            throw new InvalidBookingException("Client email must be valid: " + client.getEmail());
        }
    }

    // ✅ Utility method to check if a booking is valid (for external use)
    public boolean isValidBooking(Client client, Route route) {
        try {
            validateBookingInputs(client, route);
            return true;
        } catch (InvalidBookingException e) {
            return false;
        }
    }

    // ✅ Method to get booking summary without actually booking
    public String getBookingSummary(Client client, Route route) {
        validateBookingInputs(client, route);

        try {
            Location from = routeService.getRouteLocationFrom(route);
            Location to = routeService.getRouteLocationTo(route);
            double distance = routeService.getRouteDistance(route);

            return String.format("Booking Summary for %s (ID: %d):%nFrom: %s%nTo: %s%nDistance: %.2f km%nEmail: %s",
                    client.getName(), client.getId(),
                    from.getLocationName(), to.getLocationName(),
                    distance, client.getEmail());

        } catch (Exception e) {
            throw new BookingProcessException("Failed to generate booking summary: " + e.getMessage());
        }
    }
}