package com.example.majortask;

import java.sql.Time;

public class Ride {
    private String pickup;
    private String destination;
    private Time time;
    private double cost;
    private Car car;

    public Ride(String pickup, String destination, Time time, double cost, Car car) {
        this.pickup = pickup;
        this.destination = destination;
        this.time = time;
        this.cost = cost;
        this.car = car;
    }

    public String getPickup() {
        return pickup;
    }

    public String getDestination() {
        return destination;
    }

    public Time getTime() {
        return time;
    }

    public double getCost() {
        return cost;
    }

    public Car getCar() {
        return car;
    }
}
