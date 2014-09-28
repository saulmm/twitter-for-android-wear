package com.saulmm.tweetwear.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.saulmm.tweetwear.R;
import com.saulmm.tweetwear.data.Tweet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FragmentTweet extends Fragment {
    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");
    private static final Pattern HASHTAG_PATTERN = Pattern.compile("(#\\w+)");


    private Tweet cardTweet;
    private int row;


    public void setCardTweet(Tweet cardTweet) {
        this.cardTweet = cardTweet;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = null;
        TextView name, tweet, time;

        if (cardTweet.getTweet().length() >= 100) {

            rootView = inflater.inflate(R.layout.activity_full, null);
            name = (TextView) rootView.findViewById(R.id.tf_name_full);
            tweet = (TextView) rootView.findViewById(R.id.tf_tweet_full);
            time = (TextView) rootView.findViewById(R.id.tf_time_full);

        } else {

            rootView = inflater.inflate(R.layout.fragment_tweet, null);
            name = (TextView) rootView.findViewById(R.id.tf_name);
            tweet = (TextView) rootView.findViewById(R.id.tf_tweet);
            time = (TextView) rootView.findViewById(R.id.tf_time);
        }

        SpannableString spannableContent = new SpannableString (
            cardTweet.getTweet());

        Matcher mentionMatcher  = MENTION_PATTERN.matcher(cardTweet.getTweet());
        Matcher hashtagMatcher = HASHTAG_PATTERN.matcher(cardTweet.getTweet());

        setPatternSpan (mentionMatcher, spannableContent);
        setPatternSpan(hashtagMatcher, spannableContent);

        tweet.setText(spannableContent);
        name.setText(cardTweet.getName());
        time.setText(cardTweet.getTime());

        return rootView;
    }


    public void setPatternSpan(Matcher matcher, SpannableString spString) {

        while (matcher.find()) {

            spString.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.wall_color)),
                matcher.start(), matcher.end(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getRow() {
        return row;
    }
}
