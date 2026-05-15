package com.emergencyrouter.model.vehicles;

import com.emergencyrouter.enums.EmergencyType;
import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Report;

import java.util.Objects;

public final class Drone extends Vehicle {

    public Drone(
            String id,
            Location currentLocation
    ) {

        super(id, currentLocation);
    }

    public void scanArea() {

    }

    @Override
    public boolean canHandle(Report report) {

        return reportTypeMatches(
                report,
                EmergencyType.ACCIDENT,
                EmergencyType.FIRE
        );
    }

    @Override
    public void respondToReport(Report report) {

        Objects.requireNonNull(report);

        scanArea();

        markBusy();
    }
}