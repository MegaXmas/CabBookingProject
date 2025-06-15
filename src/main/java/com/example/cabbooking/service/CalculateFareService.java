package com.example.cabbooking.service;

import com.example.cabbooking.model.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CalculateFareService {

    private final RouteService routeService;

    @Autowired
    public CalculateFareService(RouteService routeService) {
        this.routeService = routeService;
    }

    public int dollarsPerMile = 3;
    public int initialBookingFee = 3;

    public double calculateFare(Route route) {

        double distance = routeService.getRouteDistance(route);

        double cabFare = initialBookingFee + (distance * dollarsPerMile);

        System.out.println("Cab Fare: " + cabFare);

        return cabFare;
    }
}
