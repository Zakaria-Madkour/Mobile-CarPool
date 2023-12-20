package com.example.majortask.Utils;

public class Person {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String type;

    public Person(String firstName, String lastName, String email, String phone, String type) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.type = type;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getType() {
        return type;
    }
}
