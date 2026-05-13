package com.emergencyrouter.service;

import com.emergencyrouter.enums.VehicleStatus;
import com.emergencyrouter.model.Coordinate;
import com.emergencyrouter.model.Report;
import com.emergencyrouter.model.vehicles.Ambulance;
import com.emergencyrouter.model.vehicles.FireTruck;
import com.emergencyrouter.model.vehicles.PoliceCar;
import com.emergencyrouter.model.vehicles.Vehicle;
import junit.framework.TestCase;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Tests emergency vehicle selection rules.
 */
public class DispatchServiceTest extends TestCase {
    private final Coordinate station = new Coordinate(32.8872, 13.1913);
    private final Coordinate incident = new Coordinate(32.8950, 13.2100);

    /**
     * Verifies medical reports select an available ambulance.
     */
    public void testMedicalReportSelectsAmbulance() {
        Ambulance ambulance = new Ambulance("AMB-1", station);
        FireTruck fireTruck = new FireTruck("FIRE-1", station);
        DispatchService dispatchService = new DispatchService(List.of(fireTruck, ambulance));
        Report report = new Report("R-1", "MEDICAL", incident, new Date());

        Optional<Vehicle> selected = dispatchService.selectVehicle(report);

        assertTrue(selected.isPresent());
        assertSame(ambulance, selected.get());
    }

    /**
     * Verifies busy vehicles are skipped during selection.
     */
    public void testBusyVehicleIsSkipped() {
        Ambulance busyAmbulance = new Ambulance("AMB-1", station);
        busyAmbulance.setStatus(VehicleStatus.BUSY);
        Ambulance availableAmbulance = new Ambulance("AMB-2", station);
        DispatchService dispatchService = new DispatchService(List.of(busyAmbulance, availableAmbulance));
        Report report = new Report("R-2", "MEDICAL", incident, new Date());

        Optional<Vehicle> selected = dispatchService.selectVehicle(report);

        assertTrue(selected.isPresent());
        assertSame(availableAmbulance, selected.get());
    }

    /**
     * Verifies police reports select police cars.
     */
    public void testPoliceReportSelectsPoliceCar() {
        PoliceCar policeCar = new PoliceCar("POL-1", station);
        DispatchService dispatchService = new DispatchService(List.of(policeCar));
        Report report = new Report("R-3", "POLICE", incident, new Date());

        Optional<Vehicle> selected = dispatchService.selectVehicle(report);

        assertTrue(selected.isPresent());
        assertSame(policeCar, selected.get());
    }

    /**
     * Verifies no selection is returned when no suitable vehicle exists.
     */
    public void testNoSuitableVehicleReturnsEmptyOptional() {
        FireTruck fireTruck = new FireTruck("FIRE-1", station);
        DispatchService dispatchService = new DispatchService(List.of(fireTruck));
        Report report = new Report("R-4", "MEDICAL", incident, new Date());

        Optional<Vehicle> selected = dispatchService.selectVehicle(report);

        assertTrue(selected.isEmpty());
    }
}
