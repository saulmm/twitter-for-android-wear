package com.saulmm.tweetwear.wear_tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
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

        String activityPath = message;

        NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes(googleApiClient)
            .await();

        SendMessageResult result = Wearable.MessageApi.sendMessage(
            googleApiClient, nodes.getNodes().get(0).getId(), activityPath, null)
            .await();

        if (!result.getStatus().isSuccess()) {

            Log.e ("[ERROR][DEBUG] SendMessageTask - doInBackground",
                "There has been a problem sending the message: "+message);
        }

        return null;
    }
}
