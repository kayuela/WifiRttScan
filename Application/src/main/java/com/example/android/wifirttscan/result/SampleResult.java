package com.example.android.wifirttscan.result;

import android.net.wifi.rtt.RangingResult;

public class SampleResult {
    public RangingResult mRangingResult;
    public double mEstimationTime;

    private SampleResult(RangingResult rangingResult, double estimationTime) {
        mRangingResult = rangingResult;
        mEstimationTime = estimationTime;
    }

    public static SampleResult from(RangingResult rangingResult, double estimationTime) {
        return new SampleResult(rangingResult, estimationTime);
    }
}
