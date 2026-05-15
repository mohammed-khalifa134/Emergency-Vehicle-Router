package com.emergencyrouter.model.vehicles;

import com.emergencyrouter.enums.EmergencyType;
import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Report;

import java.util.Objects;

public final class Helicopter extends Vehicle {

    public Helicopter(
            String id,
            Location currentLocation
    ) {

        super(id, currentLocation);
    }

    public void airSupport() {

    }

    @Override
    public boolean canHandle(Report report) {

        return reportTypeMatches(
                report,
                EmergencyType.MEDICAL,
                EmergencyType.FIRE,
                EmergencyType.ACCIDENT
        );
    }

    @Override
    public void respondToReport(Report report) {

        Objects.requireNonNull(report);

        airSupport();

        markBusy();
    }
}