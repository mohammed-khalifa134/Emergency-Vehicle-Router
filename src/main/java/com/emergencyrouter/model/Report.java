package com.emergencyrouter.model;

import com.emergencyrouter.enums.EmergencyType;
import com.emergencyrouter.enums.ReportStatus;
import com.emergencyrouter.interfaces.Location;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class Report {

    private final String id;

    private final EmergencyType type;

    private final Location location;

    private final LocalDateTime timestamp;

    private ReportStatus status;

    private int severityLevel;

    private Report(Builder builder) {

        this.id = requireText(
                builder.id,
                "id"
        );

        this.type = Objects.requireNonNull(
                builder.type,
                "type must not be null"
        );

        this.location = Objects.requireNonNull(
                builder.location,
                "location must not be null"
        );

        this.timestamp = Objects.requireNonNull(
                builder.timestamp,
                "timestamp must not be null"
        );

        validateSeverity(builder.severityLevel);

        this.severityLevel = builder.severityLevel;

        this.status = Objects.requireNonNullElse(
                builder.status,
                ReportStatus.RECEIVED
        );
    }

    public static Builder builder() {

        return new Builder();
    }

    public String getId() {

        return id;
    }

    public EmergencyType getType() {

        return type;
    }

    public Location getLocation() {

        return location;
    }

    public LocalDateTime getTimestamp() {

        return timestamp;
    }

    public ReportStatus getStatus() {

        return status;
    }

    public int getSeverityLevel() {

        return severityLevel;
    }

    public void setStatus(ReportStatus status) {

        this.status = Objects.requireNonNull(
                status,
                "status must not be null"
        );
    }

    private void validateSeverity(int level) {

        if (level < 1 || level > 5) {

            throw new IllegalArgumentException(
                    "Severity must be between 1 and 5"
            );
        }
    }

    private static String requireText(
            String value,
            String fieldName
    ) {

        if (value == null || value.isBlank()) {

            throw new IllegalArgumentException(
                    fieldName + " must not be blank"
            );
        }

        return value;
    }

    public static final class Builder {

        private String id =
                UUID.randomUUID().toString();

        private EmergencyType type;

        private Location location;

        private LocalDateTime timestamp =
                LocalDateTime.now();

        private ReportStatus status =
                ReportStatus.RECEIVED;

        private int severityLevel = 1;

        public Builder id(String id) {

            this.id = id;

            return this;
        }

        public Builder type(EmergencyType type) {

            this.type = type;

            return this;
        }

        public Builder location(Location location) {

            this.location = location;

            return this;
        }

        public Builder timestamp(LocalDateTime timestamp) {

            this.timestamp = timestamp;

            return this;
        }

        public Builder status(ReportStatus status) {

            this.status = status;

            return this;
        }

        public Builder severityLevel(int severityLevel) {

            this.severityLevel = severityLevel;

            return this;
        }

        public Report build() {

            return new Report(this);
        }
    }
}