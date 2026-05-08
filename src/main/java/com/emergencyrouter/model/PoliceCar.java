package com.emergencyrouter.model;

import com.emergencyrouter.interfaces.Location;

import java.util.Objects;

/**
 * Emergency vehicle specialized for police reports.
 *
 * <p>Use this class when the system needs police units at an incident. It is a
 * concrete {@link Vehicle} implementation with police-specific response
 * behavior.</p>
 */
public final class PoliceCar extends Vehicle {

    /**
     * Creates a police car.
     *
     * @param id unique police car identifier
     * @param currentLocation starting location
     */
    public PoliceCar(String id, Location currentLocation) {
        super(id, currentLocation);
    }

    /**
     * Provides police units for a security emergency.
     *
     * <p>Use this method from the police response workflow to show the
     * vehicle-specific capability from the UML diagram.</p>
     */
    public void providePoliceUnits() {
        System.out.println("Police car " + getId() + " is providing police units.");
    }

    /**
     * Checks whether this police car can handle the report.
     *
     * <p>Use this method from {@code DispatchService}. Police aliases live with
     * the police car instead of inside dispatch selection logic.</p>
     *
     * @param report emergency report to evaluate
     * @return true for police or security reports
     */
    @Override
    public boolean canHandle(Report report) {
        return reportTypeMatches(report, "POLICE", "POLICE_CAR", "POLICECAR", "SECURITY");
    }

    /**
     * Responds to a police report and marks the police car busy.
     *
     * <p>Use this method after dispatch selects the police car.</p>
     *
     * @param report emergency report to handle
     */
    @Override
    public void respondToReport(Report report) {
        Objects.requireNonNull(report, "report must not be null");
        providePoliceUnits();
        markBusy();
        System.out.println("Police car " + getId() + " responding to report " + report.getId() + ".");
    }
}
