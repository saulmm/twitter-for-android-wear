package com.saulmm.tweetwear;


/**
 * Class to keep the app constants, storing in a separate
 * class is easy to clone it into the wearable and mobile apps
 */
public class Constants {

    public final static String TWEET_SEPARATOR          = "_--__";
    public static final String TWEETS_KEY               = "tweets";

    public static final int TWEET_FRAGMENT              = 0;
    public static final int RETWEET_FRAGMENT            = 1;
    public static final int FAVORITE_FRAGMENT           = 2;

    // Wear Messages
    public final static String TIME_LINE_DATA          = "/tweets/timeline";
    public final static String MSG_RETWEET             = "/tweets/retweet/";
    public final static String MSG_FAVORITE            = "/tweets/favorite/";
    public final static String MSG_OP_OK               = "/tweets/operation/ok";
    public final static String MSG_OP_FAIL             = "/tweets/operation/fail";
    public final static String MSG_NO_LOGIN            = "/tweets/state/no_login";
    public final static String MSG_NO_ITERNET          = "/tweets/state/no_internet";
    public final static String MSG_AVAILABLE           = "/tweets/state/available";
    public final static String MSG_SALUDATE            = "/tweets/hi/";
}
