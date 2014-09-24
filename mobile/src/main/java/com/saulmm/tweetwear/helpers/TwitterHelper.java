package com.saulmm.tweetwear.helpers;


import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.saulmm.tweetwear.Config;
import com.saulmm.tweetwear.Constants;
import com.saulmm.tweetwear.Utils;
import com.saulmm.tweetwear.tasks.GetAccessTokenTask;
import com.saulmm.tweetwear.tasks.GetAuthorizationUrlTask;

import java.util.ArrayList;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitterHelper {

    private Context context;
    private Twitter twClient;

    private TwitterLoginListener loginListener;
    private TwitterOperationListener twitterListener;

    private RequestToken requestToken;
    private String oauthVerifier;


    public TwitterHelper () {}

    public TwitterHelper(String aToken, String aTokenSecret) {

        if (!TextUtils.isEmpty(Constants.CONSUMER_KEY) && !TextUtils.isEmpty(Constants.CONSUMER_SECRET) &&
            !TextUtils.isEmpty(aToken) && !TextUtils.isEmpty(aTokenSecret)) {

            AccessToken accToken = new AccessToken(aToken, aTokenSecret);

            twClient = new TwitterFactory().getInstance();

            // Authorize twitter client
            twClient.setOAuthConsumer(Constants.CONSUMER_KEY, Constants.CONSUMER_SECRET);
            twClient.setOAuthAccessToken(accToken);

        } else {

            twClient = null;
        }
    }


    /**
     * Start a twitter client instance
     */
    // TODO: review this
    public void initTwitter() {

        twClient = new TwitterFactory().getInstance();

        twClient.setOAuthConsumer(
                Config.CONSUMER_KEY,
                Config.CONSUMER_SECRET);
    }


    public void requestAuthorizationUrl() {

        new GetAuthorizationUrlTask(twClient, loginListener).execute();
    }

    public void requestTwitterTimeLine (TwitterOperationListener twitterListener) {

        new TwitterTimeLineTask(twitterListener).execute();
    }

    public void requestAccessToken() {

        new GetAccessTokenTask(context, twClient, loginListener, requestToken, oauthVerifier)
            .execute();
    }

    public void retweet (String tweetID) {

        new TwitterOperationTask(tweetID).execute(true);
    }

    public void markTweetAsFavorite (String tweetID) {

        new TwitterOperationTask(tweetID).execute(false);
    }


    public void setOauthVerifier (String oauthVerifier) {

        this.oauthVerifier = oauthVerifier;
    }

    public void setRequestToken(RequestToken requestToken) {

        this.requestToken = requestToken;
    }


    public void setLoginListener(TwitterLoginListener loginListener) {
        this.loginListener = loginListener;
    }

    public void setTwitterListener(TwitterOperationListener twitterListener) {
        this.twitterListener = twitterListener;
    }

    private class TwitterTimeLineTask extends AsyncTask<Void, Void, ArrayList<Status>> {

        private String errorMessage;
        private final TwitterOperationListener tmListener;


        public TwitterTimeLineTask (TwitterOperationListener tmListener) {

            this.tmListener = tmListener;
        }

        @Override
        protected ArrayList<twitter4j.Status> doInBackground(Void... params) {
            ArrayList<twitter4j.Status> statuses;
            errorMessage = "";

            Log.i("[INFO] SendTimeLineTask - doInBackground",
                    "Getting the timeline");

            try {
                statuses = (ArrayList<twitter4j.Status>) twClient.getHomeTimeline();
                return statuses;

            } catch (TwitterException e) {

                if (e.getMessage().equals("Network is unreachable")) {
                    errorMessage = "Network problem";
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<twitter4j.Status> statuses) {

            super.onPostExecute(statuses);

            if (errorMessage.equals("")) {
                ArrayList<String> tweets = new ArrayList<String>(statuses.size());

                for (twitter4j.Status ts : statuses) {

                    String twUsername = ts.getUser().getName();
                    String twText = Utils.removeUrl(ts.getText());
                    String twTime = Utils.getTimeDiference(ts.getCreatedAt());
                    long tweetID = ts.getId();

                    boolean isRTbyMe = ts.isRetweetedByMe();
                    boolean isFVbyMe = ts.isFavorited();

                    String tweet = twUsername + Constants.TWEET_SEPARATOR +
                        twText.replace("\n", " ") + Constants.TWEET_SEPARATOR +
                        tweetID + Constants.TWEET_SEPARATOR +
                        isFVbyMe + Constants.TWEET_SEPARATOR +
                        isRTbyMe + Constants.TWEET_SEPARATOR +
                        twTime.replace("-", "") + Constants.TWEET_SEPARATOR +
                        System.currentTimeMillis();

                    tweets.add(tweet);
                }

                tmListener.onTimeLineReceived(tweets);

            } else {

                tmListener.onTwitterFail(errorMessage);
            }
        }
    }

    private class TwitterOperationTask extends AsyncTask <Boolean, Void, Boolean> {
        private final String tweetID;
        private boolean isARetweet;

        public TwitterOperationTask(String tweetID) {
            this.tweetID = tweetID;
        }

        @Override
        protected Boolean doInBackground(Boolean... isRetweet) {

            try {
                long id = Long.parseLong(tweetID);
                isARetweet = isRetweet[0];

                if (isARetweet)
                    twClient.retweetStatus(id);

                else
                    twClient.createFavorite(id);

            } catch (Exception e) {

                twitterListener.onTwitterFail(e.getMessage());
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean operationSuccessfully) {

            super.onPostExecute(operationSuccessfully);
            twitterListener.onTwitterOperationSuccess(isARetweet);
        }
    }

}
