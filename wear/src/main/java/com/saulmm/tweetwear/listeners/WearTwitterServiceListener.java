package com.saulmm.tweetwear.listeners;

import java.util.ArrayList;

public interface WearTwitterServiceListener {
    void onWearReady (boolean connected);
    void onTimeLimeReceived(ArrayList <String> timeline);
    void onProblem (String problem);
}
