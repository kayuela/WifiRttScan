package com.example.android.wifirttscan.result;

public class BatchResult {
    public int mSuccessfulSamples;
    public int mAttemptedSamples;

    private BatchResult(int successfulSamples, int attemptedSamples) {
        mSuccessfulSamples = successfulSamples;
        mAttemptedSamples = attemptedSamples;
    }

    public static BatchResult from(int successfulSamples, int attemptedSamples) {
        return new BatchResult(successfulSamples, attemptedSamples);
    }
}
