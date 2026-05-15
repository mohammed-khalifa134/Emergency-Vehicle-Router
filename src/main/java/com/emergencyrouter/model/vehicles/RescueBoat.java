package com.emergencyrouter.model.vehicles;

import com.emergencyrouter.enums.EmergencyType;
import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Report;

import java.util.Objects;

public final class RescueBoat extends Vehicle {

    public RescueBoat(
            String id,
            Location currentLocation
    ) {

        super(id, currentLocation);
    }

    public void waterRescue() {

    }

    @Override
    public boolean canHandle(Report report) {

        return reportTypeMatches(
                report,
                EmergencyType.ACCIDENT
        );
    }

    @Override
    public void respondToReport(Report report) {

        Objects.requireNonNull(report);

        waterRescue();

        markBusy();
    }
}