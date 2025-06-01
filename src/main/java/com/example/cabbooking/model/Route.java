package com.example.cabbooking.model;

public class Route {

    private Location from;
    private Location to;
    private double distance;

    public Route(Location from, Location to) {
        this.from = from;
        this.to = to;
    }

    public Route() {}

    public Location getFrom() {
        return from;
    }

    public void setFrom(Location from) {
        this.from = from;
    }

    public Location getTo() {
        return to;
    }

    public void setTo(Location to) {
        this.to = to;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}

