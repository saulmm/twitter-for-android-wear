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

public class TweetFragment extends Fragment {
    private static final Pattern MENTION_PATTERN = Pattern.compile("@(\\w+)");
    private static final Pattern HASHTAG_PATTERN = Pattern.compile("(#\\w+)");


    private Tweet cardTweet;

    public TweetFragment () {}

    public void setCardTweet(Tweet cardTweet) {
        this.cardTweet = cardTweet;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tweet_fragment, null);

        TextView name = (TextView) rootView.findViewById(R.id.tf_name);
        TextView tweet = (TextView) rootView.findViewById(R.id.tf_tweet);
        TextView time = (TextView) rootView.findViewById(R.id.tf_time);

        if (cardTweet.getTweet().length() >= 120) {

            tweet.setTextSize(tweet.getTextSize() - 10);
            name.setTextSize(name.getTextSize() - 10);
        }

        SpannableString spannableContent = new SpannableString (
            cardTweet.getTweet());

        Matcher matcher2 = MENTION_PATTERN.matcher(cardTweet.getTweet());

        while (matcher2.find()) {
            spannableContent.setSpan(
                    new ForegroundColorSpan(getResources().getColor(R.color.wall_color)),
                    matcher2.start(), matcher2.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        Matcher matcher3 = HASHTAG_PATTERN.matcher(cardTweet.getTweet());

        while (matcher3.find()) {
            spannableContent.setSpan(
                    new ForegroundColorSpan(getResources().getColor(R.color.wall_color)),
                    matcher3.start(), matcher3.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }


        name.setText(cardTweet.getName());
        tweet.setText(spannableContent);
        time.setText(cardTweet.getTime());


        return rootView;
    }


}
