package com.emergencyrouter.strategy;

import com.emergencyrouter.interfaces.Location;
import com.emergencyrouter.model.Route;


public interface RouteStrategy {

      Route calculateRoute(
            Location start,
            Location end
    );
}