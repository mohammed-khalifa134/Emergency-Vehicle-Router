package com.emergencyrouter.strategy;

import com.emergencyrouter.model.Edge;
import com.emergencyrouter.model.Graph;
import com.emergencyrouter.model.Node;
import com.emergencyrouter.model.Route;
import junit.framework.TestCase;

import java.util.List;

/**
 * Tests Dijkstra graph routing behavior.
 */
public class DijkstraRouteStrategyTest extends TestCase {

    /**
     * Verifies Dijkstra chooses the path with the lowest effective cost.
     */
    public void testShortestPathSelectionUsesEffectiveWeight() {
        Graph graph = new Graph();
        Node a = new Node("A", 32.8800, 13.1900);
        Node b = new Node("B", 32.8900, 13.2000);
        Node c = new Node("C", 32.9000, 13.2100);

        graph.addDirectedEdge(a, b, 2.0, 0.0);
        graph.addDirectedEdge(b, c, 2.0, 0.0);
        graph.addDirectedEdge(a, c, 10.0, 0.0);

        Route route = new DijkstraRouteStrategy(graph).calculateRoute(a, c);

        assertEquals(List.of(a, b, c), route.getPath());
        assertEquals(4.0, route.getDistance(), 0.0001);
        assertEquals(4.0, route.getTime(), 0.0001);
    }

    /**
     * Verifies closed roads are skipped during route search.
     */
    public void testClosedRoadIsAvoided() {
        Graph graph = new Graph();
        Node a = new Node("A", 32.8800, 13.1900);
        Node b = new Node("B", 32.8900, 13.2000);
        Node c = new Node("C", 32.9000, 13.2100);

        graph.addDirectedEdge(a, b, 1.0, 0.0);
        Edge closedRoad = graph.addDirectedEdge(b, c, 1.0, 0.0);
        graph.addDirectedEdge(a, c, 5.0, 0.0);
        closedRoad.setClosed(true);

        Route route = new DijkstraRouteStrategy(graph).calculateRoute(a, c);

        assertEquals(List.of(a, c), route.getPath());
        assertEquals(5.0, route.getDistance(), 0.0001);
    }

    /**
     * Verifies unreachable destinations return an empty infinite route.
     */
    public void testUnreachableDestinationReturnsInfiniteRoute() {
        Graph graph = new Graph();
        Node a = new Node("A", 32.8800, 13.1900);
        Node b = new Node("B", 32.8900, 13.2000);
        Node isolated = new Node("Isolated", 32.9000, 13.2100);

        graph.addDirectedEdge(a, b, 1.0, 0.0);
        graph.addNode(isolated);

        Route route = new DijkstraRouteStrategy(graph).calculateRoute(a, isolated);

        assertTrue(route.getPath().isEmpty());
        assertEquals(Double.POSITIVE_INFINITY, route.getDistance());
        assertEquals(Double.POSITIVE_INFINITY, route.getTime());
    }

    /**
     * Verifies traffic weight can make the algorithm choose an alternative road.
     */
    public void testTrafficWeightChangesSelectedRoute() {
        Graph graph = new Graph();
        Node a = new Node("A", 32.8800, 13.1900);
        Node b = new Node("B", 32.8900, 13.2000);
        Node c = new Node("C", 32.9000, 13.2100);

        graph.addDirectedEdge(a, b, 1.0, 10.0);
        graph.addDirectedEdge(b, c, 1.0, 10.0);
        graph.addDirectedEdge(a, c, 5.0, 0.0);

        Route route = new DijkstraRouteStrategy(graph).calculateRoute(a, c);

        assertEquals(List.of(a, c), route.getPath());
        assertEquals(5.0, route.getDistance(), 0.0001);
        assertEquals(5.0, route.getTime(), 0.0001);
    }

    /**
     * Verifies a route from a node to itself is valid and has zero cost.
     */
    public void testSameStartAndEndReturnsZeroRoute() {
        Graph graph = new Graph();
        Node a = new Node("A", 32.8800, 13.1900);
        graph.addNode(a);

        Route route = new DijkstraRouteStrategy(graph).calculateRoute(a, a);

        assertEquals(List.of(a), route.getPath());
        assertEquals(0.0, route.getDistance(), 0.0001);
        assertEquals(0.0, route.getTime(), 0.0001);
    }
}
