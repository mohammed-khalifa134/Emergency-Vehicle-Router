package com.emergencyrouter.strategy;

import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Route;

import java.util.List;
import java.util.Objects;

public final class ShortestDistanceStrategy
        implements RouteStrategy {

    private static final double CITY_AVERAGE_SPEED_KPH = 50.0;

    @Override
    public Route calculateRoute(
            Location start,
            Location end
    ) {

        Objects.requireNonNull(
                start,
                "start must not be null"
        );

        Objects.requireNonNull(
                end,
                "end must not be null"
        );

        double distance =
                RouteCalculationSupport
                        .estimateDistanceInKilometers(
                                start,
                                end
                        );

        double time =
                RouteCalculationSupport
                        .estimateTimeInMinutes(
                                distance,
                                CITY_AVERAGE_SPEED_KPH
                        );

        return new Route(
                List.of(start, end),
                distance,
                time
        );
    }
}