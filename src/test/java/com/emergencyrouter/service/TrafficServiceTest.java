package com.emergencyrouter.service;

import com.emergencyrouter.model.Graph;
import com.emergencyrouter.model.Node;
import com.emergencyrouter.model.Route;
import com.emergencyrouter.model.TrafficData;
import com.emergencyrouter.strategy.DijkstraRouteStrategy;
import com.emergencyrouter.strategy.HubLabelRouteStrategy;
import junit.framework.TestCase;

/**
 * Tests traffic observer integration.
 */
public class TrafficServiceTest extends TestCase {

    /**
     * Verifies traffic updates are sent to attached observers.
     */
    public void testUpdateTrafficNotifiesAttachedObserver() {
        TrafficService trafficService = new TrafficService();
        int[] updateCount = {0};

        trafficService.attach(data -> updateCount[0]++);
        trafficService.updateTraffic(new TrafficData("A->B", 3.0, false, System.currentTimeMillis()));

        assertEquals(1, updateCount[0]);
        assertEquals("A->B", trafficService.getCurrentData().getRoadId());
    }

    /**
     * Verifies detached observers no longer receive traffic updates.
     */
    public void testDetachStopsObserverNotifications() {
        TrafficService trafficService = new TrafficService();
        int[] updateCount = {0};
        var observer = new com.emergencyrouter.observer.Observer<TrafficData>() {
            @Override
            public void update(TrafficData data) {
                updateCount[0]++;
            }
        };

        trafficService.attach(observer);
        trafficService.detach(observer);
        trafficService.updateTraffic(new TrafficData("A->B", 3.0, false, System.currentTimeMillis()));

        assertEquals(0, updateCount[0]);
    }

    /**
     * Verifies RoutingService recalculates after a road closure.
     */
    public void testRoutingServiceRecalculatesAfterTrafficUpdate() {
        SampleGraph sample = createSampleGraph();
        RoutingService routingService = new RoutingService(
                new DijkstraRouteStrategy(sample.graph()),
                sample.graph()
        );
        TrafficService trafficService = new TrafficService();

        trafficService.attach(routingService);

        Route originalRoute = routingService.calculateRoute(sample.a(), sample.c());
        trafficService.updateTraffic(new TrafficData("A->B", 0.0, true, System.currentTimeMillis()));
        Route recalculatedRoute = routingService.getCurrentRoute().get();

        assertEquals(2.0, originalRoute.getDistance(), 0.0001);
        assertEquals(5.0, recalculatedRoute.getDistance(), 0.0001);
        assertEquals(sample.a(), recalculatedRoute.getPath().getFirst());
        assertEquals(sample.c(), recalculatedRoute.getPath().getLast());
    }

    /**
     * Verifies active Hub Label strategy refreshes labels after traffic changes.
     */
    public void testHubLabelStrategyRefreshesAfterTrafficUpdate() {
        SampleGraph sample = createSampleGraph();
        HubLabelPreprocessor preprocessor = new HubLabelPreprocessor(sample.graph());
        HubLabelRouteStrategy strategy = new HubLabelRouteStrategy(sample.graph(), preprocessor.preprocess());
        RoutingService routingService = new RoutingService(strategy, sample.graph());
        TrafficService trafficService = new TrafficService();

        trafficService.attach(routingService);

        Route originalRoute = routingService.calculateRoute(sample.a(), sample.c());
        trafficService.updateTraffic(new TrafficData("A->B", 10.0, false, System.currentTimeMillis()));
        Route recalculatedRoute = routingService.getCurrentRoute().get();

        assertEquals(2.0, originalRoute.getTime(), 0.0001);
        assertEquals(5.0, recalculatedRoute.getTime(), 0.0001);
    }

    private SampleGraph createSampleGraph() {
        Graph graph = new Graph();
        Node a = new Node("A", 32.8800, 13.1900);
        Node b = new Node("B", 32.8900, 13.2000);
        Node c = new Node("C", 32.9000, 13.2100);

        graph.addDirectedEdge(a, b, 1.0, 0.0);
        graph.addDirectedEdge(b, c, 1.0, 0.0);
        graph.addDirectedEdge(a, c, 5.0, 0.0);

        return new SampleGraph(graph, a, b, c);
    }

    private record SampleGraph(Graph graph, Node a, Node b, Node c) {
    }
}
