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
import android.content.pm.PackageManager;
import android.net.wifi.rtt.RangingRequest;
import android.net.wifi.rtt.RangingResult;
import android.net.wifi.rtt.RangingResultCallback;
import android.net.wifi.rtt.WifiRttManager;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.wifirttscan.result.BatchResult;
import com.example.android.wifirttscan.result.FileRttOutputWriter;
import com.example.android.wifirttscan.result.SampleResult;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * Displays ranging information about a particular access point chosen by the user. Uses {@link
 * Handler} to trigger new requests based on
 */
public class AccessPointRangingResultsActivity extends AppCompatActivity {
    private static final String TAG = "APRRActivity";

    public static final String SCAN_RESULT_EXTRA =
            "com.example.android.wifirttscan.extra.SCAN_RESULT";

    private static final int MILLISECONDS_DELAY_BEFORE_NEW_RANGING_REQUEST_DEFAULT = 1000;

    // UI Elements.
    private TextView mSsidTextView;
    private TextView mBssidTextView;

    private TextView mNumSampleOfTotalTextView;
    private TextView mNumBatchOfTotalTextView;

    private TextView mSampleSizeTextView;
    private TextView mBatchSizeTextView;
    private TextView mActualDistanceTextView;

    private EditText mSampleSizeEditText;
    private EditText mBatchSizeEditText;
    private EditText mMillisecondsDelayBeforeNewSampleEditText;
    private EditText mMillisecondsDelayBeforeNewBatchEditText;

    // Non UI variables.
    private ScanResultComp mScanResultComp;
    private String mMAC;

    private int mNumberOfRangeRequests;
    private int mNumberOfSuccessfulRangeRequests;

    private int mMillisecondsDelayBeforeNewSample;
    private int mMillisecondsDelayBeforeNewBatch;

    private int mSampleSize;
    private int mBatchSize;
    private int mActualDistance;

    // Max sample size to calculate average for
    // 1. Distance to device (getDistanceMm) over time
    // 2. Standard deviation of the measured distance to the device (getDistanceStdDevMm) over time
    // Note: A RangeRequest result already consists of the average of 7 readings from a burst,
    // so the average in (1) is the average of these averages.
    private int batchActual=0;

    private WifiRttManager mWifiRttManager;
    private RttRangingResultCallback mRttRangingResultCallback;

    // Triggers additional RangingRequests with delay (mMillisecondsDelayBeforeNewRangingRequest).
    final Handler mRangeRequestDelayHandler = new Handler();
    private FileRttOutputWriter fileOutputWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_point_ranging_results);

        // Initializes UI elements.
        mSsidTextView = findViewById(R.id.ssid);
        mBssidTextView = findViewById(R.id.bssid);


        mActualDistanceTextView = findViewById(R.id.actual_distance_edit_value);

        mSampleSizeTextView = findViewById(R.id.number_of_samples_label);
        mBatchSizeTextView = findViewById(R.id.number_of_batches_label);

        mSampleSizeEditText = findViewById(R.id.number_of_samples_edit_value);
        mBatchSizeEditText = findViewById(R.id.number_of_batches_edit_value);


        mNumSampleOfTotalTextView = findViewById(R.id.num_samples_of_total_label);
        mNumBatchOfTotalTextView = findViewById(R.id.num_batches_of_total_label);


        mMillisecondsDelayBeforeNewSampleEditText =
                findViewById(R.id.time_between_samples_edit_value);
        mMillisecondsDelayBeforeNewSampleEditText.setText(
                MILLISECONDS_DELAY_BEFORE_NEW_RANGING_REQUEST_DEFAULT + "");

        mMillisecondsDelayBeforeNewBatchEditText =
                findViewById(R.id.time_between_batches_edit_value);
        mMillisecondsDelayBeforeNewSampleEditText.setText("");

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

        resetData();

        startRangingRequest();
    }

    private void resetData() {
        mSampleSize = Integer.parseInt(mSampleSizeEditText.getText().toString());
        mBatchSize = Integer.parseInt(mBatchSizeEditText.getText().toString());

        mMillisecondsDelayBeforeNewSample =
                Integer.parseInt(
                        mMillisecondsDelayBeforeNewSampleEditText.getText().toString());

        mMillisecondsDelayBeforeNewBatch =
                Integer.parseInt(
                        mMillisecondsDelayBeforeNewBatchEditText.getText().toString());

        mActualDistance = Integer.parseInt(mActualDistanceTextView.getText().toString());

        // Initialize the FileOutputWriter
        try {
            fileOutputWriter = new FileRttOutputWriter(this, mActualDistance);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        mNumberOfSuccessfulRangeRequests = 0;
        mNumberOfRangeRequests = 0;
    }

    private void startRangingRequest() {
        // Permission for fine location should already be granted via MainActivity (you can't get
        // to this class unless you already have permission. If they get to this class, then disable
        // fine location permission, we kick them back to main activity.
        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            finish();
        }

        mNumberOfRangeRequests++;
        fileOutputWriter.init();

        RangingRequest rangingRequest =
                new RangingRequest.Builder().addAccessPoint(mScanResultComp.getScanResult()).build();

        mWifiRttManager.startRanging(
                rangingRequest, getApplication().getMainExecutor(), mRttRangingResultCallback);
    }

    public void onResetButtonClick(View view) {
        resetData();
    }

    // Class that handles callbacks for all RangingRequests and issues new RangingRequests.
    private class RttRangingResultCallback extends RangingResultCallback {

        private void queueNextRangingRequest(int delay) {
            fileOutputWriter.end();
            mRangeRequestDelayHandler.postDelayed(
                    new Runnable() {
                        @Override
                        public void run() {
                            startRangingRequest();
                        }
                    },
                    delay );
        }

        @Override
        public void onRangingFailure(int code) {
            Log.d(TAG, "onRangingFailure() code: " + code);
            queueNextRangingRequest(mMillisecondsDelayBeforeNewSample);
        }

        @Override
        public void onRangingResults(@NonNull List<RangingResult> list) {
            Log.d(TAG, "onRangingResults(): " + list);

            // Because we are only requesting RangingResult for one access point (not multiple
            // access points), this will only ever be one. (Use loops when requesting RangingResults
            // for multiple access points.)
            if (list.size() == 1) {

                RangingResult rangingResult = list.get(0);

                if (mMAC.equals(rangingResult.getMacAddress().toString())) {

                    if (rangingResult.getStatus() == RangingResult.STATUS_SUCCESS) {

                        mNumberOfSuccessfulRangeRequests++;
                        mNumSampleOfTotalTextView.setText(mNumberOfRangeRequests+"/"+mSampleSize);
                        mNumBatchOfTotalTextView.setText(batchActual+"/"+mBatchSize);

                        try {
                            fileOutputWriter.writeSample(SampleResult.from(rangingResult));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else if (rangingResult.getStatus()
                            == RangingResult.STATUS_RESPONDER_DOES_NOT_SUPPORT_IEEE80211MC) {
                        Log.d(TAG, "RangingResult failed (AP doesn't support IEEE80211 MC.");

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
                try {
                    fileOutputWriter.writeBatch(BatchResult.from(mNumberOfSuccessfulRangeRequests, mNumberOfRangeRequests));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (batchActual < mBatchSize) {
                    queueNextRangingRequest(mMillisecondsDelayBeforeNewBatch);
                    batchActual++;
                }
            } else queueNextRangingRequest(mMillisecondsDelayBeforeNewSample);
        }
    }
}
