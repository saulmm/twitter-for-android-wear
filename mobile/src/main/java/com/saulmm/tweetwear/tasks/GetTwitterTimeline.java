package com.saulmm.tweetwear.tasks;


import android.os.AsyncTask;
import android.util.Log;

import com.saulmm.tweetwear.Utils;
import com.saulmm.tweetwear.listeners.TimeLineListener;

import java.util.ArrayList;

import twitter4j.Twitter;
import twitter4j.TwitterException;

public class GetTwitterTimeline extends AsyncTask<Void, Void, ArrayList<twitter4j.Status>> {

    private final static String TWEET_SEPARATOR = "_--__";

    private final Twitter twClient;
    private String errorMessage;
    private final TimeLineListener tmListener;


    public GetTwitterTimeline(Twitter twClient, TimeLineListener tmListener) {

        this.twClient = twClient;
        this.tmListener = tmListener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.i("[INFO] SendTimeLineTask - onPreExecute",
                "Ready to get the timeline ");

    }

    @Override
    protected ArrayList<twitter4j.Status>  doInBackground(Void... params) {
        ArrayList<twitter4j.Status> statuses;
        errorMessage = "";

        Log.i ("[INFO] SendTimeLineTask - doInBackground",
            "Getting the timeline");

        try {
            statuses = (ArrayList<twitter4j.Status>) twClient.getHomeTimeline();
            return statuses;

        } catch (TwitterException e) {

            if (e.getMessage().equals ("Network is unreachable")) {
                errorMessage = "No internet";
            }
        }

        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<twitter4j.Status> statuses) {

        super.onPostExecute(statuses);

        if (errorMessage.equals("")) {
            ArrayList<String> tweets = new ArrayList<String>(statuses.size());

            for (twitter4j.Status ts : statuses) {

                String twUsername = ts.getUser().getName();
                String twText = Utils.removeUrl(ts.getText());
                String twTime = Utils.getTimeDiference(ts.getCreatedAt());
                long tweetID = ts.getId();

                boolean isRTbyMe = ts.isRetweetedByMe();
                boolean isFVbyMe = ts.isFavorited();

                String tweet = twUsername + TWEET_SEPARATOR +
                    twText.replace("\n", " ") + TWEET_SEPARATOR +
                    tweetID + TWEET_SEPARATOR +
                    isFVbyMe + TWEET_SEPARATOR +
                    isRTbyMe + TWEET_SEPARATOR +
                    twTime.replace("-", "") + TWEET_SEPARATOR +
                    System.currentTimeMillis();

                tweets.add(tweet);
            }

            Log.i("[INFO] SendTimeLineTask - onPostExecute",
                "Tweets obtained: " + tweets.size());

            tmListener.onTimeLineReceived (tweets);

        } else {

            tmListener.onTimeLineFailed (errorMessage);
        }


    }



}
