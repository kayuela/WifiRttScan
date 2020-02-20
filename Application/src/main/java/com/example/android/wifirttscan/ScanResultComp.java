package com.example.android.wifirttscan;

import android.net.wifi.ScanResult;
import android.os.Parcel;
import android.os.Parcelable;

import java.lang.reflect.Field;

class ScanResultComp implements Parcelable {
    private static final long FLAG_80211mc_RESPONDER = 0x0000000000000002;
    private ScanResult mScanResult;
    private boolean mIs80211mcResponderAnnounced;
    private boolean mIs80211mcResponderTested;

    public ScanResultComp(ScanResult scanResult) {
        mScanResult = scanResult;
        is80211mcResponderAnnounced(scanResult.is80211mcResponder());
        is80211mcResponder(false);

        // Forcing ScanResult to act as RTT responder
        try {
            Field f = scanResult.getClass().getDeclaredField("flags");
            f.setAccessible(true);
            f.setLong(scanResult,f.getLong(scanResult) | ScanResultComp.FLAG_80211mc_RESPONDER);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
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

    public String getFrequencyBand() { return String.valueOf(mScanResult.frequency + " MHz"); };

    public ScanResult getScanResult() {
        return mScanResult;
    }

    /* Métodos utilizados para comprobar si se anuncia y si realmente son compatibles con 802.11mc */
    public boolean is80211mcResponder() { return mIs80211mcResponderTested || is80211mcResponderAnnounced(); }

    /*setter*/
    public void is80211mcResponder(boolean isResponder){
        mIs80211mcResponderTested =isResponder;
    }

    public boolean is80211mcResponderAnnounced() {
        return mIs80211mcResponderAnnounced;
    }
    public void is80211mcResponderAnnounced(boolean is80211mcResponderAnnounced) {
        this.mIs80211mcResponderAnnounced = is80211mcResponderAnnounced;
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
