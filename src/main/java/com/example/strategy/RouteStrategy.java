package com.example.strategy;

import com.example.model.Location;
import com.example.model.Route;

public interface RouteStrategy {
    Route calculateRoute(Location start, Location end);
}
