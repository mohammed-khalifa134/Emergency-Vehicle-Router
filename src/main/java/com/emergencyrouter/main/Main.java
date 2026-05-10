package com.emergencyrouter.main;

import java.util.Date;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.emergencyrouter.factory.VehicleFactory;
import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Graph;
import com.emergencyrouter.model.HubLabel;
import com.emergencyrouter.model.Node;
import com.emergencyrouter.model.Report;
import com.emergencyrouter.model.Route;
import com.emergencyrouter.model.TrafficData;
import com.emergencyrouter.model.vehicles.Vehicle;
import com.emergencyrouter.service.DispatchService;
import com.emergencyrouter.service.HubLabelPreprocessor;
import com.emergencyrouter.service.RoutingService;
import com.emergencyrouter.service.TrafficService;
import com.emergencyrouter.strategy.DijkstraRouteStrategy;
import com.emergencyrouter.strategy.HubLabelRouteStrategy;

/**
 * Starter console entry point for the Emergency Vehicle Router System.
 *
 * <p>Use this class to run the project from the command line or an IDE. The demo
 * shows factory-based vehicle creation, dispatch, Dijkstra routing, Hub Label
 * routing, traffic updates, and observer-triggered route recalculation.</p>
 */
public final class Main {

    private Main() {
        // Utility class: no object is needed to run a console main method.
    }

    /**
     * Runs the full emergency routing console simulation.
     *
     * <p>Use this method as the project demo. It intentionally prints each step
     * so the workflow is easy to follow while learning the design patterns.</p>
     *
     * @param args command-line arguments; currently unused
     */
    public static void main(String[] args) {
        System.out.println("Emergency Vehicle Router System started.");
        System.out.println("Building road graph...");

        SampleRoadNetwork roadNetwork = buildSampleRoadNetwork();
        Graph graph = roadNetwork.graph();

        VehicleFactory vehicleFactory = new VehicleFactory();

        // VehicleFactory owns creation logic, so Main does not depend on concrete constructors.
        Vehicle ambulance = vehicleFactory.createVehicle("AMBULANCE", "AMB-1", roadNetwork.station());
        Vehicle fireTruck = vehicleFactory.createVehicle("FIRE", "FIRE-1", roadNetwork.fireStation());
        Vehicle policeCar = vehicleFactory.createVehicle("POLICE", "POL-1", roadNetwork.policeStation());

        Report report = new Report("R-1", "MEDICAL", roadNetwork.incident(), new Date());
        DispatchService dispatchService = new DispatchService(List.of(ambulance, fireTruck, policeCar));
        RoutingService routingService = new RoutingService(new DijkstraRouteStrategy(graph), graph);
        TrafficService trafficService = new TrafficService();

        // Observer Pattern: traffic service notifies routing service when road data changes.
        trafficService.attach(routingService);

        System.out.println("Emergency report received: " + report.getType());
        System.out.println("Ambulance available: " + ambulance.isAvailable());
        System.out.println("Fire truck available: " + fireTruck.isAvailable());
        System.out.println("Police car available: " + policeCar.isAvailable());

        // DispatchService owns vehicle selection and calls the correct polymorphic response method.
        dispatchService.assignVehicle(report);

        TimedRoute dijkstraRoute = measureRoute(
                "Dijkstra shortest path",
                () -> routingService.calculateRoute(ambulance.getCurrentLocation(), report.getLocation())
        );
        printRoute("Dijkstra route", dijkstraRoute.route());

        System.out.println();
        System.out.println("Preprocessing Hub Labels...");
        long preprocessingStart = System.nanoTime();
        Map<Node, List<HubLabel>> labels = new HubLabelPreprocessor(graph).preprocess();
        double preprocessingMillis = elapsedMillis(preprocessingStart);
        System.out.printf("Hub Label preprocessing finished in %.3f ms%n", preprocessingMillis);
        printHubLabelRoutes(labels);

        HubLabelRouteStrategy hubLabelRouteStrategy = new HubLabelRouteStrategy(graph, labels);
        routingService.setStrategy(hubLabelRouteStrategy);

        TimedRoute hubLabelRoute = measureRoute(
                "Hub Label shortest path",
                () -> routingService.calculateRoute(ambulance.getCurrentLocation(), report.getLocation())
        );
        printRoute("Hub Label route", hubLabelRoute.route());
        printComparison(dijkstraRoute, hubLabelRoute);

        System.out.println();
        System.out.println("Switching back to Dijkstra for detailed live rerouting...");
        routingService.setStrategy(new DijkstraRouteStrategy(graph));
        routingService.calculateRoute(ambulance.getCurrentLocation(), report.getLocation());

        System.out.println();
        System.out.println("Traffic update received:");
        System.out.println("Road Station->Downtown closed");
        trafficService.updateTraffic(new TrafficData(
                "Station->Downtown",
                0.0,
                true,
                System.currentTimeMillis()
        ));

        Route updatedRoute = routingService.getCurrentRoute()
                .orElseThrow(() -> new IllegalStateException("route should exist after recalculation"));

        printRoute("Updated emergency route", updatedRoute);
        System.out.println("Alternative path selected.");
        System.out.println("Ambulance status after dispatch: " + ambulance.getStatus());
    }

    /**
     * Builds the sample road network used by the console simulation.
     *
     * <p>Use this helper to keep the main workflow focused on behavior instead
     * of graph setup details.</p>
     *
     * @return sample road network nodes and graph
     */
    private static SampleRoadNetwork buildSampleRoadNetwork() {
        Graph graph = new Graph();

        Node station = new Node("Station", 32.8872, 13.1913);
        Node fireStation = new Node("FireStation", 32.8890, 13.1800);
        Node policeStation = new Node("PoliceStation", 32.8920, 13.2000);
        Node downtown = new Node("Downtown", 32.8950, 13.2050);
        Node bridge = new Node("Bridge", 32.9020, 13.2150);
        Node bypass = new Node("Bypass", 32.8920, 13.2300);
        Node incident = new Node("Incident", 32.9100, 13.2350);

        graph.addBidirectionalEdge(station, downtown, 1.0, 0.0);
        graph.addBidirectionalEdge(downtown, bridge, 1.0, 0.0);
        graph.addBidirectionalEdge(bridge, incident, 1.0, 0.0);
        graph.addBidirectionalEdge(station, bypass, 2.5, 0.0);
        graph.addBidirectionalEdge(bypass, incident, 2.0, 0.0);
        graph.addBidirectionalEdge(fireStation, downtown, 1.2, 0.2);
        graph.addBidirectionalEdge(policeStation, downtown, 1.4, 0.1);

        return new SampleRoadNetwork(graph, station, fireStation, policeStation, incident);
    }

    /**
     * Measures route calculation time.
     *
     * <p>Use this helper to compare Dijkstra and Hub Label query speed in the
     * console output.</p>
     *
     * @param label algorithm label
     * @param routeSupplier route calculation function
     * @return route and elapsed time
     */
    private static TimedRoute measureRoute(String label, Supplier<Route> routeSupplier) {
        long start = System.nanoTime();
        Route route = routeSupplier.get();
        double elapsedMillis = elapsedMillis(start);

        System.out.printf("%s found in %.3f ms%n", label, elapsedMillis);
        return new TimedRoute(route, elapsedMillis);
    }

    /**
     * Prints a readable route summary.
     *
     * <p>Use this helper for all route outputs so Dijkstra, Hub Label, and
     * recalculated routes use the same format.</p>
     *
     * @param title route title
     * @param route route to print
     */
    private static void printRoute(String title, Route route) {
        System.out.println(title + ":");
        System.out.println("Path: " + formatPath(route));
        System.out.printf("Distance: %.2f%n", route.getDistance());
        System.out.printf("Estimated cost/time: %.2f%n", route.getTime());
    }

    /**
     * Prints a short algorithm comparison.
     *
     * <p>Use this helper to show that both strategies found the same cost while
     * using different query approaches.</p>
     *
     * @param dijkstraRoute Dijkstra measured route
     * @param hubLabelRoute Hub Label measured route
     */
    private static void printComparison(TimedRoute dijkstraRoute, TimedRoute hubLabelRoute) {
        System.out.println();
        System.out.println("Route comparison:");
        System.out.printf("Dijkstra query time: %.3f ms%n", dijkstraRoute.elapsedMillis());
        System.out.printf("Hub Label query time: %.3f ms%n", hubLabelRoute.elapsedMillis());
        System.out.printf("Both route costs: %.2f vs %.2f%n",
                dijkstraRoute.route().getTime(),
                hubLabelRoute.route().getTime()
        );
    }

    /**
     * Prints every preprocessed Hub Label route cost.
     *
     * <p>Use this method to make the Hub Label preprocessing visible in the
     * console demo. Each line means: from this source node, the system already
     * knows the cost to reach each listed hub.</p>
     *
     * @param labels preprocessed hub labels grouped by source node
     */
    private static void printHubLabelRoutes(Map<Node, List<HubLabel>> labels) {
        System.out.println();
        System.out.println("Hub Label precomputed routes:");

        labels.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(Node::getId)))
                .forEach(entry -> {
                    String hubs = entry.getValue().stream()
                            .sorted(Comparator.comparing(label -> label.getHub().getId()))
                            .map(label -> "%s(%.2f)".formatted(
                                    label.getHub().getId(),
                                    label.getDistance()
                            ))
                            .collect(Collectors.joining(", "));

                    System.out.println(entry.getKey().getId() + " -> " + hubs);
                });
    }

    /**
     * Formats route locations as readable text.
     *
     * <p>Use node ids when the route comes from graph algorithms and coordinates
     * when a route contains plain locations.</p>
     *
     * @param route route to format
     * @return printable path
     */
    private static String formatPath(Route route) {
        if (route.getPath().isEmpty()) {
            return "No route available";
        }

        return route.getPath().stream()
                .map(Main::formatLocation)
                .collect(Collectors.joining(" -> "));
    }

    private static String formatLocation(Location location) {
        if (location instanceof Node node) {
            return node.getId();
        }

        return String.format("(%.4f, %.4f)", location.getLatitude(), location.getLongitude());
    }

    private static double elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000.0;
    }

    private record SampleRoadNetwork(
            Graph graph,
            Node station,
            Node fireStation,
            Node policeStation,
            Node incident
    ) {
    }

    private record TimedRoute(Route route, double elapsedMillis) {
    }
}
