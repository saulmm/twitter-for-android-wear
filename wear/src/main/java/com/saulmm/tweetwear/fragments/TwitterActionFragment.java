package com.saulmm.tweetwear.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.wearable.activity.ConfirmationActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;

import com.saulmm.tweetwear.DeviceHandler;
import com.saulmm.tweetwear.R;
import com.saulmm.tweetwear.data.Tweet;
import com.saulmm.tweetwear.enums.TwitterAction;
import com.saulmm.tweetwear.listeners.ActionListener;

@SuppressLint("ValidFragment")
public class TwitterActionFragment extends Fragment implements View.OnClickListener {

    private Tweet currentTweet;
    private TwitterAction twAction;

    private TextView actionText;
    private ImageButton actionImg;
    private AnimationSet setAnimMeaningOn;
    private DeviceHandler handler;


    public void setTwAction(TwitterAction twAction) {
        this.twAction = twAction;
    }

    public void setCurrentTweet(Tweet currentTweet) {
        this.currentTweet = currentTweet;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.twitter_action_fragment, null);

        handler = DeviceHandler.getInstance();

        actionText = (TextView) rootView.findViewById(R.id.tw_fragment_text);
        actionImg = (ImageButton) rootView.findViewById(R.id.tw_fragment_img);

        setAnimMeaningOn = new AnimationSet(true);
        setAnimMeaningOn.addAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.loading_animation));

        actionImg.setOnClickListener(this);

        configureAction();

        return rootView;
    }

    /**
     * Configure the action depending if there is a Retweet or a Favorite action
     */
    private void configureAction() {

        switch (twAction) {

            case RETWEET:
                actionText.setText ("Retweet");

                int drawableRT = (!currentTweet.isRetweeted())
                        ? R.drawable.tw_rt
                        : R.drawable.tw_rt_ed;

                actionImg.setImageDrawable(getResources()
                        .getDrawable(drawableRT));
                break;

            case FAVORITE:
                actionText.setText ("Favorite");

                int drawableFav = (!currentTweet.isFavorite())
                    ? R.drawable.tw_favorite
                    : R.drawable.tw_favorite_ed;


                actionImg.setImageDrawable(getResources()
                        .getDrawable(drawableFav));

                break;
        }

        handler.setOnActionListener (actionListener);
    }

    @Override
    public void onClick(View v) {
        actionImg.startAnimation(setAnimMeaningOn);

        if (handler.isConnected())
            handler.requestAction(twAction, currentTweet.getId());

        else {
            Log.d("[DEBUG] TwitterActionFragment - onClick", "Cannot retweet, not connected");
        }
    }


    ActionListener actionListener = new ActionListener() {
        @Override
        public void onActionOK () {
            Log.d ("[DEBUG] TwitterActionFragment - onActionOK", "OnAction OK");
            
            Intent confirmationActivity = new Intent(getActivity(), ConfirmationActivity.class)
                .putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION)
                .putExtra(ConfirmationActivity.EXTRA_MESSAGE, actionText.getText()+"ed");

            startActivity(confirmationActivity);
        }

        @Override
        public void onActionFail () {
            Log.d ("[DEBUG] TwitterActionFragment - onActionFail", "On action fail");

            Intent intent = new Intent(getActivity(), ConfirmationActivity.class);
            intent.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.FAILURE_ANIMATION);
            intent.putExtra(ConfirmationActivity.EXTRA_MESSAGE, "Error!");
            startActivity(intent);
        }
    };

}
