package com.example.cabbooking.service;

import org.springframework.stereotype.Service;

@Service
public class DistanceCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

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

    //---------------Kilometers to Miles Conversion----------------
        public static double kmToMiles(double kilometers) {
            return kilometers * 0.621371;
        }

}