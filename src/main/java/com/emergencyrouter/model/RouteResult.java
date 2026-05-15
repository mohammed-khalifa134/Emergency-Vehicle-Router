package com.emergencyrouter.model;

import com.emergencyrouter.enums.RoutingAlgorithm;

import java.util.Objects;

public final class RouteResult {

    private final Route route;

    private final RoutingAlgorithm algorithm;

    private final long calculationTimeMillis;

    private final boolean recalculated;

    public RouteResult(
            Route route,
            RoutingAlgorithm algorithm,
            long calculationTimeMillis,
            boolean recalculated
    ) {

        this.route =
                Objects.requireNonNull(route);

        this.algorithm =
                Objects.requireNonNull(algorithm);

        this.calculationTimeMillis =
                calculationTimeMillis;

        this.recalculated =
                recalculated;
    }

    public Route getRoute() {
        return route;
    }

    public RoutingAlgorithm getAlgorithm() {
        return algorithm;
    }

    public long getCalculationTimeMillis() {
        return calculationTimeMillis;
    }

    public boolean isRecalculated() {
        return recalculated;
    }
}