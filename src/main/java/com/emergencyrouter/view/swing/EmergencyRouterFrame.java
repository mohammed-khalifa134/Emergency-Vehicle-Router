package com.emergencyrouter.view.swing;

import javax.swing.JFrame;

import com.emergencyrouter.controller.EmergencyRouterController;

/**
 * Top-level Swing window for the emergency router desktop application.
 *
 * <p>Impact on the system: this file creates the actual desktop window while
 * keeping detailed UI layout inside {@link EmergencyRouterPanel}. That
 * separation keeps startup/window settings away from dashboard behavior.</p>
 *
 * <p>Main method in this file:</p>
 * <ul>
 *     <li>{@link #EmergencyRouterFrame(EmergencyRouterController)} builds the
 *     window, installs the main panel, sets size, and centers it.</li>
 * </ul>
 */
public final class EmergencyRouterFrame extends JFrame {

    /**
     * Creates the application window around the main Swing panel.
     *
     * @param controller controller used by the UI panel
     */
    public EmergencyRouterFrame(EmergencyRouterController controller) {
        super("Emergency Vehicle Router System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(new EmergencyRouterPanel(controller));
        setSize(1180, 760);
        setLocationRelativeTo(null);
    }
}
