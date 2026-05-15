package com.emergencyrouter.bootstrap;

import com.emergencyrouter.controller.MapController;
import com.emergencyrouter.enums.LocationType;
import com.emergencyrouter.enums.RoutingAlgorithm;
import com.emergencyrouter.enums.VehicleType;
import com.emergencyrouter.factory.VehicleFactory;
import com.emergencyrouter.model.Graph;
import com.emergencyrouter.model.MapLocation;
import com.emergencyrouter.model.Node;
import com.emergencyrouter.model.vehicles.Vehicle;
import com.emergencyrouter.service.DispatchService;
import com.emergencyrouter.service.RoutingService;
import com.emergencyrouter.strategy.DijkstraRouteStrategy;
import com.emergencyrouter.strategy.RouteStrategy;

import java.util.List;

public final class ApplicationBootstrap {

    private ApplicationBootstrap() {
    }

    public static MapController createSystem() {

        /*
         * graph
         */
        Graph graph = new Graph();

        /*
         * nodes
         */
        Node hospital =
                new Node(
                        "HOSPITAL",
                        32.8872,
                        13.1913
                );

        Node downtown =
                new Node(
                        "DOWNTOWN",
                        32.8920,
                        13.1800
                );

        Node airport =
                new Node(
                        "AIRPORT",
                        32.6635,
                        13.1590
                );

        graph.addBidirectionalEdge(
                hospital,
                downtown,
                5,
                1
        );

        graph.addBidirectionalEdge(
                downtown,
                airport,
                10,
                1.5
        );

        /*
         * strategy
         */
        RouteStrategy strategy =
                new DijkstraRouteStrategy(graph);

        /*
         * routing service
         */
        RoutingService routingService =
                new RoutingService(
                        strategy,
                        RoutingAlgorithm.DIJKSTRA,
                        graph
                );

        /*
         * vehicle factory
         */
        VehicleFactory factory =
                new VehicleFactory();

        /*
         * vehicles
         */
        Vehicle ambulance =
                factory.createVehicle(
                        VehicleType.AMBULANCE,
                        "AMB-1",
                        MapLocation.builder()
                                .latitude(32.8872)
                                .longitude(13.1913)
                                .name("مستشفى طرابلس")
                                .type(LocationType.HOSPITAL)
                                .build()
                );

        Vehicle fireTruck =
                factory.createVehicle(
                        VehicleType.FIRE_TRUCK,
                        "FIRE-1",
                        MapLocation.builder()
                                .latitude(32.8920)
                                .longitude(13.1800)
                                .name("مركز الإطفاء")
                                .type(LocationType.STATION)
                                .build()
                );

        Vehicle police =
                factory.createVehicle(
                        VehicleType.POLICE_CAR,
                        "POL-1",
                        MapLocation.builder()
                                .latitude(32.8950)
                                .longitude(13.1700)
                                .name("مركز الشرطة")
                                .type(LocationType.STATION)
                                .build()
                );

        List<Vehicle> vehicles =
                List.of(
                        ambulance,
                        fireTruck,
                        police
                );

        /*
         * dispatch service
         */
        DispatchService dispatchService =
                new DispatchService(
                        vehicles,
                        routingService
                );

        /*
         * controller
         */
        return new MapController(
                routingService,
                dispatchService
        );
    }
}