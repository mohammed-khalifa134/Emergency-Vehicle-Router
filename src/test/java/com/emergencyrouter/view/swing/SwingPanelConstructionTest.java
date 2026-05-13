package com.emergencyrouter.view.swing;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.emergencyrouter.app.ApplicationState;
import com.emergencyrouter.app.SampleScenarioBuilder;
import com.emergencyrouter.controller.EmergencyRouterController;

import junit.framework.TestCase;

/**
 * Tests Swing component construction without opening a desktop window.
 *
 * <p>Impact on the system: this file gives lightweight safety coverage for the
 * Swing layer in environments where opening a real window may not be possible.
 * It verifies that the dashboard panel can be constructed and that the map can
 * paint graph/route data into an off-screen image.</p>
 *
 * <p>Main test methods in this file:</p>
 * <ul>
 *     <li>{@link #testEmergencyRouterPanelCanBeConstructed()} checks the main
 *     dashboard panel wiring.</li>
 *     <li>{@link #testRoadMapPanelCanPaintGraph()} checks Java2D map rendering
 *     does not crash.</li>
 * </ul>
 */
public class SwingPanelConstructionTest extends TestCase {

    /**
     * Verifies the main Swing panel can be constructed around the controller.
     */
    public void testEmergencyRouterPanelCanBeConstructed() {
        ApplicationState state = SampleScenarioBuilder.buildDefaultState();
        EmergencyRouterController controller = new EmergencyRouterController(state);

        EmergencyRouterPanel panel = new EmergencyRouterPanel(controller);

        assertTrue(panel.getComponentCount() > 0);
    }

    /**
     * Verifies the map panel can paint the sample graph and route without error.
     */
    public void testRoadMapPanelCanPaintGraph() {
        ApplicationState state = SampleScenarioBuilder.buildDefaultState();
        EmergencyRouterController controller = new EmergencyRouterController(state);

        controller.createReport("R-MAP-1", "MEDICAL", "Incident");
        controller.dispatchCurrentReport();
        controller.calculateCurrentRoute();

        RoadMapPanel mapPanel = new RoadMapPanel();
        mapPanel.setSize(800, 500);
        mapPanel.setGraph(state.getGraph());
        mapPanel.setRoute(state.getCurrentRoute().get());

        BufferedImage image = new BufferedImage(800, 500, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        mapPanel.paint(graphics);
        graphics.dispose();

        assertEquals(800, image.getWidth());
    }
}
