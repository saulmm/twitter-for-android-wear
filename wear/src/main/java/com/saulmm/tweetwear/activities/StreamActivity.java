package com.saulmm.tweetwear.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.wearable.view.DismissOverlayView;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;

import com.saulmm.tweetwear.R;
import com.saulmm.tweetwear.data.Tweet;
import com.saulmm.tweetwear.enums.TwitterAction;
import com.saulmm.tweetwear.fragments.TweetFragment;
import com.saulmm.tweetwear.fragments.TwitterActionFragment;

import java.util.ArrayList;


public class StreamActivity extends Activity {
    private ArrayList <Tweet> visibleTweets;
    private DismissOverlayView dismissView;
    private GridViewPager streamPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initTweets ();

        initUI();
    }

    private void initTweets() {
        ArrayList <String> rawTweets = getIntent().getExtras().getStringArrayList("tweets");
        visibleTweets = new ArrayList<Tweet> (rawTweets.size());

        for (String rawTweet: rawTweets) {
            Tweet timeLineTweet = new Tweet();
            String [] tweetParts = rawTweet.split ("_--__");

            String tweetUsername = tweetParts [0];
            String tweetText = tweetParts [1];
            String tweetID = tweetParts [2];
            boolean isFavorite = Boolean.parseBoolean (tweetParts [3]);
            boolean isRetweeted = Boolean.parseBoolean (tweetParts [4]);
            String time = tweetParts [5];

            timeLineTweet.setId (tweetID);
            timeLineTweet.setTweet (tweetText);
            timeLineTweet.setName (tweetUsername);
            timeLineTweet.setFavorite (isFavorite);
            timeLineTweet.setRetweeted (isRetweeted);
            timeLineTweet.setTime (time);

            visibleTweets.add (timeLineTweet);
        }
    }

    private void initUI() {

        setContentView(R.layout.rect_activity_my);

        streamPager = (GridViewPager) findViewById(R.id.stream_pager);

        streamPager.setAdapter(new TwitterAdapter (StreamActivity.this,
                getFragmentManager(), visibleTweets));

    }

    @Override
    protected void onPause() {

        super.onPause();
        this.finish();
    }

    class TwitterAdapter extends FragmentGridPagerAdapter {
        private Context context;
        private ArrayList<Tweet> tweets;

        public TwitterAdapter(Context context, FragmentManager fm, ArrayList<Tweet> tweets) {
            super(fm);

            this.tweets = tweets;
            this.context = context;
        }

        @Override
        public Fragment getFragment(int row, int column) {
            Tweet currentTweet = tweets.get(row);
            TwitterActionFragment twitterActionFragment = new TwitterActionFragment();

            if (column == 0) {
                TweetFragment tf = new TweetFragment();
                tf.setCardTweet(currentTweet);
                return tf;

            } else if (column == 1) {
                twitterActionFragment.setTwAction(TwitterAction.RETWEET);

            } else if (column == 2) {
                twitterActionFragment.setTwAction(TwitterAction.FAVORITE);
            }

            twitterActionFragment.setCurrentTweet(currentTweet);
            return twitterActionFragment;
        }

        @Override
        public int getRowCount() {
            return tweets.size();
        }

        @Override
        public int getColumnCount(int row) {
            return 3;
        }
    }
}

