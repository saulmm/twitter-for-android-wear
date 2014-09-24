package com.saulmm.tweetwear.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;

import static com.google.android.gms.wearable.MessageApi.SendMessageResult;

public class SendMessageTask extends AsyncTask<Void, Void, Void> {

    private String message;
    private Node connectedNode;
    private GoogleApiClient googleApiClient;

    public SendMessageTask(String message, GoogleApiClient googleApiClient, Node connectedNode) {

        this.message = message;
        this.googleApiClient = googleApiClient;
        this.connectedNode = connectedNode;
    }

    @Override
    protected Void doInBackground(Void... params) {

        Log.d("[DEBUG] SendMessageTask - doInBackground",
                "Sending message: " + message);

        String activityPath = message;

        SendMessageResult result = Wearable.MessageApi.sendMessage(
            googleApiClient, connectedNode.getId(), activityPath, null)
            .await();

        if (!result.getStatus().isSuccess()) {
            Log.e ("[ERROR][DEBUG] SendMessageTask - doInBackground",
                "There has been a problem sending the message: "+message);

        } else {
            Log.d ("[DEBUG] SendMessageTask - doInBackground",
                "Message sent ok, "+message);
        }

        return null;
    }
}
