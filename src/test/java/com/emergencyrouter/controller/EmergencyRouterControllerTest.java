package com.emergencyrouter.controller;

import java.util.Optional;

import com.emergencyrouter.app.ApplicationState;
import com.emergencyrouter.app.SampleScenarioBuilder;
import com.emergencyrouter.enums.RoutingAlgorithm;
import com.emergencyrouter.enums.VehicleStatus;
import com.emergencyrouter.model.Route;
import com.emergencyrouter.model.vehicles.Vehicle;

import junit.framework.TestCase;

/**
 * Tests the Swing controller workflow without opening a window.
 *
 * <p>Impact on the system: this file protects the new Swing controller from
 * regressions while keeping tests independent from the actual desktop window.
 * It proves the GUI action layer can create reports, dispatch vehicles, switch
 * algorithms, react to traffic, and edit vehicles through the same services the
 * application uses.</p>
 *
 * <p>Main test methods in this file:</p>
 * <ul>
 *     <li>{@link #testCreateReportStoresReportInState()} checks report creation.</li>
 *     <li>{@link #testDispatchCurrentReportSelectsAmbulance()} checks dispatch.</li>
 *     <li>{@link #testChangeRoutingAlgorithmRecalculatesWhenReady()} checks
 *     Strategy Pattern switching.</li>
 *     <li>{@link #testTrafficUpdateRecalculatesRoute()} checks traffic-triggered
 *     rerouting.</li>
 *     <li>{@link #testAddAndUpdateVehicle()} checks fleet editing.</li>
 *     <li>{@link #testDefaultScenarioContainsExpandedRoadNetwork()} checks the
 *     richer sample road map used by Swing.</li>
 * </ul>
 */
public class EmergencyRouterControllerTest extends TestCase {

    /**
     * Verifies reports created by the controller are stored in application state.
     */
    public void testCreateReportStoresReportInState() {
        ApplicationState state = SampleScenarioBuilder.buildDefaultState();
        EmergencyRouterController controller = new EmergencyRouterController(state);

        controller.createReport("R-CTRL-1", "MEDICAL", "Incident");

        assertTrue(state.getCurrentReport().isPresent());
        assertEquals("R-CTRL-1", state.getCurrentReport().get().getId());
    }

    /**
     * Verifies dispatch selects the matching available vehicle.
     */
    public void testDispatchCurrentReportSelectsAmbulance() {
        ApplicationState state = SampleScenarioBuilder.buildDefaultState();
        EmergencyRouterController controller = new EmergencyRouterController(state);

        controller.createReport("R-CTRL-2", "MEDICAL", "Incident");
        Optional<Vehicle> selectedVehicle = controller.dispatchCurrentReport();

        assertTrue(selectedVehicle.isPresent());
        assertEquals("AMB-1", selectedVehicle.get().getId());
        assertEquals(VehicleStatus.BUSY, selectedVehicle.get().getStatus());
    }

    /**
     * Verifies route strategies can be switched through the controller.
     */
    public void testChangeRoutingAlgorithmRecalculatesWhenReady() {
        ApplicationState state = SampleScenarioBuilder.buildDefaultState();
        EmergencyRouterController controller = new EmergencyRouterController(state);

        controller.createReport("R-CTRL-3", "MEDICAL", "Incident");
        controller.dispatchCurrentReport();
        controller.calculateCurrentRoute();
        controller.changeRoutingAlgorithm(RoutingAlgorithm.HUB_LABEL);

        assertEquals("Hub Label", state.getSelectedStrategyName());
        assertTrue(state.getCurrentRoute().isPresent());
    }

    /**
     * Verifies a traffic update recalculates the active route.
     */
    public void testTrafficUpdateRecalculatesRoute() {
        ApplicationState state = SampleScenarioBuilder.buildDefaultState();
        EmergencyRouterController controller = new EmergencyRouterController(state);

        controller.createReport("R-CTRL-4", "MEDICAL", "Incident");
        controller.dispatchCurrentReport();
        Route originalRoute = controller.calculateCurrentRoute();

        controller.applyTrafficUpdate("Station->Downtown", 0.0, true);
        Route updatedRoute = state.getCurrentRoute().get();

        assertEquals(3.0, originalRoute.getDistance(), 0.0001);
        assertEquals(4.5, updatedRoute.getDistance(), 0.0001);
        assertTrue(updatedRoute.getPath().stream().anyMatch(location -> location.toString().equals("Bypass")));
    }

    /**
     * Verifies vehicle editing actions use the factory and update state.
     */
    public void testAddAndUpdateVehicle() {
        ApplicationState state = SampleScenarioBuilder.buildDefaultState();
        EmergencyRouterController controller = new EmergencyRouterController(state);

        Vehicle vehicle = controller.addVehicle("AMBULANCE", "AMB-2", "Station");
        controller.updateVehicleStatus("AMB-2", VehicleStatus.OUT_OF_SERVICE);
        controller.updateVehicleLocation("AMB-2", "Bridge");

        assertEquals("AMB-2", vehicle.getId());
        assertEquals(VehicleStatus.OUT_OF_SERVICE, vehicle.getStatus());
        assertEquals("Bridge", vehicle.getCurrentLocation().toString());
    }

    /**
     * Verifies the default Swing scenario includes the expanded city map.
     */
    public void testDefaultScenarioContainsExpandedRoadNetwork() {
        ApplicationState state = SampleScenarioBuilder.buildDefaultState();

        assertTrue(state.getGraph().getNode("Hospital").isPresent());
        assertTrue(state.getGraph().getNode("University").isPresent());
        assertTrue(state.getGraph().getNode("Airport").isPresent());
        assertTrue(state.getGraph().getNode("Harbor").isPresent());
        assertTrue(state.getGraph().getEdge("Hospital->Harbor").isPresent());
        assertTrue(state.getGraph().getEdge("Airport->Incident").isPresent());
        assertTrue(state.getGraph().getEdges().size() >= 36);
    }
}
