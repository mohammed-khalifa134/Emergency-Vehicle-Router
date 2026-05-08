package com.emergencyrouter.main;

import java.util.Date;
import java.util.List;

import com.emergencyrouter.factory.VehicleFactory;
import com.emergencyrouter.model.Coordinate;
import com.emergencyrouter.model.Report;
import com.emergencyrouter.model.Route;
import com.emergencyrouter.model.Vehicle;
import com.emergencyrouter.service.DispatchService;

/**
 * Starter console entry point for the Emergency Vehicle Router System.
 *
 * <p>Use this class to run the project from the command line or an IDE. The
 * current version demonstrates the Phase 3 factory and dispatch workflow. Later
 * phases will expand this workflow to use route strategies, graph algorithms,
 * and traffic observer updates.</p>
 */
public final class Main {

    private Main() {
        // Utility class: no object is needed to run a console main method.
    }

    /**
     * Runs a small demo of the current domain model.
     *
     * <p>Use this method as the starting point for manual testing while the
     * system is built phase by phase.</p>
     *
     * @param args command-line arguments; currently unused
     */
    public static void main(String[] args) {
        System.out.println("Emergency Vehicle Router System started.");

        Coordinate ambulanceLocation = new Coordinate(32.8872, 13.1913);
        Coordinate fireStationLocation = new Coordinate(32.8890, 13.1800);
        Coordinate policeStationLocation = new Coordinate(32.8920, 13.2000);
        Coordinate incidentLocation = new Coordinate(32.8950, 13.2100);

        VehicleFactory vehicleFactory = new VehicleFactory();

        // VehicleFactory owns creation logic, so Main does not depend on concrete constructors.
        Vehicle ambulance = vehicleFactory.createVehicle("AMBULANCE", "AMB-1", ambulanceLocation);
        Vehicle fireTruck = vehicleFactory.createVehicle("FIRE", "FIRE-1", fireStationLocation);
        Vehicle policeCar = vehicleFactory.createVehicle("POLICE", "POL-1", policeStationLocation);

        Report report = new Report("R-1", "MEDICAL", incidentLocation, new Date());
        DispatchService dispatchService = new DispatchService(List.of(ambulance, fireTruck, policeCar));

        System.out.println("Emergency report received: " + report.getType());
        System.out.println("Ambulance available: " + ambulance.isAvailable());
        System.out.println("Fire truck available: " + fireTruck.isAvailable());
        System.out.println("Police car available: " + policeCar.isAvailable());
 
        // DispatchService owns vehicle selection and calls the correct polymorphic response method.
        dispatchService.assignVehicle(report);

        Route starterRoute = new Route(
                List.of(ambulance.getCurrentLocation(), report.getLocation()),
                2.5,
                6.0
        );

        System.out.println("Starter route created.");
        System.out.println("Route points: " + starterRoute.getPath().size());
        System.out.println("Route distance: " + starterRoute.getDistance());
        System.out.println("Estimated time: " + starterRoute.getTime());
        System.out.println("Ambulance status after dispatch: " + ambulance.getStatus());
    }
}
