package com.saulmm.tweetwear.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import com.saulmm.tweetwear.Constants;
import com.saulmm.tweetwear.R;
import com.saulmm.tweetwear.fragments.LoginFragment;
import com.saulmm.tweetwear.fragments.UserFragment;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences(
            Constants.PREFS, Context.MODE_PRIVATE);

        String aToken = preferences.getString ("ACCESS_TOKEN", "");
        String aTokenSecret = preferences.getString ("ACCESS_TOKEN_SECRET", "");

        Fragment fragmentToLoad = (TextUtils.isEmpty(aToken)|| TextUtils.isEmpty(aTokenSecret))
            ? new LoginFragment()
            : new UserFragment();

        FragmentManager fm = getFragmentManager();
        fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
            if(getFragmentManager().getBackStackEntryCount() == 0) finish();
            }
        });

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, fragmentToLoad);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();
    }
}