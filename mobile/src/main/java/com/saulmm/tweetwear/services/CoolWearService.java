package com.saulmm.tweetwear.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.saulmm.tweetwear.Constants;
import com.saulmm.tweetwear.helpers.TwitterHelper;
import com.saulmm.tweetwear.listeners.TimeLineListener;
import com.saulmm.tweetwear.tasks.GetTwitterTimeline;
import com.saulmm.tweetwear.tasks.SendDataCoolTask;
import com.saulmm.tweetwear.tasks.SendMessageTask;

import java.util.ArrayList;
import java.util.HashSet;


public class CoolWearService extends WearableListenerService  {

    private TwitterHelper twHelper;
    private Node connectedNode;
    private GoogleApiClient googleApiClient;

    @Override
    public void onCreate() {

        super.onCreate();

        SharedPreferences preferences = getSharedPreferences(
            Constants.PREFS, Context.MODE_PRIVATE);

        // Init a twitter helper instance
        String aToken = preferences.getString ("ACCESS_TOKEN", "");
        String aTokenSecret = preferences.getString ("ACCESS_TOKEN_SECRET", "");
        twHelper = new TwitterHelper(aToken, aTokenSecret);

        initGoogleApiClient();
    }


    private void initGoogleApiClient() {

        googleApiClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks (gConnectionCallbacks)
            .addApi(Wearable.API)
            .build();

        googleApiClient.connect();
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        super.onDataChanged(dataEvents);

        Log.d("[DEBUG] CoolWearService - onDataChanged", "HII");
    }

    @Override
    public void onPeerConnected(Node connectedNode) {

        super.onPeerConnected(connectedNode);
        this.connectedNode = connectedNode;

        Log.d ("[DEBUG] CoolWearService - onPeerConnected", "Peer ID: "+connectedNode.getId());

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        super.onMessageReceived(messageEvent);

        String msg = messageEvent.getPath();
        Log.d("[DEBUG] CoolWearService - onMessageReceived", "MS received: " + msg);

        // Message: /tweets/hi/
        if (msg.equals(Constants.MSG_SALUDATE)) {

            try {
                Node connectedNode = getNodes(googleApiClient);

                new SendMessageTask(Constants.MSG_AVAILABLE, googleApiClient,connectedNode)
                    .execute();

            } catch (NullPointerException e) {
                Log.e("[ERROR] WearService - onMessageReceived",
                    "" + msg + " - " + e.getMessage());
            }
        }

        // Message: /tweets/timeline
        if (msg.equals(Constants.MSG_LOAD_LAST_TIMELINE)) {

            new GetTwitterTimeline(twHelper.getTwClient(), timeLineListener)
                .execute();

        // /tweets/retweet/
        } else if (msg.startsWith(Constants.MSG_RETWEET)) {
            Log.d("[DEBUG] WearService - onMessageReceived", "Retweeting");

            String twID = msg.split("/")[3];

//            new TwitterOperationTask(twID, twHelper.getTwClient(),
//                    googleApiClient, connectedNode).execute(true);
        }
    }


    private Node getNodes(GoogleApiClient googleApiClient) {

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
