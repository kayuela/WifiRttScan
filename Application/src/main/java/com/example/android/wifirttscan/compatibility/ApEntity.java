package com.example.android.wifirttscan.compatibility;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ApEntity {
    @PrimaryKey
    @ColumnInfo (name = "Compatibility")
    public boolean mComp;

    @ColumnInfo (name = "SSID")
    public String SSID;

    public ApEntity(){

    }

    public String getSSID() {
        return SSID;
    }

    public void setSsid(String ssid){
        this.SSID=ssid;
    }

    public boolean getmComp(){
        return mComp;
    }

    public void setmComp(boolean comp){
        this.mComp=comp;
    }

}
