package com.saulmm.tweetwear.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.saulmm.tweetwear.helpers.TwitterHelperListener;

import twitter4j.Twitter;
import twitter4j.auth.RequestToken;


/**
 * This class is an AsyncTasks to get a valid url to authenticate the user
 * with twitter into a webview
 */
public class GetAuthorizationUrlTask extends AsyncTask <Void, Void, String> {

    private String authorizationURL = "";
    private String errorMessage = "";

    private Twitter twitterClient = null;
    private TwitterHelperListener twListener = null;
    private RequestToken rToken;


    public GetAuthorizationUrlTask(Twitter twitterClient, TwitterHelperListener twListener) {

        this.twitterClient = twitterClient;
        this.twListener = twListener;
    }

    @Override
    protected String doInBackground(Void... params) {

        try {

            rToken = twitterClient.getOAuthRequestToken();
            authorizationURL = rToken.getAuthorizationURL();

        } catch (Exception e) {

            errorMessage = e.getMessage();

            Log.e("[ERROR] GetAuthorizationUrlTask - doInBackground",
                    "Message: " + errorMessage);
        }

        return authorizationURL;
    }


    @Override
    protected void onPostExecute(String url) {

        super.onPostExecute(url);

        if (!url.equals ("")) {
            twListener.onRequestTokenReceived (rToken);
            twListener.onAuthorizationURLReceived (url);

        } else {
            twListener.onAuthorizationURLFailed(errorMessage);
        }
    }
}
