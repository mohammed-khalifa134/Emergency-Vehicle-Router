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
 *
 * <p>Impact on the system: this file is the road-network foundation used by
 * routing algorithms, traffic updates, the Swing map, and graph editing forms.
 * The Swing work expanded it so the UI can list, replace, update, and remove
 * roads/nodes without accessing internal maps directly.</p>
 *
 * <p>Main methods in this file:</p>
 * <ul>
 *     <li>{@link #addNode(Node)}, {@link #addDirectedEdge(Node, Node, double, double)},
 *     and {@link #addBidirectionalEdge(Node, Node, double, double)} build the
 *     road network.</li>
 *     <li>{@link #getNode(String)}, {@link #getNodes()}, {@link #getEdges()},
 *     {@link #getEdge(String)}, and {@link #getEdgesFrom(Node)} provide safe
 *     read-only snapshots for services and views.</li>
 *     <li>{@link #updateTraffic(String, double, boolean)} applies congestion
 *     and closure changes.</li>
 *     <li>{@link #addOrReplaceDirectedEdge(Node, Node, double, double, boolean)},
 *     {@link #updateOrReplaceRoad(String, double, double, boolean)},
 *     {@link #removeEdge(String)}, and {@link #removeNode(String)} support the
 *     Swing editing screens.</li>
 *     <li>{@link #findNearestNode(Location)} connects arbitrary locations to
 *     graph nodes before algorithms run.</li>
 * </ul>
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
     * Gets all directed roads in the graph.
     *
     * <p>Use this method from UI tables, traffic controls, and diagnostics when
     * every known road should be displayed without exposing internal maps.</p>
     *
     * @return immutable snapshot of directed edges
     */
    public Collection<Edge> getEdges() {
        return List.copyOf(edgesByRoadId.values());
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
     * Adds a directed road, replacing an existing road with the same id.
     *
     * <p>Use this method from graph editing screens where users may intentionally
     * correct an existing road's distance, traffic weight, or closure state.</p>
     *
     * @param source road source
     * @param destination road destination
     * @param distance physical road distance
     * @param trafficWeight congestion cost
     * @param closed true when the road should start closed
     * @return added replacement edge
     */
    public Edge addOrReplaceDirectedEdge(
            Node source,
            Node destination,
            double distance,
            double trafficWeight,
            boolean closed
    ) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(destination, "destination must not be null");

        removeEdge(Edge.roadId(source, destination));
        Edge edge = addDirectedEdge(source, destination, distance, trafficWeight);
        edge.setClosed(closed);
        return edge;
    }

    /**
     * Updates a road while preserving its source and destination.
     *
     * <p>Use this method when the UI edits distance as well as traffic data. The
     * existing edge is replaced because edge distance is immutable by design.</p>
     *
     * @param roadId directed road id
     * @param distance new physical distance
     * @param trafficWeight new congestion cost
     * @param closed true when the road is closed
     * @return updated edge
     */
    public Edge updateOrReplaceRoad(String roadId, double distance, double trafficWeight, boolean closed) {
        Edge existing = getEdge(roadId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown road id: " + roadId));

        return addOrReplaceDirectedEdge(
                existing.getSource(),
                existing.getDestination(),
                distance,
                trafficWeight,
                closed
        );
    }

    /**
     * Removes one directed road from the graph.
     *
     * <p>Use this method from graph editing screens. Removing a road updates both
     * the road lookup map and the source node's adjacency list.</p>
     *
     * @param roadId directed road id
     * @return true when a road was removed
     */
    public boolean removeEdge(String roadId) {
        Edge removed = edgesByRoadId.remove(roadId);

        if (removed == null) {
            return false;
        }

        List<Edge> outgoingEdges = adjacencyList.get(removed.getSource());
        if (outgoingEdges != null) {
            outgoingEdges.removeIf(edge -> edge.getRoadId().equals(roadId));
        }

        return true;
    }

    /**
     * Removes a node and every road connected to it.
     *
     * <p>Use this method from graph editing screens so the graph never keeps
     * orphan roads pointing at a deleted location.</p>
     *
     * @param nodeId node id to remove
     * @return true when a node was removed
     */
    public boolean removeNode(String nodeId) {
        Node removedNode = nodesById.remove(nodeId);

        if (removedNode == null) {
            return false;
        }

        List<Edge> outgoingEdges = adjacencyList.remove(removedNode);
        if (outgoingEdges != null) {
            outgoingEdges.forEach(edge -> edgesByRoadId.remove(edge.getRoadId()));
        }

        for (List<Edge> edges : adjacencyList.values()) {
            edges.removeIf(edge -> {
                boolean connectedToRemovedNode = edge.getDestination().equals(removedNode);
                if (connectedToRemovedNode) {
                    edgesByRoadId.remove(edge.getRoadId());
                }
                return connectedToRemovedNode;
            });
        }

        return true;
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
