package com.emergencyrouter.model;

import com.emergencyrouter.interfaces.Location;

import java.util.Date;
import java.util.Objects;

/**
 * Represents an emergency report received by the system.
 *
 * <p>Use this class to carry the emergency type, incident location, and time
 * into dispatch and routing services.</p>
 */
public final class Report {
    private final String id;
    private final String type;
    private final Location location;
    private final Date timestamp;

    /**
     * Creates an emergency report.
     *
     * @param id unique report identifier
     * @param type emergency type, such as MEDICAL, FIRE, or POLICE
     * @param location incident location
     * @param timestamp time the report was created
     */
    public Report(String id, String type, Location location, Date timestamp) {
        this.id = requireText(id, "id");
        this.type = requireText(type, "type");
        this.location = Objects.requireNonNull(location, "location must not be null");
        this.timestamp = new Date(Objects.requireNonNull(timestamp, "timestamp must not be null").getTime());
    }

    /**
     * Gets the report identifier.
     *
     * <p>Use this method in logs or console output to show which incident is
     * being handled.</p>
     *
     * @return report id
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the emergency type.
     *
     * <p>Use this method when selecting a suitable vehicle for the report.</p>
     *
     * @return report type
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the emergency location.
     *
     * <p>Use this method as the route destination during dispatch.</p>
     *
     * @return incident location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Gets the report timestamp.
     *
     * <p>Use this method when displaying when the emergency request arrived.</p>
     *
     * @return defensive copy of report timestamp
     */
    public Date getTimestamp() {
        return new Date(timestamp.getTime());
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
