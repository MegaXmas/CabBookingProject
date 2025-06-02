package com.example.cabbooking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.cabbooking.model.Location;


@Service
public class LocationDistanceCalculatorService {

    private final DistanceCalculatorService distanceCalculatorService;

    @Autowired
    public LocationDistanceCalculatorService(DistanceCalculatorService distanceCalculatorService) {
        this.distanceCalculatorService = distanceCalculatorService;
    }

    public double calculateDistanceUsingLocation(Location loc1, Location loc2) {
        return DistanceCalculatorService.calculateDistance(
                loc1.getLatitude(), loc1.getLongitude(),
                loc2.getLatitude(), loc2.getLongitude()
        );
    }


    public void printDistanceReport(Location from, Location to) {
        double km = calculateDistanceUsingLocation(from, to);
        double miles = distanceCalculatorService.kmToMiles(km);

        System.out.println("=== Distance Report ===");
        System.out.println("From: " + from);
        System.out.println("To: " + to);
        System.out.printf("Distance: %.2f km (%.2f miles)%n", km, miles);
        System.out.println("=======================");
    }
}


