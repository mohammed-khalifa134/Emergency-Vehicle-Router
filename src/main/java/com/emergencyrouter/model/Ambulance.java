package com.emergencyrouter.model;

import com.emergencyrouter.interfaces.Location;

import java.util.Objects;

/**
 * Emergency vehicle specialized for medical reports.
 *
 * <p>Use this class when the system needs doctors or medical support at an
 * incident. It satisfies Liskov Substitution because it can be used anywhere a
 * {@link Vehicle} is expected.</p>
 */
public final class Ambulance extends Vehicle {

    /**
     * Creates an ambulance.
     *
     * @param id unique ambulance identifier
     * @param currentLocation starting location
     */
    public Ambulance(String id, Location currentLocation) {
        super(id, currentLocation);
    }

    /**
     * Provides doctors for a medical emergency.
     *
     * <p>Use this method from the ambulance response workflow to show the
     * ambulance-specific capability from the UML diagram.</p>
     */
    public void provideDoctors() {
        System.out.println("Ambulance " + getId() + " is providing doctors.");
    }

    /**
     * Responds to a medical report and marks the ambulance busy.
     *
     * <p>Use this method after dispatch selects the ambulance. It demonstrates
     * polymorphism because the caller can use the {@link Vehicle} type.</p>
     *
     * @param report emergency report to handle
     */
    @Override
    public void respondToReport(Report report) {
        Objects.requireNonNull(report, "report must not be null");
        provideDoctors();
        markBusy();
        System.out.println("Ambulance " + getId() + " responding to report " + report.getId() + ".");
    }
}
