package com.saulmm.tweetwear.listeners;

import java.util.ArrayList;

public interface TimeLineListener {

    public void onTimeLineFailed (String errorMessage);
    public void onTimeLineReceived (ArrayList<String> tweets);
}
