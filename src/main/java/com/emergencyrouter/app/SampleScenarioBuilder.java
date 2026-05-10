package com.emergencyrouter.app;

import java.util.List;

import com.emergencyrouter.factory.VehicleFactory;
import com.emergencyrouter.model.Graph;
import com.emergencyrouter.model.Node;
import com.emergencyrouter.model.vehicles.Vehicle;
import com.emergencyrouter.service.RoutingService;
import com.emergencyrouter.service.TrafficService;
import com.emergencyrouter.strategy.DijkstraRouteStrategy;

/**
 * Builds reusable in-memory demo data for the emergency router application.
 *
 * <p>Use this builder for the Swing UI startup so the interface opens with a
 * complete road network and fleet ready to explore.</p>
 *
 * <p>Impact on the system: this file gives the Swing app a ready-to-use sample
 * city without touching the console {@code Main}. It creates the graph,
 * emergency stations, demo incident node, default vehicles, routing service,
 * and traffic service in one place.</p>
 *
 * <p>Main methods in this file:</p>
 * <ul>
 *     <li>{@link #buildDefaultState()} creates the full in-memory scenario used
 *     by {@code SwingMain}.</li>
 * </ul>
 */
public final class SampleScenarioBuilder {

    private SampleScenarioBuilder() {
        // Utility class: scenario setup does not require an object instance.
    }

    /**
     * Creates the default application state used by the Swing app.
     *
     * @return initialized in-memory state
     */
    public static ApplicationState buildDefaultState() {
        Graph graph = new Graph();

        Node station = new Node("Station", 32.8872, 13.1913);
        Node fireStation = new Node("FireStation", 32.8890, 13.1800);
        Node policeStation = new Node("PoliceStation", 32.8920, 13.2000);
        Node downtown = new Node("Downtown", 32.8950, 13.2050);
        Node bridge = new Node("Bridge", 32.9020, 13.2150);
        Node bypass = new Node("Bypass", 32.8920, 13.2300);
        Node incident = new Node("Incident", 32.9100, 13.2350);
        Node hospital = new Node("Hospital", 32.9060, 13.2000);
        Node university = new Node("University", 32.9000, 13.1900);
        Node airport = new Node("Airport", 32.8800, 13.2450);
        Node harbor = new Node("Harbor", 32.9180, 13.2200);

        graph.addBidirectionalEdge(station, downtown, 1.0, 0.0);
        graph.addBidirectionalEdge(downtown, bridge, 1.0, 0.0);
        graph.addBidirectionalEdge(bridge, incident, 1.0, 0.0);
        graph.addBidirectionalEdge(station, bypass, 2.5, 0.0);
        graph.addBidirectionalEdge(bypass, incident, 2.0, 0.0);
        graph.addBidirectionalEdge(fireStation, downtown, 1.2, 0.2);
        graph.addBidirectionalEdge(policeStation, downtown, 1.4, 0.1);

        // Extra demo roads make the Swing map and route alternatives richer.
        graph.addBidirectionalEdge(station, university, 3.2, 0.3);
        graph.addBidirectionalEdge(university, downtown, 2.1, 0.2);
        graph.addBidirectionalEdge(university, hospital, 2.2, 0.4);
        graph.addBidirectionalEdge(downtown, hospital, 2.2, 0.3);
        graph.addBidirectionalEdge(hospital, bridge, 1.8, 0.2);
        graph.addBidirectionalEdge(hospital, harbor, 1.8, 0.3);
        graph.addBidirectionalEdge(bridge, harbor, 2.0, 0.2);
        graph.addBidirectionalEdge(harbor, incident, 2.6, 0.4);
        graph.addBidirectionalEdge(policeStation, airport, 2.4, 0.5);
        graph.addBidirectionalEdge(airport, bypass, 2.6, 0.2);
        graph.addBidirectionalEdge(airport, incident, 4.0, 0.6);

        VehicleFactory vehicleFactory = new VehicleFactory();
        List<Vehicle> vehicles = List.of(
                vehicleFactory.createVehicle("AMBULANCE", "AMB-1", station),
                vehicleFactory.createVehicle("FIRE", "FIRE-1", fireStation),
                vehicleFactory.createVehicle("POLICE", "POL-1", policeStation)
        );

        RoutingService routingService = new RoutingService(new DijkstraRouteStrategy(graph), graph);
        TrafficService trafficService = new TrafficService();
        trafficService.attach(routingService);

        ApplicationState state = new ApplicationState(
                graph,
                vehicles,
                routingService,
                trafficService,
                "Dijkstra"
        );
        state.addLog("Sample scenario loaded.");
        return state;
    }
}
