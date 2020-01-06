package com.example.android.wifirttscan;

import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;

class ScanResultComp implements Parcelable {
    private ScanResult mScanResult;

    public ScanResultComp(ScanResult scanResult) {
        this.mScanResult = scanResult;
    }

    protected ScanResultComp(Parcel in) {
        mScanResult = in.readParcelable(ScanResult.class.getClassLoader());
    }

    public static final Creator<ScanResultComp> CREATOR = new Creator<ScanResultComp>() {
        @Override
        public ScanResultComp createFromParcel(Parcel in) {
            return new ScanResultComp(in);
        }

        @Override
        public ScanResultComp[] newArray(int size) {
            return new ScanResultComp[size];
        }
    };

    public String getSSID() {
        return mScanResult.SSID;
    }

    public String getBSSID() {
        return mScanResult.BSSID;
    }

    public ScanResult getScanResult() {
        return mScanResult;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mScanResult, flags);
    }
}
