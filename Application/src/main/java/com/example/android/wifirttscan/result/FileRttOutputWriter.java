package com.example.android.wifirttscan.result;

import android.content.Context;
import android.net.wifi.rtt.RangingResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class FileRttOutputWriter implements RttOutputWriter {
    private final double mActualDistance;
    private PrintWriter mFowSamples;
    private PrintWriter mFowBatches;
    private int mNumBatch;


    public FileRttOutputWriter(Context context, double actualDistance) {
        final SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH);
        final String currentDate = formatter.format(new Date());

        final String sampleFilename = currentDate + "-" + RttOutputWriter.mSampleFilename;
        final String batchFilename = currentDate + "-" + RttOutputWriter.mBatchFilename;
        final String scanDir = context.getExternalFilesDir("scans").getPath() + File.separator;

        mFowSamples = getWriter(scanDir + sampleFilename);
        mFowBatches = getWriter(scanDir + batchFilename);

        this.mNumBatch = 1;
        this.mActualDistance = actualDistance;
    }

    public PrintWriter getWriter(String path) {
        try {
            File file = new File(path);
            file.createNewFile();
            return new PrintWriter(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void writeSample(SampleResult result) {
        final RangingResult rangingResult = result.getRangingResult();

        mFowSamples.printf("%d,%f,%f,%f,%d,%f,%d,%d\n",
                mNumBatch,
                mActualDistance,
                rangingResult.getDistanceMm()/1000f,
                rangingResult.getDistanceStdDevMm()/1000f,
                rangingResult.getRssi(),
                result.getFTMProcedureDuration(),
                rangingResult.getNumSuccessfulMeasurements(),
                rangingResult.getNumAttemptedMeasurements()
        );

        mFowSamples.flush();
    }

    @Override
    public void writeBatch(BatchResult result, RangingResult rangingResult) {

        mFowBatches.printf("%s,%d,%d,%d\n",
                rangingResult.getMacAddress().toString(),
                mNumBatch,
                result.getSuccessfulSamples(),
                result.getAttemptedSamples()
        );
        mNumBatch++;
    }


    @Override
    public void finish() {
        mFowSamples.close();
        mFowBatches.close();
    }
}
