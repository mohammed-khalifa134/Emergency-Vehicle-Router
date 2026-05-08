package com.example.model;

public class TrafficData {

    private String roadId;
    private double congestionLevel;
    private boolean isClosed;
    private long timestamp;

    public TrafficData(String roadId, double congestionLevel, boolean isClosed) {
        this.roadId          = roadId;
        this.congestionLevel = congestionLevel;
        this.isClosed        = isClosed;
        this.timestamp       = System.currentTimeMillis();
    }

    public String getRoadId() {
        return roadId;
    }

    public double getCongestionLevel() {
        return congestionLevel;
    }

    public boolean isRoadClosed() {
        return isClosed;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "TrafficData{roadId='" + roadId + "', congestion=" + congestionLevel + ", closed=" + isClosed + "}";
    }
}
