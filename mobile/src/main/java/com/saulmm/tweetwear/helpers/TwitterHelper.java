package com.saulmm.tweetwear.helpers;


import android.content.Context;

import com.saulmm.tweetwear.Config;
import com.saulmm.tweetwear.helpers.tasks.GetAuthorizationUrlTask;
import com.saulmm.tweetwear.helpers.tasks.SaveAccessTokenTask;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;

public class TwitterHelper {

    private Context context;
    private Twitter twitterClient;
    private TwitterHelperListener listener;
    private RequestToken requestToken;
    private String oauthVerifier;



    /**
     * Start a twitter client instance
     */
    public void initTwitter() {

        twitterClient = new TwitterFactory().getInstance();

        twitterClient.setOAuthConsumer(
            Config.CONSUMER_KEY,
            Config.CONSUMER_SECRET);
    }

    /**
     * Executes the task to retrieve the authorization Url
     */
    public void startAuthorizationUrlTask () {

        new GetAuthorizationUrlTask(twitterClient, listener).execute();
    }


    /**
     * Executes the task to retrieve the access token
     */
    public void startAccessTokenTask () {

        new SaveAccessTokenTask(context, twitterClient, listener, requestToken, oauthVerifier)
            .execute();
    }


    public void setListener (TwitterHelperListener listener) {
        this.listener = listener;
    }


    public void setContext(Context context) {
        this.context = context;
    }


    public void setOauthVerifier (String oauthVerifier) {

        this.oauthVerifier = oauthVerifier;
    }


    public void setRequestToken(RequestToken requestToken) {

        this.requestToken = requestToken;
    }

}
