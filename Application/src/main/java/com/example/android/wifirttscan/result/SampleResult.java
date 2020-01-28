package com.example.android.wifirttscan.result;

import android.net.wifi.rtt.RangingResult;

public class SampleResult {
    public RangingResult mRangingResult;

    private SampleResult(RangingResult rangingResult) {
        mRangingResult = rangingResult;
    }

    public static SampleResult from(RangingResult rangingResult) {
        return new SampleResult(rangingResult);
    }
}
