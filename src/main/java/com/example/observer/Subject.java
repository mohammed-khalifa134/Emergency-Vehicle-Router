package com.example.observer;

public interface Subject {
    void attach(Observer observer);
    void notifyObservers();
}
