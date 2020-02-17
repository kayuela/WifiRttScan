package com.example.android.wifirttscan.compatibility;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ApDao {

    @Query("SELECT * FROM ApEntity")
    LiveData<List<ApEntity>> getAll();

    @Query("SELECT * FROM ApEntity WHERE BSSID LIKE :BSSID LIMIT 1")
    ApEntity findBySSID(String BSSID);

    @Insert
    void insertAP(ApEntity apEntity);

}
