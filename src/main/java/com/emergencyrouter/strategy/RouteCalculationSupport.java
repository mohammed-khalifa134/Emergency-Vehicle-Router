package com.emergencyrouter.strategy;


import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Route;


import java.util.List;

final class RouteCalculationSupport {

    private static final double KILOMETERS_PER_COORDINATE_DEGREE = 111.0;

    private static final double MINUTES_PER_HOUR = 60.0;

    private RouteCalculationSupport() {
    }

    static double estimateDistanceInKilometers(
            Location start,
            Location end
    ) {

        double latitudeDifference =
                start.getLatitude()
                        - end.getLatitude();

        double longitudeDifference =
                start.getLongitude()
                        - end.getLongitude();

        double coordinateDistance =
                Math.sqrt(
                        latitudeDifference * latitudeDifference
                                + longitudeDifference * longitudeDifference
                );

        return coordinateDistance
                * KILOMETERS_PER_COORDINATE_DEGREE;
    }

    static double estimateTimeInMinutes(
            double distanceInKilometers,
            double averageSpeedKilometersPerHour
    ) {

        if (averageSpeedKilometersPerHour <= 0) {

            throw new IllegalArgumentException(
                    "average speed must be greater than zero"
            );
        }

        return distanceInKilometers
                / averageSpeedKilometersPerHour
                * MINUTES_PER_HOUR;
    }

    static Route buildDirectRoute(
        Location start,
        Location end,
        double distance,
        double time
) {

    return new Route(
            List.of(start, end),
            distance,
            time
    );
}
}