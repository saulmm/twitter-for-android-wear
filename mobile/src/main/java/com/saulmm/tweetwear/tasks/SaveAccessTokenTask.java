package com.saulmm.tweetwear.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.saulmm.tweetwear.Constants;
import com.saulmm.tweetwear.helpers.TwitterHelperListener;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import static android.content.SharedPreferences.Editor;


public class SaveAccessTokenTask extends AsyncTask <Void, Void, String> {

    private final TwitterHelperListener twListener;
    private final SharedPreferences preferences;
    private final RequestToken rToken;
    private final Twitter twClient;
    private final String verifier;
    private String errorMessage;


    public SaveAccessTokenTask (Context context, Twitter twClient,
            TwitterHelperListener twListener, RequestToken rToken, String verifier) {

        this.twListener = twListener;
        this.twClient = twClient;
        this.verifier = verifier;
        this.rToken = rToken;
        this.errorMessage = "";

        preferences = context.getSharedPreferences(
            Constants.PREFS,
            Context.MODE_PRIVATE);
    }

    @Override
    protected String doInBackground(Void... params) {

        try {

            AccessToken aToken = twClient.getOAuthAccessToken (rToken, verifier);
            User twUser = twClient.showUser (aToken.getUserId());

            fillPrefWithTwitterData(twUser, aToken.getToken(),
                    aToken.getTokenSecret());

        } catch (TwitterException e) {

            Log.e("[ERROR] SaveAccessTokenTask - doInBackground",
                    "Message: " + e.getMessage());

            errorMessage = e.getMessage();
        }

        return errorMessage;
    }


    @Override
    protected void onPostExecute(String errorMessage) {

        super.onPostExecute (errorMessage);

        if (errorMessage.equals("")) {
            twListener.onAccessTokenReceived();

        } else {
            twListener.onAccessTokenReceived();
        }
    }

    private void fillPrefWithTwitterData(User user, String aToken, String aTokenSecret) {

        Editor prefEditor = preferences.edit();

        prefEditor.putString("NAME", user.getName());
        prefEditor.putString("USER_NAME", user.getScreenName());
        prefEditor.putString("IMAGE_URL", user.getOriginalProfileImageURL());

        prefEditor.putString("CONSUMER_KEY", Constants.CONSUMER_KEY);
        prefEditor.putString("CONSUMER_SECRET", Constants.CONSUMER_SECRET);
        prefEditor.putString("ACCESS_TOKEN", aToken);
        prefEditor.putString("ACCESS_TOKEN_SECRET", aTokenSecret);

        prefEditor.putString("BACKGROUND_IMG", user.getProfileBackgroundImageURL());

        prefEditor.apply();
    }


}
