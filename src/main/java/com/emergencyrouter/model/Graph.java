package com.emergencyrouter.model;

import com.emergencyrouter.interfaces.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents the city road network as a graph.
 *
 * <p>Use this class as the shared data structure for Dijkstra, Hub Labels, and
 * later traffic-aware route recalculation. Nodes represent intersections or
 * places; edges represent roads.</p>
 */
public final class Graph {
    private final Map<String, Node> nodesById = new HashMap<>();
    private final Map<Node, List<Edge>> adjacencyList = new HashMap<>();
    private final Map<String, Edge> edgesByRoadId = new HashMap<>();

    /**
     * Adds a node to the graph.
     *
     * <p>Use this method before adding roads connected to the node.</p>
     *
     * @param node node to add
     */
    public void addNode(Node node) {
        Objects.requireNonNull(node, "node must not be null");
        nodesById.put(node.getId(), node);
        adjacencyList.putIfAbsent(node, new ArrayList<>());
    }

    /**
     * Adds a directed road from source to destination.
     *
     * <p>Use this method for one-way roads. For two-way roads, use
     * {@link #addBidirectionalEdge(Node, Node, double, double)}.</p>
     *
     * @param source road source
     * @param destination road destination
     * @param distance physical road distance
     * @param trafficWeight congestion cost
     * @return created edge
     */
    public Edge addDirectedEdge(Node source, Node destination, double distance, double trafficWeight) {
        addNode(source);
        addNode(destination);

        Edge edge = new Edge(source, destination, distance, trafficWeight);
        adjacencyList.get(source).add(edge);
        edgesByRoadId.put(edge.getRoadId(), edge);
        return edge;
    }

    /**
     * Adds roads in both directions between two nodes.
     *
     * <p>Use this method for normal two-way streets in the sample road network.</p>
     *
     * @param first first node
     * @param second second node
     * @param distance physical road distance
     * @param trafficWeight congestion cost
     */
    public void addBidirectionalEdge(Node first, Node second, double distance, double trafficWeight) {
        addDirectedEdge(first, second, distance, trafficWeight);
        addDirectedEdge(second, first, distance, trafficWeight);
    }

    /**
     * Finds a node by id.
     *
     * <p>Use this method when a service or test needs to retrieve a known road
     * network point.</p>
     *
     * @param id node id
     * @return optional node
     */
    public Optional<Node> getNode(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(nodesById.get(id));
    }

    /**
     * Gets all graph nodes.
     *
     * <p>Use this method for preprocessing algorithms such as Hub Labels.</p>
     *
     * @return immutable snapshot of nodes
     */
    public Collection<Node> getNodes() {
        return List.copyOf(nodesById.values());
    }

    /**
     * Gets outgoing edges from a node.
     *
     * <p>Use this method during graph traversal to visit neighboring nodes.</p>
     *
     * @param node node whose roads should be returned
     * @return immutable snapshot of outgoing edges
     */
    public List<Edge> getEdgesFrom(Node node) {
        Objects.requireNonNull(node, "node must not be null");
        return List.copyOf(adjacencyList.getOrDefault(node, List.of()));
    }

    /**
     * Finds an edge by road id.
     *
     * <p>Use this method when applying traffic updates or verifying road state
     * in tests.</p>
     *
     * @param roadId directed road id, such as {@code A->B}
     * @return optional edge
     */
    public Optional<Edge> getEdge(String roadId) {
        if (roadId == null || roadId.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(edgesByRoadId.get(roadId));
    }

    /**
     * Updates traffic data for a road.
     *
     * <p>Use this method when real-time traffic data changes congestion or road
     * closure state. Later observer integration will call this method from
     * traffic updates.</p>
     *
     * @param roadId directed road id
     * @param congestionLevel new traffic weight
     * @param closed true when the road is closed
     */
    public void updateTraffic(String roadId, double congestionLevel, boolean closed) {
        Edge edge = getEdge(roadId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown road id: " + roadId));
        edge.setTrafficWeight(congestionLevel);
        edge.setClosed(closed);
    }

    /**
     * Finds the nearest graph node to a location.
     *
     * <p>Use this method to connect a vehicle coordinate or report coordinate to
     * the graph before running pathfinding algorithms.</p>
     *
     * @param location location to match
     * @return nearest node
     */
    public Node findNearestNode(Location location) {
        Objects.requireNonNull(location, "location must not be null");

        return nodesById.values().stream()
                .min((first, second) -> Double.compare(
                        squaredDistance(first, location),
                        squaredDistance(second, location)
                ))
                .orElseThrow(() -> new IllegalStateException("graph has no nodes"));
    }

    private double squaredDistance(Location first, Location second) {
        double latitudeDifference = first.getLatitude() - second.getLatitude();
        double longitudeDifference = first.getLongitude() - second.getLongitude();
        return latitudeDifference * latitudeDifference + longitudeDifference * longitudeDifference;
    }
}
