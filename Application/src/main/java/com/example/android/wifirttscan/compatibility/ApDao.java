package com.example.android.wifirttscan.compatibility;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ApDao {

    @Query("SELECT * FROM apentity")
    List<ApEntity> getAll();

    @Query("SELECT * FROM apentity WHERE SSID LIKE :ssid LIMIT 1")
    ApEntity findBySSID(String ssid);

    @Insert
    void insertAP(ApEntity apEntity);

}
