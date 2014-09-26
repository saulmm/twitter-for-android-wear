package com.saulmm.tweetwear;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.saulmm.tweetwear.enums.TwitterAction;
import com.saulmm.tweetwear.listeners.ActionListener;
import com.saulmm.tweetwear.listeners.WearTwitterServiceListener;

import java.util.ArrayList;

import static com.google.android.gms.common.api.GoogleApiClient.Builder;
import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import static com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import static com.google.android.gms.wearable.DataApi.DataListener;
import static com.google.android.gms.wearable.MessageApi.MessageListener;


public class DeviceHandler implements ConnectionCallbacks, OnConnectionFailedListener,
        DataListener, MessageListener {

    private static boolean isConnected;
    private boolean isTwitterServiceIsRunning;
    public static DeviceHandler instance;

    private ArrayList<Node> wearNodes;
    private GoogleApiClient googleApiClient;

    private ActionListener onRetweetListener;
    private WearTwitterServiceListener deviceListener;


    public static DeviceHandler getInstance () {

        if (instance == null) {
            instance = new DeviceHandler();
        }

        return instance;
    }


    public boolean isConnected() {

        return isConnected;
    }


    private DeviceHandler() {

        Log.d ("[DEBUG] DeviceService - DeviceService",
            "Starting handFel manager: isConnected: "+isConnected);
    }


    public void setDeviceListener(WearTwitterServiceListener deviceListener) {

        this.deviceListener = deviceListener;
    }


    public void setOnActionListener(ActionListener onRetweetListener) {

        this.onRetweetListener = onRetweetListener;
    }


    public void initGoogleApiClient(Context context) {

        googleApiClient = new Builder(context.getApplicationContext())
            .addApi(Wearable.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .build();
    }


    public void connectGoogleApiClient () {

        Log.d ("[INFO] DeviceService - connectGoogleApiClient",
                "Connecting to google wear api");

        googleApiClient.connect();
    }


    @Override
    public void onConnected(Bundle bundle) {

        Log.i ("[INFO] DeviceService - onConnected", "Android wear connected to the wear google api...");
        Log.i ("[INFO] DeviceService - onConnected", "Listeners on DeviceService registered");

        Wearable.DataApi.addListener(googleApiClient, this);
        Wearable.MessageApi.addListener(googleApiClient, this);
        Wearable.NodeApi.addListener(googleApiClient, nodeListener);

        Log.d ("[INFO] DeviceService - onConnected", "Getting nodes...");
        new GetNodesTasks().execute();
    }


    @Override
    public void onConnectionSuspended(int i) {

        Log.e ("[ERROR] DeviceService - onConnectionSuspended",
            "The connection has been suspended");
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.d ("[DEBUG] DeviceService - onConnectionFailed",
            "The connection has failed");
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        for (DataEvent event: dataEvents) {

            Log.d("[DEBUG] DeviceService - onDataChanged",
                    "Event received: " + event.getDataItem().getUri());

            String eventUri = event.getDataItem().getUri().toString();

            if (eventUri.contains ("/twitter/timeline")) {
                DataMapItem dataItem = DataMapItem.fromDataItem (event.getDataItem());
                ArrayList <String> tweets = dataItem.getDataMap().getStringArrayList("contents");

                Log.d("[DEBUG] DeviceService - onDataChanged", "Sending timeline to the listener");

                deviceListener.onTimeLimeReceived(tweets);
            }
        }
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        String messagePath = messageEvent.getPath();

        // Message: /tweets/operation/ok
        if (messagePath.equals(Constants.MSG_OP_OK)) {
            onRetweetListener.onActionOK();
        }

        // Message: /tweets/operation/fail
        if (messagePath.equals(Constants.MSG_OP_FAIL)) {
            onRetweetListener.onActionFail();
        }

        // Message: /tweets/state/no_internet
        if (messagePath.equals(Constants.MSG_NO_ITERNET)) {
            deviceListener.onProblem(messagePath);
        }

        // Message: /tweets/state/no_login
        if (messagePath.equals(Constants.MSG_NO_LOGIN)) {
            deviceListener.onProblem(messagePath);
        }

        // Message: /tweets/state/available
        if (messagePath.equals("/tweets/state/available")) {
            isTwitterServiceIsRunning = true;
        }
    }


    class SendMessageTask extends AsyncTask <Void, Void, Void> {

        private final String message;

        public SendMessageTask(String message) {
            this.message = message;
        }

        @Override
        protected Void doInBackground(Void... params) {
            Log.d ("[DEBUG] SendMessageTask - DeviceService - doInBackground",
                "Sending message: "+message);

            Node firstDeviceConnceted = wearNodes.get (0);

            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                    googleApiClient, firstDeviceConnceted.getId(), message, null).await();

            if (!result.getStatus ().isSuccess ()) {
                Log.d("[ERROR][DEBUG] SendMessageTask - DeviceService - doInBackground - sendHelloToWearable",
                    "Failed sending message");

            } else {
                Log.d ("[DEBUG] SendMessageTask - DeviceService - doInBackground",
                    "Message: "+message+ " sent");
            }

            return null;
        }
    }


    class GetNodesTasks extends AsyncTask <Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            NodeApi.GetConnectedNodesResult nodes =
                    Wearable.NodeApi.getConnectedNodes(googleApiClient)
                            .await();

            wearNodes = (ArrayList<Node>) nodes.getNodes();
            isConnected = !nodes.getNodes().isEmpty();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            super.onPostExecute(aVoid);

            new SendMessageTask (Constants.MSG_SALUDATE).execute();

            // Wait 3 seconds to see if the wear service is running
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (isTwitterServiceIsRunning) {
                        deviceListener.onWearReady (true);

                    } else {
                        deviceListener.onProblem ("Service not running");
                    }

                }

            }, 3000);

        }
    }


    public void requestTwitterTimeline () {
        new SendMessageTask(Constants.TIME_LINE_DATA).execute();
    }


    public void requestAction(TwitterAction twAction, String tweetID) {

        if (twAction == TwitterAction.RETWEET) {
            new SendMessageTask(Constants.MSG_RETWEET + tweetID).execute();

        } else if (twAction == TwitterAction.FAVORITE) {
            new SendMessageTask(Constants.MSG_FAVORITE + tweetID).execute();
        }
    }
    
    
    private NodeApi.NodeListener nodeListener = new NodeApi.NodeListener() {
        @Override
        public void onPeerConnected(Node node) {
            Log.i ("[INFO] DeviceService - onPeerConnected",
                    "The wearable has been connected");
            
            isConnected = true;
        }

        @Override
        public void onPeerDisconnected(Node node) {
            Log.i ("[INFO] DeviceService - onPeerDisconnected",
                    "The wearable has been disconnected");

            isConnected = false;
        }
    };
}