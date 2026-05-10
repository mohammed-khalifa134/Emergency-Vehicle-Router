package com.emergencyrouter.app;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.emergencyrouter.model.Graph;
import com.emergencyrouter.model.Report;
import com.emergencyrouter.model.Route;
import com.emergencyrouter.model.vehicles.Vehicle;
import com.emergencyrouter.service.RoutingService;
import com.emergencyrouter.service.TrafficService;

/**
 * Holds the current in-memory state for the Swing application.
 *
 * <p>Use this class as the bridge between controller actions and Swing views.
 * Views read snapshots from here, while controllers update it after business
 * services finish their work.</p>
 *
 * <p>Impact on the system: this file makes the Swing UI stateful without
 * changing the existing console workflow. It centralizes the active graph,
 * vehicle fleet, current report, selected vehicle, current route, selected
 * strategy name, and visible log messages so every Swing panel refreshes from
 * one source of truth.</p>
 *
 * <p>Main methods in this file:</p>
 * <ul>
 *     <li>{@link #addChangeListener(PropertyChangeListener)} and
 *     {@link #removeChangeListener(PropertyChangeListener)} connect Swing
 *     panels to state changes.</li>
 *     <li>{@link #notifyStateChanged()} tells the UI to redraw tables, labels,
 *     and the map after internal objects change.</li>
 *     <li>{@link #addVehicle(Vehicle)} and {@link #findVehicle(String)} manage
 *     the in-memory vehicle fleet.</li>
 *     <li>{@link #setCurrentReport(Report)}, {@link #setSelectedVehicle(Vehicle)},
 *     and {@link #setCurrentRoute(Route)} store the active emergency workflow.</li>
 *     <li>{@link #addLog(String)} records user-visible system actions.</li>
 * </ul>
 */
public final class ApplicationState {
    private static final DateTimeFormatter LOG_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);
    private final Graph graph;
    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<String> logs = new ArrayList<>();
    private final TrafficService trafficService;
    private RoutingService routingService;
    private Report currentReport;
    private Vehicle selectedVehicle;
    private Route currentRoute;
    private String selectedStrategyName;

    /**
     * Creates the shared GUI state.
     *
     * @param graph active road graph
     * @param vehicles initial vehicle fleet
     * @param routingService routing service used by the controller
     * @param trafficService traffic publisher used by the controller
     * @param selectedStrategyName display name of the initial route strategy
     */
    public ApplicationState(
            Graph graph,
            List<Vehicle> vehicles,
            RoutingService routingService,
            TrafficService trafficService,
            String selectedStrategyName
    ) {
        this.graph = Objects.requireNonNull(graph, "graph must not be null");
        this.vehicles.addAll(Objects.requireNonNull(vehicles, "vehicles must not be null"));
        this.routingService = Objects.requireNonNull(routingService, "routingService must not be null");
        this.trafficService = Objects.requireNonNull(trafficService, "trafficService must not be null");
        this.selectedStrategyName = requireText(selectedStrategyName, "selectedStrategyName");
    }

    /**
     * Registers a listener that should refresh when application state changes.
     *
     * @param listener change listener
     */
    public void addChangeListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(Objects.requireNonNull(listener, "listener must not be null"));
    }

    /**
     * Removes a previously registered listener.
     *
     * @param listener change listener
     */
    public void removeChangeListener(PropertyChangeListener listener) {
        changes.removePropertyChangeListener(listener);
    }

    /**
     * Notifies views that graph or object internals changed.
     */
    public void notifyStateChanged() {
        changes.firePropertyChange("state", null, this);
    }

    /**
     * Gets the active road graph used by routing, traffic, and map views.
     *
     * @return active graph
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * Gets an immutable snapshot of the current vehicle fleet.
     *
     * @return vehicle list snapshot
     */
    public List<Vehicle> getVehicles() {
        return List.copyOf(vehicles);
    }

    /**
     * Adds a vehicle to the in-memory fleet and notifies the UI.
     *
     * @param vehicle vehicle to add
     */
    public void addVehicle(Vehicle vehicle) {
        vehicles.add(Objects.requireNonNull(vehicle, "vehicle must not be null"));
        notifyStateChanged();
    }

    /**
     * Finds a vehicle by id.
     *
     * @param id vehicle id
     * @return matching vehicle, or empty when none exists
     */
    public Optional<Vehicle> findVehicle(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }

        return vehicles.stream()
                .filter(vehicle -> vehicle.getId().equals(id))
                .findFirst();
    }

    /**
     * Gets the current emergency report.
     *
     * @return optional active report
     */
    public Optional<Report> getCurrentReport() {
        return Optional.ofNullable(currentReport);
    }

    /**
     * Stores the current emergency report and refreshes the UI.
     *
     * @param currentReport report to store
     */
    public void setCurrentReport(Report currentReport) {
        this.currentReport = Objects.requireNonNull(currentReport, "currentReport must not be null");
        notifyStateChanged();
    }

    /**
     * Gets the vehicle selected by dispatch.
     *
     * @return optional selected vehicle
     */
    public Optional<Vehicle> getSelectedVehicle() {
        return Optional.ofNullable(selectedVehicle);
    }

    /**
     * Stores the selected vehicle and refreshes the UI.
     *
     * @param selectedVehicle selected dispatch vehicle
     */
    public void setSelectedVehicle(Vehicle selectedVehicle) {
        this.selectedVehicle = Objects.requireNonNull(selectedVehicle, "selectedVehicle must not be null");
        notifyStateChanged();
    }

    /**
     * Clears the selected vehicle when a new report starts.
     */
    public void clearSelectedVehicle() {
        selectedVehicle = null;
        notifyStateChanged();
    }

    /**
     * Gets the most recent calculated route.
     *
     * @return optional current route
     */
    public Optional<Route> getCurrentRoute() {
        return Optional.ofNullable(currentRoute);
    }

    /**
     * Stores the latest route and refreshes route/map views.
     *
     * @param currentRoute route to display
     */
    public void setCurrentRoute(Route currentRoute) {
        this.currentRoute = Objects.requireNonNull(currentRoute, "currentRoute must not be null");
        notifyStateChanged();
    }

    /**
     * Clears the route after graph edits make the old route unsafe to display.
     */
    public void clearCurrentRoute() {
        currentRoute = null;
        notifyStateChanged();
    }

    /**
     * Gets the routing service currently used by the controller.
     *
     * @return active routing service
     */
    public RoutingService getRoutingService() {
        return routingService;
    }

    /**
     * Replaces the routing service and refreshes the UI.
     *
     * @param routingService new routing service
     */
    public void setRoutingService(RoutingService routingService) {
        this.routingService = Objects.requireNonNull(routingService, "routingService must not be null");
        notifyStateChanged();
    }

    /**
     * Gets the traffic publisher used by the observer workflow.
     *
     * @return traffic service
     */
    public TrafficService getTrafficService() {
        return trafficService;
    }

    /**
     * Gets the display name of the selected routing algorithm.
     *
     * @return selected strategy name
     */
    public String getSelectedStrategyName() {
        return selectedStrategyName;
    }

    /**
     * Stores the selected routing algorithm name and refreshes the UI.
     *
     * @param selectedStrategyName strategy display name
     */
    public void setSelectedStrategyName(String selectedStrategyName) {
        this.selectedStrategyName = requireText(selectedStrategyName, "selectedStrategyName");
        notifyStateChanged();
    }

    /**
     * Gets immutable log lines for the Logs tab.
     *
     * @return log snapshot
     */
    public List<String> getLogs() {
        return List.copyOf(logs);
    }

    /**
     * Adds a timestamped log line and refreshes the UI.
     *
     * @param message message to record
     */
    public void addLog(String message) {
        logs.add(LocalTime.now().format(LOG_TIME_FORMAT) + " - " + requireText(message, "message"));
        notifyStateChanged();
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
