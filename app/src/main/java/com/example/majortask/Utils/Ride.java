package com.example.majortask.Utils;

import com.example.majortask.Utils.Car;

public class Ride {
    private String pickup;
    private String destination;
    private String time;
    private double cost;
    private Car car;

    public Ride(String pickup, String destination, String time, double cost, Car car) {
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

    public String getTime() {
        return time;
    }

    public double getCost() {
        return cost;
    }

    public Car getCar() {
        return car;
    }
}
