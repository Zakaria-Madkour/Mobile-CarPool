package com.example.majortask.Utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "user")
public class Person {
    @ColumnInfo(name = "first_name")
    private String firstName;
    @ColumnInfo(name = "last_name")
    private String lastName;
    @ColumnInfo(name = "emai")
    private String email;
    @ColumnInfo(name = "phone")
    private String phone;
    @ColumnInfo(name = "type")
    private String type;
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "user_id")
    @NonNull
    private String userId;


    public Person(String firstName, String lastName, String email, String phone, String type, String userId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.type = type;
        this.userId = userId;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return (getClass() == obj.getClass() && this.getUserId().equals(((Person) obj).getUserId())) ? true : false;
    }

    public Boolean isSame(Person person) {
        return (this.getUserId().equals(person.getUserId())) ? true : false;
    }

    public String getUserId() {
        return userId;
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
