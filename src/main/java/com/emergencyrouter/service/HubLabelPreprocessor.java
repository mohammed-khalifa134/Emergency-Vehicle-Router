package com.emergencyrouter.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

import com.emergencyrouter.model.Edge;
import com.emergencyrouter.model.Graph;
import com.emergencyrouter.model.HubLabel;
import com.emergencyrouter.model.Node;

/**
 * Builds Hub Label lookup data from a graph.
 *
 * <p>Use this service once during startup, or again after traffic changes. This
 * educational implementation runs Dijkstra from every node and stores the cost
 * to every reachable hub. That is more preprocessing work, but later route
 * queries become simple label lookups.</p>
 */
public final class HubLabelPreprocessor {
    private final Graph graph;

    /**
     * Creates a preprocessor for a road graph.
     *
     * @param graph graph to preprocess
     */
    public HubLabelPreprocessor(Graph graph) {
        this.graph = Objects.requireNonNull(graph, "graph must not be null");
    }

    /**
     * Generates labels for every graph node.
     *
     * <p>Use this method before creating or refreshing a
     * {@code HubLabelRouteStrategy}. Closed roads are ignored and traffic weight
     * is included through {@link Edge#getEffectiveWeight()}.</p>
     *
     * @return immutable map of node to its hub labels
     */
    public Map<Node, List<HubLabel>> preprocess() {
        Map<Node, List<HubLabel>> labelsByNode = new HashMap<>();

        for (Node source : graph.getNodes()) {
            Map<Node, Double> distances = calculateDistancesFrom(source);
            List<HubLabel> labels = distances.entrySet().stream()
                    .filter(entry -> !Double.isInfinite(entry.getValue()))
                    .map(entry -> new HubLabel(entry.getKey(), entry.getValue()))
                    .toList();

            labelsByNode.put(source, List.copyOf(labels));
        }

        return Map.copyOf(labelsByNode);
    }

    /**
     * Calculates shortest effective cost from one source to all reachable nodes.
     *
     * <p>This is the preprocessing version of Dijkstra. Instead of stopping at
     * one destination, it visits the whole reachable graph so every reachable
     * node can become a hub label for the source.</p>
     *
     * @param source source node
     * @return shortest cost to every graph node
     */
    private Map<Node, Double> calculateDistancesFrom(Node source) {
        Map<Node, Double> distances = new HashMap<>();
        PriorityQueue<NodeDistance> queue = new PriorityQueue<>();

        for (Node node : graph.getNodes()) {
            distances.put(node, Double.POSITIVE_INFINITY);
        }

        distances.put(source, 0.0);
        queue.add(new NodeDistance(source, 0.0));

        while (!queue.isEmpty()) {
            NodeDistance current = queue.poll();

            if (current.distance() > distances.get(current.node())) {
                continue;
            }

            for (Edge edge : graph.getEdgesFrom(current.node())) {
                if (edge.isClosed()) {
                    continue;
                }

                Node neighbor = edge.getDestination();
                double candidate = current.distance() + edge.getEffectiveWeight();

                if (candidate < distances.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
                    distances.put(neighbor, candidate);
                    queue.add(new NodeDistance(neighbor, candidate));
                }
            }
        }

        return distances;
    }

    private record NodeDistance(Node node, double distance) implements Comparable<NodeDistance> {
        @Override
        public int compareTo(NodeDistance other) {
            return Double.compare(distance, other.distance);
        }
    }
}
