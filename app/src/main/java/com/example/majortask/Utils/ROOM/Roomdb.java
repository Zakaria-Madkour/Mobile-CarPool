package com.example.majortask.Utils.ROOM;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.majortask.Utils.Person;

@Database(entities = {Person.class}, version = 0)
public abstract class Roomdb extends RoomDatabase {

    private static Roomdb INSTANCE;
    public abstract PersonDao personDao();

    public static Roomdb getDbInstance(Context context){
        if (INSTANCE == null){
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(), Roomdb.class, "USERS_DB")
                    .allowMainThreadQueries().build();
        }
        return INSTANCE;
    }
}
