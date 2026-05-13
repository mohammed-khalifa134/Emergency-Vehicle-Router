package com.emergencyrouter.model;

import java.util.Objects;

/**
 * Represents a road connection between two graph nodes.
 *
 * <p>Use this class inside {@link Graph} adjacency lists. Routing algorithms
 * will read the distance, traffic weight, and closed status from each edge.</p>
 */
public final class Edge {
    private final Node source;
    private final Node destination;
    private final double distance;
    private double trafficWeight;
    private boolean closed;

    /**
     * Creates a road edge.
     *
     * @param source starting node
     * @param destination ending node
     * @param distance physical road distance
     * @param trafficWeight extra cost caused by congestion
     */
    public Edge(Node source, Node destination, double distance, double trafficWeight) {
        this.source = Objects.requireNonNull(source, "source must not be null");
        this.destination = Objects.requireNonNull(destination, "destination must not be null");
        validateNonNegative(distance, "distance");
        validateNonNegative(trafficWeight, "trafficWeight");
        this.distance = distance;
        this.trafficWeight = trafficWeight;
    }

    /**
     * Gets the directed road id.
     *
     * <p>Use this id when applying traffic updates to a specific road. The
     * format is {@code sourceId->destinationId}.</p>
     *
     * @return road id
     */
    public String getRoadId() {
        return roadId(source, destination);
    }

    /**
     * Builds a road id for two nodes.
     *
     * <p>Use this helper when callers need to update or retrieve a road by id.</p>
     *
     * @param source source node
     * @param destination destination node
     * @return directed road id
     */
    public static String roadId(Node source, Node destination) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(destination, "destination must not be null");
        return source.getId() + "->" + destination.getId();
    }

    /**
     * Gets the source node.
     *
     * <p>Use this method when traversing outgoing roads from a node.</p>
     *
     * @return source node
     */
    public Node getSource() {
        return source;
    }

    /**
     * Gets the destination node.
     *
     * <p>Use this method when a routing algorithm moves from one node to the
     * next.</p>
     *
     * @return destination node
     */
    public Node getDestination() {
        return destination;
    }

    /**
     * Gets the physical distance of the road.
     *
     * <p>Use this value when comparing shortest paths.</p>
     *
     * @return road distance
     */
    public double getDistance() {
        return distance;
    }

    /**
     * Gets the current congestion cost.
     *
     * <p>Use this value when route strategies need traffic-aware weights.</p>
     *
     * @return traffic weight
     */
    public double getTrafficWeight() {
        return trafficWeight;
    }

    /**
     * Updates the congestion cost.
     *
     * <p>Use this method when traffic data reports heavier or lighter
     * congestion.</p>
     *
     * @param trafficWeight new traffic weight
     */
    public void setTrafficWeight(double trafficWeight) {
        validateNonNegative(trafficWeight, "trafficWeight");
        this.trafficWeight = trafficWeight;
    }

    /**
     * Checks whether the road is closed.
     *
     * <p>Use this method so routing algorithms can skip unusable roads.</p>
     *
     * @return true when road is closed
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Opens or closes the road.
     *
     * <p>Use this method when traffic updates report accidents, blocked roads,
     * or reopened roads.</p>
     *
     * @param closed true to close the road
     */
    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    /**
     * Gets the traffic-aware cost for routing.
     *
     * <p>Use this method in graph algorithms instead of reading distance and
     * traffic separately. Closed roads return infinity so they are never chosen
     * as a best route.</p>
     *
     * @return distance plus traffic weight, or infinity when closed
     */
    public double getEffectiveWeight() {
        if (closed) {
            return Double.POSITIVE_INFINITY;
        }
        return distance + trafficWeight;
    }

    private static void validateNonNegative(double value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " must not be negative");
        }
    }
}
