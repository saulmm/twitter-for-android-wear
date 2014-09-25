package com.saulmm.tweetwear.wear_tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.saulmm.tweetwear.Constants;

import java.util.ArrayList;

public class SendTimeLineTask extends AsyncTask<Void, Void, Void> {

    private ArrayList<String> contents;
    private GoogleApiClient googleApiClient;

    public SendTimeLineTask(ArrayList<String> contents, GoogleApiClient googleApiClient) {

        this.contents = contents;
        this.googleApiClient = googleApiClient;
    }

    @Override
    protected Void doInBackground(Void... params) {

        PutDataMapRequest dataMap = PutDataMapRequest
            .create(Constants.TIME_LINE_DATA);

        dataMap.getDataMap().putStringArrayList("contents", contents);

        PutDataRequest request = dataMap.asPutDataRequest();

        DataApi.DataItemResult dataItemResult = Wearable.DataApi
                .putDataItem(googleApiClient, request).await();

        Log.d("[DEBUG] SendDataCoolTask - doInBackground",
                "Sent: " + Constants.TIME_LINE_DATA + " status, " + getStatus());

        Log.d ("[DEBUG] SendDataCoolTask - doInBackground",
            "Result: " + dataItemResult.getStatus());

        return null;
    }
}
