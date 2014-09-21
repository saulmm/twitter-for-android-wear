package com.saulmm.tweetwear.services;

import android.util.Log;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.WearableListenerService;
import com.saulmm.tweetwear.helpers.TwitterHelper;


public class CoolWearService extends WearableListenerService {

    private TwitterHelper twHelper;

    @Override
    public void onCreate() {

        super.onCreate();

        twHelper = new TwitterHelper();
        twHelper.setContext(this);
        twHelper.initTwitter();
    }


    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        super.onDataChanged(dataEvents);

        Log.d("[DEBUG] CoolWearService - onDataChanged", "HII");
    }


//    @Override
//    public void onMessageReceived(MessageEvent messageEvent) {
//
//        super.onMessageReceived(messageEvent);
//
//        String msg = messageEvent.getPath();
//
//        if (msg.equals(Constants.MSG_LOAD_LAST_TIMELINE)) {
//
//            new GetTwitterTimeline(getAuthorizedTwitterClient(),
//                    timeLineListener).execute();
//        }
//
//        Log.d ("[DEBUG] CoolWearService - onMessageReceived", "HII");
//    }
}
