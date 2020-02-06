package com.example.android.wifirttscan.result;

public class BatchResult {
    private int mSuccessfulSamples;
    private int mAttemptedSamples;

    private BatchResult(int successfulSamples, int attemptedSamples) {
        mSuccessfulSamples = successfulSamples;
        mAttemptedSamples = attemptedSamples;
    }

    public static BatchResult from(int successfulSamples, int attemptedSamples) {
        return new BatchResult(successfulSamples, attemptedSamples);
    }

    public int getSuccessfulSamples() {
        return mSuccessfulSamples;
    }

    public int getAttemptedSamples() {
        return mAttemptedSamples;
    }
}
