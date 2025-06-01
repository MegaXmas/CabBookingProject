package com.example.cabbooking.service;

import com.example.cabbooking.model.Location;
import com.example.cabbooking.model.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RouteService {

    private final LocationService locationService;
    private final LocationDistanceCalculatorService locationDistanceCalculatorService;

    @Autowired
    public RouteService(LocationService locationService,
                        LocationDistanceCalculatorService locationDistanceCalculatorService) {
        this.locationService = locationService;
        this.locationDistanceCalculatorService = locationDistanceCalculatorService;
    }

    public Route createRoute(Location from, Location to) {

        Route route = new Route();
        route.setFrom(from);
        route.setTo(to);
        route.setDistance(locationDistanceCalculatorService.calculateDistance(from, to));

        return route;
    }

}
