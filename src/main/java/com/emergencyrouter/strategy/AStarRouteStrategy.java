package com.emergencyrouter.strategy;

import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Edge;
import com.emergencyrouter.model.Graph;
import com.emergencyrouter.model.Node;
import com.emergencyrouter.model.Route;

import java.util.*;

public final class AStarRouteStrategy
        implements RouteStrategy {

    private final Graph graph;

    public AStarRouteStrategy(Graph graph) {

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

        Node startNode = graph.findNearestNode(start);

        Node endNode = graph.findNearestNode(end);

        Map<Node, Double> gScore = new HashMap<>();

        Map<Node, Edge> previous = new HashMap<>();

        PriorityQueue<NodeRecord> open =
                new PriorityQueue<>();

        for (Node node : graph.getNodes()) {
            gScore.put(node, Double.POSITIVE_INFINITY);
        }

        gScore.put(startNode, 0.0);

        open.add(
                new NodeRecord(
                        startNode,
                        heuristic(startNode, endNode)
                )
        );

        while (!open.isEmpty()) {

            Node current = open.poll().node();

            if (current.equals(endNode)) {

                return buildRoute(
                        startNode,
                        endNode,
                        previous,
                        gScore.get(endNode)
                );
            }

            for (Edge edge : graph.getEdgesFrom(current)) {

                if (edge.isClosed()) {
                    continue;
                }

                Node neighbor = edge.getDestination();

                double tentative =
                        gScore.get(current)
                                + edge.getEffectiveWeight();

                if (tentative < gScore.get(neighbor)) {

                    previous.put(neighbor, edge);

                    gScore.put(neighbor, tentative);

                    open.add(
                            new NodeRecord(
                                    neighbor,
                                    tentative
                                            + heuristic(
                                            neighbor,
                                            endNode
                                    )
                            )
                    );
                }
            }
        }

        return new Route(
                List.of(),
                Double.POSITIVE_INFINITY,
                Double.POSITIVE_INFINITY
        );
    }

    private double heuristic(
            Node first,
            Node second
    ) {

        double lat =
                first.getLatitude()
                        - second.getLatitude();

        double lon =
                first.getLongitude()
                        - second.getLongitude();

        return Math.sqrt(lat * lat + lon * lon);
    }

    private Route buildRoute(
            Node start,
            Node end,
            Map<Node, Edge> previous,
            double cost
    ) {

        List<Location> path = new ArrayList<>();

        Node current = end;

        path.add(current);

        while (!current.equals(start)) {

            Edge edge = previous.get(current);

            if (edge == null) {
                break;
            }

            current = edge.getSource();

            path.add(current);
        }

        Collections.reverse(path);

        return new Route(
                path,
                cost,
                cost
        );
    }

    private record NodeRecord(
            Node node,
            double score
    ) implements Comparable<NodeRecord> {

        @Override
        public int compareTo(
                NodeRecord other
        ) {

            return Double.compare(
                    score,
                    other.score
            );
        }
    }
}