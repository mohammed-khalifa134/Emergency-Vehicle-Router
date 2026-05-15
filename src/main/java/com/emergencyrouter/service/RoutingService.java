package com.emergencyrouter.service;

import com.emergencyrouter.enums.RoutingAlgorithm;
import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Graph;
import com.emergencyrouter.model.Route;
import com.emergencyrouter.model.TrafficData;
import com.emergencyrouter.observer.Observer;
import com.emergencyrouter.strategy.HubLabelRouteStrategy;
import com.emergencyrouter.strategy.RouteStrategy;

import java.util.Objects;
import java.util.Optional;

public final class RoutingService
        implements Observer<TrafficData> {

    private RouteStrategy strategy;

    private final Graph graph;

    private Route currentRoute;

    private Location lastStart;

    private Location lastEnd;

    private RoutingAlgorithm currentAlgorithm;

    public RoutingService(
            RouteStrategy strategy,
            RoutingAlgorithm algorithm
    ) {

        this(strategy, algorithm, null);
    }

    public RoutingService(
            RouteStrategy strategy,
            RoutingAlgorithm algorithm,
            Graph graph
    ) {

        this.graph = graph;

        setStrategy(strategy, algorithm);
    }

    public void setStrategy(
            RouteStrategy strategy,
            RoutingAlgorithm algorithm
    ) {

        this.strategy = Objects.requireNonNull(
                strategy,
                "strategy must not be null"
        );

        this.currentAlgorithm = Objects.requireNonNull(
                algorithm,
                "algorithm must not be null"
        );
    }

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

        this.lastStart = start;

        this.lastEnd = end;

        currentRoute = strategy.calculateRoute(
                start,
                end
        );

        return currentRoute;
    }

    @Override
    public void update(TrafficData data) {

        Objects.requireNonNull(
                data,
                "traffic data must not be null"
        );

        if (graph == null) {
            return;
        }

        graph.updateTraffic(
                data.getRoadId(),
                data.getCongestionLevel(),
                data.isRoadClosed()
        );

        refreshStrategyData();

        recalculateCurrentRoute();
    }

    public Optional<Route> getCurrentRoute() {

        return Optional.ofNullable(currentRoute);
    }

    public RouteStrategy getStrategy() {

        return strategy;
    }

    public RoutingAlgorithm getCurrentAlgorithm() {

        return currentAlgorithm;
    }

    private void recalculateCurrentRoute() {

        if (lastStart == null || lastEnd == null) {
            return;
        }

        currentRoute = strategy.calculateRoute(
                lastStart,
                lastEnd
        );
    }

    private void refreshStrategyData() {

        if (graph == null) {
            return;
        }

        if (strategy instanceof HubLabelRouteStrategy
                hubLabelStrategy) {

            HubLabelPreprocessor preprocessor =
                    new HubLabelPreprocessor(graph);

            hubLabelStrategy.refreshLabels(
                    preprocessor.preprocess()
            );
        }
    }
}