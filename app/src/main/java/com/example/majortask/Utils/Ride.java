package com.example.majortask.Utils;


public class Ride {
    private String pickup;
    private String destination;
    private String time;
    private String cost;
    private Boolean status;
    private String driverId;
    private String rideId;
    private String day;


    public Ride(String pickup, String destination, String time, String cost, Boolean status, String driverId, String rideId, String day) {
        this.pickup = pickup;
        this.destination = destination;
        this.time = time;
        this.cost = cost;
        this.status = status;
        this.driverId = driverId;
        this.rideId = rideId;
        this.day = day;
    }

    public Ride(String pickup, String destination, String time, String cost, String driverId, String rideId, String day) {
        this.pickup = pickup;
        this.destination = destination;
        this.time = time;
        this.cost = cost;
        this.driverId = driverId;
        this.rideId = rideId;
        this.day = day;
    }

    public Ride(String pickup, String destination, String time, String cost) {
        this.pickup = pickup;
        this.destination = destination;
        this.time = time;
        this.cost = cost;
    }

    public Ride(String pickup, String destination, String time, String cost, String driverId, String rideId) {
        this.pickup = pickup;
        this.destination = destination;
        this.time = time;
        this.cost = cost;
        this.driverId = driverId;
        this.rideId = rideId;
    }


    public String getDay() {
        return day;
    }
    public String getRideId() {
        return rideId;
    }

    public Boolean getStatus() {
        return status;
    }

    public String getDriverId() {
        return driverId;
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

    public String getCost() {
        return cost;
    }


}
