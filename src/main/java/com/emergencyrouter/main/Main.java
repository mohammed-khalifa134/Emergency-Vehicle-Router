package com.emergencyrouter.main;

import com.emergencyrouter.bootstrap.ApplicationBootstrap;
import com.emergencyrouter.view.swing.EmergencyDashboard;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {

        System.setProperty(
                "http.agent",
                "EmergencyRouter"
        );

        System.setProperty(
                "https.protocols",
                "TLSv1.2"
        );

        SwingUtilities.invokeLater(() -> {

            EmergencyDashboard dashboard =
                    new EmergencyDashboard(
                            ApplicationBootstrap
                                    .createSystem()
                    );

            dashboard.setVisible(true);
        });
    }
}