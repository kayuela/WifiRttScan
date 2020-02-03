package com.example.android.wifirttscan.result;

import android.app.Application;
import android.content.Context;
import android.net.wifi.rtt.RangingResult;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class FileRttOutputWriter implements RttOutputWriter {
    private final int mActualDistance;
    private OutputStreamWriter mFowSamples;
    private OutputStreamWriter mFowBatches;
    private int mNumBatch;


    public FileRttOutputWriter(Context context, int actualDistance) throws FileNotFoundException {
        final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);
        final String currentDate = formatter.format(new Date());

        final String sampleFilename = currentDate + "-" + RttOutputWriter.mSampleFilename;
        final String batchFilename = currentDate + "-" + RttOutputWriter.mBatchFilename;
        FileOutputStream fosSamples = context.openFileOutput(sampleFilename, Context.MODE_PRIVATE);
        FileOutputStream fosBatches = context.openFileOutput(batchFilename, Context.MODE_PRIVATE);

        this.mFowSamples = new OutputStreamWriter(fosSamples);
        this.mFowBatches = new OutputStreamWriter(fosBatches);
        this.mNumBatch = 1;
        this.mActualDistance = actualDistance;
    }

    @Override
    public void writeSample(SampleResult result) throws IOException {
        final RangingResult rangingResult = result.mRangingResult;

        mFowSamples.write(String.valueOf(rangingResult.getMacAddress()));
        mFowSamples.write(" ");
        mFowSamples.write(mNumBatch);
        mFowSamples.write(" ");
        mFowSamples.write(mActualDistance);
        mFowSamples.write(" ");
        mFowSamples.write(String.valueOf(rangingResult.getDistanceMm()/1000f));
        mFowSamples.write(" ");
        mFowSamples.write(String.valueOf(rangingResult.getDistanceStdDevMm()/1000f));
        mFowSamples.write(" ");
        mFowSamples.write(rangingResult.getRssi());
        mFowSamples.write(" ");
        mFowSamples.write(String.valueOf(result.mEstimationTime));
        mFowSamples.write(" ");
        mFowSamples.write(rangingResult.getNumSuccessfulMeasurements());
        mFowSamples.write(" ");
        mFowSamples.write(rangingResult.getNumAttemptedMeasurements());
        mFowSamples.write("\n");
    }

    @Override
    public void writeBatch(BatchResult result) throws IOException {
        mFowBatches.write(mNumBatch);
        mFowBatches.write(" ");
        mFowBatches.write(result.mSuccessfulSamples);
        mFowBatches.write(" ");
        mFowBatches.write(result.mAttemptedSamples);
        mFowBatches.write("\n");
        mNumBatch++;
    }


    @Override
    public void finish() throws IOException {
        mFowSamples.close();
        mFowBatches.close();
    }
}
