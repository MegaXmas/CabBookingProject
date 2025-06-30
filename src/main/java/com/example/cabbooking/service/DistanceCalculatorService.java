package com.example.cabbooking.service;

import org.springframework.stereotype.Service;

@Service
public class DistanceCalculatorService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * method to run the Haversine Formula, which calculates
     * the distance between two locations using their latitude and longitude
     * @param lat1 latitude of location 1
     * @param lon1 longitude of location 1
     * @param lat2 latitude of location 2
     * @param lon2 longitude of location 2
     * @return The distance between the two locations in kilometers
     */
    public static double calculateDistance(double lat1, double lon1,
                                           double lat2, double lon2) {

        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        double deltaLat = lat2Rad - lat1Rad;  // How far apart north-south?
        double deltaLon = lon2Rad - lon1Rad;  // How far apart east-west?

//----------------Haversine Formula--------------------
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double kilometers = EARTH_RADIUS_KM * c;
        return kilometers;
    }

    /**
     * method which converts kilometers to miles
     * (will run after calculateDistance() and use the returned kilometers variable)
     * @param kilometers Kilometers delivered from calculateDistance() to be converted into miles
     * @return The conversion of kilometers to miles
     */
    public static double kmToMiles(double kilometers) {
        double miles = kilometers * 0.621371;
        return miles;
    }
}