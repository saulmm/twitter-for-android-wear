package com.saulmm.tweetwear.helpers.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.saulmm.tweetwear.listeners.wear.ServiceNodeListener;

import java.util.ArrayList;

public class GetNodesTask extends AsyncTask <Void, Void, ArrayList <Node>> {

    private final GoogleApiClient googleApiClient;
    private final ServiceNodeListener nodeListener;


    public GetNodesTask(ServiceNodeListener nodeListener, GoogleApiClient googleApiClient) {
        this.nodeListener = nodeListener;
        this.googleApiClient = googleApiClient;
    }


    @Override
    protected ArrayList <Node> doInBackground(Void... params) {
        Log.d("[DEBUG] GetNodesTask - doInBackground", "Nodes tasks");

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