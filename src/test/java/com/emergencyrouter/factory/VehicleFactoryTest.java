package com.emergencyrouter.factory;

import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Ambulance;
import com.emergencyrouter.model.Coordinate;
import com.emergencyrouter.model.FireTruck;
import com.emergencyrouter.model.PoliceCar;
import com.emergencyrouter.model.Report;
import com.emergencyrouter.model.Vehicle;
import junit.framework.TestCase;

/**
 * Tests vehicle creation behavior from the factory.
 */
public class VehicleFactoryTest extends TestCase {
    private final VehicleFactory factory = new VehicleFactory();
    private final Coordinate location = new Coordinate(32.8872, 13.1913);

    /**
     * Verifies medical aliases create ambulances.
     */
    public void testCreateAmbulance() {
        Vehicle vehicle = factory.createVehicle("AMBULANCE", "AMB-1", location);

        assertTrue(vehicle instanceof Ambulance);
        assertEquals("AMB-1", vehicle.getId());
    }

    /**
     * Verifies fire aliases create fire trucks.
     */
    public void testCreateFireTruck() {
        Vehicle vehicle = factory.createVehicle("FIRE", "FIRE-1", location);

        assertTrue(vehicle instanceof FireTruck);
        assertEquals("FIRE-1", vehicle.getId());
    }

    /**
     * Verifies police aliases create police cars.
     */
    public void testCreatePoliceCar() {
        Vehicle vehicle = factory.createVehicle("POLICE", "POL-1", location);

        assertTrue(vehicle instanceof PoliceCar);
        assertEquals("POL-1", vehicle.getId());
    }

    /**
     * Verifies invalid factory input fails clearly.
     */
    public void testInvalidVehicleTypeThrowsException() {
        try {
            factory.createVehicle("TOW_TRUCK", "TOW-1", location);
            fail("Expected IllegalArgumentException for unsupported vehicle type.");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Unsupported vehicle type"));
        }
    }

    /**
     * Verifies new vehicle types can be registered without changing factory
     * switch logic.
     */
    public void testRegisteredVehicleTypeCanBeCreatedDynamically() {
        factory.registerVehicleType("RESCUE", "RES-DEFAULT", RescueVehicle::new, "RESCUE_TEAM");

        Vehicle vehicle = factory.createVehicle("RESCUE_TEAM", "RES-1", location);

        assertTrue(vehicle instanceof RescueVehicle);
        assertEquals("RES-1", vehicle.getId());
    }

    private static final class RescueVehicle extends Vehicle {

        private RescueVehicle(String id, Location location) {
            super(id, location);
        }

        @Override
        public void respondToReport(Report report) {
            markBusy();
        }

        @Override
        public boolean canHandle(Report report) {
            return reportTypeMatches(report, "RESCUE");
        }
    }
}
