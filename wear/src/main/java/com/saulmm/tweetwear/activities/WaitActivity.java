package com.saulmm.tweetwear.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.saulmm.tweetwear.DeviceHandler;
import com.saulmm.tweetwear.R;
import com.saulmm.tweetwear.listeners.WearTwitterServiceListener;

import java.util.ArrayList;

import static android.util.Log.d;


public class WaitActivity extends Activity implements WearTwitterServiceListener {
    private DeviceHandler handler;
    private ImageView loadingSegment;
    private TextView stateMessageTV;
    private FrameLayout loadingFL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        initUI();
        initWearHandler ();
    }

    private void initWearHandler() {

        handler = DeviceHandler.getInstance();
        handler.initGoogleApiClient (this);
        handler.setDeviceListener(this);
    }

    private void initUI() {
        setContentView(R.layout.activity_wait);

        stateMessageTV = (TextView) findViewById (R.id.loading_textview);
        loadingFL = (FrameLayout) findViewById (R.id.loading_frame);
        loadingSegment = (ImageView) findViewById(R.id.loading_segment);
        loadingSegment.startAnimation(AnimationUtils.loadAnimation(this, R.anim.loading_animation));
        printWelcomeMessage();
    }

    private void printWelcomeMessage() {
        Log.d ("[DEBUG] WaitActivity - printWelcomeMessage",
                "\n\n\n\n[INFO][DEBUG][ERROR] \n [INFO][DEBUG][ERROR] \n [INFO][DEBUG][ERROR] \n [INFO][DEBUG][ERROR] \n [INFO][DEBUG][ERROR] New run \n [INFO][DEBUG][ERROR] \n [INFO][DEBUG][ERROR] \n [INFO][DEBUG][ERROR] \n\n\n\n");
        
    }

    @Override
    protected void onStart() {

        super.onStart();
        handler.connectGoogleApiClient();
    }

    @Override
    public void onWearReady(boolean connected) {

        Log.d("[DEBUG] WaitActivity - onWearReady", "Wear device ready: "+connected);

        if (connected)
            handler.requestTwitterTimeline();

        else {
            showError("Android device not found");
        }


    }

    private void showError(String errorMessage) {

        loadingSegment.clearAnimation();
        loadingSegment.setVisibility(View.INVISIBLE);
        stateMessageTV.setText (errorMessage);
        stateMessageTV.setTextSize (stateMessageTV.getTextSize() - 20);

        TransitionDrawable transition = (TransitionDrawable) loadingFL.getBackground();
        transition.startTransition(1000);
    }

    @Override
    public void onTimeLimeReceived (ArrayList <String> timeline) {
        d("[DEBUG] WaitActivity - onTimeLimeReceived", "Time line received, tweets: " + timeline.size());
        Intent streamIntent = new Intent (WaitActivity.this, StreamActivity.class);
        Bundle b  = new Bundle();
        b.putStringArrayList ("tweets", timeline);
        streamIntent.putExtras(b);
        startActivity (streamIntent);
        this.finish();
    }

    @Override
    public void onProblem(String problem) {

        // TODO constants
        if (problem.equals ("/tweets/state/no_internet")) {
            Log.d("[DEBUG] WaitActivity - onProblem", "NO internet");
            showError("Please check your\n internet connection");
        }

        if(problem.equals("Service not running"))
            showError(getString(R.string.error_no_service));
    }


    @Override
    protected void onPause() {
        super.onPause();
    }
}
