package com.example.factory;

import com.example.model.*;

public class VehicleFactory {

    public static Vehicle createVehicle(String type, String id, Location location) {
        switch (type.toLowerCase()) {
            case "ambulance":
                System.out.println("[VehicleFactory] Creating Ambulance with id: " + id);
                return new Ambulance(id, location);
            case "firetruck":
                System.out.println("[VehicleFactory] Creating FireTruck with id: " + id);
                return new FireTruck(id, location);
            case "policecar":
                System.out.println("[VehicleFactory] Creating PoliceCar with id: " + id);
                return new PoliceCar(id, location);
            default:
                throw new IllegalArgumentException("[VehicleFactory] Unknown vehicle type: " + type);
        }
    }
}
