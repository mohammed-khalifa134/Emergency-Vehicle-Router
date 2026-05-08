package com.example.model;

import java.util.Date;

public class Report {

    private String id;
    private String type;
    private Location location;
    private Date timestamp;

    public Report(String id, String type, Location location) {
        this.id        = id;
        this.type      = type;
        this.location  = location;
        this.timestamp = new Date();
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Location getLocation() {
        return location;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Report{id='" + id + "', type='" + type + "', location=" + location + ", timestamp=" + timestamp + "}";
    }
}
