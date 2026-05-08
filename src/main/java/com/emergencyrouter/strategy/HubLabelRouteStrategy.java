package com.emergencyrouter.strategy;

import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Graph;
import com.emergencyrouter.model.HubLabel;
import com.emergencyrouter.model.Node;
import com.emergencyrouter.model.Route;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Route strategy that answers shortest-path queries using precomputed Hub
 * Labels.
 *
 * <p>Use this strategy when low-latency route queries are more important than
 * startup preprocessing time. The query compares common hubs instead of running
 * a full graph traversal for every emergency.</p>
 */
public final class HubLabelRouteStrategy implements RouteStrategy {
    private final Graph graph;
    private Map<Node, List<HubLabel>> labelsByNode;

    /**
     * Creates a Hub Label route strategy.
     *
     * @param graph graph used to snap coordinates to nodes
     * @param labelsByNode preprocessed labels from {@code HubLabelPreprocessor}
     */
    public HubLabelRouteStrategy(Graph graph, Map<Node, List<HubLabel>> labelsByNode) {
        this.graph = Objects.requireNonNull(graph, "graph must not be null");
        refreshLabels(labelsByNode);
    }

    /**
     * Replaces labels after preprocessing or traffic changes.
     *
     * <p>Use this method after road closures or traffic-weight updates because
     * old labels describe the previous graph costs.</p>
     *
     * @param labelsByNode refreshed labels
     */
    public void refreshLabels(Map<Node, List<HubLabel>> labelsByNode) {
        Objects.requireNonNull(labelsByNode, "labelsByNode must not be null");

        Map<Node, List<HubLabel>> safeCopy = new HashMap<>();
        labelsByNode.forEach((node, labels) -> safeCopy.put(node, List.copyOf(labels)));
        this.labelsByNode = Map.copyOf(safeCopy);
    }

    /**
     * Calculates a route by intersecting the labels of the start and end nodes.
     *
     * <p>Hub Label query logic:
     * find hubs reachable from both nodes, choose the shared hub with the lowest
     * {@code startToHub + endToHub}, then return that low-cost result. For this
     * educational project, the path is represented as {@code start -> hub -> end}
     * with duplicates removed. Full production hub labeling stores extra path
     * metadata for exact turn-by-turn reconstruction.</p>
     *
     * @param start starting location
     * @param end destination location
     * @return route using the best common hub, or an infinite route when no hub
     * exists
     */
    @Override
    public Route calculateRoute(Location start, Location end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");

        System.out.println("Running Hub Label shortest path...");

        Node startNode = graph.findNearestNode(start);
        Node endNode = graph.findNearestNode(end);

        if (startNode.equals(endNode)) {
            return new Route(List.of(startNode), 0, 0);
        }

        BestHub bestHub = findBestCommonHub(startNode, endNode);

        if (bestHub == null) {
            return new Route(List.of(), Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        }

        List<Location> path = buildEducationalPath(startNode, bestHub.hub(), endNode);
        return new Route(path, bestHub.totalDistance(), bestHub.totalDistance());
    }

    /**
     * Finds the shared hub with the lowest combined distance.
     *
     * <p>Use maps here so comparing common hubs is quick and clear: convert the
     * end labels to {@code hub -> distance}, then scan the start labels for hubs
     * that also exist in the end label set.</p>
     *
     * @param startNode start graph node
     * @param endNode end graph node
     * @return best shared hub, or null when no shared hub exists
     */
    private BestHub findBestCommonHub(Node startNode, Node endNode) {
        List<HubLabel> startLabels = labelsByNode.getOrDefault(startNode, List.of());
        List<HubLabel> endLabels = labelsByNode.getOrDefault(endNode, List.of());
        Map<Node, Double> endDistancesByHub = new HashMap<>();

        for (HubLabel endLabel : endLabels) {
            endDistancesByHub.put(endLabel.getHub(), endLabel.getDistance());
        }

        return startLabels.stream()
                .filter(label -> endDistancesByHub.containsKey(label.getHub()))
                .map(label -> new BestHub(
                        label.getHub(),
                        label.getDistance() + endDistancesByHub.get(label.getHub())
                ))
                .min(Comparator.comparingDouble(BestHub::totalDistance))
                .orElse(null);
    }

    /**
     * Builds a simple path for display from the selected common hub.
     *
     * <p>Use this method to keep path output readable while the label data stores
     * only distances. Duplicate nodes are removed for cases like
     * {@code start == hub} or {@code hub == end}.</p>
     *
     * @param startNode start graph node
     * @param hub best shared hub
     * @param endNode end graph node
     * @return display path with duplicates removed
     */
    private List<Location> buildEducationalPath(Node startNode, Node hub, Node endNode) {
        List<Location> path = new ArrayList<>();
        path.add(startNode);

        if (!hub.equals(startNode) && !hub.equals(endNode)) {
            path.add(hub);
        }

        if (!endNode.equals(startNode)) {
            path.add(endNode);
        }

        return path;
    }

    private record BestHub(Node hub, double totalDistance) {
    }
}
