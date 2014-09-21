package com.saulmm.tweetwear;


/**
 * Class to keep the app constants, storing in a separate
 * class is easy to clone it into the wearable and mobile apps
 */
public class Constants {

    public final static String TWEET_SEPARATOR         = "_--__";

    // Wear Messages
    public final static String TIME_LINE_DATA          = "/twitter/timeline";
    public final static String MSG_LOAD_LAST_TIMELINE  = "/tweets/timeline";
    public final static String MSG_RETWEET             = "/tweets/retweet/";
    public final static String MSG_FAVORITE            = "/tweets/favorite/";
    public final static String MSG_RETWEET_OK          = "/tweets/operation/ok";
    public final static String MSG_RETWEET_FAIL        = "/tweets/operation/fail";
    public final static String MSG_NO_ITERNET          = "/tweets/state/no_internet";
    public final static String MSG_AVAILABLE           = "/tweets/state/available";
    public final static String MSG_SALUDATE            = "/tweets/hi/";

    public static final String CONSUMER_KEY            = "b150R2Rdg2YcxjCzcEtMJFbNL";
    public static final String CONSUMER_SECRET         = "WDlGeEDipGzW4WYo9eZbkrWguITQEcSjU56k4QELNQAnU6lUSB";

    public static final String PREFS                   = "tweet_preferences";

}
