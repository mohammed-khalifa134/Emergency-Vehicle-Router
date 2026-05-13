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
 *
 * <p>Use this strategy when routes must consider real road connections,
 * congestion weights, and closed roads. The algorithm is kept here so business
 * services do not need to know pathfinding details.</p>
 */
public final class DijkstraRouteStrategy implements RouteStrategy {
    private final Graph graph;

    /**
     * Creates a Dijkstra strategy for a road graph.
     *
     * <p>Use this constructor during routing setup after the city road network
     * has been built.</p>
     *
     * @param graph road network used for pathfinding
     */
    public DijkstraRouteStrategy(Graph graph) {
        this.graph = Objects.requireNonNull(graph, "graph must not be null");
    }

    /**
     * Calculates the shortest traffic-aware path between two locations.
     *
     * <p>Use this method through {@link com.emergencyrouter.service.RoutingService}.
     * The start and end locations are first snapped to their nearest graph
     * nodes, then Dijkstra searches the graph.</p>
     *
     * @param start starting location
     * @param end destination location
     * @return route containing ordered graph nodes, physical distance, and
     * traffic-aware estimated time/cost
     */
    @Override
    public Route calculateRoute(Location start, Location end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");

        System.out.println("Running Dijkstra shortest path...");

        Node startNode = graph.findNearestNode(start);
        Node endNode = graph.findNearestNode(end);

        if (startNode.equals(endNode)) {
            return new Route(List.of(startNode), 0, 0);
        }

        PathResult result = findShortestPath(startNode, endNode);

        if (result.path().isEmpty()) {
            return new Route(List.of(), Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        }

        return new Route(result.path(), result.physicalDistance(), result.effectiveCost());
    }

    /**
     * Runs Dijkstra's Algorithm from start to destination.
     *
     * <p>Use {@link PriorityQueue} so the next node to explore is always the
     * currently cheapest known node. This avoids scanning all unvisited nodes on
     * every loop and gives the standard efficient Dijkstra behavior.</p>
     *
     * @param startNode source node
     * @param endNode destination node
     * @return shortest path result
     */
    private PathResult findShortestPath(Node startNode, Node endNode) {
        Map<Node, Double> bestCosts = new HashMap<>();
        Map<Node, Edge> previousEdges = new HashMap<>();
        PriorityQueue<NodeDistance> queue = new PriorityQueue<>();

      
        for (Node node : graph.getNodes()) {
            bestCosts.put(node, Double.POSITIVE_INFINITY);
        }

        bestCosts.put(startNode, 0.0);
        queue.add(new NodeDistance(startNode, 0.0));

        
        while (!queue.isEmpty()) {
            NodeDistance current = queue.poll();

            // Ignore stale queue entries left behind after a better path was found.
            if (current.distance() > bestCosts.get(current.node())) {
                continue;
            }

            if (current.node().equals(endNode)) {
                break;
            }

            relaxOutgoingEdges(current.node(), bestCosts, previousEdges, queue);
        }

        // If the destination never received a previous edge, no open route reaches it.
        if (!previousEdges.containsKey(endNode)) {
            return new PathResult(List.of(), Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        }

        return rebuildPath(startNode, endNode, previousEdges, bestCosts.get(endNode));
    }

    /**
     * Tries to improve paths through each outgoing edge.
     *
     * <p>This is the edge relaxation step: if reaching a neighbor through the
     * current node is cheaper than the best known cost, update the best cost and
     * remember which edge led there.</p>
     *
     * @param currentNode node currently being explored
     * @param bestCosts best known cost for each node
     * @param previousEdges edge used to reach each node on the current best path
     * @param queue priority queue of nodes waiting to be explored
     */
    private void relaxOutgoingEdges(
            Node currentNode,
            Map<Node, Double> bestCosts,
            Map<Node, Edge> previousEdges,
            PriorityQueue<NodeDistance> queue
    ) {
        for (Edge edge : graph.getEdgesFrom(currentNode)) {
            // Closed roads are not usable by emergency routing.
            if (edge.isClosed()) {
                continue;
            }

            double candidateCost = bestCosts.get(currentNode) + edge.getEffectiveWeight();
            Node neighbor = edge.getDestination();

            if (candidateCost < bestCosts.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
                bestCosts.put(neighbor, candidateCost);
                previousEdges.put(neighbor, edge);
                queue.add(new NodeDistance(neighbor, candidateCost));
            }
        }
    }

    /**
     * Reconstructs the ordered path from the previous-edge map.
     *
     * <p>Use this after Dijkstra finishes to turn predecessor data into a
     * user-facing route path.</p>
     *
     * @param startNode source node
     * @param endNode destination node
     * @param previousEdges edge used to reach each node
     * @param effectiveCost final traffic-aware cost
     * @return reconstructed path result
     */
    private PathResult rebuildPath(
            Node startNode,
            Node endNode,
            Map<Node, Edge> previousEdges,
            double effectiveCost
    ) {
        List<Location> path = new ArrayList<>();
        double physicalDistance = 0;
        Node current = endNode;

        path.add(current);

        while (!current.equals(startNode)) {
            Edge edge = previousEdges.get(current);

            if (edge == null) {
                return new PathResult(List.of(), Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
            }

            physicalDistance += edge.getDistance();
            current = edge.getSource();
            path.add(current);
        }

        Collections.reverse(path);
        return new PathResult(path, physicalDistance, effectiveCost);
    }

    private record NodeDistance(Node node, double distance) implements Comparable<NodeDistance> {
        @Override
        public int compareTo(NodeDistance other) {
            return Double.compare(distance, other.distance);
        }
    }

    private record PathResult(List<Location> path, double physicalDistance, double effectiveCost) {
    }
}
