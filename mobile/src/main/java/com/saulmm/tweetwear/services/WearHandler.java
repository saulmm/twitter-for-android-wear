package com.saulmm.tweetwear.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.saulmm.tweetwear.Constants;
import com.saulmm.tweetwear.helpers.TwitterHelper;
import com.saulmm.tweetwear.helpers.TwitterOperationListener;
import com.saulmm.tweetwear.wear_tasks.SendMessageTask;
import com.saulmm.tweetwear.wear_tasks.SendTimeLineTask;

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


        twHelper = new TwitterHelper(this);
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

        if (!twHelper.isUserLogged()) {

            Toast.makeText(this, "Please open wear app and log in with twitter", Toast.LENGTH_SHORT)
                .show();

            sendMessageToWearable(Constants.MSG_NOT_LOGGED);
            return;
        }

        // Message: /tweets/hi/
        if (msg.equals(Constants.MSG_SALUDATE)) {

            sendMessageToWearable(Constants.MSG_AVAILABLE);
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

        new SendMessageTask(message, googleApiClient, connectedNode)
            .execute();
    }
}
