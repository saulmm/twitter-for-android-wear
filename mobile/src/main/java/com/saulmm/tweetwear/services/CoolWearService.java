package com.saulmm.tweetwear.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.saulmm.tweetwear.Constants;
import com.saulmm.tweetwear.helpers.TwitterHelper;
import com.saulmm.tweetwear.listeners.TimeLineListener;
import com.saulmm.tweetwear.tasks.SendDataCoolTask;
import com.saulmm.tweetwear.tasks.SendMessageTask;

import java.util.ArrayList;
import java.util.HashSet;


public class CoolWearService extends WearableListenerService  {

    private TwitterHelper twHelper;
    private GoogleApiClient googleApiClient;

    @Override
    public void onCreate() {

        super.onCreate();

        SharedPreferences preferences = getSharedPreferences(
            Constants.PREFS, Context.MODE_PRIVATE);

        // Get an instance of an authorized tw helper
        twHelper = new TwitterHelper (
            preferences.getString ("ACCESS_TOKEN", ""),
            preferences.getString ("ACCESS_TOKEN_SECRET", ""));

        // Init and connect the client to use the wear api
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks (gConnectionCallbacks)
                .addApi(Wearable.API)
                .build();

        googleApiClient.connect();
    }


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        super.onMessageReceived(messageEvent);

        String msg = messageEvent.getPath();

        // Message: /tweets/hi/
        if (msg.equals(Constants.MSG_SALUDATE)) {

            Node connectedNode = getConnectedNode(googleApiClient);

            if (connectedNode != null)
                new SendMessageTask(Constants.MSG_AVAILABLE, googleApiClient, connectedNode)
                    .execute();

            else
               Log.e ("[ERROR] CoolWearService - onMessageReceived", "No nodes detected ");
        }

        // Message: /tweets/timeline
        if (msg.equals(Constants.MSG_LOAD_LAST_TIMELINE)) {

            twHelper.requestTwitterTimeLine(timeLineListener);


        // /tweets/retweet/
        } else if (msg.startsWith(Constants.MSG_RETWEET)) {
            Log.d("[DEBUG] WearService - onMessageReceived", "Retweeting");

            String twID = msg.split("/")[3];

//            new TwitterOperationTask(twID, twHelper.getTwClient(),
//                    googleApiClient, connectedNode).execute(true);
        }
    }


    private Node getConnectedNode(GoogleApiClient googleApiClient) {

        HashSet<String> results= new HashSet<String>();
        NodeApi.GetConnectedNodesResult nodes =
            Wearable.NodeApi.getConnectedNodes(googleApiClient).await();

        Node connectedNode = (nodes.getNodes().isEmpty()) ? null : nodes.getNodes().get(0);
        return connectedNode;
    }


    private GoogleApiClient.ConnectionCallbacks gConnectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {

        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };


    private TimeLineListener timeLineListener = new TimeLineListener() {

        @Override
        public void onTimeLineReceived(ArrayList<String> tweets) {

            new SendDataCoolTask (tweets, googleApiClient)
                .execute();
        }

        @Override
        public void onTimeLineFailed(String errorMessage) {

            Log.d ("[DEBUG] CoolWearService - onTimeLineFailed",
                "Error");
        }
    };
}
