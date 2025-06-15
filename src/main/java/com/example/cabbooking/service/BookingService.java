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

    @Autowired
    public BookingService(RouteService routeService, LocationDistanceCalculatorService locationDistanceCalculatorService) {
        this.routeService = routeService;
        this.locationDistanceCalculatorService = locationDistanceCalculatorService;
    }

    public void bookCab(Client client, Route route) {

        Location from = routeService.getRouteLocationFrom(route);
        Location to = routeService.getRouteLocationTo(route);

        System.out.println("Booking cab from " + from + " to " + to);
        System.out.println("Booking cab to client " + client );

        locationDistanceCalculatorService.calculateDistanceUsingLocation(from, to);
        locationDistanceCalculatorService.printDistanceReport(from, to);

        routeService.getRouteDistance(route);
    }

    public void finishBookingCab(Client client, Route route) {
        Location from = routeService.getRouteLocationFrom(route);
        Location to = routeService.getRouteLocationTo(route);
        System.out.println("Your cab from " + from + " to " + to + " is booked!" + '\'' +
                "We hope you enjoy your ride!");
    }
}
