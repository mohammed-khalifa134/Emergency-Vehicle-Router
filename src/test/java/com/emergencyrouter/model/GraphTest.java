package com.emergencyrouter.model;

import junit.framework.TestCase;

import java.util.List;

/**
 * Tests graph road-network behavior.
 *
 * <p>Impact on the system: this file verifies the graph operations used by
 * Dijkstra, Hub Labels, traffic updates, and the Swing graph editor. The Swing
 * work added tests for listing edges, replacing roads, removing roads, and
 * removing nodes with connected roads.</p>
 *
 * <p>Main test methods in this file:</p>
 * <ul>
 *     <li>{@link #testAddAndRetrieveNode()} and {@link #testFindNearestNode()}
 *     check node lookup behavior.</li>
 *     <li>{@link #testAddDirectedEdge()},
 *     {@link #testAddBidirectionalEdge()}, and
 *     {@link #testGetEdgesFromReturnsOutgoingRoads()} check road creation.</li>
 *     <li>{@link #testUpdateTrafficChangesRoadState()} checks traffic updates.</li>
 *     <li>{@link #testGetEdgesReturnsAllDirectedRoads()},
 *     {@link #testAddOrReplaceDirectedEdgeReplacesExistingRoad()},
 *     {@link #testRemoveEdgeUpdatesLookupAndAdjacency()}, and
 *     {@link #testRemoveNodeRemovesConnectedRoads()} check graph editing support.</li>
 * </ul>
 */
public class GraphTest extends TestCase {

    /**
     * Verifies nodes can be added and retrieved by id.
     */
    public void testAddAndRetrieveNode() {
        Graph graph = new Graph();
        Node hospital = new Node("Hospital", 32.8872, 13.1913);

        graph.addNode(hospital);

        assertTrue(graph.getNode("Hospital").isPresent());
        assertSame(hospital, graph.getNode("Hospital").get());
    }

    /**
     * Verifies directed edges appear only from source to destination.
     */
    public void testAddDirectedEdge() {
        Graph graph = new Graph();
        Node a = new Node("A", 32.8800, 13.1900);
        Node b = new Node("B", 32.8900, 13.2000);

        Edge edge = graph.addDirectedEdge(a, b, 4.0, 1.5);

        assertEquals("A->B", edge.getRoadId());
        assertEquals(1, graph.getEdgesFrom(a).size());
        assertEquals(0, graph.getEdgesFrom(b).size());
    }

    /**
     * Verifies bidirectional roads create two directed edges.
     */
    public void testAddBidirectionalEdge() {
        Graph graph = new Graph();
        Node a = new Node("A", 32.8800, 13.1900);
        Node b = new Node("B", 32.8900, 13.2000);

        graph.addBidirectionalEdge(a, b, 4.0, 1.5);

        assertTrue(graph.getEdge("A->B").isPresent());
        assertTrue(graph.getEdge("B->A").isPresent());
        assertEquals(1, graph.getEdgesFrom(a).size());
        assertEquals(1, graph.getEdgesFrom(b).size());
    }

    /**
     * Verifies adjacency lists expose outgoing roads.
     */
    public void testGetEdgesFromReturnsOutgoingRoads() {
        Graph graph = new Graph();
        Node a = new Node("A", 32.8800, 13.1900);
        Node b = new Node("B", 32.8900, 13.2000);
        Node c = new Node("C", 32.9000, 13.2100);

        graph.addDirectedEdge(a, b, 4.0, 0.5);
        graph.addDirectedEdge(a, c, 7.0, 0.25);

        List<Edge> outgoing = graph.getEdgesFrom(a);

        assertEquals(2, outgoing.size());
        assertEquals(b, outgoing.get(0).getDestination());
        assertEquals(c, outgoing.get(1).getDestination());
    }

    /**
     * Verifies traffic updates change congestion and closed status.
     */
    public void testUpdateTrafficChangesRoadState() {
        Graph graph = new Graph();
        Node a = new Node("A", 32.8800, 13.1900);
        Node b = new Node("B", 32.8900, 13.2000);

        graph.addDirectedEdge(a, b, 4.0, 0.5);
        graph.updateTraffic("A->B", 6.0, true);

        Edge edge = graph.getEdge("A->B").get();
        assertEquals(6.0, edge.getTrafficWeight(), 0.0001);
        assertTrue(edge.isClosed());
        assertEquals(Double.POSITIVE_INFINITY, edge.getEffectiveWeight());
    }

    /**
     * Verifies all road edges can be listed for UI tables and controls.
     */
    public void testGetEdgesReturnsAllDirectedRoads() {
        Graph graph = new Graph();
        Node a = new Node("A", 32.8800, 13.1900);
        Node b = new Node("B", 32.8900, 13.2000);

        graph.addBidirectionalEdge(a, b, 4.0, 0.5);

        assertEquals(2, graph.getEdges().size());
    }

    /**
     * Verifies replacing a road updates distance without duplicating adjacency.
     */
    public void testAddOrReplaceDirectedEdgeReplacesExistingRoad() {
        Graph graph = new Graph();
        Node a = new Node("A", 32.8800, 13.1900);
        Node b = new Node("B", 32.8900, 13.2000);

        graph.addDirectedEdge(a, b, 4.0, 0.5);
        graph.addOrReplaceDirectedEdge(a, b, 6.0, 1.0, true);

        assertEquals(1, graph.getEdgesFrom(a).size());
        Edge edge = graph.getEdge("A->B").get();
        assertEquals(6.0, edge.getDistance(), 0.0001);
        assertEquals(1.0, edge.getTrafficWeight(), 0.0001);
        assertTrue(edge.isClosed());
    }

    /**
     * Verifies a directed road can be removed cleanly.
     */
    public void testRemoveEdgeUpdatesLookupAndAdjacency() {
        Graph graph = new Graph();
        Node a = new Node("A", 32.8800, 13.1900);
        Node b = new Node("B", 32.8900, 13.2000);

        graph.addDirectedEdge(a, b, 4.0, 0.5);
        boolean removed = graph.removeEdge("A->B");

        assertTrue(removed);
        assertTrue(graph.getEdge("A->B").isEmpty());
        assertEquals(0, graph.getEdgesFrom(a).size());
    }

    /**
     * Verifies removing a node removes connected roads too.
     */
    public void testRemoveNodeRemovesConnectedRoads() {
        Graph graph = new Graph();
        Node a = new Node("A", 32.8800, 13.1900);
        Node b = new Node("B", 32.8900, 13.2000);
        Node c = new Node("C", 32.9000, 13.2100);

        graph.addDirectedEdge(a, b, 4.0, 0.5);
        graph.addDirectedEdge(b, c, 2.0, 0.0);
        graph.addDirectedEdge(c, a, 3.0, 0.0);

        boolean removed = graph.removeNode("B");

        assertTrue(removed);
        assertTrue(graph.getNode("B").isEmpty());
        assertTrue(graph.getEdge("A->B").isEmpty());
        assertTrue(graph.getEdge("B->C").isEmpty());
        assertTrue(graph.getEdge("C->A").isPresent());
    }

    /**
     * Verifies nearest-node lookup connects free coordinates to the graph.
     */
    public void testFindNearestNode() {
        Graph graph = new Graph();
        Node near = new Node("Near", 32.8800, 13.1900);
        Node far = new Node("Far", 33.5000, 14.0000);

        graph.addNode(near);
        graph.addNode(far);

        Node result = graph.findNearestNode(new Coordinate(32.8810, 13.1910));

        assertSame(near, result);
    }
}
