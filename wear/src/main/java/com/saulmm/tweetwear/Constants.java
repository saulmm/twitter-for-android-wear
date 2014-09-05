package com.saulmm.tweetwear;


/**
 * Class to keep the app constants, storing in a separate
 * class is easy to clone it into the wearable and mobile apps
 */
public class Constants {

    private final static String TWEET_SEPARATOR         = "_--__";

    // Wear Messages
    private final static String TIME_LINE_DATA          = "/twitter/timeline";
    private final static String MSG_LOAD_LAST_TIMELINE  = "/tweets/timeline";
    private final static String MSG_RETWEET             = "/tweets/retweet/";
    private final static String MSG_FAVORITE            = "/tweets/favorite/";
    private final static String MSG_RETWEET_OK          = "/tweets/operation/ok";
    private final static String MSG_RETWEET_FAIL        = "/tweets/operation/fail";
    private final static String MSG_NO_ITERNET          = "/tweets/state/no_internet";
    private final static String MSG_AVAILABLE           = "/tweets/state/available";
    private final static String MSG_SALUDATE            = "/tweets/hi/";
}
