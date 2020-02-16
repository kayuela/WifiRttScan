/*
 * Copyright (C) 2018 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.wifirttscan;

import android.Manifest.permission;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.rtt.RangingRequest;
import android.net.wifi.rtt.RangingResult;
import android.net.wifi.rtt.RangingResultCallback;
import android.net.wifi.rtt.WifiRttManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.wifirttscan.compatibility.ApEntity;
import com.example.android.wifirttscan.compatibility.DataBase;
import com.example.android.wifirttscan.result.BatchResult;
import com.example.android.wifirttscan.result.FileRttOutputWriter;
import com.example.android.wifirttscan.result.SampleResult;

import java.util.Date;
import java.util.List;

/**
 * Displays ranging information about a particular access point chosen by the user. Uses {@link
 * Handler} to trigger new requests based on
 */
public class AccessPointRangingResultsActivity extends AppCompatActivity {
    private static final String TAG = "APRRActivity";
    private static final int MILLISECONDS_DELAY_BEFORE_NEW_RANGING_REQUEST_DEFAULT = 1000;
    private static final int MILLISECONDS_DELAY_BEFORE_NEW_BATCH_DEFAULT = 60000;
    private static final int SAMPLE_SIZE_DEFAULT = 500;
    private static final int BATCH_SIZE_DEFAULT = 20;
    private static final int ACTUAL_DISTANCE_DEFAULT = 3;
    public static final String SCAN_RESULT_EXTRA = "com.example.android.wifirttscan.extra.SCAN_RESULT";

    private enum State { STOPPED, RUNNING;}


    private Animation animBlink;
    private TextView mStatusTextView;

    private TextView mRangeLabelTextView;
    private TextView mRangeValueTextView;
    private TextView mStdValueTextView;
    private TextView mStdLabelTextView;

    // UI Elements.
    private TextView mSsidTextView;
    private TextView mBssidTextView;

    private TextView mNumSampleOfTotalTextView;
    private TextView mNumBatchOfTotalTextView;

    private TextView mSampleSizeLabel;
    private TextView mBatchSizeLabel;
    private TextView mActualDistanceLabel;
    private TextView mMillisecondsDelayBeforeNewSampleLabel;
    private TextView mMillisecondsDelayBeforeNewBatchLabel;

    private EditText mSampleSizeEditText;
    private EditText mBatchSizeEditText;
    private EditText mMillisecondsDelayBeforeNewSampleEditText;
    private EditText mMillisecondsDelayBeforeNewBatchEditText;
    private EditText mActualDistanceEditText;
    private Button mStartButton;
    private Button mAbortButton;

    // Non UI variables.
    private ScanResultComp mScanResultComp;
    private String mMAC;

    private int mNumberOfRangeRequests;
    private int mNumberOfSuccessfulRangeRequests;

    private int mMillisecondsDelayBeforeNewSample;
    private int mMillisecondsDelayBeforeNewBatch;

    private int mSampleSize;
    private int mBatchSize;
    private double mActualDistance;

    // Max sample size to calculate average for
    // 1. Distance to device (getDistanceMm) over time
    // 2. Standard deviation of the measured distance to the device (getDistanceStdDevMm) over time
    // Note: A RangeRequest result already consists of the average of 7 readings from a burst,
    // so the average in (1) is the average of these averages.
    private int mCurrentBatch =1;

    private WifiRttManager mWifiRttManager;
    private RttRangingResultCallback mRttRangingResultCallback;

    // Triggers additional RangingRequests with delay (mMillisecondsDelayBeforeNewRangingRequest).
    final Handler mRangeRequestDelayHandler = new Handler();
    private FileRttOutputWriter fileOutputWriter;
    private double mEstimationTime=0;
    private double mEstimationInitialTime=0;
    private double mEstimationFinalTime=0;
    private Date date;

    // State
    private State state;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_point_ranging_results);

        state = State.STOPPED;

        //Add back button
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Initializes UI elements.
        mStartButton = findViewById(R.id.start_button);
        mAbortButton = findViewById(R.id.abort_button);
        mSsidTextView = findViewById(R.id.ssid);
        mBssidTextView = findViewById(R.id.bssid);


        mActualDistanceLabel = findViewById(R.id.real_actual_distance_label);
        mActualDistanceEditText = findViewById(R.id.real_actual_distance_edit_value);

        mRangeLabelTextView = findViewById(R.id.range_label);
        mRangeValueTextView = findViewById(R.id.range_value);
        mStdLabelTextView = findViewById(R.id.std_label);
        mStdValueTextView = findViewById(R.id.std_value);

        mSampleSizeLabel = findViewById(R.id.number_of_samples_label);
        mBatchSizeLabel = findViewById(R.id.number_of_batches_label);
        mSampleSizeLabel.setText("#Samples");
        mBatchSizeLabel.setText("#Batches");

        mSampleSizeEditText = findViewById(R.id.number_of_samples_edit_value);
        mBatchSizeEditText = findViewById(R.id.number_of_batches_edit_value);

        mNumSampleOfTotalTextView = findViewById(R.id.num_samples_of_total_value);
        mNumBatchOfTotalTextView = findViewById(R.id.num_batches_of_total_value);


        mMillisecondsDelayBeforeNewSampleLabel = findViewById(R.id.time_between_samples_label);
        mMillisecondsDelayBeforeNewSampleEditText = findViewById(R.id.time_between_samples_edit_value);

        mMillisecondsDelayBeforeNewBatchLabel = findViewById(R.id.time_between_batches_label);
        mMillisecondsDelayBeforeNewBatchEditText = findViewById(R.id.time_between_batches_edit_value);

        mStatusTextView = findViewById(R.id.status_textview);
        // load the animation
        animBlink = AnimationUtils.loadAnimation(this, R.anim.blink);
        mStatusTextView.setVisibility(View.GONE);

        // Retrieve ScanResult from Intent.
        Intent intent = getIntent();
        mScanResultComp = intent.getParcelableExtra(SCAN_RESULT_EXTRA);

        if (mScanResultComp == null) {
            finish();
        }

        mMAC = mScanResultComp.getBSSID();

        mSsidTextView.setText(mScanResultComp.getSSID());
        mBssidTextView.setText(mScanResultComp.getBSSID());

        mWifiRttManager = (WifiRttManager) getSystemService(Context.WIFI_RTT_RANGING_SERVICE);
        mRttRangingResultCallback = new RttRangingResultCallback();
        date = new Date();

        resetData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPreferences();
        setInputData();
    }

    @Override
    public void onPause(){
        super.onPause();
        refreshValues();
        savePreferences();
    }

    private void resetData() {
        mNumberOfSuccessfulRangeRequests = 0;
        mNumberOfRangeRequests = 0;
        mCurrentBatch = 1;
    }

    private void setInputData() {
        mActualDistanceEditText.setText(String.valueOf(mActualDistance));
        mSampleSizeEditText.setText(String.valueOf(mSampleSize));
        mBatchSizeEditText.setText(String.valueOf(mBatchSize));
        mMillisecondsDelayBeforeNewSampleEditText.setText(String.valueOf(mMillisecondsDelayBeforeNewSample));
        mMillisecondsDelayBeforeNewBatchEditText.setText(String.valueOf(mMillisecondsDelayBeforeNewBatch));
    }

    public void loadPreferences(){
        SharedPreferences prefs = getSharedPreferences(SharedConf.MEASUREMENT_PROPERTIES,Context.MODE_PRIVATE);
        this.mSampleSize = prefs.getInt(SharedConf.NUM_SAMPLES, SAMPLE_SIZE_DEFAULT);
        this.mBatchSize = prefs.getInt(SharedConf.NUM_BATCHES, BATCH_SIZE_DEFAULT);
        this.mMillisecondsDelayBeforeNewSample = prefs.getInt(SharedConf.TIME_SAMPLES, MILLISECONDS_DELAY_BEFORE_NEW_RANGING_REQUEST_DEFAULT);
        this.mMillisecondsDelayBeforeNewBatch = prefs.getInt(SharedConf.TIME_BATCHES, MILLISECONDS_DELAY_BEFORE_NEW_BATCH_DEFAULT);
        this.mActualDistance = prefs.getFloat(SharedConf.ACTUAL_DISTANCE, ACTUAL_DISTANCE_DEFAULT);

        //this.mSavedPreferences = prefs.getBoolean("preferencesSaved", false);
    }

    public void savePreferences(){
        refreshValues();

        SharedPreferences prefs = getSharedPreferences(SharedConf.MEASUREMENT_PROPERTIES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        //editor.putBoolean("preferencesSaved",true);
        editor.putInt(SharedConf.NUM_SAMPLES, mSampleSize);
        editor.putInt(SharedConf.NUM_BATCHES, mBatchSize);
        editor.putInt(SharedConf.TIME_SAMPLES, mMillisecondsDelayBeforeNewSample);
        editor.putInt(SharedConf.TIME_BATCHES, mMillisecondsDelayBeforeNewBatch);
        editor.putFloat(SharedConf.ACTUAL_DISTANCE, (float) mActualDistance);
        editor.commit();
        Toast.makeText(this,"Saving Preferences",Toast.LENGTH_SHORT).show();

    }

    private void refreshValues() {
        mActualDistance = Double.parseDouble(mActualDistanceEditText.getText().toString());
        mSampleSize = Integer.parseInt(mSampleSizeEditText.getText().toString());
        mBatchSize = Integer.parseInt(mBatchSizeEditText.getText().toString());
        mMillisecondsDelayBeforeNewSample = Integer.parseInt(mMillisecondsDelayBeforeNewSampleEditText.getText().toString());
        mMillisecondsDelayBeforeNewBatch = Integer.parseInt(mMillisecondsDelayBeforeNewBatchEditText.getText().toString());
    }

    private void startRangingRequest() {
        // Permission for fine location should already be granted via MainActivity (you can't get
        // to this class unless you already have permission. If they get to this class, then disable
        // fine location permission, we kick them back to main activity.

        synchronized (state) {
            if (state != State.RUNNING) return;
        }

        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            finish();
        }

        mNumberOfRangeRequests++;
        //fileOutputWriter.init();
        mEstimationInitialTime=date.getTime();

        RangingRequest rangingRequest =
                new RangingRequest.Builder().addAccessPoint(mScanResultComp.getScanResult()).build();

        mWifiRttManager.startRanging(
                rangingRequest, getApplication().getMainExecutor(), mRttRangingResultCallback);
    }

    // For the back button
    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if (id == android.R.id.home){
            // Ends this activity
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }



    public void onAbortButtonClick(View view) {
        synchronized (state) {
            if (state == State.STOPPED) return;
            state = State.STOPPED;
        }

        mRttRangingResultCallback.abort();
        setStartButtonEnabled(true);
        resetData();
        Toast.makeText(this, getString(R.string.measurement_process_aborted), Toast.LENGTH_LONG).show();
    }

    public void onStartButtonClick(View view) {
        synchronized (state) {
            if (state == State.RUNNING) return;
            state = State.RUNNING;
        }
        resetData();
        refreshValues();
        createFiles();
        setStartButtonEnabled(false);
        startRangingRequest();
    }

    private void createFiles() {
        fileOutputWriter = new FileRttOutputWriter(this, mActualDistance);
    }

    private void setStartButtonEnabled(boolean state) {
        mStartButton.setEnabled(state);
        mAbortButton.setEnabled(!state);

        mActualDistanceLabel.setEnabled(state);
        mActualDistanceEditText.setEnabled(state);

        mSampleSizeLabel.setEnabled(state);
        mSampleSizeEditText.setEnabled(state);

        mBatchSizeLabel.setEnabled(state);
        mBatchSizeEditText.setEnabled(state);

        mMillisecondsDelayBeforeNewSampleLabel.setEnabled(state);
        mMillisecondsDelayBeforeNewSampleEditText.setEnabled(state);
        mMillisecondsDelayBeforeNewBatchLabel.setEnabled(state);
        mMillisecondsDelayBeforeNewBatchEditText.setEnabled(state);

    }

    // Class that handles callbacks for all RangingRequests and issues new RangingRequests.
    private class RttRangingResultCallback extends RangingResultCallback {

        private Runnable mCurrentRangeRequestRunnable;

        private Runnable sampleRequestOnSameBatch() {
            return () -> startRangingRequest();
        }

        private Runnable sampleRequestOnNewBatch() {
            return () -> {
                mStatusTextView.clearAnimation();
                mStatusTextView.setVisibility(View.GONE);
                startRangingRequest();
            };
        }

        private void queueNextRangingRequest(final Runnable runnable, int delay) {
            //fileOutputWriter.end();
            mCurrentRangeRequestRunnable = runnable;

            mRangeRequestDelayHandler.postDelayed(
                    mCurrentRangeRequestRunnable,
                    delay );
        }

        public void abort() {
            mRangeRequestDelayHandler.removeCallbacks(mCurrentRangeRequestRunnable);
        }

        @Override
        public void onRangingFailure(int code) {
            Log.d(TAG, "onRangingFailure() code: " + code);
            queueNextRangingRequest(sampleRequestOnSameBatch(), mMillisecondsDelayBeforeNewSample);
        }

        @Override
        public void onRangingResults(@NonNull List<RangingResult> list) {
            Log.d(TAG, "onRangingResults(): " + list);
            mEstimationFinalTime=date.getTime();

            // Because we are only requesting RangingResult for one access point (not multiple
            // access points), this will only ever be one. (Use loops when requesting RangingResults
            // for multiple access points.)
            if (list.size() == 1) {

                RangingResult rangingResult = list.get(0);
                if (mMAC.equals(rangingResult.getMacAddress().toString())) {
                    synchronized (state) {
                        if (state != State.RUNNING) return;
                    }

                    if (rangingResult.getStatus() == RangingResult.STATUS_SUCCESS) {

                        //DATABASE PROCEDURE
                        //Adding the new AP to the DataBase
                        ApEntity newAP = new ApEntity();
                        newAP.setSsid(mMAC);
                        newAP.setmComp(true);
                        DataBase.getDataBase(getApplication()).apDao().insertAP(newAP);
                        //FINISH OF THE DATABASE PROCEDURE

                        //Update its proved compatibility attribute
                        mScanResultComp.is80211mcResponder(true);

                        mNumberOfSuccessfulRangeRequests++;

                        String textSample=mNumberOfSuccessfulRangeRequests+"/"+mSampleSize;
                        String textBatch= mCurrentBatch +"/"+mBatchSize;
                        mNumSampleOfTotalTextView.setText(textSample);
                        mNumBatchOfTotalTextView.setText(textBatch);

                        mEstimationTime = mEstimationFinalTime - mEstimationInitialTime;
                        fileOutputWriter.writeSample(SampleResult.from(rangingResult,mEstimationTime));

                        // Showing the results into the view
                        mRangeValueTextView.setText(String.valueOf(rangingResult.getDistanceMm()/1000f));
                        mStdValueTextView.setText(String.valueOf(rangingResult.getDistanceStdDevMm()/1000f));

                    } else if (rangingResult.getStatus()
                            == RangingResult.STATUS_RESPONDER_DOES_NOT_SUPPORT_IEEE80211MC) {

                        final String msg = getString(R.string.ieee80211mc_not_supported);
                        Log.d(TAG, msg);

                        Toast.makeText(AccessPointRangingResultsActivity.this, msg, Toast.LENGTH_LONG).show();

                        setStartButtonEnabled(true);
                        resetData();
                        return;

                    } else {
                        Log.d(TAG, "RangingResult failed.");
                    }
                } else {
                    Toast.makeText(
                                    getApplicationContext(),
                                    R.string
                                            .mac_mismatch_message_activity_access_point_ranging_results,
                                    Toast.LENGTH_LONG)
                            .show();
                }
            }

            if (mNumberOfSuccessfulRangeRequests == mSampleSize) {
                fileOutputWriter.writeBatch(BatchResult.from(mNumberOfSuccessfulRangeRequests, mNumberOfRangeRequests));

                if (mCurrentBatch < mBatchSize) {
                    // Reset data for the next batch
                    mNumberOfSuccessfulRangeRequests=0;
                    mNumberOfRangeRequests = 0;

                    // Increasing the batch number
                    mCurrentBatch++;

                    // Setting the waiting message
                    mStatusTextView.setVisibility(View.VISIBLE);
                    mStatusTextView.setAnimation(animBlink);
                    mStatusTextView.setText(getString(R.string.waiting_for_new_batch, mMillisecondsDelayBeforeNewBatch / 1000));

                    // Programming the next sample according to the inter-batch delay
                    queueNextRangingRequest(sampleRequestOnNewBatch(), mMillisecondsDelayBeforeNewBatch);
                }
                else {
                    fileOutputWriter.finish();
                    setStartButtonEnabled(true);
                    Toast.makeText(AccessPointRangingResultsActivity.this, R.string.inform_rtt_process_ended_successfully, Toast.LENGTH_SHORT).show();
                }
            } else queueNextRangingRequest(sampleRequestOnSameBatch(), mMillisecondsDelayBeforeNewSample);
        }

    }
}
