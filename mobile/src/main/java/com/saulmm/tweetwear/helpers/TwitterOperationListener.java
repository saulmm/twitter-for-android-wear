package com.saulmm.tweetwear.helpers;

import java.util.ArrayList;

/**
 * Created by wtf on 25/09/14.
 */
public interface TwitterOperationListener {

    public void onTwitterFail(String errorMessage);
    public void onTimeLineReceived (ArrayList<String> tweets);
    public void onTwitterOperationSuccess(boolean isARetweet);
}
