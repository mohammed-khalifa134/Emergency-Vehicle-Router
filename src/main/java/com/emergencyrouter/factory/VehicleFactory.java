package com.emergencyrouter.factory;

import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Ambulance;
import com.emergencyrouter.model.Coordinate;
import com.emergencyrouter.model.FireTruck;
import com.emergencyrouter.model.PoliceCar;
import com.emergencyrouter.model.Vehicle;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Factory Pattern implementation for creating emergency vehicles.
 *
 * <p>Use this class whenever the application needs a vehicle object but should
 * not directly depend on a concrete constructor. This keeps creation logic in
 * one focused place.</p>
 *
 * <p>Open/Closed Principle: new vehicle types can be added by registering a
 * creator function instead of adding another switch branch.</p>
 */
public final class VehicleFactory {
    private final Map<String, VehicleRegistration> registrations = new HashMap<>();

    /**
     * Creates a factory with the default emergency vehicle types registered.
     *
     * <p>Use this constructor in the application startup code. Extra vehicle
     * types can still be added later with {@link #registerVehicleType}.</p>
     */
    public VehicleFactory() {
        registerVehicleType("AMBULANCE", "AMB-DEFAULT", Ambulance::new, "MEDICAL");
        registerVehicleType("FIRE_TRUCK", "FIRE-DEFAULT", FireTruck::new, "FIRE", "FIRETRUCK");
        registerVehicleType("POLICE_CAR", "POL-DEFAULT", PoliceCar::new, "POLICE", "POLICECAR");
    }

    /**
     * Registers a vehicle type and any aliases that should create it.
     *
     * <p>Use this method when adding a new vehicle. For example, a future
     * rescue vehicle can be registered once here or during application startup
     * without changing dispatch logic or adding switch cases.</p>
     *
     * @param type canonical vehicle type name
     * @param defaultId id used by the simple UML-compatible create method
     * @param creator function that creates the concrete vehicle
     * @param aliases optional alternate names for the same vehicle type
     */
    public void registerVehicleType(String type, String defaultId, VehicleCreator creator, String... aliases) {
        VehicleRegistration registration = new VehicleRegistration(
                requireText(defaultId, "defaultId"),
                Objects.requireNonNull(creator, "creator must not be null")
        );

        registrations.put(normalizeType(type), registration);

        for (String alias : aliases) {
            registrations.put(normalizeType(alias), registration);
        }
    }

    /**
     * Creates a vehicle with a generated demo id and a default location.
     *
     * <p>Use this UML-compatible overload only for simple demos where the caller
     * does not care about the exact vehicle id or starting location.</p>
     *
     * @param type requested vehicle type
     * @return created vehicle
     */
    public Vehicle createVehicle(String type) {
        VehicleRegistration registration = findRegistration(type);

        return registration.creator().create(registration.defaultId(), new Coordinate(0, 0));
    }

    /**
     * Creates a vehicle with a caller-provided id and starting location.
     *
     * <p>Use this overload in production workflow code because dispatch and
     * routing need a real vehicle id and current location.</p>
     *
     * @param type requested vehicle type
     * @param id vehicle identifier
     * @param location starting vehicle location
     * @return created vehicle
     */
    public Vehicle createVehicle(String type, String id, Location location) {
        requireText(id, "id");
        Objects.requireNonNull(location, "location must not be null");

        return findRegistration(type).creator().create(id, location);
    }

    /**
     * Finds the registered creator for a requested vehicle type.
     *
     * <p>Use this helper inside create methods so unsupported types fail with
     * one clear error message.</p>
     *
     * @param type requested vehicle type or alias
     * @return matching vehicle registration
     */
    private VehicleRegistration findRegistration(String type) {
        VehicleRegistration registration = registrations.get(normalizeType(type));

        if (registration == null) {
            throw new IllegalArgumentException("Unsupported vehicle type: " + type);
        }

        return registration;
    }

    /**
     * Converts user input into a stable registry key.
     *
     * <p>Use this helper so values like "fire truck", "FIRE-TRUCK", and
     * "FIRE_TRUCK" are treated consistently without hardcoded switch logic.</p>
     *
     * @param type raw vehicle type
     * @return normalized registry key
     */
    private String normalizeType(String type) {
        return requireText(type, "vehicle type")
                .trim()
                .toUpperCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    /**
     * Constructor function used by the dynamic vehicle registry.
     */
    @FunctionalInterface
    public interface VehicleCreator {

        /**
         * Creates a concrete vehicle.
         *
         * @param id vehicle identifier
         * @param location starting location
         * @return created vehicle
         */
        Vehicle create(String id, Location location);
    }

    private record VehicleRegistration(String defaultId, VehicleCreator creator) {
    }
}
