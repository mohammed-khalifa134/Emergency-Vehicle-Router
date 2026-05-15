package com.emergencyrouter.model.vehicles;

import com.emergencyrouter.enums.EmergencyType;
import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Report;

import java.util.Objects;

public final class PoliceCar extends Vehicle {

    public PoliceCar(
            String id,
            Location currentLocation
    ) {

        super(id, currentLocation);
    }

    public void providePoliceUnits() {

    }

    @Override
    public boolean canHandle(Report report) {

        return reportTypeMatches(
                report,
                EmergencyType.POLICE
        );
    }

    @Override
    public void respondToReport(Report report) {

        Objects.requireNonNull(report);

        providePoliceUnits();

        markBusy();
    }
}