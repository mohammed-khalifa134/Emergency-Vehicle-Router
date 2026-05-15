package com.emergencyrouter.model;

import com.emergencyrouter.enums.LocationType;
import com.emergencyrouter.interfaces.Location;

public class MapLocation implements Location {

    private final double latitude;
    private final double longitude;
    private final String name;
    private final LocationType type;

    private MapLocation(Builder builder) {
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.name = builder.name;
        this.type = builder.type;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private double latitude;
        private double longitude;
        private String name;
        private LocationType type;

        public Builder latitude(double latitude) {
            this.latitude = latitude;
            return this;
        }

        public Builder longitude(double longitude) {
            this.longitude = longitude;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(LocationType type) {
            this.type = type;
            return this;
        }

        public MapLocation build() {
            return new MapLocation(this);
        }
    }

    @Override
    public double getLatitude() {
        return latitude;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }
}