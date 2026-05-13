package com.emergencyrouter.view.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import com.emergencyrouter.app.ApplicationState;
import com.emergencyrouter.controller.EmergencyRouterController;
import com.emergencyrouter.enums.RoutingAlgorithm;
import com.emergencyrouter.enums.VehicleStatus;
import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Edge;
import com.emergencyrouter.model.Node;
import com.emergencyrouter.model.Report;
import com.emergencyrouter.model.Route;
import com.emergencyrouter.model.vehicles.Vehicle;

/**
 * Main Swing content panel for the emergency router desktop UI.
 *
 * <p>Use this panel inside {@link EmergencyRouterFrame}. It owns Swing widgets,
 * while business actions are delegated to {@link EmergencyRouterController}.</p>
 *
 * <p>Impact on the system: this file provides the complete visible dashboard.
 * It does not own emergency business rules. Instead, it builds forms, tables,
 * tabs, buttons, and summaries, then delegates every meaningful action to
 * {@link EmergencyRouterController}. This keeps the UI clean and makes the
 * controller testable without opening a window.</p>
 *
 * <p>Main methods in this file:</p>
 * <ul>
 *     <li>{@link #EmergencyRouterPanel(EmergencyRouterController)} wires the
 *     controller, state listener, and initial refresh.</li>
 *     <li>{@code createTabs()} builds Dashboard, Reports, Vehicles, Routing,
 *     Traffic, Map, and Logs sections.</li>
 *     <li>{@code startWorkflowFromQuickPanel()},
 *     {@code completeWorkflowFromQuickPanel()},
 *     {@code startWorkflowFromReportsPanel()}, and
 *     {@code completeWorkflowFromReportsPanel()} guide users through report,
 *     vehicle, incident, and routing steps.</li>
 *     <li>{@code createReportsPanel()}, {@code createVehiclesPanel()},
 *     {@code createRoutingPanel()}, {@code createTrafficPanel()}, and
 *     {@code createMapEditorPanel()} build the major user workflows.</li>
 *     <li>{@code refreshAll()}, {@code refreshTables()},
 *     {@code refreshComboBoxes()}, and {@code refreshSummary()} redraw Swing
 *     components from {@link ApplicationState}.</li>
 *     <li>{@code runAction(...)} catches controller errors and shows clean
 *     user-facing messages.</li>
 * </ul>
 */
public final class EmergencyRouterPanel extends JPanel {
    private final EmergencyRouterController controller;
    private final ApplicationState state;
    private final JLabel reportStatusLabel = new JLabel("No report");
    private final JLabel selectedVehicleLabel = new JLabel("No vehicle");
    private final JLabel routeStatusLabel = new JLabel("No route");
    private final JLabel strategyStatusLabel = new JLabel("Dijkstra");
    private final JLabel nextStepLabel = new JLabel("Create a report or run the quick workflow");
    private final JTextField quickReportIdField = new JTextField("R-QUICK-1", 12);
    private final JComboBox<String> quickReportTypeCombo = new JComboBox<>(new String[] {"MEDICAL", "FIRE", "POLICE"});
    private final JComboBox<String> quickVehicleCombo = new JComboBox<>();
    private final JComboBox<String> quickIncidentNodeCombo = new JComboBox<>();
    private final JTextField reportIdField = new JTextField("R-UI-1", 12);
    private final JComboBox<String> reportTypeCombo = new JComboBox<>(new String[] {"MEDICAL", "FIRE", "POLICE"});
    private final JComboBox<String> reportVehicleCombo = new JComboBox<>();
    private final JComboBox<String> incidentNodeCombo = new JComboBox<>();
    private final JComboBox<RoutingAlgorithm> algorithmCombo = new JComboBox<>(RoutingAlgorithm.values());
    private final JTextArea dashboardRouteSummaryArea = readOnlyTextArea(5, 36);
    private final JTextArea reportRouteSummaryArea = readOnlyTextArea(5, 36);
    private final JTextArea routingSummaryArea = readOnlyTextArea(5, 36);
    private final JTextArea logArea = readOnlyTextArea(16, 60);
    private final JTable vehiclesTable = table("ID", "Type", "Status", "Location");
    private final JTable nodesTable = table("ID", "Latitude", "Longitude");
    private final JTable roadsTable = table("Road ID", "Source", "Destination", "Distance", "Traffic", "Closed");
    private final JTable routeStepsTable = table("Step", "Location");
    private final JComboBox<String> vehicleTypeCombo = new JComboBox<>(new String[] {"AMBULANCE", "FIRE", "POLICE"});
    private final JTextField vehicleIdField = new JTextField(12);
    private final JComboBox<String> vehicleLocationCombo = new JComboBox<>();
    private final JComboBox<VehicleStatus> vehicleStatusCombo = new JComboBox<>(VehicleStatus.values());
    private final JTextField nodeIdField = new JTextField(12);
    private final JTextField nodeLatitudeField = new JTextField(8);
    private final JTextField nodeLongitudeField = new JTextField(8);
    private final JComboBox<String> removeNodeCombo = new JComboBox<>();
    private final JComboBox<String> sourceNodeCombo = new JComboBox<>();
    private final JComboBox<String> destinationNodeCombo = new JComboBox<>();
    private final JTextField roadDistanceField = new JTextField(8);
    private final JTextField roadTrafficField = new JTextField(8);
    private final JCheckBox roadClosedCheckBox = new JCheckBox("Closed");
    private final JCheckBox bidirectionalRoadCheckBox = new JCheckBox("Two-way road");
    private final JComboBox<String> roadEditCombo = new JComboBox<>();
    private final JComboBox<String> trafficRoadCombo = new JComboBox<>();
    private final JTextField trafficCongestionField = new JTextField(8);
    private final JCheckBox trafficClosedCheckBox = new JCheckBox("Road closed");
    private final RoadMapPanel dashboardMapPanel = new RoadMapPanel();
    private final RoadMapPanel mapPanel = new RoadMapPanel();

    /**
     * Builds the complete dashboard panel.
     *
     * @param controller controller that handles all business actions
     */
    public EmergencyRouterPanel(EmergencyRouterController controller) {
        super(new BorderLayout(10, 10));
        this.controller = controller;
        this.state = controller.getState();

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(createHeader(), BorderLayout.NORTH);
        add(createTabs(), BorderLayout.CENTER);

        state.addChangeListener(event -> refreshAll());
        configureSelections();
        refreshAll();
    }

    private JPanel createHeader() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Emergency Vehicle Router System"));

        addRow(panel, 0, "Report:", reportStatusLabel);
        addRow(panel, 1, "Vehicle:", selectedVehicleLabel);
        addRow(panel, 2, "Route:", routeStatusLabel);
        addRow(panel, 3, "Strategy:", strategyStatusLabel);
        addRow(panel, 4, "Next:", nextStepLabel);
        return panel;
    }

    private JTabbedPane createTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Dashboard", createDashboardPanel());
        tabs.addTab("Reports", createReportsPanel());
        tabs.addTab("Vehicles", createVehiclesPanel());
        tabs.addTab("Routing", createRoutingPanel());
        tabs.addTab("Traffic", createTrafficPanel());
        tabs.addTab("Map", createMapEditorPanel());
        tabs.addTab("Logs", createLogsPanel());
        return tabs;
    }

    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel workflowPanel = new JPanel(new GridBagLayout());
        workflowPanel.setBorder(BorderFactory.createTitledBorder("Quick Emergency Workflow"));

        addRow(workflowPanel, 0, "Report ID:", quickReportIdField);
        addRow(workflowPanel, 1, "Emergency type:", quickReportTypeCombo);
        addRow(workflowPanel, 2, "Vehicle:", quickVehicleCombo);
        addRow(workflowPanel, 3, "Incident node:", quickIncidentNodeCombo);

        JButton startWorkflowButton = new JButton("1. Start Report");
        JButton completeWorkflowButton = new JButton("2. Select Incident + Calculate");
        JButton closeDemoRoadButton = new JButton("Close Station->Downtown");
        JButton reopenDemoRoadButton = new JButton("Reopen Station->Downtown");
        JButton resetFleetButton = new JButton("Reset Fleet");

        startWorkflowButton.addActionListener(event -> runAction(this::startWorkflowFromQuickPanel));
        completeWorkflowButton.addActionListener(event -> runAction(this::completeWorkflowFromQuickPanel));
        closeDemoRoadButton.addActionListener(event -> runAction(() ->
                applyRoadUpdate("Station->Downtown", 0.0, true)));
        reopenDemoRoadButton.addActionListener(event -> runAction(() ->
                applyRoadUpdate("Station->Downtown", 0.0, false)));
        resetFleetButton.addActionListener(event -> runAction(controller::resetFleetAvailability));

        JPanel quickButtons = new JPanel();
        quickButtons.add(startWorkflowButton);
        quickButtons.add(completeWorkflowButton);
        quickButtons.add(closeDemoRoadButton);
        quickButtons.add(reopenDemoRoadButton);
        quickButtons.add(resetFleetButton);
        addWideRow(workflowPanel, 4, quickButtons);

        JTextArea helpText = readOnlyTextArea(7, 36);
        helpText.setText("""
                New report workflow:
                1. Enter report details and pick the vehicle.
                2. Press Start Report.
                3. Select the incident node.
                4. Press Select Incident + Calculate.
                5. The system calculates the route automatically.

                Detailed controls are still available in the tabs.
                """);

        JPanel leftPanel = new JPanel(new BorderLayout(8, 8));
        leftPanel.add(workflowPanel, BorderLayout.NORTH);
        leftPanel.add(new JScrollPane(helpText), BorderLayout.CENTER);
        leftPanel.add(new JScrollPane(dashboardRouteSummaryArea), BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, dashboardMapPanel);
        splitPane.setResizeWeight(0.25);
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createReportsPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel form = new JPanel(new GridBagLayout());

        addRow(form, 0, "Report ID:", reportIdField);
        addRow(form, 1, "Emergency type:", reportTypeCombo);
        addRow(form, 2, "Vehicle:", reportVehicleCombo);
        addRow(form, 3, "Incident node:", incidentNodeCombo);

        JButton startWorkflowButton = new JButton("1. Start Report + Pick Vehicle");
        JButton completeWorkflowButton = new JButton("2. Select Incident + Calculate");
        JButton resetFleetButton = new JButton("Reset Fleet");

        startWorkflowButton.addActionListener(event -> runAction(this::startWorkflowFromReportsPanel));
        completeWorkflowButton.addActionListener(event -> runAction(this::completeWorkflowFromReportsPanel));
        resetFleetButton.addActionListener(event -> runAction(controller::resetFleetAvailability));

        JPanel buttons = new JPanel();
        buttons.add(startWorkflowButton);
        buttons.add(completeWorkflowButton);
        buttons.add(resetFleetButton);

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(reportRouteSummaryArea), BorderLayout.CENTER);
        panel.add(buttons, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel createVehiclesPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel form = new JPanel(new GridBagLayout());

        addRow(form, 0, "Vehicle type:", vehicleTypeCombo);
        addRow(form, 1, "Vehicle ID:", vehicleIdField);
        addRow(form, 2, "Location:", vehicleLocationCombo);
        addRow(form, 3, "Status:", vehicleStatusCombo);

        JButton addVehicleButton = new JButton("Add Vehicle");
        JButton updateVehicleButton = new JButton("Update Selected Vehicle");
        JButton resetFleetButton = new JButton("Reset Fleet Availability");

        addVehicleButton.addActionListener(event -> runAction(() -> {
            Vehicle vehicle = controller.addVehicle(
                    selectedString(vehicleTypeCombo),
                    vehicleIdField.getText(),
                    selectedString(vehicleLocationCombo)
            );
            controller.updateVehicleStatus(vehicle.getId(), selectedStatus());
        }));
        updateVehicleButton.addActionListener(event -> runAction(() -> {
            String vehicleId = selectedVehicleIdFromTable();
            controller.updateVehicleStatus(vehicleId, selectedStatus());
            controller.updateVehicleLocation(vehicleId, selectedString(vehicleLocationCombo));
        }));
        resetFleetButton.addActionListener(event -> runAction(controller::resetFleetAvailability));

        JPanel buttons = new JPanel();
        buttons.add(addVehicleButton);
        buttons.add(updateVehicleButton);
        buttons.add(resetFleetButton);

        JPanel editor = new JPanel(new BorderLayout());
        editor.add(form, BorderLayout.CENTER);
        editor.add(buttons, BorderLayout.SOUTH);

        panel.add(new JScrollPane(vehiclesTable), BorderLayout.CENTER);
        panel.add(editor, BorderLayout.EAST);
        return panel;
    }

    private JPanel createRoutingPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel form = new JPanel();
        JButton applyStrategyButton = new JButton("Apply Strategy");
        JButton recalculateButton = new JButton("Recalculate Route");

        applyStrategyButton.addActionListener(event -> runAction(() ->
                controller.changeRoutingAlgorithm((RoutingAlgorithm) algorithmCombo.getSelectedItem())));
        recalculateButton.addActionListener(event -> runAction(controller::calculateCurrentRoute));

        form.add(new JLabel("Algorithm:"));
        form.add(algorithmCombo);
        form.add(applyStrategyButton);
        form.add(recalculateButton);

        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(routeStepsTable),
                new JScrollPane(routingSummaryArea)
        );
        splitPane.setResizeWeight(0.45);

        panel.add(form, BorderLayout.NORTH);
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createTrafficPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel form = new JPanel();
        JButton applyTrafficButton = new JButton("Apply Traffic Update");
        JButton closeRoadButton = new JButton("Close Selected Road");
        JButton openRoadButton = new JButton("Open Selected Road");

        applyTrafficButton.addActionListener(event -> runAction(() ->
                applySelectedTrafficUpdate(trafficClosedCheckBox.isSelected())));
        closeRoadButton.addActionListener(event -> runAction(() -> applySelectedTrafficUpdate(true)));
        openRoadButton.addActionListener(event -> runAction(() -> applySelectedTrafficUpdate(false)));

        form.add(new JLabel("Road:"));
        form.add(trafficRoadCombo);
        form.add(new JLabel("Congestion:"));
        form.add(trafficCongestionField);
        form.add(trafficClosedCheckBox);
        form.add(applyTrafficButton);
        form.add(closeRoadButton);
        form.add(openRoadButton);

        panel.add(form, BorderLayout.NORTH);
        panel.add(new JScrollPane(roadsTable), BorderLayout.CENTER);
        return panel;
    }

    private JPanel createMapEditorPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, mapPanel, createGraphEditorPanel());
        splitPane.setResizeWeight(0.72);
        panel.add(splitPane, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createGraphEditorPanel() {
        JPanel editor = new JPanel(new BorderLayout(8, 8));
        editor.setPreferredSize(new Dimension(330, 420));
        editor.add(createNodeEditorPanel(), BorderLayout.NORTH);
        editor.add(createRoadEditorPanel(), BorderLayout.CENTER);

        JScrollPane nodeScrollPane = new JScrollPane(nodesTable);
        nodeScrollPane.setPreferredSize(new Dimension(320, 130));
        editor.add(nodeScrollPane, BorderLayout.SOUTH);
        return editor;
    }

    private JPanel createNodeEditorPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Nodes"));

        addRow(panel, 0, "Node ID:", nodeIdField);
        addRow(panel, 1, "Latitude:", nodeLatitudeField);
        addRow(panel, 2, "Longitude:", nodeLongitudeField);
        addRow(panel, 3, "Remove:", removeNodeCombo);

        JButton addNodeButton = new JButton("Add Node");
        JButton removeNodeButton = new JButton("Remove Node");
        addNodeButton.addActionListener(event -> runAction(() ->
                controller.addNode(
                        nodeIdField.getText(),
                        parseDouble(nodeLatitudeField, "latitude"),
                        parseDouble(nodeLongitudeField, "longitude")
                )));
        removeNodeButton.addActionListener(event -> runAction(() ->
                controller.removeNode(selectedString(removeNodeCombo))));

        JPanel buttons = new JPanel();
        buttons.add(addNodeButton);
        buttons.add(removeNodeButton);
        addWideRow(panel, 4, buttons);
        return panel;
    }

    private JPanel createRoadEditorPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Roads"));

        addRow(panel, 0, "Source:", sourceNodeCombo);
        addRow(panel, 1, "Destination:", destinationNodeCombo);
        addRow(panel, 2, "Distance:", roadDistanceField);
        addRow(panel, 3, "Traffic:", roadTrafficField);
        addRow(panel, 4, "Edit road:", roadEditCombo);
        addWideRow(panel, 5, roadClosedCheckBox);
        addWideRow(panel, 6, bidirectionalRoadCheckBox);

        JButton saveRoadButton = new JButton("Add / Replace Road");
        JButton updateRoadButton = new JButton("Update Selected Road");
        JButton removeRoadButton = new JButton("Remove Selected Road");

        saveRoadButton.addActionListener(event -> runAction(() ->
                controller.addOrUpdateRoad(
                        selectedString(sourceNodeCombo),
                        selectedString(destinationNodeCombo),
                        parseDouble(roadDistanceField, "distance"),
                        parseDouble(roadTrafficField, "traffic"),
                        roadClosedCheckBox.isSelected(),
                        bidirectionalRoadCheckBox.isSelected()
                )));
        updateRoadButton.addActionListener(event -> runAction(() ->
                controller.updateRoad(
                        selectedString(roadEditCombo),
                        parseDouble(roadDistanceField, "distance"),
                        parseDouble(roadTrafficField, "traffic"),
                        roadClosedCheckBox.isSelected()
                )));
        removeRoadButton.addActionListener(event -> runAction(() ->
                controller.removeRoad(selectedString(roadEditCombo))));

        JPanel buttons = new JPanel();
        buttons.add(saveRoadButton);
        buttons.add(updateRoadButton);
        buttons.add(removeRoadButton);
        addWideRow(panel, 7, buttons);
        return panel;
    }

    private JPanel createLogsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(logArea), BorderLayout.CENTER);
        return panel;
    }

    private void configureSelections() {
        vehiclesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roadsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        vehiclesTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                populateVehicleEditorFromSelection();
            }
        });
        roadsTable.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting()) {
                populateRoadEditorFromSelection();
            }
        });
        quickReportTypeCombo.addActionListener(event ->
                selectPreferredVehicleForType(quickVehicleCombo, selectedValueOrNull(quickReportTypeCombo)));
        reportTypeCombo.addActionListener(event ->
                selectPreferredVehicleForType(reportVehicleCombo, selectedValueOrNull(reportTypeCombo)));
    }

    private void refreshAll() {
        refreshComboBoxes();
        refreshTables();
        refreshSummary();
        dashboardMapPanel.setGraph(state.getGraph());
        dashboardMapPanel.setRoute(state.getCurrentRoute().orElse(null));
        mapPanel.setGraph(state.getGraph());
        mapPanel.setRoute(state.getCurrentRoute().orElse(null));
        logArea.setText(String.join(System.lineSeparator(), state.getLogs()));
    }

    private void refreshComboBoxes() {
        List<String> nodeIds = sortedNodes().stream().map(Node::getId).toList();
        List<String> roadIds = sortedEdges().stream().map(Edge::getRoadId).toList();
        List<String> vehicleIds = sortedVehicles().stream().map(Vehicle::getId).toList();

        replaceItems(quickIncidentNodeCombo, nodeIds, "Incident");
        replaceItems(incidentNodeCombo, nodeIds, "Incident");
        replaceItems(quickVehicleCombo, vehicleIds,
                preferredVehicleIdFor(selectedValueOrNull(quickReportTypeCombo)).orElse("AMB-1"));
        replaceItems(reportVehicleCombo, vehicleIds,
                preferredVehicleIdFor(selectedValueOrNull(reportTypeCombo)).orElse("AMB-1"));
        replaceItems(vehicleLocationCombo, nodeIds, "Station");
        replaceItems(removeNodeCombo, nodeIds, null);
        replaceItems(sourceNodeCombo, nodeIds, "Station");
        replaceItems(destinationNodeCombo, nodeIds, "Incident");
        replaceItems(roadEditCombo, roadIds, "Station->Downtown");
        replaceItems(trafficRoadCombo, roadIds, "Station->Downtown");
    }

    private void refreshTables() {
        DefaultTableModel vehicleModel = modelOf(vehiclesTable);
        vehicleModel.setRowCount(0);
        for (Vehicle vehicle : state.getVehicles()) {
            vehicleModel.addRow(new Object[] {
                    vehicle.getId(),
                    vehicle.getClass().getSimpleName(),
                    vehicle.getStatus(),
                    formatLocation(vehicle.getCurrentLocation())
            });
        }

        DefaultTableModel nodeModel = modelOf(nodesTable);
        nodeModel.setRowCount(0);
        for (Node node : sortedNodes()) {
            nodeModel.addRow(new Object[] {
                    node.getId(),
                    String.format("%.4f", node.getLatitude()),
                    String.format("%.4f", node.getLongitude())
            });
        }

        DefaultTableModel roadModel = modelOf(roadsTable);
        roadModel.setRowCount(0);
        for (Edge edge : sortedEdges()) {
            roadModel.addRow(new Object[] {
                    edge.getRoadId(),
                    edge.getSource().getId(),
                    edge.getDestination().getId(),
                    String.format("%.2f", edge.getDistance()),
                    String.format("%.2f", edge.getTrafficWeight()),
                    edge.isClosed()
            });
        }

        DefaultTableModel routeModel = modelOf(routeStepsTable);
        routeModel.setRowCount(0);
        Optional<Route> currentRoute = state.getCurrentRoute();
        if (currentRoute.isPresent()) {
            List<Location> path = currentRoute.get().getPath();
            for (int index = 0; index < path.size(); index++) {
                routeModel.addRow(new Object[] {index + 1, formatLocation(path.get(index))});
            }
        }
    }

    private void refreshSummary() {
        reportStatusLabel.setText(state.getCurrentReport()
                .map(report -> report.getId() + " (" + report.getType() + ")")
                .or(() -> state.getPendingReportId()
                        .map(reportId -> "Pending " + reportId + " ("
                                + state.getPendingEmergencyType().orElse("UNKNOWN") + ")"))
                .orElse("No report"));
        selectedVehicleLabel.setText(state.getSelectedVehicle()
                .map(vehicle -> vehicle.getId() + " - " + vehicle.getStatus())
                .orElse("No vehicle"));
        strategyStatusLabel.setText(state.getSelectedStrategyName());
        routeStatusLabel.setText(state.getCurrentRoute()
                .map(route -> String.format("%.2f distance / %.2f cost", route.getDistance(), route.getTime()))
                .orElse("No route"));
        nextStepLabel.setText(nextStepText());

        String summary = state.getCurrentRoute()
                .map(this::formatRoute)
                .orElse("No route calculated yet.");
        dashboardRouteSummaryArea.setText(summary);
        reportRouteSummaryArea.setText(summary);
        routingSummaryArea.setText(summary);
    }

    private void populateVehicleEditorFromSelection() {
        int selectedRow = vehiclesTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        vehicleIdField.setText(String.valueOf(vehiclesTable.getValueAt(selectedRow, 0)));
        vehicleStatusCombo.setSelectedItem(vehiclesTable.getValueAt(selectedRow, 2));
        vehicleLocationCombo.setSelectedItem(String.valueOf(vehiclesTable.getValueAt(selectedRow, 3)));
    }

    private void populateRoadEditorFromSelection() {
        int selectedRow = roadsTable.getSelectedRow();
        if (selectedRow < 0) {
            return;
        }

        String roadId = String.valueOf(roadsTable.getValueAt(selectedRow, 0));
        roadEditCombo.setSelectedItem(roadId);
        trafficRoadCombo.setSelectedItem(roadId);
        sourceNodeCombo.setSelectedItem(String.valueOf(roadsTable.getValueAt(selectedRow, 1)));
        destinationNodeCombo.setSelectedItem(String.valueOf(roadsTable.getValueAt(selectedRow, 2)));
        roadDistanceField.setText(String.valueOf(roadsTable.getValueAt(selectedRow, 3)));
        roadTrafficField.setText(String.valueOf(roadsTable.getValueAt(selectedRow, 4)));
        trafficCongestionField.setText(String.valueOf(roadsTable.getValueAt(selectedRow, 4)));
        roadClosedCheckBox.setSelected(Boolean.parseBoolean(String.valueOf(roadsTable.getValueAt(selectedRow, 5))));
        trafficClosedCheckBox.setSelected(Boolean.parseBoolean(String.valueOf(roadsTable.getValueAt(selectedRow, 5))));
    }

    private void runAction(Action action) {
        try {
            action.run();
        } catch (RuntimeException exception) {
            JOptionPane.showMessageDialog(
                    this,
                    exception.getMessage(),
                    "Action could not be completed",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void startWorkflowFromQuickPanel() {
        reportIdField.setText(quickReportIdField.getText());
        reportTypeCombo.setSelectedItem(selectedString(quickReportTypeCombo));
        reportVehicleCombo.setSelectedItem(selectedString(quickVehicleCombo));
        controller.startReportWorkflow(
                quickReportIdField.getText(),
                selectedString(quickReportTypeCombo),
                selectedString(quickVehicleCombo)
        );
    }

    private void completeWorkflowFromQuickPanel() {
        incidentNodeCombo.setSelectedItem(selectedString(quickIncidentNodeCombo));
        controller.completeReportWorkflow(selectedString(quickIncidentNodeCombo));
    }

    private void startWorkflowFromReportsPanel() {
        quickReportIdField.setText(reportIdField.getText());
        quickReportTypeCombo.setSelectedItem(selectedString(reportTypeCombo));
        quickVehicleCombo.setSelectedItem(selectedString(reportVehicleCombo));
        controller.startReportWorkflow(
                reportIdField.getText(),
                selectedString(reportTypeCombo),
                selectedString(reportVehicleCombo)
        );
    }

    private void completeWorkflowFromReportsPanel() {
        quickIncidentNodeCombo.setSelectedItem(selectedString(incidentNodeCombo));
        controller.completeReportWorkflow(selectedString(incidentNodeCombo));
    }

    private void applySelectedTrafficUpdate(boolean closed) {
        applyRoadUpdate(
                selectedString(trafficRoadCombo),
                parseDouble(trafficCongestionField, "traffic congestion"),
                closed
        );
        trafficClosedCheckBox.setSelected(closed);
    }

    private void applyRoadUpdate(String roadId, double congestionLevel, boolean closed) {
        controller.applyTrafficUpdate(roadId, congestionLevel, closed);
    }

    private String nextStepText() {
        if (state.getPendingReportId().isPresent() && state.getCurrentReport().isEmpty()) {
            return "Select the incident node, then calculate.";
        }
        if (state.getCurrentReport().isEmpty()) {
            return "Start a report and pick a vehicle.";
        }
        if (state.getSelectedVehicle().isEmpty()) {
            return "Pick a vehicle for this report.";
        }
        if (state.getCurrentRoute().isEmpty()) {
            return "Calculate the route.";
        }
        return "Route ready. Try closing a road or switching algorithms.";
    }

    private List<Node> sortedNodes() {
        return state.getGraph().getNodes().stream()
                .sorted((first, second) -> first.getId().compareToIgnoreCase(second.getId()))
                .toList();
    }

    private List<Edge> sortedEdges() {
        return state.getGraph().getEdges().stream()
                .sorted((first, second) -> first.getRoadId().compareToIgnoreCase(second.getRoadId()))
                .toList();
    }

    private List<Vehicle> sortedVehicles() {
        return state.getVehicles().stream()
                .sorted((first, second) -> first.getId().compareToIgnoreCase(second.getId()))
                .toList();
    }

    private void selectPreferredVehicleForType(JComboBox<String> vehicleCombo, String emergencyType) {
        if (emergencyType == null || vehicleCombo.getItemCount() == 0) {
            return;
        }

        preferredVehicleIdFor(emergencyType).ifPresent(vehicleCombo::setSelectedItem);
    }

    private Optional<String> preferredVehicleIdFor(String emergencyType) {
        if (emergencyType == null || emergencyType.isBlank()) {
            return Optional.empty();
        }

        return state.getVehicles().stream()
                .filter(Vehicle::isAvailable)
                .filter(vehicle -> canVehicleHandleType(vehicle, emergencyType))
                .map(Vehicle::getId)
                .findFirst();
    }

    private boolean canVehicleHandleType(Vehicle vehicle, String emergencyType) {
        Report previewReport = new Report(
                "PREVIEW",
                emergencyType,
                vehicle.getCurrentLocation(),
                new Date()
        );

        return vehicle.canHandle(previewReport);
    }

    private String selectedValueOrNull(JComboBox<?> comboBox) {
        Object selectedItem = comboBox.getSelectedItem();
        return selectedItem == null ? null : selectedItem.toString();
    }

    private String selectedString(JComboBox<?> comboBox) {
        Object selectedItem = comboBox.getSelectedItem();
        if (selectedItem == null || selectedItem.toString().isBlank()) {
            throw new IllegalArgumentException("Select a value first.");
        }
        return selectedItem.toString();
    }

    private VehicleStatus selectedStatus() {
        return (VehicleStatus) vehicleStatusCombo.getSelectedItem();
    }

    private String selectedVehicleIdFromTable() {
        int selectedRow = vehiclesTable.getSelectedRow();
        if (selectedRow < 0) {
            throw new IllegalStateException("Select a vehicle row first.");
        }
        return String.valueOf(vehiclesTable.getValueAt(selectedRow, 0));
    }

    private double parseDouble(JTextField field, String fieldName) {
        try {
            return Double.parseDouble(field.getText().trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Enter a valid number for " + fieldName + ".");
        }
    }

    private String formatRoute(Route route) {
        String path = route.getPath().stream()
                .map(this::formatLocation)
                .collect(Collectors.joining(" -> "));

        if (path.isBlank()) {
            path = "No reachable path";
        }

        return "Path: " + path
                + System.lineSeparator()
                + String.format("Distance: %.2f", route.getDistance())
                + System.lineSeparator()
                + String.format("Estimated cost/time: %.2f", route.getTime());
    }

    private String formatLocation(Location location) {
        if (location instanceof Node node) {
            return node.getId();
        }

        return String.format("(%.4f, %.4f)", location.getLatitude(), location.getLongitude());
    }

    private static JTextArea readOnlyTextArea(int rows, int columns) {
        JTextArea textArea = new JTextArea(rows, columns);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        return textArea;
    }

    private static JTable table(String... columns) {
        JTable table = new JTable(new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        });
        table.setFillsViewportHeight(true);
        return table;
    }

    private static DefaultTableModel modelOf(JTable table) {
        return (DefaultTableModel) table.getModel();
    }

    private static void replaceItems(JComboBox<String> comboBox, List<String> values, String preferredValue) {
        Object selected = comboBox.getSelectedItem();
        comboBox.removeAllItems();
        values.forEach(comboBox::addItem);

        if (selected != null && values.contains(selected.toString())) {
            comboBox.setSelectedItem(selected);
        } else if (preferredValue != null && values.contains(preferredValue)) {
            comboBox.setSelectedItem(preferredValue);
        } else if (!values.isEmpty()) {
            comboBox.setSelectedIndex(0);
        }
    }

    private static void addRow(JPanel panel, int row, String label, java.awt.Component component) {
        GridBagConstraints labelConstraints = new GridBagConstraints();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.anchor = GridBagConstraints.LINE_END;
        labelConstraints.insets = new Insets(4, 4, 4, 4);
        panel.add(new JLabel(label, SwingConstants.RIGHT), labelConstraints);

        GridBagConstraints fieldConstraints = new GridBagConstraints();
        fieldConstraints.gridx = 1;
        fieldConstraints.gridy = row;
        fieldConstraints.weightx = 1;
        fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
        fieldConstraints.insets = new Insets(4, 4, 4, 4);
        panel.add(component, fieldConstraints);
    }

    private static void addWideRow(JPanel panel, int row, java.awt.Component component) {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(4, 4, 4, 4);
        panel.add(component, constraints);
    }

    @FunctionalInterface
    private interface Action {
        void run();
    }
}
