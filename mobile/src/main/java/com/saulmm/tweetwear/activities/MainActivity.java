package com.saulmm.tweetwear.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.saulmm.tweetwear.R;
import com.saulmm.tweetwear.fragments.LoginFragment;
import com.saulmm.tweetwear.fragments.UserFragment;

public class MainActivity extends Activity {
    public static final String PREFS = "tweet_preferences";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO Debug
        printWelcomeMessage ();

        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences(
                PREFS, Context.MODE_PRIVATE);

        String aToken = preferences.getString ("ACCESS_TOKEN", "");
        String aTokenSecret = preferences.getString ("ACCESS_TOKEN_SECRET", "");

        Fragment fragmentToLoad = (aToken.equals ("") || aTokenSecret.equals("")) ?
           new LoginFragment() : new UserFragment();

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

    private void printWelcomeMessage() {
        Log.d ("[DEBUG] MainActivity - printWelcomeMessage", "\n\n\n [INFO][DEBUG][ERROR] Last release\n [INFO][DEBUG][ERROR] \n [INFO][DEBUG][ERROR] \n [INFO][DEBUG][ERROR] \n [INFO][DEBUG][ERROR] New run \n [INFO][DEBUG][ERROR] \n [INFO][DEBUG][ERROR] \n [INFO][DEBUG][ERROR]\n\n\n");
        
    }



}