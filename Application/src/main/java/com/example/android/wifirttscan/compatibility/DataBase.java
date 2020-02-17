package com.example.android.wifirttscan.compatibility;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = ApEntity.class, version = 1, exportSchema = false)
public abstract class DataBase extends RoomDatabase {

    public static final String DATABASE_NAME="Compatibility-DataBase";

    public abstract ApDao apDao();

    private static DataBase dbInstance;

    public static DataBase getDataBase(final Context context){
        if (dbInstance==null) {
                if (dbInstance == null) {
                    dbInstance = Room.databaseBuilder(context.getApplicationContext(), DataBase.class, DATABASE_NAME).allowMainThreadQueries().build();
                }
        }
        return dbInstance;
    }
}
