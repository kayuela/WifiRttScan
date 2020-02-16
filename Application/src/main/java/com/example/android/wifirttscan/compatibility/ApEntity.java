package com.example.android.wifirttscan.compatibility;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ApEntity {
    @PrimaryKey
    public boolean mComp;

    @ColumnInfo (name = "SSID")
    public String SSID;
}
