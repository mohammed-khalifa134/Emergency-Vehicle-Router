package com.emergencyrouter.factory;

import com.emergencyrouter.enums.VehicleType;
import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Coordinate;
import com.emergencyrouter.model.vehicles.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class VehicleFactory {

    private final Map<VehicleType, VehicleRegistration>
            registrations = new HashMap<>();

    public VehicleFactory() {

        registerVehicleType(
                VehicleType.AMBULANCE,
                "AMB-DEFAULT",
                Ambulance::new
        );

        registerVehicleType(
                VehicleType.FIRE_TRUCK,
                "FIRE-DEFAULT",
                FireTruck::new
        );

        registerVehicleType(
                VehicleType.POLICE_CAR,
                "POL-DEFAULT",
                PoliceCar::new
        );

        registerVehicleType(
                VehicleType.HELICOPTER,
                "HEL-DEFAULT",
                Helicopter::new
        );

        registerVehicleType(
                VehicleType.DRONE,
                "DRN-DEFAULT",
                Drone::new
        );

        registerVehicleType(
                VehicleType.RESCUE_BOAT,
                "BOT-DEFAULT",
                RescueBoat::new
        );
    }

    public void registerVehicleType(
            VehicleType type,
            String defaultId,
            VehicleCreator creator
    ) {

        registrations.put(
                Objects.requireNonNull(type),
                new VehicleRegistration(
                        requireText(defaultId),
                        Objects.requireNonNull(creator)
                )
        );
    }

    public Vehicle createVehicle(VehicleType type) {

        VehicleRegistration registration =
                findRegistration(type);

        return registration.creator()
                .create(
                        registration.defaultId(),
                        new Coordinate(0, 0)
                );
    }

    public Vehicle createVehicle(
            VehicleType type,
            String id,
            Location location
    ) {

        Objects.requireNonNull(location);

        VehicleRegistration registration =
                findRegistration(type);

        return registration.creator()
                .create(id, location);
    }

    private VehicleRegistration findRegistration(
            VehicleType type
    ) {

        VehicleRegistration registration =
                registrations.get(type);

        if (registration == null) {

            throw new IllegalArgumentException(
                    "Unsupported vehicle type: " + type
            );
        }

        return registration;
    }

    private String requireText(String value) {

        if (value == null || value.isBlank()) {

            throw new IllegalArgumentException(
                    "Text must not be blank"
            );
        }

        return value;
    }

    @FunctionalInterface
    public interface VehicleCreator {

        Vehicle create(
                String id,
                Location location
        );
    }

    private record VehicleRegistration(
            String defaultId,
            VehicleCreator creator
    ) {
    }
}