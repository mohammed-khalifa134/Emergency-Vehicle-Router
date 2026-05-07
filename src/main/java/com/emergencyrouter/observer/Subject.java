package com.emergencyrouter.observer;

/**
 * Observer Pattern contract for classes that publish updates.
 *
 * @param <T> type of update data sent to observers
 */
public interface Subject<T> {

    /**
     * Registers an observer.
     *
     * @param observer observer to register
     */
    void attach(Observer<T> observer);

    /**
     * Removes a registered observer.
     *
     * @param observer observer to remove
     */
    void detach(Observer<T> observer);

    /**
     * Notifies all registered observers about the latest update.
     */
    void notifyObservers();
}
