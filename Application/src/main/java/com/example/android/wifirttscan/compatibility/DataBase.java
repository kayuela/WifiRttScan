package com.example.android.wifirttscan.compatibility;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = ApEntity.class, version = 1, exportSchema = false)
public abstract class DataBase extends RoomDatabase {

    public static final String DATABASE_NAME="Compatibility-DataBase";
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public abstract ApDao apDao();

    private static DataBase dbInstance;

    public static DataBase getDataBase(final Context context){
        if (dbInstance==null) {
            synchronized (Database.class) {
                if (dbInstance == null) {
                    dbInstance = Room.databaseBuilder(context.getApplicationContext(), DataBase.class, DATABASE_NAME).allowMainThreadQueries().build();
                }
            }
        }
        return dbInstance;
    }
}
