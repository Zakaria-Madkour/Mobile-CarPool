package com.example.majortask;

public class Car {
    private String licencePlate;
    private String driver;
    private String color;
    private String make;
    private String model;

    public Car(String licencePlate, String driver, String color, String make, String model) {
        this.licencePlate = licencePlate;
        this.driver = driver;
        this.color = color;
        this.make = make;
        this.model = model;
    }

    public String getLicencePlate() {
        return licencePlate;
    }

    public String getDriver() {
        return driver;
    }

    public String getColor() {
        return color;
    }

    public String getMake() {
        return make;
    }

    public String getModel() {
        return model;
    }
}
