package com.saulmm.tweetwear.helpers;

import twitter4j.auth.RequestToken;


public interface TwitterHelperListener {

    public void onAuthorizationURLReceived (String url);
    public void onAuthorizationURLFailed (String cause);
    public void onAccessTokenReceived ();

    public void onRequestTokenReceived (RequestToken rToken);
}
