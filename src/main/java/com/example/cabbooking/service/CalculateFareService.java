package com.example.cabbooking.service;

import com.example.cabbooking.model.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class CalculateFareService {

    private final DistanceCalculatorService distanceCalculatorService;
    private final LocationService locationService;
    private final LocationDistanceCalculatorService locationDistanceCalculatorService;
    private final RouteService routeService;

    @Autowired
    public CalculateFareService(DistanceCalculatorService distanceCalculatorService, LocationService locationService,
                                LocationDistanceCalculatorService locationDistanceCalculatorService, RouteService routeService) {
        this.distanceCalculatorService = distanceCalculatorService;
        this.locationService = locationService;
        this.locationDistanceCalculatorService = locationDistanceCalculatorService;
        this.routeService = routeService;
    }

    public int dollarsPerMile = 3;
    public int initialBookingFee = 3;

    public double calculateFare(Route route) {

        double distance = route.getDistance();

        double cabFare = initialBookingFee + (distance * dollarsPerMile);

        System.out.println("Cab Fare: " + cabFare);

        return cabFare;
    }


}
