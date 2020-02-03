package com.example.android.wifirttscan.result;

import android.net.wifi.rtt.RangingResult;

import java.io.IOException;

public interface RttOutputWriter {
    String mSampleFilename = "samples.txt";
    String mBatchFilename = "batches.txt";

    default void init() {}
    void writeSample(SampleResult result) throws IOException;
    void writeBatch(BatchResult result) throws IOException;
    default void end() {}
    void finish() throws IOException;

}
