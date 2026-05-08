package com.example.model;

public class Coordinate implements Location {

    private double latitude;
    private double longitude;

    public Coordinate(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public double getLatitude() {
        return latitude;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return "Coordinate{lat=" + latitude + ", lng=" + longitude + "}";
    }
}
