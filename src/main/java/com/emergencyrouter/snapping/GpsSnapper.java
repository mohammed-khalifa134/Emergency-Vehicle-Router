package com.emergencyrouter.snapping;

import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Graph;
import com.emergencyrouter.model.Node;

import java.util.Objects;

public final class GpsSnapper {

    private final Graph graph;

    public GpsSnapper(Graph graph) {
        this.graph = Objects.requireNonNull(graph);
    }

    public Node snap(Location location) {

        return graph.findNearestNode(location);
    }
}