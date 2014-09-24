package com.saulmm.tweetwear.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.saulmm.tweetwear.Constants;
import com.saulmm.tweetwear.helpers.TwitterHelper;
import com.saulmm.tweetwear.helpers.TwitterOperationListener;
import com.saulmm.tweetwear.tasks.SendMessageTask;
import com.saulmm.tweetwear.tasks.SendTimeLineTask;

import java.util.ArrayList;


public class WearHandler extends WearableListenerService  {

    private TwitterHelper twHelper;
    private GoogleApiClient googleApiClient;
    private Node connectedNode;

    @Override
    public void onCreate() {

        super.onCreate();

        SharedPreferences preferences = getSharedPreferences(
            Constants.PREFS, Context.MODE_PRIVATE);

        // Get an instance of an authorized tw helper
        twHelper = new TwitterHelper (
            preferences.getString ("ACCESS_TOKEN", ""),
            preferences.getString ("ACCESS_TOKEN_SECRET", ""));

        twHelper.setTwitterListener(twitterListener);

        // Init and connect the client to use the wear api
        googleApiClient = new GoogleApiClient.Builder(this)
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

            new RequestConnectedNodes().execute();
        }

        // Message: /tweets/timeline
        if (msg.equals(Constants.MSG_LOAD_LAST_TIMELINE)) {

            twHelper.requestTwitterTimeLine(twitterListener);

        // Message /tweets/retweet/<tweet id>
        } else if (msg.startsWith(Constants.MSG_RETWEET)) {

            String twID = msg.split("/")[3];
            twHelper.retweet(twID);

        // Message /tweets/favorite/<tweet id>
        } else if (msg.startsWith(Constants.MSG_FAVORITE)) {

            String twID = msg.split("/")[3];
            twHelper.markTweetAsFavorite(twID);
        }
    }



    private class  RequestConnectedNodes extends AsyncTask<Void, Void, Node> {

        @Override
        protected Node doInBackground(Void... params) {

            NodeApi.GetConnectedNodesResult nodes =
                    Wearable.NodeApi.getConnectedNodes(googleApiClient).await();

            if (nodes.getNodes().isEmpty())
                return null;

            else
                return nodes.getNodes().get(0);
        }

        @Override
        protected void onPostExecute(Node node) {

            super.onPostExecute(node);
            connectedNode = node;

            sendMessageToWearable(Constants.MSG_AVAILABLE);
        }
    }


    private TwitterOperationListener twitterListener = new TwitterOperationListener() {

        @Override
        public void onTimeLineReceived(ArrayList<String> tweets) {

            new SendTimeLineTask(tweets, googleApiClient)
                    .execute();
        }

        @Override
        public void onTwitterOperationSuccess(boolean success) {

            String messageToWear = (success)
                ? Constants.MSG_RETWEET_OK
                : Constants.MSG_RETWEET_FAIL;

            sendMessageToWearable(messageToWear);
        }

        @Override
        public void onTwitterFail(String errorMessage) {

            Log.e ("[ERROR] WearHandler - onTwitterFail", "Error: "+errorMessage);
        }
    };


    public void sendMessageToWearable (String message) {

        Log.d ("[DEBUG] WearHandler - sendMessageToWearable", "Is connectednode null: "+(connectedNode == null));

        new SendMessageTask(message, googleApiClient, connectedNode)
            .execute();
    }
}
