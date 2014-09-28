package com.saulmm.tweetwear.activities;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.GridViewPager;

import com.saulmm.tweetwear.Constants;
import com.saulmm.tweetwear.R;
import com.saulmm.tweetwear.data.Tweet;
import com.saulmm.tweetwear.enums.TwitterAction;
import com.saulmm.tweetwear.fragments.FragmentTweet;
import com.saulmm.tweetwear.fragments.TwitterActionFragment;

import java.util.ArrayList;


public class StreamActivity extends Activity {

    private ArrayList <Tweet> visibleTweets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getTweets();
        initUI();
    }


    private void getTweets() {

        ArrayList <String> rawTweets = getIntent().getExtras().getStringArrayList(Constants.TWEETS_KEY);
        visibleTweets = new ArrayList<Tweet> (rawTweets.size());

        for (String rawTweet: rawTweets) {

            Tweet timeLineTweet = new Tweet();
            String [] tweetParts = rawTweet.split (Constants.TWEET_SEPARATOR);

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

        setContentView(R.layout.activity_stream);
        GridViewPager streamPager = (GridViewPager) findViewById(R.id.stream_pager);

        streamPager.setAdapter(new TwitterAdapter(getFragmentManager(),
                visibleTweets));
    }

    @Override
    protected void onPause() {

        super.onPause();
        this.finish();
    }


    class TwitterAdapter extends FragmentGridPagerAdapter {

        private ArrayList<Tweet> tweets;
        private FragmentTweet tf;


        public TwitterAdapter(FragmentManager fm, ArrayList<Tweet> tweets) {

            super(fm);
            this.tweets = tweets;
        }


        @Override
        public Fragment getFragment(int row, int column) {

            Tweet currentTweet = tweets.get(row);
            TwitterActionFragment twitterActionFragment = new TwitterActionFragment();

            if (column == Constants.TWEET_FRAGMENT) {

                tf = new FragmentTweet();
                tf.setCardTweet(currentTweet);
                tf.setRow(row);
                return tf;

            } else if (column == Constants.RETWEET_FRAGMENT) {

                twitterActionFragment.setTwAction(TwitterAction.RETWEET);

            } else if (column == Constants.FAVORITE_FRAGMENT) {

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

