package com.saulmm.tweetwear.helpers;


import android.content.Context;
import android.text.TextUtils;

import com.saulmm.tweetwear.Config;
import com.saulmm.tweetwear.Constants;
import com.saulmm.tweetwear.tasks.GetAuthorizationUrlTask;
import com.saulmm.tweetwear.tasks.SaveAccessTokenTask;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitterHelper {

    private Context context;
    private Twitter twClient;
    private TwitterHelperListener listener;
    private RequestToken requestToken;
    private String oauthVerifier;


    public TwitterHelper () {}

    public TwitterHelper(String aToken, String aTokenSecret) {

        if (!TextUtils.isEmpty(Constants.CONSUMER_KEY) && !TextUtils.isEmpty(Constants.CONSUMER_SECRET) &&
            !TextUtils.isEmpty(aToken) && !TextUtils.isEmpty(aTokenSecret)) {

            AccessToken accToken = new AccessToken(aToken, aTokenSecret);

            twClient = new TwitterFactory().getInstance();

            // Authorize twitter client
            twClient.setOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
            twClient.setOAuthAccessToken(accToken);

        } else {

            twClient = null;
        }
    }


    public void setTwClient(Twitter twClient) {
        this.twClient = twClient;
    }

    /**
     * Start a twitter client instance
     */
    // TODO: review this
    public void initTwitter() {

        twClient = new TwitterFactory().getInstance();

        twClient.setOAuthConsumer(
                Config.CONSUMER_KEY,
                Config.CONSUMER_SECRET);
    }

    /**
     * Executes the task to retrieve the authorization Url
     */
    public void startAuthorizationUrlTask () {

        new GetAuthorizationUrlTask(twClient, listener).execute();
    }


    /**
     * Executes the task to retrieve the access token
     */
    public void startAccessTokenTask () {

        new SaveAccessTokenTask(context, twClient, listener, requestToken, oauthVerifier)
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


    public Twitter getTwClient() {
        return twClient;
    }

    public void setRequestToken(RequestToken requestToken) {

        this.requestToken = requestToken;
    }

}
