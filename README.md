# Twitter client for android wear

## 1. Motivation

The motivation of this project was to learn how the android wear framework works, using theirs views, communication APIs, etc...

A second objective was to share it as a reference project, so others can use it, modify it or make what they want with it. 

I think that is very useful to learn by example, so I really aprecciate projects like this.

## 2. Project structure

If you create a project with android wear and android studio, you will be se two main modules: __wear__ and __mobile__.

When you make an android wear _release_, you get a single _.apk_, which will be downloaded from google play by your users, when one of them install that _.apk_ on his phone, another app will be installed automatically on his wearable. That's because the release _apk's_ with android wear, has an 'micro-apk' inside them.

That _micro-apk_ is built by the __wear__ module, and the app that is installed on their phone consists on the __mobile__ module.

![](https://87ccd03f34f5850e9f58050faacff843fdb2a928.googledrive.com/host/0B62SZ3WRM2R2TDVIN2ptNGVndE0)

At develop time, you can compile your __wear__ module directly on your wear device or emulator.

## 3. Handheld app

Like a normal android app, has an _activity_, a few _fragments_ and some layouts, the user, when press the _login_ button, will see a browser with the twitter login, if everything goes well the main fragment will change and the user will see their twitter profile photo, username, and the background will turn to their twitter profile background.

![](https://cef58420144e1df00f893fa4436747dce6972cb0.googledrive.com/host/0B62SZ3WRM2R2V0Y2MlpwR3lEcUU)

The interesting part from my point of view is the communication with the wearable. 

The clock awalys could ask the list of tweets, so the class that asks twitter for the tweets must always be available and able to take care of it, so I think that a service is the best choice.

```
<service android:name=".services.WearService"/>
```

That service must be started when the device boots, otherwise the user would have to open the application after start manually, I do not think it's the best option, I can reach that with a broadcast receiver

```
<receiver android:name=".receivers.BootReceiver" android:enabled="true" android:exported="false">
  <intent-filter>
    <action android:name="android.intent.action.BOOT_COMPLETED"/>
  </intent-filter>
</receiver>
```

For all twitter connections I had relied in the library [twitter4j](http://twitter4j.org/en/index.html), a very mature and complete java library responsible for all interaction with twitter APIs, you only have to create an app in twitter developers, __twitter4j__ will do the hard work.

### Wearable comunication

And here's the fun part, after reviewing the documentation developers android communication with the wearable device is almost trivial. There are certain steps that must be respected:

1. Connect with **Google Play Services**

```java
@Override
public void onCreate() {
   super.onCreate();

   // Init & connect gApiClient
   googleApiClient = new Builder(this)
      .addConnectionCallbacks (gConnectionCallbacks)
      .addOnConnectionFailedListener (gConnectionFailed)
      .addApi(Wearable.API)
      .build();
}
```

2. Call ```.connect()``` method

```java
   googleApiClient.connect();
```

2. Wait ```onConnected()``` to be called

```java
private final ConnectionCallbacks gConnectionCallbacks = new ConnectionCallbacks() {
    @Override
    public void onConnected(Bundle bundle) {

       // Subscribe wear listeners
       Wearable.MessageApi.addListener(googleApiClient, wearMessageListener);
    }

    @Override
    public void onConnectionSuspended(int i) {

        [...]
    }
};

```
At this point, the device is ready to communicate with android wear devices if all went well, otherwhise the ```onConnectionFailedListener``` would be fired.

Now we can use the communicate APIs that google provide us, there is 3 APIs to communicate to communicate with the wearable: ```MessageApi```, ```NodeApi```, ```DataApi```.

### MessageApi


La primera comunicación con el wearable trata de averiguar si el servicio está corriendo, es algo parecido a:

_¿Hola, estás ahí?  _
_Aqui estoy, adelante _<br>

En el lenguaje de wear:

```/tweets/state/hi/``` _(wearable)_ 
```/tweets/state/how4u/``` _(device)_ <br>

Después de esto el _wearable_ ya sabe que hay el servicio esta disponible para poder pedir la lista de tweets del usuario. Para mandar mensajes, tanto en el _wearable_ como en el _device_ he usado la misma implementación de una _asynctask_


```
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

After call to ```googleApiClient()```and wait to ```onConnected()``` were called, we registered to receive message events using the ```Wear.MessageListener.addListener(myListener)``` method, so we are able to manage the messages with this method

```
private final MessageApi.MessageListener wearMessageListener = new MessageApi.MessageListener() {
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        String msg = messageEvent.getPath();
        Log.d("[DEBUG] WearService - onMessageReceived", "Message received: " + msg);
        								
        [...]
    }
}
```

### DataApi

The DataApi allows you to synchronice data between handhelf and wearables, a message in the Data Api consists of a __Payload__, to send whatever data you wish, ad __Path__, a unique string starting with a forward slash.

Instead to send a byte array you can use ```DataMaps```, are used as android bundles inside the DataApi

To send & receive the tweets from the handfeld to the wear device, y used DataMaps with strings composed by fields separed by a pattern (Maybe this is not the best option).

In this case, I have implemented an _asynctask_ for send the twitter timeline after receiving with _twitter4_. The timelien is sent as a ```ArrayList<String>```

````

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

![](https://219ede0846187d7fd9922311fa441d9820915b2c.googledrive.com/host/0B62SZ3WRM2R2bHhlVExmakJEaFU)

An application for android wear is programmed like a normal android app, with a few differences, you can't all the android APIs that you normally use in a common android app such as ```android.net```. Also,  you have to notice that the user experience is a little bit difference than a normal Android app.

The wear app, is composed by the following activities:

- ```WaitActivity``` - An activity shown meanwhile the handfeld app is requesting the user timeline. In backwards this activity makes a hard work of communication with the handfeld app.

- ```StreamActivity```- An activity that shows the user _timeline_, also allows to _retweet_ a tweet or flag as favorited.

#### WaitActivity

The effect of the 'spinner' is a simple ImageView, with the following animation applied

![](https://3a3c2487a21adf7eab9f9e1c949cb1c6fbc1a71f.googledrive.com/host/0B62SZ3WRM2R2MldQdDNXVTk5bmc)
  

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

![](https://919c5da1a105a3a06e03fa4ec2c9901a70398228.googledrive.com/host/0B62SZ3WRM2R2TXNNWlJPREVXQTA)

```
<?xml version="1.0" encoding="UTF-8"?>
<transition xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:drawable="@drawable/wait_idle" />
    <item android:drawable="@drawable/wait_error" />
</transition>
```

```
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

```
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

```
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

"/tweets/hi/"					_(Wearable)_	_MessageApi_ 
"/tweets/state/available"		_(Device)_ 		_MessageApi_
"/twitter/timeline"				_(Wearable)_ 	_MessageApi_
"/twitter/timeline"				_(Device)_ 		_DataApi_


**Retweet a tweet:**

"/tweets/retweet/"					_(Wearable)_	_MessageApi_ 
"/tweets/operation/ok"				_(Device)_ 		_MessageApi_
"/tweets/operation/fail"				_(Device)_ 		_MessageApi_


**Flag a tweet as favorite:**

"/tweets/favorite/"					_(Wearable)_	_MessageApi_ 
"/tweets/operation/ok"				_(Device)_ 		_MessageApi_
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