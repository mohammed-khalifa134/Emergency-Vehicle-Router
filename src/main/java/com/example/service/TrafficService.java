package com.example.service;

import java.util.ArrayList;
import java.util.List;

import com.example.model.TrafficData;
import com.example.observer.Observer;
import com.example.observer.Subject;

public class TrafficService implements Subject {

    private TrafficData currentData;
    private List<Observer> observers = new ArrayList<>();

    public void updateTraffic(TrafficData data) {
        this.currentData = data;
        System.out.println("[TrafficService] Traffic updated: " + data);
        notifyObservers();
    }

    public TrafficData getCurrentData() {
        return currentData;
    }

    @Override
    public void attach(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(currentData);
        }
    }
}
