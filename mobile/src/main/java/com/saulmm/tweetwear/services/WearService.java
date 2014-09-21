package com.saulmm.tweetwear.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.saulmm.tweetwear.Config;
import com.saulmm.tweetwear.Constants;
import com.saulmm.tweetwear.listeners.TimeLineListener;
import com.saulmm.tweetwear.tasks.GetTwitterTimeline;

import java.util.ArrayList;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import static com.google.android.gms.common.api.GoogleApiClient.Builder;
import static com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import static com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;


public class WearService extends Service {

    private GoogleApiClient googleApiClient;
    private ArrayList<Node> connectedNodes;
    private boolean isConnected;

    // Twitter stuff
    private Twitter twitterClient;

    // Other stuff
    private SharedPreferences preferences;

    // Tasks
    private AsyncTask <Void, Void, String> authorizationURLTask;
    private AsyncTask <Void, Void, String> accessTokenTask;


    public boolean isConnected() {
        return isConnected;
    }


    /**
     * Cancel all running if there is any
     */
    public void cancelAllTasks() {

        if (authorizationURLTask != null) {
            authorizationURLTask.cancel (false);
        }

        if (accessTokenTask != null) {
            accessTokenTask.cancel (false);
        }
    }


    public class LocalBinder extends Binder {
        public WearService getService() {
            return WearService.this;
        }
    }


    @Override
    public void onCreate() {

        super.onCreate();

        // Init & connect gApiClient
        googleApiClient = new Builder(this)
            .addConnectionCallbacks (gConnectionCallbacks)
            .addOnConnectionFailedListener (gConnectionFailed)
            .addApi(Wearable.API)
            .build();

        googleApiClient.connect();

        Log.i("[INFO] WearService - onCreate",
                "Wear service started");

        // Init preferences
        preferences = getSharedPreferences(Constants.PREFS,
            Context.MODE_PRIVATE);

        registerForNetworkChanges();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    /**
     * Register a receiver to know when user has no connectivity, when a
     * connectivity event is fired I get notified on the 'networkStateReceiver'
     */
    private void registerForNetworkChanges() {

        IntentFilter networkFilter = new IntentFilter (ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkStateReceiver, networkFilter);
    }


    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }


    public void addNodeApiListener (NodeApi.NodeListener listener) {
        Wearable.NodeApi.addListener (googleApiClient, listener);
    }


    private final ConnectionCallbacks gConnectionCallbacks = new ConnectionCallbacks() {
        @Override
        public void onConnected(Bundle bundle) {

            Wearable.MessageApi.addListener (googleApiClient, wearMessageListener);

            Log.i ("[INFO] WearService - onConnected", "Message listener added...");
        }

        @Override
        public void onConnectionSuspended(int i) {

            Log.e ("[ERROR] WearService - onConnectionSuspended",
                    "The connection has been suspended, parameter: "+i);
        }
    };


    /**
     * This receiver checks network updates, if it detects no internet
     * a message to the connected wearable will be sent
     */
    private final BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            boolean noInternetConnection;

            if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                noInternetConnection = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);

                Log.d("[DEBUG] WearService - onReceive", "Connectivity change, is the user connected? " +
                        !noInternetConnection);

                if (noInternetConnection)
                    new SendMessageTask(Constants.MSG_NO_ITERNET)
                            .execute();

            } else if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                Intent startServiceIntent = new Intent(context, WearService.class);
                context.startService(startServiceIntent);
            }
        }
    };



    private final OnConnectionFailedListener gConnectionFailed = new OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {

            Log.e("[ERROR] WearService - onConnectionFailed",
                    "The wear connection had failed, error code: " + connectionResult.getErrorCode());
        }
    };



    class SendMessageTask extends AsyncTask <Void, Void, Void> {

        private final String message;

        SendMessageTask(String message) {
            this.message = message;
        }

        @Override
        protected Void doInBackground(Void... params) {

            Log.d ("[DEBUG] SendMessageTask - doInBackground",
                    "Sending message: "+message);

            String activityPath = message;

            MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                    googleApiClient, connectedNodes.get(0).getId(), activityPath, null).await();

            if (!result.getStatus().isSuccess()) {
                Log.e ("[ERROR][DEBUG] SendMessageTask - doInBackground",
                    "There has been a problem sending the message: "+message);

            } else {
                Log.d ("[DEBUG] SendMessageTask - doInBackground",
                    "Message sent ok, "+message);
            }

            return null;
        }
    }

    class SendDataCoolTask  extends AsyncTask<Void, Void, Void> {

        private final ArrayList <String>  contents;

        public SendDataCoolTask (Context c, ArrayList <String> contents) {
            this.contents = contents;
        }

        @Override
        protected Void doInBackground(Void... params) {

            PutDataMapRequest dataMap = PutDataMapRequest
                .create (Constants.TIME_LINE_DATA);

            dataMap.getDataMap().putStringArrayList("contents", contents);

            PutDataRequest request = dataMap.asPutDataRequest();

            DataApi.DataItemResult dataItemResult = Wearable.DataApi
                    .putDataItem(googleApiClient, request).await();

            Log.d ("[DEBUG] SendDataCoolTask - doInBackground",
                "Sent: "+Constants.TIME_LINE_DATA+ " status, "+getStatus());

            Log.d ("[DEBUG] SendDataCoolTask - doInBackground",
                "Result: " + dataItemResult.getStatus());

            return null;
        }
    }


    /**
     * Configures an authorized twitter client with
     * the proper keys
     */
    private Twitter getAuthorizedTwitterClient() {

        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
            .setOAuthConsumerKey (Config.CONSUMER_KEY)
            .setOAuthConsumerSecret (Config.CONSUMER_SECRET)
            .setOAuthAccessToken(preferences.getString("ACCESS_TOKEN", ""))
            .setOAuthAccessTokenSecret(preferences.getString("ACCESS_TOKEN_SECRET", ""));

        TwitterFactory tf = new TwitterFactory(cb.build());
        return tf.getInstance();
    }


    /**
     * This listener will receive the message sent by
     * the connected wearable, all messages had the following syntax /messageSegment/<messageSegments>
     */
    private final MessageApi.MessageListener wearMessageListener = new MessageApi.MessageListener() {
        @Override
        public void onMessageReceived(MessageEvent messageEvent) {

            Log.d("[DEBUG] WearService - onMessageReceived", "Message received: " + messageEvent.getPath());
            String msg = messageEvent.getPath();

            
            if (msg.equals(Constants.MSG_LOAD_LAST_TIMELINE)) {
                Log.d("[DEBUG] WearService - onMessageReceived", "Starting timeline request...");
                new GetTwitterTimeline(getAuthorizedTwitterClient(),
                        timeLineListener).execute();

            } else if (msg.startsWith(Constants.MSG_RETWEET)) {
                Log.d("[DEBUG] WearService - onMessageReceived", "Retweeting");

                String tweetID = msg.split("/")[3];

                Log.d("[DEBUG] WearService - onMessageReceived", "Retweet: " + tweetID);
                new TwitterOperationTask(tweetID).execute(true);

            } else if (msg.startsWith(Constants.MSG_FAVORITE)) {
                Log.d("[DEBUG] WearService - onMessageReceived", "Making tweet favorite");
                String tweetID = msg.split("/")[3];
                new TwitterOperationTask(tweetID).execute(false);

            } else if (msg.startsWith(Constants.MSG_SALUDATE)) {
                try {
                    Log.i ("[INFO] WearService - onMessageReceived", 
                            "Saying hellow to: "+connectedNodes.get(0));

                    new SendMessageTask(Constants.MSG_AVAILABLE).execute();


                 // if there is no nodes connect again
                } catch (NullPointerException e) {
                    Log.e ("[ERROR] WearService - onMessageReceived",
                            ""+msg+ " - "+e.getMessage());
                }
            }
        }
    };


    /**
     * This listener will be fired when a list with the user
     * status arrive, after that, the list is sent to the wearable
     */
    private final TimeLineListener timeLineListener = new TimeLineListener() {

        @Override
        public void onTimeLineReceived(ArrayList<String> tweets) {

            new SendDataCoolTask (WearService.this, tweets).execute();
        }

        @Override
        public void onTimeLineFailed(String errorMessage) {

            Log.e ("[ERROR] WearService - onTimeLineFailed", "Message: "+errorMessage);

        }
    };



    public class TwitterOperationTask extends AsyncTask <Boolean, Void, Boolean> {
        private final String tweetID;

        public TwitterOperationTask(String tweetID) {
            this.tweetID = tweetID;
        }


        @Override
        protected Boolean doInBackground(Boolean... isRetweet) {

            try {
                long id = Long.parseLong(tweetID);

                if (isRetweet[0])
                    twitterClient.retweetStatus(id);

                else
                    twitterClient.createFavorite(id);

            } catch (Exception e) {

                Log.e ("[ERROR] RetweetTask - doInBackground",
                        "Problem with retweet: "+e.getMessage());

                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean operationSuccessfully) {
            super.onPostExecute(operationSuccessfully);

            String messageToWear = (operationSuccessfully)
                ? Constants.MSG_RETWEET_OK
                : Constants.MSG_RETWEET_FAIL;


            Log.d ("[DEBUG] RetweetTask - onPostExecute", "Ready to send retweet result: "+messageToWear);
            new SendMessageTask (messageToWear).execute();

        }
    }



}

