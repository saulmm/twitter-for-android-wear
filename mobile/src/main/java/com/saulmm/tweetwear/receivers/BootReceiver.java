package com.saulmm.tweetwear.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.saulmm.tweetwear.services.WearService;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent myIntent = new Intent(context, WearService.class);
        context.startService(myIntent);
    }
}
