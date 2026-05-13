package com.emergencyrouter.service;

import com.emergencyrouter.model.TrafficData;
import com.emergencyrouter.observer.Observer;
import com.emergencyrouter.observer.Subject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Publishes traffic updates to interested observers.
 *
 * <p>Use this service when new road data arrives. It stores the latest update
 * and notifies observers, such as routing services, that routes may need to
 * change.</p>
 */
public final class TrafficService implements Subject<TrafficData> {
    private TrafficData currentData;
    private final List<Observer<TrafficData>> observers = new ArrayList<>();

    /**
     * Receives and publishes a traffic update.
     *
     * <p>Use this method from simulations or future real-time traffic adapters.</p>
     *
     * @param data latest traffic update
     */
    public void updateTraffic(TrafficData data) {
        currentData = Objects.requireNonNull(data, "data must not be null");
        System.out.println("Traffic updated: " + data.getRoadId());

        // Added route recalculation after traffic update.
        notifyObservers();
    }

    /**
     * Registers an observer for traffic updates.
     *
     * <p>Use this method to connect {@link RoutingService} to real-time traffic
     * notifications.</p>
     *
     * @param observer observer to register
     */
    @Override
    public void attach(Observer<TrafficData> observer) {
        Objects.requireNonNull(observer, "observer must not be null");
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Removes a traffic observer.
     *
     * <p>Use this method when a service no longer wants traffic updates.</p>
     *
     * @param observer observer to remove
     */
    @Override
    public void detach(Observer<TrafficData> observer) {
        observers.remove(observer);
    }

    /**
     * Notifies every registered observer about the current traffic update.
     *
     * <p>Use this method internally after {@link #updateTraffic(TrafficData)}
     * stores new data.</p>
     */
    @Override
    public void notifyObservers() {
        if (currentData == null) {
            return;
        }

        for (Observer<TrafficData> observer : List.copyOf(observers)) {
            observer.update(currentData);
        }
    }

    /**
     * Gets the latest traffic update.
     *
     * <p>Use this method in tests or monitoring code.</p>
     *
     * @return latest traffic data, or null when no update has arrived
     */
    public TrafficData getCurrentData() {
        return currentData;
    }
}
