package com.emergencyrouter.model;

import com.emergencyrouter.interfaces.Location;

import java.util.Objects;

/**
 * Represents a road-network point such as an intersection, station, or incident
 * location.
 *
 * <p>Use this class when building the graph for advanced routing algorithms.
 * It implements {@link Location}, so route results can store graph nodes in the
 * same path list used by the simpler coordinate strategies.</p>
 */
public final class Node implements Location {
    private final String id;
    private final double latitude;
    private final double longitude;

    /**
     * Creates a graph node.
     *
     * @param id unique node id, such as "Hospital" or "A"
     * @param latitude latitude from -90 to 90
     * @param longitude longitude from -180 to 180
     */
    public Node(String id, double latitude, double longitude) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("longitude must be between -180 and 180");
        }

        this.id = id;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Gets the node id.
     *
     * <p>Use this method for road IDs, debug output, and graph lookup.</p>
     *
     * @return node id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the latitude coordinate.
     *
     * <p>Use this method when finding the nearest graph node to a report or
     * vehicle location.</p>
     *
     * @return latitude value
     */
    @Override
    public double getLatitude() {
        return latitude;
    }

    /**
     * Returns the longitude coordinate.
     *
     * <p>Use this method together with latitude for nearest-node calculations.</p>
     *
     * @return longitude value
     */
    @Override
    public double getLongitude() {
        return longitude;
    }

    /**
     * Compares nodes by id.
     *
     * <p>Use id-based equality because the graph treats the node id as the
     * stable identity even if two nodes share similar coordinates.</p>
     *
     * @param object object to compare
     * @return true when both nodes have the same id
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Node node)) {
            return false;
        }
        return id.equals(node.id);
    }

    /**
     * Generates a hash code from the node id.
     *
     * <p>Use this method indirectly when nodes are keys in adjacency maps.</p>
     *
     * @return id-based hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a readable node label.
     *
     * <p>Use this output in console logs and tests.</p>
     *
     * @return node id
     */
    @Override
    public String toString() {
        return id;
    }
}
