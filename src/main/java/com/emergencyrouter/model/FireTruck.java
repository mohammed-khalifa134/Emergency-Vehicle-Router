package com.emergencyrouter.model;

import com.emergencyrouter.interfaces.Location;

import java.util.Objects;

/**
 * Emergency vehicle specialized for fire reports.
 *
 * <p>Use this class when the system needs firefighters at an incident. It keeps
 * fire-specific behavior out of the base {@link Vehicle} class.</p>
 */
public final class FireTruck extends Vehicle {

    /**
     * Creates a fire truck.
     *
     * @param id unique fire truck identifier
     * @param currentLocation starting location
     */
    public FireTruck(String id, Location currentLocation) {
        super(id, currentLocation);
    }

    /**
     * Provides firefighters for a fire emergency.
     *
     * <p>Use this method from the fire-truck response workflow to show the
     * vehicle-specific capability from the UML diagram.</p>
     */
    public void provideFirefighters() {
        System.out.println("Fire truck " + getId() + " is providing firefighters.");
    }

    /**
     * Checks whether this fire truck can handle the report.
     *
     * <p>Use this method from {@code DispatchService}. Fire aliases live with
     * the fire truck instead of inside dispatch selection logic.</p>
     *
     * @param report emergency report to evaluate
     * @return true for fire emergency reports
     */
    @Override
    public boolean canHandle(Report report) {
        return reportTypeMatches(report, "FIRE", "FIRE_TRUCK", "FIRETRUCK");
    }

    /**
     * Responds to a fire report and marks the fire truck busy.
     *
     * <p>Use this method after dispatch selects the fire truck.</p>
     *
     * @param report emergency report to handle
     */
    @Override
    public void respondToReport(Report report) {
        Objects.requireNonNull(report, "report must not be null");
        provideFirefighters();
        markBusy();
        System.out.println("Fire truck " + getId() + " responding to report " + report.getId() + ".");
    }
}
