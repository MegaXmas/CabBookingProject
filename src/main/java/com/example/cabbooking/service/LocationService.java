package com.example.cabbooking.service;

import com.example.cabbooking.model.Location;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class LocationService {

    private final List<Location> locations = new ArrayList<>();

    public Location createLocation(String name, double lat, double lng) {
        Location location = new Location();
        location.setLocationName(name);
        location.setLatitude(lat);
        location.setLongitude(lng);
        locations.add(location);
        return location;
    }

    public Location updateLocation(int index, String newName, double newLat, double newLng) {
        if (index < locations.size()) {
            Location location = locations.get(index);
            location.setLocationName(newName);  // Update with setter
            location.setLatitude(newLat);
            location.setLongitude(newLng);
            return location;
        }
        return null;
    }

    public void printLocationInfo(Location location) {
        System.out.println("Name: " + location.getLocationName());
        System.out.println("Lat: " + location.getLatitude());
        System.out.println("Lng: " + location.getLongitude());
    }

    public List<Location> getAllLocations() {
        return locations;
    }
}