package com.saulmm.tweetwear.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.saulmm.tweetwear.R;
import com.saulmm.tweetwear.data.Tweet;

public class FragmentContainer extends Fragment  {

    private final int row;
    private final Tweet currentTweet;

    public FragmentContainer(int row, Tweet currentTweet) {

        this.row = row;
        this.currentTweet = currentTweet;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        initTweet();
        return inflater.inflate(R.layout.fragment_container, null);
    }

    public void initTweet() {

        FragmentTweet fTweet = new FragmentTweet();
        fTweet.setCardTweet(currentTweet);

        FragmentManager fm = getActivity().getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.fcontainer, fTweet, "tweet:" + row);
        ft.commit();
    }
}
