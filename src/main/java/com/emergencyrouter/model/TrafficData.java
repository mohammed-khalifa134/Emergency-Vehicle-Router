package com.emergencyrouter.model;

/**
 * Carries real-time traffic information for one road.
 *
 * <p>Use this model when a road becomes congested, closes, or reopens. The
 * traffic service publishes this object to observers such as
 * {@code RoutingService}.</p>
 */
public final class TrafficData {
    private final String roadId;
    private final double congestionLevel;
    private final boolean roadClosed;
    private final long timestamp;

    /**
     * Creates traffic update data.
     *
     * @param roadId directed road id, such as {@code A->B}
     * @param congestionLevel new traffic weight for the road
     * @param roadClosed true when the road is closed
     * @param timestamp update time in milliseconds
     */
    public TrafficData(String roadId, double congestionLevel, boolean roadClosed, long timestamp) {
        if (roadId == null || roadId.isBlank()) {
            throw new IllegalArgumentException("roadId must not be blank");
        }
        if (congestionLevel < 0) {
            throw new IllegalArgumentException("congestionLevel must not be negative");
        }

        this.roadId = roadId;
        this.congestionLevel = congestionLevel;
        this.roadClosed = roadClosed;
        this.timestamp = timestamp;
    }

    /**
     * Gets the road id affected by this traffic update.
     *
     * <p>Use this value to find the matching {@link Edge} in the graph.</p>
     *
     * @return road id
     */
    public String getRoadId() {
        return roadId;
    }

    /**
     * Gets the new congestion cost.
     *
     * <p>Use this value to update the edge's traffic weight.</p>
     *
     * @return congestion level
     */
    public double getCongestionLevel() {
        return congestionLevel;
    }

    /**
     * Checks whether the road is closed.
     *
     * <p>Use this method so routing algorithms avoid roads that cannot be used.</p>
     *
     * @return true when the road is closed
     */
    public boolean isRoadClosed() {
        return roadClosed;
    }

    /**
     * Gets the update timestamp.
     *
     * <p>Use this value in logs or future monitoring features.</p>
     *
     * @return timestamp in milliseconds
     */
    public long getTimestamp() {
        return timestamp;
    }
}
