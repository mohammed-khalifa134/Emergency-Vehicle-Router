package com.example.observer;

import com.example.model.TrafficData;

public interface Observer {
    void update(TrafficData data);
}
