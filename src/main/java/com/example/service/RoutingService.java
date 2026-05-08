package com.example.service;

import com.example.model.Location;
import com.example.model.Route;
import com.example.model.TrafficData;
import com.example.strategy.FastestRouteStrategy;
import com.example.strategy.RouteStrategy;
import com.example.strategy.ShortestDistanceStrategy;

public class RoutingService {

    private RouteStrategy strategy;

    public RoutingService() {
        // Default strategy
        this.strategy = new FastestRouteStrategy();
    }

    public void setStrategy(RouteStrategy strategy) {
        this.strategy = strategy;
        System.out.println("[RoutingService] Strategy set to: " + strategy.getClass().getSimpleName());
    }

    public Route calculateRoute(Location start, Location end) {
        System.out.println("[RoutingService] Calculating route from " + start + " to " + end);
        return strategy.calculateRoute(start, end);
    }

    public Route calculateRoute(Location start, Location end, TrafficData trafficData) {
        // Choose strategy based on traffic conditions
        if (trafficData != null && (trafficData.getCongestionLevel() > 0.7 || trafficData.isRoadClosed())) {
            System.out.println("[RoutingService] High congestion detected — switching to FastestRouteStrategy");
            setStrategy(new FastestRouteStrategy());
        } else {
            System.out.println("[RoutingService] Normal traffic — using ShortestDistanceStrategy");
            setStrategy(new ShortestDistanceStrategy());
        }
        return strategy.calculateRoute(start, end);
    }
}
