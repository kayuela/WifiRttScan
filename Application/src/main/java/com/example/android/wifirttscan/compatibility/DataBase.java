package com.example.android.wifirttscan.compatibility;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = ApEntity.class, version = 1)
public abstract class DataBase extends RoomDatabase {
    public abstract ApDao apDao();
}
