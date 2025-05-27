package com.example.cabbooking.service;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import com.example.cabbooking.model.Location;


@Service
public class LocationDistanceCalculator extends DistanceCalculator {

    public static double calculateDistance(Location loc1, Location loc2) {
        return calculateDistance(
                loc1.getLatitude(), loc1.getLongitude(),
                loc2.getLatitude(), loc2.getLongitude()
        );
    }


    public static void printDistanceReport(Location from, Location to) {
        double km = calculateDistance(from, to);
        double miles = kmToMiles(km);

        System.out.println("=== Distance Report ===");
        System.out.println("From: " + from);
        System.out.println("To: " + to);
        System.out.printf("Distance: %.2f km (%.2f miles)%n", km, miles);
        System.out.println("=======================");
    }
}


