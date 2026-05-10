package com.emergencyrouter.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import com.emergencyrouter.app.ApplicationState;
import com.emergencyrouter.enums.RoutingAlgorithm;
import com.emergencyrouter.enums.VehicleStatus;
import com.emergencyrouter.factory.VehicleFactory;
import com.emergencyrouter.model.Edge;
import com.emergencyrouter.model.Graph;
import com.emergencyrouter.model.HubLabel;
import com.emergencyrouter.model.Node;
import com.emergencyrouter.model.Report;
import com.emergencyrouter.model.Route;
import com.emergencyrouter.model.TrafficData;
import com.emergencyrouter.model.vehicles.Vehicle;
import com.emergencyrouter.service.DispatchService;
import com.emergencyrouter.service.HubLabelPreprocessor;
import com.emergencyrouter.strategy.DijkstraRouteStrategy;
import com.emergencyrouter.strategy.FastestRouteStrategy;
import com.emergencyrouter.strategy.HubLabelRouteStrategy;
import com.emergencyrouter.strategy.RouteStrategy;
import com.emergencyrouter.strategy.ShortestDistanceStrategy;

/**
 * Coordinates Swing UI actions with the existing emergency routing services.
 *
 * <p>Use this controller from button listeners instead of placing business
 * logic inside Swing components. That keeps the user interface focused on
 * display and input handling.</p>
 *
 * <p>Impact on the system: this file is the main connection point between the
 * Swing views and the domain/service layer. It protects the GUI from knowing
 * how dispatch, traffic observation, strategy switching, graph editing, and
 * vehicle creation are implemented.</p>
 *
 * <p>Main methods in this file:</p>
 * <ul>
 *     <li>{@link #createReport(String, String, String)} creates the active
 *     emergency report.</li>
 *     <li>{@link #runEmergencyWorkflow(String, String, String)} performs the
 *     common create-dispatch-route workflow in one action.</li>
 *     <li>{@link #dispatchCurrentReport()} selects and marks a suitable vehicle
 *     as busy.</li>
 *     <li>{@link #calculateCurrentRoute()} calculates and stores the active
 *     route.</li>
 *     <li>{@link #changeRoutingAlgorithm(RoutingAlgorithm)} switches Strategy
 *     Pattern implementations.</li>
 *     <li>{@link #applyTrafficUpdate(String, double, boolean)} triggers the
 *     Observer Pattern traffic workflow.</li>
 *     <li>{@link #addNode(String, double, double)},
 *     {@link #addOrUpdateRoad(String, String, double, double, boolean, boolean)},
 *     {@link #updateRoad(String, double, double, boolean)}, and
 *     {@link #removeRoad(String)} support form-based graph editing.</li>
 *     <li>{@link #addVehicle(String, String, String)},
 *     {@link #updateVehicleStatus(String, VehicleStatus)}, and
 *     {@link #updateVehicleLocation(String, String)} support fleet editing.</li>
 *     <li>{@link #resetFleetAvailability()} makes repeated demos easy by
 *     returning every vehicle to AVAILABLE.</li>
 * </ul>
 */
public final class EmergencyRouterController {
    private final ApplicationState state;
    private final VehicleFactory vehicleFactory;

    /**
     * Creates a controller for a Swing application state.
     *
     * @param state in-memory application state
     */
    public EmergencyRouterController(ApplicationState state) {
        this(state, new VehicleFactory());
    }

    /**
     * Creates a controller with an injectable factory for tests.
     *
     * @param state in-memory application state
     * @param vehicleFactory vehicle creation factory
     */
    public EmergencyRouterController(ApplicationState state, VehicleFactory vehicleFactory) {
        this.state = Objects.requireNonNull(state, "state must not be null");
        this.vehicleFactory = Objects.requireNonNull(vehicleFactory, "vehicleFactory must not be null");
    }

    /**
     * Gets the state object used by views for refreshes and table data.
     *
     * @return application state
     */
    public ApplicationState getState() {
        return state;
    }

    /**
     * Creates and stores a new emergency report.
     *
     * @param reportId report id
     * @param emergencyType emergency type
     * @param incidentNodeId incident node id
     * @return created report
     */
    public Report createReport(String reportId, String emergencyType, String incidentNodeId) {
        Node incidentNode = requireNode(incidentNodeId);
        Report report = new Report(
                requireText(reportId, "reportId"),
                requireText(emergencyType, "emergencyType"),
                incidentNode,
                new Date()
        );

        state.setCurrentReport(report);
        state.clearSelectedVehicle();
        state.clearCurrentRoute();
        state.addLog("Emergency report " + report.getId() + " created for " + report.getType() + ".");
        return report;
    }

    /**
     * Runs the most common emergency workflow in one controller action.
     *
     * <p>Use this from the Swing dashboard when the user wants a fast path:
     * create report, dispatch a vehicle, and calculate the route immediately.</p>
     *
     * @param reportId report id
     * @param emergencyType emergency type
     * @param incidentNodeId incident node id
     * @return calculated route
     */
    public Route runEmergencyWorkflow(String reportId, String emergencyType, String incidentNodeId) {
        Report report = createReport(reportId, emergencyType, incidentNodeId);
        Optional<Vehicle> selectedVehicle = dispatchCurrentReport();

        if (selectedVehicle.isEmpty()) {
            throw new IllegalStateException("No suitable available vehicle found for report " + report.getId() + ".");
        }

        return calculateCurrentRoute();
    }

    /**
     * Selects and dispatches a suitable available vehicle for the current report.
     *
     * @return selected vehicle when dispatch succeeds
     */
    public Optional<Vehicle> dispatchCurrentReport() {
        Report report = state.getCurrentReport()
                .orElseThrow(() -> new IllegalStateException("Create a report before dispatching."));
        DispatchService dispatchService = new DispatchService(state.getVehicles());
        Optional<Vehicle> selectedVehicle = dispatchService.selectVehicle(report);

        if (selectedVehicle.isEmpty()) {
            state.addLog("No suitable available vehicle found for report " + report.getId() + ".");
            return Optional.empty();
        }

        Vehicle vehicle = selectedVehicle.get();
        vehicle.respondToReport(report);
        state.setSelectedVehicle(vehicle);
        state.addLog(vehicle.getId() + " dispatched to report " + report.getId() + ".");
        return Optional.of(vehicle);
    }

    /**
     * Calculates a route for the selected vehicle and current report.
     *
     * @return calculated route
     */
    public Route calculateCurrentRoute() {
        Vehicle vehicle = state.getSelectedVehicle()
                .orElseThrow(() -> new IllegalStateException("Dispatch a vehicle before calculating a route."));
        Report report = state.getCurrentReport()
                .orElseThrow(() -> new IllegalStateException("Create a report before calculating a route."));

        Route route = state.getRoutingService().calculateRoute(
                vehicle.getCurrentLocation(),
                report.getLocation()
        );
        state.setCurrentRoute(route);
        state.addLog("Route calculated with " + state.getSelectedStrategyName() + ".");
        return route;
    }

    /**
     * Switches the active routing algorithm and recalculates when possible.
     *
     * @param algorithm selected algorithm
     */
    public void changeRoutingAlgorithm(RoutingAlgorithm algorithm) {
        RoutingAlgorithm selectedAlgorithm = Objects.requireNonNull(algorithm, "algorithm must not be null");
        RouteStrategy strategy = createStrategy(selectedAlgorithm);

        state.getRoutingService().setStrategy(strategy);
        state.setSelectedStrategyName(selectedAlgorithm.getDisplayName());
        state.addLog("Routing strategy changed to " + selectedAlgorithm.getDisplayName() + ".");
        recalculateIfReady();
    }

    /**
     * Applies a traffic update through the Observer Pattern.
     *
     * @param roadId directed road id
     * @param congestionLevel traffic cost
     * @param closed true when road is closed
     */
    public void applyTrafficUpdate(String roadId, double congestionLevel, boolean closed) {
        requireText(roadId, "roadId");
        state.getTrafficService().updateTraffic(new TrafficData(
                roadId,
                congestionLevel,
                closed,
                System.currentTimeMillis()
        ));
        state.getRoutingService().getCurrentRoute().ifPresent(state::setCurrentRoute);
        state.addLog("Traffic updated for " + roadId + ".");
    }

    public Node addNode(String id, double latitude, double longitude) {
        String nodeId = requireText(id, "id");
        if (state.getGraph().getNode(nodeId).isPresent()) {
            throw new IllegalArgumentException("Node already exists: " + nodeId);
        }

        Node node = new Node(nodeId, latitude, longitude);
        state.getGraph().addNode(node);
        state.addLog("Node " + node.getId() + " added.");
        state.notifyStateChanged();
        return node;
    }

    /**
     * Removes a node and connected roads from the active graph.
     *
     * @param nodeId node to remove
     * @return true when the node existed and was removed
     */
    public boolean removeNode(String nodeId) {
        boolean removed = state.getGraph().removeNode(requireText(nodeId, "nodeId"));
        if (removed) {
            state.clearCurrentRoute();
            state.addLog("Node " + nodeId + " removed.");
        }
        return removed;
    }

    /**
     * Adds or replaces a road from the graph editing form.
     *
     * @param sourceNodeId source node id
     * @param destinationNodeId destination node id
     * @param distance physical road distance
     * @param trafficWeight congestion cost
     * @param closed true when the road should be closed
     * @param bidirectional true to create the reverse road as well
     * @return saved directed edge
     */
    public Edge addOrUpdateRoad(
            String sourceNodeId,
            String destinationNodeId,
            double distance,
            double trafficWeight,
            boolean closed,
            boolean bidirectional
    ) {
        Node source = requireNode(sourceNodeId);
        Node destination = requireNode(destinationNodeId);
        if (source.equals(destination)) {
            throw new IllegalArgumentException("Source and destination must be different nodes.");
        }

        Edge edge = state.getGraph().addOrReplaceDirectedEdge(source, destination, distance, trafficWeight, closed);

        if (bidirectional) {
            state.getGraph().addOrReplaceDirectedEdge(destination, source, distance, trafficWeight, closed);
        }

        state.addLog("Road " + edge.getRoadId() + " saved.");
        state.notifyStateChanged();
        recalculateIfReady();
        return edge;
    }

    /**
     * Updates a selected road while keeping its source and destination.
     *
     * @param roadId selected road id
     * @param distance new physical distance
     * @param trafficWeight new congestion cost
     * @param closed true when the road is closed
     * @return updated edge
     */
    public Edge updateRoad(String roadId, double distance, double trafficWeight, boolean closed) {
        Edge edge = state.getGraph().updateOrReplaceRoad(
                requireText(roadId, "roadId"),
                distance,
                trafficWeight,
                closed
        );
        state.addLog("Road " + edge.getRoadId() + " updated.");
        state.notifyStateChanged();
        recalculateIfReady();
        return edge;
    }

    /**
     * Removes a selected road and clears stale route display.
     *
     * @param roadId selected road id
     * @return true when the road existed and was removed
     */
    public boolean removeRoad(String roadId) {
        boolean removed = state.getGraph().removeEdge(requireText(roadId, "roadId"));
        if (removed) {
            state.clearCurrentRoute();
            state.addLog("Road " + roadId + " removed.");
        }
        return removed;
    }

    /**
     * Adds a vehicle through {@link VehicleFactory}.
     *
     * @param vehicleType vehicle type or alias
     * @param vehicleId unique vehicle id
     * @param nodeId starting node id
     * @return created vehicle
     */
    public Vehicle addVehicle(String vehicleType, String vehicleId, String nodeId) {
        String id = requireText(vehicleId, "vehicleId");
        if (state.findVehicle(id).isPresent()) {
            throw new IllegalArgumentException("Vehicle already exists: " + id);
        }

        Vehicle vehicle = vehicleFactory.createVehicle(
                requireText(vehicleType, "vehicleType"),
                id,
                requireNode(nodeId)
        );
        state.addVehicle(vehicle);
        state.addLog("Vehicle " + vehicle.getId() + " added.");
        return vehicle;
    }

    /**
     * Updates a vehicle's availability status.
     *
     * @param vehicleId vehicle id
     * @param status new status
     */
    public void updateVehicleStatus(String vehicleId, VehicleStatus status) {
        Vehicle vehicle = requireVehicle(vehicleId);
        vehicle.setStatus(Objects.requireNonNull(status, "status must not be null"));
        state.addLog("Vehicle " + vehicle.getId() + " status changed to " + status + ".");
        state.notifyStateChanged();
    }

    /**
     * Moves a vehicle to another graph node.
     *
     * @param vehicleId vehicle id
     * @param nodeId destination node id
     */
    public void updateVehicleLocation(String vehicleId, String nodeId) {
        Vehicle vehicle = requireVehicle(vehicleId);
        Node node = requireNode(nodeId);
        vehicle.updateLocation(node);
        state.addLog("Vehicle " + vehicle.getId() + " moved to " + node.getId() + ".");
        state.notifyStateChanged();
        recalculateIfReady();
    }

    /**
     * Marks every vehicle as available for another demo run.
     */
    public void resetFleetAvailability() {
        state.getVehicles().forEach(vehicle -> vehicle.setStatus(VehicleStatus.AVAILABLE));
        state.addLog("Fleet reset to AVAILABLE.");
        state.notifyStateChanged();
    }

    private RouteStrategy createStrategy(RoutingAlgorithm algorithm) {
        Graph graph = state.getGraph();

        return switch (algorithm) {
            case DIJKSTRA -> new DijkstraRouteStrategy(graph);
            case HUB_LABEL -> {
                Map<Node, List<HubLabel>> labels = new HubLabelPreprocessor(graph).preprocess();
                yield new HubLabelRouteStrategy(graph, labels);
            }
            case FASTEST -> new FastestRouteStrategy();
            case SHORTEST_DISTANCE -> new ShortestDistanceStrategy();
        };
    }

    private void recalculateIfReady() {
        if (state.getSelectedVehicle().isPresent() && state.getCurrentReport().isPresent()) {
            calculateCurrentRoute();
        }
    }

    private Node requireNode(String nodeId) {
        return state.getGraph().getNode(requireText(nodeId, "nodeId"))
                .orElseThrow(() -> new IllegalArgumentException("Unknown node id: " + nodeId));
    }

    private Vehicle requireVehicle(String vehicleId) {
        return state.findVehicle(requireText(vehicleId, "vehicleId"))
                .orElseThrow(() -> new IllegalArgumentException("Unknown vehicle id: " + vehicleId));
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}
