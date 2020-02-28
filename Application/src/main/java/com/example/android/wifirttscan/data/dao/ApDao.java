package com.example.android.wifirttscan.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.android.wifirttscan.data.entity.ApEntity;

@Dao
public interface ApDao {

    @Query("SELECT * FROM ApEntity WHERE BSSID LIKE :BSSID")
    ApEntity findByBSSID(String BSSID);

    @Insert
    void insertAP(ApEntity apEntity);

}
