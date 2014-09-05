package com.saulmm.tweetwear.listeners.wear;

import com.google.android.gms.wearable.Node;

import java.util.ArrayList;

public interface ServiceNodeListener {
    void onNodesReceived (ArrayList<Node> wearNodes);
    void onFailedNodes();
}
