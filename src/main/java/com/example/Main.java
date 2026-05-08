package com.example;

import com.example.factory.VehicleFactory;
import com.example.model.Coordinate;
import com.example.model.Location;
import com.example.model.Report;
import com.example.model.TrafficData;
import com.example.model.Vehicle;
import com.example.service.DispatchService;
import com.example.service.TrafficService;

public class Main {

    public static void main(String[] args) {

        System.out.println("========================================");
        System.out.println("   Emergency Vehicle Router System      ");
        System.out.println("========================================\n");

        // ── 1. Create vehicles using Factory Pattern ──────────────────────
        Vehicle ambulance  = VehicleFactory.createVehicle("ambulance",  "AMB-01", new Coordinate(32.887, 13.191));
        Vehicle fireTruck  = VehicleFactory.createVehicle("firetruck",  "FIR-01", new Coordinate(32.901, 13.175));
        Vehicle policeCar  = VehicleFactory.createVehicle("policecar",  "POL-01", new Coordinate(32.875, 13.210));

        // ── 2. Setup DispatchService ──────────────────────────────────────
        DispatchService dispatchService = new DispatchService();
        dispatchService.addVehicle(ambulance);
        dispatchService.addVehicle(fireTruck);
        dispatchService.addVehicle(policeCar);

        // ── 3. Setup TrafficService + Observer Pattern ────────────────────
        TrafficService trafficService = new TrafficService();
        dispatchService.subscribeToTrafficService(trafficService);

        // ── 4. Scenario 1: Medical emergency ─────────────────────────────
        System.out.println("\n--- Scenario 1: Medical Emergency ---");
        Location accidentLocation1 = new Coordinate(32.895, 13.180);
        Report report1 = new Report("RPT-001", "Medical", accidentLocation1);
        dispatchService.assignVehicle(report1);

        // ── 5. Scenario 2: Fire emergency ─────────────────────────────────
        System.out.println("\n--- Scenario 2: Fire Emergency ---");
        Location accidentLocation2 = new Coordinate(32.910, 13.200);
        Report report2 = new Report("RPT-002", "Fire", accidentLocation2);
        dispatchService.assignVehicle(report2);

        // ── 6. Scenario 3: Traffic update triggers reroute ────────────────
        System.out.println("\n--- Scenario 3: Traffic Update ---");
        TrafficData trafficUpdate = new TrafficData("ROAD-55", 0.85, true);
        trafficService.updateTraffic(trafficUpdate);

        // ── 7. Scenario 4: Security incident ─────────────────────────────
        System.out.println("\n--- Scenario 4: Security Incident ---");
        Location accidentLocation3 = new Coordinate(32.880, 13.195);
        Report report3 = new Report("RPT-003", "Security", accidentLocation3);
        dispatchService.assignVehicle(report3);

        // ── 8. Show final vehicle statuses ────────────────────────────────
        System.out.println("\n========================================");
        System.out.println("        Final Vehicle Statuses          ");
        System.out.println("========================================");
        for (Vehicle v : dispatchService.getVehicles()) {
            System.out.println(v);
        }
    }
}
