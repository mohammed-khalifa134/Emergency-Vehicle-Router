package com.emergencyrouter.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Edge;
import com.emergencyrouter.model.Graph;
import com.emergencyrouter.model.Node;
import com.emergencyrouter.model.Route;

/**
 * Route strategy that uses Dijkstra's Algorithm over the road graph.
 */
public final class DijkstraRouteStrategy
        implements RouteStrategy {

    private final Graph graph;

    public DijkstraRouteStrategy(
            Graph graph
    ) {

        this.graph = Objects.requireNonNull(
                graph,
                "graph must not be null"
        );
    }

    @Override
    public Route calculateRoute(
            Location start,
            Location end
    ) {

        Objects.requireNonNull(
                start,
                "start must not be null"
        );

        Objects.requireNonNull(
                end,
                "end must not be null"
        );

        System.out.println(
                "Running Dijkstra shortest path..."
        );

        Node startNode =
                graph.findNearestNode(start);

        Node endNode =
                graph.findNearestNode(end);

        if (startNode.equals(endNode)) {

            return new Route(
                    List.of(startNode),
                    0,
                    0
            );
        }

        PathResult result =
                findShortestPath(
                        startNode,
                        endNode
                );

        if (result.path().isEmpty()) {

            return new Route(
                    List.of(),
                    Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY
            );
        }

        return new Route(
                result.path(),
                result.physicalDistance(),
                result.effectiveCost()
        );
    }

    private PathResult findShortestPath(
            Node startNode,
            Node endNode
    ) {

        Map<Node, Double> bestCosts =
                new HashMap<>();

        Map<Node, Edge> previousEdges =
                new HashMap<>();

        PriorityQueue<NodeDistance> queue =
                new PriorityQueue<>();

        for (Node node : graph.getNodes()) {

            bestCosts.put(
                    node,
                    Double.POSITIVE_INFINITY
            );
        }

        bestCosts.put(startNode, 0.0);

        queue.add(
                new NodeDistance(
                        startNode,
                        0.0
                )
        );

        while (!queue.isEmpty()) {

            NodeDistance current =
                    queue.poll();

            if (current.distance()
                    > bestCosts.get(current.node())) {

                continue;
            }

            if (current.node().equals(endNode)) {
                break;
            }

            relaxOutgoingEdges(
                    current.node(),
                    bestCosts,
                    previousEdges,
                    queue
            );
        }

        if (!previousEdges.containsKey(endNode)) {

            return new PathResult(
                    List.of(),
                    Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY
            );
        }

        return rebuildPath(
                startNode,
                endNode,
                previousEdges,
                bestCosts.get(endNode)
        );
    }

    private void relaxOutgoingEdges(
            Node currentNode,
            Map<Node, Double> bestCosts,
            Map<Node, Edge> previousEdges,
            PriorityQueue<NodeDistance> queue
    ) {

        for (Edge edge :
                graph.getEdgesFrom(currentNode)) {

            if (edge.isClosed()) {
                continue;
            }

            double candidateCost =
                    bestCosts.get(currentNode)
                            + edge.getEffectiveWeight();

            Node neighbor =
                    edge.getDestination();

            if (candidateCost
                    < bestCosts.getOrDefault(
                    neighbor,
                    Double.POSITIVE_INFINITY
            )) {

                bestCosts.put(
                        neighbor,
                        candidateCost
                );

                previousEdges.put(
                        neighbor,
                        edge
                );

                queue.add(
                        new NodeDistance(
                                neighbor,
                                candidateCost
                        )
                );
            }
        }
    }

    private PathResult rebuildPath(
            Node startNode,
            Node endNode,
            Map<Node, Edge> previousEdges,
            double effectiveCost
    ) {

        List<Location> path =
                new ArrayList<>();

        double physicalDistance = 0;

        Node current = endNode;

        path.add(current);

        while (!current.equals(startNode)) {

            Edge edge =
                    previousEdges.get(current);

            if (edge == null) {

                return new PathResult(
                        List.of(),
                        Double.POSITIVE_INFINITY,
                        Double.POSITIVE_INFINITY
                );
            }

            physicalDistance +=
                    edge.getDistance();

            current = edge.getSource();

            path.add(current);
        }

        Collections.reverse(path);

        return new PathResult(
                path,
                physicalDistance,
                effectiveCost
        );
    }

    private record NodeDistance(
            Node node,
            double distance
    ) implements Comparable<NodeDistance> {

        @Override
        public int compareTo(
                NodeDistance other
        ) {

            return Double.compare(
                    distance,
                    other.distance
            );
        }
    }

    private record PathResult(
            List<Location> path,
            double physicalDistance,
            double effectiveCost
    ) {
    }
}