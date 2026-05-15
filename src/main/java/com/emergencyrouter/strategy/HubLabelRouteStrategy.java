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
 * Route strategy that answers shortest-path queries using precomputed Hub Labels.
 */
public final class HubLabelRouteStrategy
        implements RouteStrategy {

    private final Graph graph;

    private Map<Node, List<HubLabel>> labelsByNode;

    public HubLabelRouteStrategy(
            Graph graph,
            Map<Node, List<HubLabel>> labelsByNode
    ) {

        this.graph = Objects.requireNonNull(
                graph,
                "graph must not be null"
        );

        refreshLabels(labelsByNode);
    }

    public void refreshLabels(
            Map<Node, List<HubLabel>> labelsByNode
    ) {

        Objects.requireNonNull(
                labelsByNode,
                "labelsByNode must not be null"
        );

        Map<Node, List<HubLabel>> safeCopy =
                new HashMap<>();

        labelsByNode.forEach(
                (node, labels) ->
                        safeCopy.put(
                                node,
                                List.copyOf(labels)
                        )
        );

        this.labelsByNode =
                Map.copyOf(safeCopy);
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
                "Running Hub Label shortest path..."
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

        BestHub bestHub =
                findBestCommonHub(
                        startNode,
                        endNode
                );

        if (bestHub == null) {

            return new Route(
                    List.of(),
                    Double.POSITIVE_INFINITY,
                    Double.POSITIVE_INFINITY
            );
        }

        List<Location> path =
                buildEducationalPath(
                        startNode,
                        bestHub.hub(),
                        endNode
                );

        return new Route(
                path,
                bestHub.totalDistance(),
                bestHub.totalDistance()
        );
    }

    private BestHub findBestCommonHub(
            Node startNode,
            Node endNode
    ) {

        List<HubLabel> startLabels =
                labelsByNode.getOrDefault(
                        startNode,
                        List.of()
                );

        List<HubLabel> endLabels =
                labelsByNode.getOrDefault(
                        endNode,
                        List.of()
                );

        Map<Node, Double> endDistancesByHub =
                new HashMap<>();

        for (HubLabel endLabel : endLabels) {

            endDistancesByHub.put(
                    endLabel.getHub(),
                    endLabel.getDistance()
            );
        }

        return startLabels.stream()
                .filter(label ->
                        endDistancesByHub.containsKey(
                                label.getHub()
                        )
                )
                .map(label ->
                        new BestHub(
                                label.getHub(),
                                label.getDistance()
                                        + endDistancesByHub.get(
                                        label.getHub()
                                )
                        )
                )
                .min(
                        Comparator.comparingDouble(
                                BestHub::totalDistance
                        )
                )
                .orElse(null);
    }

    private List<Location> buildEducationalPath(
            Node startNode,
            Node hub,
            Node endNode
    ) {

        List<Location> path =
                new ArrayList<>();

        path.add(startNode);

        if (!hub.equals(startNode)
                && !hub.equals(endNode)) {

            path.add(hub);
        }

        if (!endNode.equals(startNode)) {

            path.add(endNode);
        }

        return path;
    }

    private record BestHub(
            Node hub,
            double totalDistance
    ) {
    }
}