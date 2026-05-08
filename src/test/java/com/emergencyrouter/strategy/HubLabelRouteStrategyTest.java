package com.emergencyrouter.strategy;

import com.emergencyrouter.model.Graph;
import com.emergencyrouter.model.HubLabel;
import com.emergencyrouter.model.Node;
import com.emergencyrouter.model.Route;
import com.emergencyrouter.service.HubLabelPreprocessor;
import junit.framework.TestCase;

import java.util.List;
import java.util.Map;

/**
 * Tests Hub Label preprocessing and route queries.
 */
public class HubLabelRouteStrategyTest extends TestCase {

    /**
     * Verifies preprocessing creates labels for graph nodes.
     */
    public void testPreprocessorCreatesLabelsForNodes() {
        SampleGraph sample = createSampleGraph();
        Map<Node, List<HubLabel>> labels = new HubLabelPreprocessor(sample.graph()).preprocess();

        assertTrue(labels.containsKey(sample.a()));
        assertTrue(labels.containsKey(sample.b()));
        assertTrue(labels.containsKey(sample.c()));
        assertFalse(labels.get(sample.a()).isEmpty());
    }

    /**
     * Verifies Hub Label route cost matches Dijkstra's effective cost.
     */
    public void testHubLabelRouteMatchesDijkstraCost() {
        SampleGraph sample = createSampleGraph();
        Map<Node, List<HubLabel>> labels = new HubLabelPreprocessor(sample.graph()).preprocess();

        Route dijkstraRoute = new DijkstraRouteStrategy(sample.graph()).calculateRoute(sample.a(), sample.c());
        Route hubLabelRoute = new HubLabelRouteStrategy(sample.graph(), labels).calculateRoute(sample.a(), sample.c());

        assertEquals(dijkstraRoute.getTime(), hubLabelRoute.getTime(), 0.0001);
        assertFalse(hubLabelRoute.getPath().isEmpty());
        assertEquals(sample.a(), hubLabelRoute.getPath().getFirst());
        assertEquals(sample.c(), hubLabelRoute.getPath().getLast());
    }

    /**
     * Verifies refreshed labels reflect updated traffic costs.
     */
    public void testRefreshLabelsReflectsTrafficChanges() {
        SampleGraph sample = createSampleGraph();
        HubLabelPreprocessor preprocessor = new HubLabelPreprocessor(sample.graph());
        HubLabelRouteStrategy strategy = new HubLabelRouteStrategy(sample.graph(), preprocessor.preprocess());

        Route beforeTraffic = strategy.calculateRoute(sample.a(), sample.c());

        sample.graph().updateTraffic("A->B", 10.0, false);
        sample.graph().updateTraffic("B->A", 10.0, false);
        strategy.refreshLabels(preprocessor.preprocess());

        Route afterTraffic = strategy.calculateRoute(sample.a(), sample.c());

        assertEquals(2.0, beforeTraffic.getTime(), 0.0001);
        assertEquals(5.0, afterTraffic.getTime(), 0.0001);
    }

    /**
     * Verifies no shared hub returns an infinite route.
     */
    public void testNoCommonHubReturnsInfiniteRoute() {
        Graph graph = new Graph();
        Node a = new Node("A", 32.8800, 13.1900);
        Node b = new Node("B", 32.8900, 13.2000);
        Node isolated = new Node("Isolated", 32.9000, 13.2100);

        graph.addDirectedEdge(a, b, 1.0, 0.0);
        graph.addNode(isolated);

        Map<Node, List<HubLabel>> labels = new HubLabelPreprocessor(graph).preprocess();
        Route route = new HubLabelRouteStrategy(graph, labels).calculateRoute(a, isolated);

        assertTrue(route.getPath().isEmpty());
        assertEquals(Double.POSITIVE_INFINITY, route.getDistance());
        assertEquals(Double.POSITIVE_INFINITY, route.getTime());
    }

    private SampleGraph createSampleGraph() {
        Graph graph = new Graph();
        Node a = new Node("A", 32.8800, 13.1900);
        Node b = new Node("B", 32.8900, 13.2000);
        Node c = new Node("C", 32.9000, 13.2100);

        graph.addBidirectionalEdge(a, b, 1.0, 0.0);
        graph.addBidirectionalEdge(b, c, 1.0, 0.0);
        graph.addBidirectionalEdge(a, c, 5.0, 0.0);

        return new SampleGraph(graph, a, b, c);
    }

    private record SampleGraph(Graph graph, Node a, Node b, Node c) {
    }
}
