package com.emergencyrouter.observer;

/**
 * Observer Pattern contract for classes that react to traffic updates.
 *
 * @param <T> type of update data received by the observer
 */
public interface Observer<T> {

    /**
     * Receives an update from a subject.
     *
     * @param data update payload
     */
    void update(T data);
}
