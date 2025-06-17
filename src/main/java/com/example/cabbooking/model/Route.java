package com.example.cabbooking.model;

import java.util.Objects;

public class Route {

    private Location from;
    private Location to;
    private double distance;

    //------------Constructors-----------------
    public Route() {}


    public Route(Location from, Location to, double distance) {
        this.from = from;
        this.to = to;
        this.distance = distance;
    }

    //------------Getters and Setters-------------
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Route route = (Route) o;
        return Double.compare(distance, route.distance) == 0 && Objects.equals(from, route.from) && Objects.equals(to, route.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to, distance);
    }

    @Override
    public String toString() {
        return "Route{" +
                "from=" + from +
                ", to=" + to +
                ", distance=" + distance +
                '}';
    }
}

