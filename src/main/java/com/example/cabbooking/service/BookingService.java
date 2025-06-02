package com.example.cabbooking.service;

import com.example.cabbooking.model.Client;
import com.example.cabbooking.model.Location;
import com.example.cabbooking.model.Route;
import com.example.cabbooking.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookingService {

    private final CalculateFareService calculateFareService;
    private final LocationDistanceCalculatorService locationDistanceCalculatorService;
    private final RouteService routeService;
    private ClientRepository clientRepository;


    @Autowired
    public BookingService(ClientRepository clientRepository, CalculateFareService calculateFareService,
                          LocationDistanceCalculatorService locationDistanceCalculatorService, RouteService routeService) {
        this.clientRepository = clientRepository;
        this.calculateFareService = calculateFareService;
        this.locationDistanceCalculatorService = locationDistanceCalculatorService;
        this.routeService = routeService;
    }

    public void bookCab(Client client, Route route) {

        Location from = routeService.getRouteLocationFrom(route);
        Location to = routeService.getRouteLocationTo(route);

        System.out.println("Booking cab from " + from + " to " + to);
        System.out.println("Booking cab to client " + client );

        LocationDistanceCalculatorService.calculateDistance(from, to);
        LocationDistanceCalculatorService.printDistanceReport(from, to);

        routeService.getRouteDistance(route);
        //printDistance returns miles
    }

    public void finishBookingCab(Client client, Route route) {
        Location from = routeService.getRouteLocationFrom(route);
        Location to = routeService.getRouteLocationTo(route);
        System.out.println("Your cab from " + from + " to " + to + " is booked!" + '\'' +
                "We hope you enjoy your ride!");
    }
}
