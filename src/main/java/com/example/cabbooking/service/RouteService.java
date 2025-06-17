package com.example.cabbooking.service;

import com.example.cabbooking.model.Location;
import com.example.cabbooking.model.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RouteService {

    private final LocationDistanceCalculatorService locationDistanceCalculatorService;

    @Autowired
    public RouteService(LocationDistanceCalculatorService locationDistanceCalculatorService) {
        this.locationDistanceCalculatorService = locationDistanceCalculatorService;
    }

    public Route createRoute(Location from, Location to) {

        double miles = locationDistanceCalculatorService.printDistanceReport(from, to);

        Route route = new Route();
        route.setFrom(from);
        route.setTo(to);
        route.setDistance(miles);

        System.out.println("Route created");
        System.out.println(route);

        return route;
    }

    public Location getRouteLocationFrom(Route route) {
        return route.getFrom();
    }
    public Location getRouteLocationTo(Route route) {
        return route.getTo();
    }

    public double getRouteDistance(Route route) {
        return route.getDistance();
    }
}
