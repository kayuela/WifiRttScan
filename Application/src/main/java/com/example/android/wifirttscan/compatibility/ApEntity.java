package com.example.android.wifirttscan.compatibility;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ApEntity {
    @PrimaryKey
    @NonNull
    @ColumnInfo (name = "BSSID")
    public String BSSID;

    public ApEntity(){

    }

    public String getBSSID() {
        return BSSID;
    }

    public void setSsid(String bssid){
        this.BSSID=bssid;
    }

}
