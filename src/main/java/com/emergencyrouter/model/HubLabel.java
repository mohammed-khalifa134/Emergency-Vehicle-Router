package com.emergencyrouter.model;

import java.util.Objects;

/**
 * Stores precomputed distance from one node to a hub node.
 *
 * <p>Use this class in Hub Label routing. A node receives several labels, and
 * each label says: "from this node, the cost to reach this hub is X."</p>
 */
public final class HubLabel {
    private final Node hub;
    private final double distance;

    /**
     * Creates a hub label.
     *
     * @param hub reachable hub node
     * @param distance precomputed cost to the hub
     */
    public HubLabel(Node hub, double distance) {
        this.hub = Objects.requireNonNull(hub, "hub must not be null");
        if (distance < 0) {
            throw new IllegalArgumentException("distance must not be negative");
        }
        this.distance = distance;
    }

    /**
     * Gets the hub node.
     *
     * <p>Use this method when looking for hubs shared by a start node and an end
     * node.</p>
     *
     * @return hub node
     */
    public Node getHub() {
        return hub;
    }

    /**
     * Gets the precomputed distance to the hub.
     *
     * <p>Use this value during query time to calculate
     * {@code startToHub + endToHub}.</p>
     *
     * @return distance to hub
     */
    public double getDistance() {
        return distance;
    }
}
