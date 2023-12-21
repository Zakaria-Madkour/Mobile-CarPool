package com.example.majortask.Utils.ROOM;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.majortask.Utils.Person;

import java.util.List;

@Dao
public interface PersonDao {
    @Query("SELECT * FROM user")
    List<Person> getAllUsers();

    @Query("SELECT * FROM user WHERE user_id = :userId")
    Person getPersonById(String userId);

    @Insert
    void insertPerson(Person person);
}
