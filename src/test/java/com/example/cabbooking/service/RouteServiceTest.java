package com.example.cabbooking.service;

import com.example.cabbooking.model.Location;
import com.example.cabbooking.model.Route;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RouteServiceTest {

    private RouteService routeService;
    private Location startLocation;
    private Location endLocation;

    @BeforeEach
    void setUp() {
        routeService = new RouteService(new LocationDistanceCalculatorService(new DistanceCalculatorService()));

        startLocation = new Location("Central Park", 40.7829, -73.9654);
        endLocation = new Location("Times Square", 40.7580, -73.9855);
    }

    @Test
    void createRouteTest() {

        Route route = routeService.createRoute(startLocation, endLocation);
        assertNotNull(route);
        assertEquals(startLocation, route.getFrom());
        assertEquals(endLocation, route.getTo());
        System.out.println(route);
    }
}