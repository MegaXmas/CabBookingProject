package com.example.cabbooking.model;

import java.util.Objects;

public class Location {

    private double latitude;
    private double longitude;
    private String locationName;

    //-------------Constructors---------------
    public Location() {}


    public Location(String locationName, double latitude, double longitude) {
        this.locationName = locationName;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    //------------Getters and Setters-------------
    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Double.compare(latitude, location.latitude) == 0 &&
                Double.compare(longitude, location.longitude) == 0 &&
                Objects.equals(locationName, location.locationName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, locationName);
    }

    @Override
    public String toString() {
        return String.format("Location{latitude=%.4f, longitude=%.4f, name='%s'}",
                latitude, longitude, locationName);
    }
}

