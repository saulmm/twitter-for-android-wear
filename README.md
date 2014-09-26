# Twitter client for android wear

<div style="text-align:center"><img src ="https://s3.amazonaws.com/pushbullet-uploads/ujDhpJwIHv2-blQnn5U92wbgrQLbg4jhwRMO5PzaMJYx/header.png" /></div>

#### Google Play link: 

- [http://goo.gl/hHUaz5](http://goo.gl/hHUaz5)

## 1. Motivation

The motivation of this project was to learn how the android wear framework works, using theirs views, communication APIs, etc...

A second objective was to share it as a reference project, so others can use it, modify it or make whatever they want with it. 

I think that is very useful to learn by examples, so I really aprecciate projects like this.

## 2. Project structure

If you create a project with android wear and android studio, when the wizard finishes, you will see two main modules: __wear__ and __mobile__.

When you make an android wear _release_, you get a single _.apk_, which will be downloaded from google play by your users, when one of them install your app on their phone, another app will be installed automatically on their wearable. That is because the release _apk's_ with android wear, has a 'micro-apk' hidden inside them.

That _micro-apk_ is built by the __wear__ module.

<div style="text-align:center"><img src ="https://87ccd03f34f5850e9f58050faacff843fdb2a928.googledrive.com/host/0B62SZ3WRM2R2TDVIN2ptNGVndE0" /></div>

At develop time, you can compile your __wear__ module directly on your wear device or emulator.

## 3. The handheld app

Like a normal android app, has an _activity_, a few _fragments_ and some _layouts_, the user, when press the _login_ button, will see a browser with a twitter login, if everything goes well after insert they twitter credentials, the main fragment will change and the user will see their twitter profile photo, username, and the background will turn to their twitter profile background.

<div style="text-align:center"><img src ="https://cef58420144e1df00f893fa4436747dce6972cb0.googledrive.com/host/0B62SZ3WRM2R2V0Y2MlpwR3lEcUU" /></div>

The interesting part from my point of view is the communication with the wearable. 

The clock always could ask the list of tweets, so the class that asks twitter for the tweets must be always available and able to take care of it, so I think that a [WearListenerService](http://developer.android.com/reference/com/google/android/gms/wearable/WearableListenerService.html) is the best choice.

```xml
<service android:name=".services.WearHandler">
  <intent-filter>
      <action android:name="com.google.android.gms.wearable.BIND_LISTENER" />
  </intent-filter>
</service>
```


For all twitter connections I had relied in the library [twitter4j](http://twitter4j.org/en/index.html), a very mature and complete java library responsible for all interaction with twitter APIs, you only have to create an app in [twitter developers](https://dev.twitter.com/), __twitter4j__ will do the hard work.

### Wearable comunication

And here is the fun part, after reviewing the documentation of [android developers](http://developer.android.com/training/wearables/data-layer/index.html)  I realized that the communication with the wearable device is almost trivial. There are certain steps that must be respected:

1. Connect with **Google Play Services**

```java
package com.saulmm.tweetwear.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.saulmm.tweetwear.Constants;
import com.saulmm.tweetwear.helpers.TwitterHelper;
import com.saulmm.tweetwear.helpers.TwitterOperationListener;
import com.saulmm.tweetwear.wear_tasks.SendMessageTask;
import com.saulmm.tweetwear.wear_tasks.SendTimeLineTask;

import java.util.ArrayList;


public class WearHandler extends WearableListenerService  {

    private TwitterHelper twHelper;
    private GoogleApiClient googleApiClient;
    private Node connectedNode;

    @Override
    public void onCreate() {

        super.onCreate();

        SharedPreferences preferences = getSharedPreferences(
            Constants.PREFS, Context.MODE_PRIVATE);
        
        twHelper = new TwitterHelper(this);
        twHelper.setTwitterListener(twitterListener);

        // Init and connect the client to use the wear api
        googleApiClient = new GoogleApiClient.Builder(this)
            .addApi(Wearable.API)
            .build();

        // Connect to google play services
        googleApiClient.connect();
    }


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        super.onMessageReceived(messageEvent);

        // The message that was send by the wearable
        String msg = messageEvent.getPath();

        if (!twHelper.isUserLogged()) {

            Toast.makeText(this, "Please open wear app and log in with twitter", Toast.LENGTH_SHORT)
                .show();

            sendMessageToWearable(Constants.MSG_NOT_LOGGED);
            return;
        }

        // Message: /tweets/hi/
        if (msg.equals(Constants.MSG_SALUDATE)) {

            sendMessageToWearable(Constants.MSG_AVAILABLE);
        }

        // Message: /tweets/timeline
        if (msg.equals(Constants.MSG_LOAD_LAST_TIMELINE)) {

            twHelper.requestTwitterTimeLine(twitterListener);

        // Message /tweets/retweet/<tweet id>
        } else if (msg.startsWith(Constants.MSG_RETWEET)) {

            String twID = msg.split("/")[3];
            twHelper.retweet(twID);

        // Message /tweets/favorite/<tweet id>
        } else if (msg.startsWith(Constants.MSG_FAVORITE)) {

            String twID = msg.split("/")[3];
            twHelper.markTweetAsFavorite(twID);
        }
    }


    private TwitterOperationListener twitterListener = new TwitterOperationListener() {

        @Override
        public void onTimeLineReceived(ArrayList<String> tweets) {

            new SendTimeLineTask(tweets, googleApiClient)
                .execute();
        }

        @Override
        public void onTwitterOperationSuccess(boolean success) {

            String messageToWear = (success)
                ? Constants.MSG_RETWEET_OK
                : Constants.MSG_RETWEET_FAIL;

            sendMessageToWearable(messageToWear);
        }

        @Override
        public void onTwitterFail(String errorMessage) {

            Log.e ("[ERROR] WearHandler - onTwitterFail", "Error: "+errorMessage);
        }
    };


    public void sendMessageToWearable (String message) {

        new SendMessageTask(message, googleApiClient)
            .execute();/**/
    }
}

```

Now there are available the communication APIs that google provide us, there are 3 APIs wich can be used: [MessageApi](http://developer.android.com/reference/com/google/android/gms/wearable/MessageApi.html), [NodeApi](https://developer.android.com/reference/com/google/android/gms/wearable/NodeApi.html) & [DataApi](http://developer.android.com/reference/com/google/android/gms/wearable/DataApi.html).

### MessageApi

The first communication from the wearable tries to find out if the service is running, is something like:

_Hi men !  _
_Here I am :) _<br>

In the _wear_ language you are sending the following messages:

```/tweets/state/hi/``` _(wearable)_ 
```/tweets/state/how4u/``` _(device)_ <br>

Both modules, _mobile_ & _wear_ have the same implementation of an asynctask used to send messages with the ```MessageApi```.

```java
class SendMessageTask extends AsyncTask <Void, Void, Void> {

   private final String message;

   SendMessageTask(String message) {
      this.message = message;
   }

   @Override
   protected Void doInBackground(Void... params) {
       
       // Such as /wear/message
       String activityPath = message;

       MessageApi.SendMessageResult result =  MessageApi.sendMessage(
           googleApiClient, connectedNodes.get(0).getId(), 
           activityPath, null)
           .await();
    }

    return null;
    }
}
```

After call to ```googleApiClient()```and wait to ```onConnected()``` were called, the service was registered to receive message events using the ```Wearable.MessageApi.addListener(googleApiClient, myListener)``` method, so the service is now able to handle incoming messages with this method:

```java
private final MessageApi.MessageListener wearMessageListener = new MessageApi.MessageListener() {
    
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        String msg = messageEvent.getPath();
        [...]
    }
}
```

### NodeApi

The [NodeApi](https://developer.android.com/reference/com/google/android/gms/wearable/NodeApi.html) as the documentation says, exposes to learn about local or connected Nodes, so you can figure out what nodes (_wear devices_) are connected to the handfeld app. I created another asynctask to get the connected nodes:

```java
public class GetNodesTask extends AsyncTask <Void, Void, ArrayList <Node>> {

    private final GoogleApiClient googleApiClient;
    private final ServiceNodeListener nodeListener;


    public GetNodesTask(ServiceNodeListener nodeListener, GoogleApiClient googleApiClient) {
        this.nodeListener = nodeListener;
        this.googleApiClient = googleApiClient;
    }


    @Override
    protected ArrayList <Node> doInBackground(Void... params) {

        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(googleApiClient)
                        .await();

        return (ArrayList<Node>) nodes.getNodes();
    }


    @Override
    protected void onPostExecute(ArrayList <Node> nodes) {
        super.onPostExecute(nodes);

        if (nodes != null && nodes.size() > 0)
            nodeListener.onNodesReceived(nodes);

        else
            nodeListener.onFailedNodes();
    }
}
```

When the wear nodes are received, you use the MessageApi or the DataApi with that nodes, I always used the first wearable connected to the main device, it would be nice select what wearable do you want to use if there is more than one.

### DataApi

The [DataApi](http://developer.android.com/reference/com/google/android/gms/wearable/DataApi.html) allows you to synchronice data between the handhelf app and wearables, a message in the DataApi consists of a __Payload__, to send whatever data you wish, and a __Path__, a unique string starting with a forward slash.

Instead to send a byte array you can use [DataMaps](http://developer.android.com/reference/com/google/android/gms/wearable/DataMap.html) which are used as an android [bundle](http://developer.android.com/reference/android/os/Bundle.html) inside the DataApi.

To send & receive the tweets from the handfeld to the wear device, I have used [DataMaps](http://developer.android.com/reference/com/google/android/gms/wearable/DataMap.html) with strings composed by fields separed by a pattern (Maybe this is not the best option).

In this case, I have implemented an _asynctask_ for send the twitter timeline after receiving it by _twitter4j_. The timeline is sent as an ```ArrayList<String>```

```java
class SendTimeLineTask  extends AsyncTask<Void, Void, Void> {

    private final ArrayList <String>  contents;

    public SendTimeLineTask (Context c, ArrayList <String> contents) {
        this.contents = contents;
}

    @Override
    protected Void doInBackground(Void... nodes) {

      PutDataMapRequest dataMap = PutDataMapRequest
        .create (Constants.TIME_LINE_DATA);

      dataMap.getDataMap().putStringArrayList("contents", contents);

      PutDataRequest request = dataMap.asPutDataRequest();

      DataApi.DataItemResult dataItemResult = Wearable.DataApi
        .putDataItem(googleApiClient, request)
        .await();

      return null;
    }
}
```

### 4.Wearable app

<div style="text-align:center"><img src ="https://219ede0846187d7fd9922311fa441d9820915b2c.googledrive.com/host/0B62SZ3WRM2R2bHhlVExmakJEaFU" /></div>

An application for android wear is programmed like a normal android app, with a few differences, you can't all the android APIs that you normally use in a common android app such as ```android.net```. Also,  you have to notice that the user experience is a little bit difference than a normal Android app.

The wear app, is composed by the following activities:

- ```WaitActivity``` - An activity shown meanwhile the handfeld app is requesting the user timeline. In backwards this activity makes a hard work of communication with the handfeld app.

- ```StreamActivity```- An activity that shows the user _timeline_, also allows to _retweet_ a tweet or flag as favorited.

#### WaitActivity

The effect of the 'spinner' is a simple ImageView, with the following animation applied

<div style="text-align:center"><img src ="https://3a3c2487a21adf7eab9f9e1c949cb1c6fbc1a71f.googledrive.com/host/0B62SZ3WRM2R2MldQdDNXVTk5bmc" /></div>
  

```xml
<rotate
	xmlns:android="http://schemas.android.com/apk/res/android"
	android:duration="1000"
	android:repeatCount="infinite"
	android:fromDegrees="0"
	android:interpolator="@android:anim/anticipate_overshoot_interpolator"
	android:pivotX="50%"
	android:pivotY="50%"
	android:toDegrees="359" />
```

```java
  loadingSegment = (ImageView) findViewById(R.id.loading_segment);
  loadingSegment.startAnimation(AnimationUtils.loadAnimation(this, R.anim.loading_animation));
```

If there is any problem the 'Loading...' message will turn to show the error and the background, produced by a ```<transition>```

<div style="text-align:center"><img src ="https://919c5da1a105a3a06e03fa4ec2c9901a70398228.googledrive.com/host/0B62SZ3WRM2R2TXNNWlJPREVXQTA" /></div>

```xml
<?xml version="1.0" encoding="UTF-8"?>
<transition xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:drawable="@drawable/wait_idle" />
    <item android:drawable="@drawable/wait_error" />
</transition>
```

```java
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    ...
    android:background="@drawable/tr_error"
    ...
    >
</FrameLayout>    
```


A protocol of messages is established to perform the communication, first of all, in the ```WaitActivity``` a message task is sent with the messaje 'available', after 3 seconds if the handfeld app doesn't responds, it means that there is a problem with the service, so show the proper message to the user. 

If the handfeld app responds successfully the wearable will sent another message taks to tell the handfeld app that has to start to request the user tweets, when the request is done, the handfeld app will sent a DataTask with a list of tweets

The wear message listener...

```java
  @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        String messagePath = messageEvent.getPath();

        if (messagePath.equals("/tweets/operation/ok")) {
            onRetweetListener.onActionOK();
        }

        if (messagePath.equals("/tweets/operation/fail")) {
            onRetweetListener.onActionFail();
        }

        if (messagePath.equals("/tweets/state/no_internet")) {
            deviceListener.onProblem(messagePath);
        }

        if (messagePath.equals("/tweets/state/available")) {
            isTwitterServiceIsRunning = true;
        }
    }
``` 
  
The tweets message listener (DataListener):

```java
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {

        for (DataEvent event: dataEvents) {

            String eventUri = event.getDataItem().getUri().toString();

            if (eventUri.contains ("/twitter/timeline")) {

                DataMapItem dataItem = DataMapItem.fromDataItem (event.getDataItem());
                ArrayList <String> tweets = dataItem.getDataMap().getStringArrayList("contents");
                deviceListener.onTimeLimeReceived(tweets);
            }
        }
    }
```

####  Protocol:

**To get the timeline in the wearable:**

"/tweets/hi/"					_(Wearable)_	_MessageApi_  <br>
"/tweets/state/available"		_(Device)_ 		_MessageApi_ <br>
"/twitter/timeline"				_(Wearable)_ 	_MessageApi_ <br>
"/twitter/timeline"				_(Device)_ 		_DataApi_

<br>
**Retweet a tweet:**

"/tweets/retweet/"					_(Wearable)_	_MessageApi_ <br>
"/tweets/operation/ok"				_(Device)_ 		_MessageApi_<br>
"/tweets/operation/fail"				_(Device)_ 		_MessageApi_

<br>
**Flag a tweet as favorite:**

"/tweets/favorite/"					_(Wearable)_	_MessageApi_ <br>
"/tweets/operation/ok"				_(Device)_ 		_MessageApi_<br>
"/tweets/operation/fail"			_(Device)_ 		_MessageApi_


## StreamActivity

After _'WaitActivity'_ do the hard work, the _'StreamActivity'_ will be shown, this one will show a ```GridViewPager```, that view will allow to scroll down seeing the available tweets, scrolling right the user will be able to rewtweet a tweet o flag one as favorite.

The ```GridViewPager``` works with a ```FragmentGridAdapter```, that works like the common adapters used in ```ListViews```, ```GridViews```, etc...

```java
        ...
        streamPager = (GridViewPager) findViewById(R.id.stream_pager);

        streamPager.setAdapter(new TwitterAdapter (StreamActivity.this,
                getFragmentManager(), visibleTweets));
                
        ...
```
```java
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
```

And the layout

```xml
<?xml version="1.0" encoding="utf-8"?>

<LinearLayout
	xmlns:android="http://schemas.android.com/apk/res/android"
	android:layout_width="match_parent"
	android:layout_height="match_parent"
	android:orientation="vertical"
	android:background="@drawable/tw_wall"
	>

	<android.support.wearable.view.GridViewPager
	android:id="@+id/stream_pager"
	android:layout_width="match_parent"
	android:layout_height="match_parent"
	android:keepScreenOn="true"
	/>
</LinearLayout>

__NOTE__ _There is a known bug , sometimes the handfeld service is not retweeting and marking a tweet as favorite well, that's because the twitter keys expires. Will be fixed soon_

### Third party libraries

- [Picasso](http://square.github.io/picasso/)
- [CircleImageView](https://github.com/hdodenhof/CircleImageView)
- [Android flat button](https://github.com/hoang8f/android-flat-button)
- [twitter4j](http://twitter4j.org/en/index.html)