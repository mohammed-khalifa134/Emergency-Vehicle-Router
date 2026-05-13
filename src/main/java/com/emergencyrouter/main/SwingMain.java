package com.emergencyrouter.main;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.emergencyrouter.app.ApplicationState;
import com.emergencyrouter.app.SampleScenarioBuilder;
import com.emergencyrouter.controller.EmergencyRouterController;
import com.emergencyrouter.view.swing.EmergencyRouterFrame;

/**
 * Swing desktop entry point for the Emergency Vehicle Router System.
 *
 * <p>Use this class when you want the visual dashboard instead of the console
 * simulation in {@link Main}.</p>
 *
 * <p>Impact on the system: this file adds a second runnable application mode
 * without replacing the console demo. It starts Swing on the Event Dispatch
 * Thread, loads the sample in-memory scenario, creates the controller, and
 * opens the main dashboard frame.</p>
 *
 * <p>Main methods in this file:</p>
 * <ul>
 *     <li>{@link #main(String[])} starts the desktop UI.</li>
 *     <li>{@code setSystemLookAndFeel()} applies the host operating system's
 *     Swing look and feel when available.</li>
 * </ul>
 */
public final class SwingMain {

    private SwingMain() {
        // Utility class: Swing starts from the static main method.
    }

    /**
     * Starts the Swing application on the Event Dispatch Thread.
     *
     * @param args command-line arguments; currently unused
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            setSystemLookAndFeel();
            ApplicationState state = SampleScenarioBuilder.buildDefaultState();
            EmergencyRouterController controller = new EmergencyRouterController(state);
            new EmergencyRouterFrame(controller).setVisible(true);
        });
    }

    /**
     * Applies the system look and feel for a more native desktop appearance.
     */
    private static void setSystemLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException exception) {
            System.err.println("Using default Swing look and feel: " + exception.getMessage());
        }
    }
}
