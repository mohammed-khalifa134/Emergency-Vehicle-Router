package com.example.model;

import java.util.List;

public class Route {

    private List<Location> path;
    private double distance;
    private double time;

    public Route(List<Location> path, double distance, double time) {
        this.path     = path;
        this.distance = distance;
        this.time     = time;
    }

    public List<Location> getPath() {
        return path;
    }

    public double getDistance() {
        return distance;
    }

    public double getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "Route{distance=" + distance + " km, time=" + time + " min, stops=" + path.size() + "}";
    }
}
