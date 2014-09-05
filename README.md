# Cliente de Twitter para android wear

1. Motivación
2. Estructura
3. Aplicación del dispositivo
4. Aplicación del wearable
5. Librerías de terceros


## 1. Motivación

La motivación de este proyecto era aprender yo mismo el funcionamiento de android wear: _customViews_, comunicaciones, etc...

Un segundo objetivo y no menos importante es que sirva de proyecto para que otros puedan usarlo a modo de consulta para construir sus proyectos, pienso que resulta muy util un proyecto que implemente algo que te interese para que lo puedas consultar.

## 2. Estructura

Un proyecto de android wear, consta de dos módulos, uno es el proyecto que construirá la aplicación para el dispositivo, y el otro, para el wearable

![](md_images/file_estructure.png)

Ambos proyectos constan de sus propias actividades, recuros, manifest.xml etc... dos proyectos separados, una nota importante es que ambos proyectos han de tener el mismo nombre del paquete.

En tiempo de desarrollo, pudes probar las aplicaciones instalando directamente en el wearable o en el disposivo, pero al hacer la release, el apk sera del proyecto mobile, dentro de ella, se hallará una micro apk que se instalará automáticamente en el dispositivo wearable del usuario.


## 3. Aplicación para el dispositivo

Una aplicación normal android, con sus _activities_, _layouts_ básicamente, de cara al usuario se presenta un navegador para loguearse con twitter, si todo va bién se muestra una actividad con diciéndole que ya está conectado mostrándole su foto de perfil.

La parte interesante desde mi punto de vista es la comunicación con el wearable.

En todo momento el reloj podría pedir la lista de tweets, por lo que una parte de la aplicación siempre debería estar ejecutándose y poder encargarse de ello, para ello creo que un servicio es la mejor opción.

```
<service android:name=".services.WearService"/>
```

Por otro lado, el servicio debe iniciarse desde que se enciende el dispositivo, de otra forma el usuario tendría que abrir la aplicación despues de iniciarlo, no creo que sea la mejor opcion.

```
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
```

Para toda las conexiones con twitter me he basado en la librería **twitter4j**, una librería muy madura y completa para java encargada de toda la interacción con las APIs de twitter, unicamente crear la apllicación en twitter developers, seguir un poco la documentación y listo.

### Comunicación con wearable

Y aquí la parte divertida, después de revisar la documentación de android developers la comunicación con el dispositivo wearable es casi trivial. Hay ciertos pasos que hay que respetar:

1. Conectar gon _Google Play Services_

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

   googleApiClient.connect();
}
```

2. Esperar a que ```onConnected()``` sea llamado

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

En este punto, el dispositivo está preparado para recibir mensajes mandados desde el dispositivos, en _android wear_, hay tres APIs de comunicación: ```MessageApi```, ```NodeApi```, ```DataApi```. 




EXPLICACIón MUY BREVE DE LAS APis
### MessageApi



La primera comunicación con el wearable trata de averiguar si el servicio está corriendo, es algo parecido a:

- ¿Hola, estás ahí?
- Aqui estoy adelante

En el lenguaje de wear:

- ```/tweets/state/hi/``` _(wearable)_
- ```/tweets/state/how4u/``` _(device)_

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

Como se registro el listener para la api de mensajes en ```onConnected()``` también podemos recibir mensajes desde el wearable:

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

Para sincronizar datos, la mejor opción es la ```DataApi```, la que permite enviar tanto un ```payload``` serializando bytes como desees, o en forma de ```DataMaps```, envíando los datos como si de un ```Bundle``` se tratara.

Para esta aplicación se ha utilizado ```DataMaps``` de esa forma, separando los datos por una cadena determinada, (quizás esta no sea la mejor opción) se consiguen enviar datos de una forma sencilla.

,En este caso, se ha implementado una _asynctask_ para el envio del _timeline_, tras recibirlo con _twitter4j_ es mandado como un	 ```ArrayList<string>``` al wearable, se podría haber elegido otro modo, como serializar un _JSON_ y hacer el parser en el wearble, pero por rapidez se eligió ese.

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

### 4.Aplicación para el wearable

Una aplicación para android wear se programa casi igual que una aplicación normal android, la aplicacion que se instala en el wearable tiene únicamente dos actividades:

- ```WaitActivity``` - Actividad mostrada mientras el dispositivo hace la petición a twitter y envía los tweets al wearable

- ```StreamActivity```- Actividad que mostrará el _timeline_ del usuario, también permite hacer _retweet_ o marcar como favorito un _tweet_ determinado

#### WaitActivity

Esta aplicación espera a que el dispositivo haga la petición mientras, por medio de una animación y una vista sencilla construye una especie de spinner y muestra un mensaje al usuario.


![](https://3a3c2487a21adf7eab9f9e1c949cb1c6fbc1a71f.googledrive.com/host/0B62SZ3WRM2R2MldQdDNXVTk5bmc)

El efector del spinner es una simple imagen aplicada por una simple animación

```
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

Si hubiera algún problema el mensaje de 'Loading...' cambiaría al error producido y el fondo mediante un ```<transition>``` se cambiaría a rojo.


![](https://919c5da1a105a3a06e03fa4ec2c9901a70398228.googledrive.com/host/0B62SZ3WRM2R2TXNNWlJPREVXQTA)

```
<?xml version="1.0" encoding="UTF-8"?>
<transition xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:drawable="@drawable/wait_idle" />
    <item android:drawable="@drawable/wait_error" />
</transition>
```


La parte de comunicación con el dispositivo es como la explicada en la parte de la app del dispositivo, se etablece un protocolo de mensajes mediante la ```MessageApi```para finalmente ecuchar al método ```OnDataChanged()``` implementado por ```Wear.DataApi.Listener```, para recibir los tweets en forma de ``ÀrrayList<string>```, 

El protocolo de  comunicación en cuanto al envío es el siguiente:

**Para obtener el timeline:**

"/tweets/hi/"					_(Wearable)_	_MessageApi_ 
"/tweets/state/available"		_(Device)_ 		_MessageApi_
"/twitter/timeline"				_(Wearable)_ 	_MessageApi_
"/twitter/timeline"				_(Device)_ 		_DataApi_


**Para hacer un retweet:**

"/tweets/retweet/"					_(Wearable)_	_MessageApi_ 
"/tweets/operation/ok"				_(Device)_ 		_MessageApi_
"/tweets/operation/fail"				_(Device)_ 		_MessageApi_


**Para hacer un Favorito:**

"/tweets/favorite/"					_(Wearable)_	_MessageApi_ 
"/tweets/operation/ok"				_(Device)_ 		_MessageApi_
"/tweets/operation/fail"			_(Device)_ 		_MessageApi_


En cuanto llega el mensaje ```"/tweets/operation/ok"``` se abre una ```ConfirmationActivity``` produciendo el siguente efecto:


IMAGEN

```java
Intent confirmationActivity = new Intent(getActivity(), ConfirmationActivity.class)
	.putExtra(ConfirmationActivity.EXTRA_ANIMATION_TYPE, ConfirmationActivity.SUCCESS_ANIMATION)
	.putExtra(ConfirmationActivity.EXTRA_MESSAGE, actionText.getText()+"ed");

startActivity(confirmationActivity);

```

Es un poco raro el hecho de declarar dicha actividad en el ```Manifest.xml```

```
...
<activity android:name="android.support.wearable.activity.ConfirmationActivity"/>
...
```

The layout

Despues de pasar 'WaitActivity', se pasa a StreamActivity, esta permite, consultar los tweets del usuario, hacer retweet y marcarlos como favoritos

** __NOTE__ Hay un error conocido conforme a veces se producen errores al hacer retweet después de usar la aplicación varias veces, esto es debido a las keys de twitter, se solucionará en próximos commits

El layout de StreamActivity es el siguiente:

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

```

El GridViewPager, es lo único hay que tener en cuenta, con un simple adaptador podemos asignar un scroll horizontal para cambiar entre fragments de acciones

```java
...
streamPager = (GridViewPager) findViewById(R.id.stream_pager);

streamPager.setAdapter(new TwitterAdapter (StreamActivity.this,
    getFragmentManager(), visibleTweets));
...


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



