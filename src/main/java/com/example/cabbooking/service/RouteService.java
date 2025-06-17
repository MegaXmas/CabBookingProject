package com.example.cabbooking.service;

import com.example.cabbooking.model.Location;
import com.example.cabbooking.model.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RouteService {

    private final LocationDistanceCalculatorService locationDistanceCalculatorService;

    public static class InvalidRouteException extends RuntimeException {
        public InvalidRouteException(String message) {
            super(message);
        }
    }

    public static class RouteCalculationException extends RuntimeException {
        public RouteCalculationException(String message) {
            super(message);
        }
    }

    @Autowired
    public RouteService(LocationDistanceCalculatorService locationDistanceCalculatorService) {
        this.locationDistanceCalculatorService = locationDistanceCalculatorService;
    }

    public Route createRoute(Location from, Location to) {

        validateRouteInputs(from, to);

        try {
            Route route = new Route();
            route.setFrom(from);
            route.setTo(to);
            route.setDistance(locationDistanceCalculatorService.calculateDistanceUsingLocation(from, to));
            return route;
        } catch (Exception e) {
            throw new RouteCalculationException("Failed to calculate route distance: " + e.getMessage());
        }
    }

    public Location getRouteLocationFrom(Route route) {
        validateRoute(route);
        return route.getFrom();
    }

    public Location getRouteLocationTo(Route route) {
        validateRoute(route);
        return route.getTo();
    }

    public double getRouteDistance(Route route) {
        validateRoute(route);

        if (route.getDistance() < 0) {
            throw new InvalidRouteException("Route distance cannot be negative: " + route.getDistance());
        }

        return route.getDistance();
    }

    private void validateRouteInputs(Location from, Location to) {
        if (from == null) {
            throw new InvalidRouteException("Starting location cannot be null");
        }

        if (to == null) {
            throw new InvalidRouteException("Destination location cannot be null");
        }

        if (from.getLocationName() == null || from.getLocationName().trim().isEmpty()) {
            throw new InvalidRouteException("Starting location must have a valid name");
        }

        if (to.getLocationName() == null || to.getLocationName().trim().isEmpty()) {
            throw new InvalidRouteException("Destination location must have a valid name");
        }
    }

    private void validateRoute(Route route) {
        if (route == null) {
            throw new InvalidRouteException("Route cannot be null");
        }

        if (route.getFrom() == null || route.getTo() == null) {
            throw new InvalidRouteException("Route must have valid starting and destination locations");
        }
    }
}