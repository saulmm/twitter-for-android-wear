package com.saulmm.tweetwear.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.saulmm.tweetwear.R;
import com.saulmm.tweetwear.activities.MainActivity;
import com.saulmm.tweetwear.helpers.tasks.GetNodesTask;
import com.saulmm.tweetwear.listeners.wear.ServiceNodeListener;
import com.saulmm.tweetwear.services.WearService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class UserFragment extends Fragment
    implements NodeApi.NodeListener {

    private SharedPreferences preferences;

    // Service
    private WearService wearService;
    private boolean isBound;
    private LinearLayout hintHolderLn;
    private ImageView hintIconImg;
    private TextView hintTv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        preferences = getActivity().getSharedPreferences(
            MainActivity.PREFS,
            Context.MODE_PRIVATE);

        View rootView = initUI(inflater);

//        Utils.startServiceIfNeccessary(getActivity());

        return rootView;
    }

    private View initUI(LayoutInflater inflater) {
        View rootView = inflater.inflate(R.layout.fragment_user, null);

        TextView nameTv             = (TextView) rootView.findViewById (R.id.tw_name);
        TextView usernameTv         = (TextView) rootView.findViewById (R.id.tw_username);
        Button revokeButton         = (Button) rootView.findViewById (R.id.tw_user_revoke);
        ImageView profileImg        = (ImageView) rootView.findViewById (R.id.tw_profile_img);
        ImageView userBackground    = (ImageView) rootView.findViewById (R.id.tw_user_background);

        hintTv                      = (TextView) rootView.findViewById (R.id.tw_hint_text);
        hintIconImg                 = (ImageView) rootView.findViewById (R.id.tw_hint_icon);
        hintHolderLn                = (LinearLayout) rootView.findViewById (R.id.tw_hint_holder);

        Picasso.with(getActivity())
            .load(preferences.getString("IMAGE_URL", ""))
            .placeholder(R.drawable.placeholder_user)
            .into(profileImg);

        String backgroundURL = preferences.getString("BACKGROUND_IMG", "");

        if (!backgroundURL.equals("")) {

            Picasso.with(getActivity())
                .load(backgroundURL)
                .placeholder(R.drawable.background)
                .error(R.drawable.background)
                .into(userBackground);
        }

        nameTv.setText (preferences.getString("NAME", ""));
        usernameTv.setText ("@"+preferences.getString("USER_NAME", ""));
        revokeButton.setOnClickListener(onClickListener);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().startService(
            new Intent (getActivity(),
            WearService.class));

//        doBindService();

        if (wearService != null) {
            wearService.addNodeApiListener(this);
            Log.d ("[DEBUG] UserFragment - onResume", "Node listener added");
        }
    }

    @Override
    public void onPause() {

        super.onPause();
        doUnbindService();
    }


    private final View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

        SharedPreferences.Editor prefEditor = preferences.edit();
        prefEditor.putString("ACCESS_TOKEN", "");
        prefEditor.putString("ACCESS_TOKEN_SECRET", "");
        prefEditor.commit();

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, new LoginFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.remove(UserFragment.this);
        ft.commit();
        }
    };


    // Binder to maintain a conversation with the wear & twitter service
    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            wearService = ((WearService.LocalBinder) service).getService();
            Log.i("[INFO] UserFragment - onServiceConnected", "Service connected");
            Log.d ("[DEBUG] UserFragment - onServiceConnected", "Is connected ? "+wearService.isConnected());

            new GetNodesTask(nodeListener, wearService.getGoogleApiClient()).execute();
        }


        @Override
        public void onServiceDisconnected(ComponentName name) {

            wearService = null;
            Log.i ("[INFO] UserFragment - onServiceDisconnected", "Service disconnected");
        }
    };

    private final ServiceNodeListener nodeListener = new ServiceNodeListener() {
        @Override
        public void onNodesReceived(ArrayList<Node> wearNodes) {
            Log.d ("[DEBUG] UserFragment - onNodesReceived", "Nodes received...");
            hintTv.setText(getString(R.string.connected_wearable_message));
            hintIconImg.setImageResource(R.drawable.ok);

            hintHolderLn.setVisibility(View.VISIBLE);
            hintHolderLn.startAnimation(AnimationUtils
                    .loadAnimation(getActivity(), R.anim.alpha_on));
        }

        @Override
        public void onFailedNodes() {
            hintIconImg.setImageResource(R.drawable.error);
            hintTv.setText( getString(R.string.disconnected_wearable));

            hintHolderLn.setVisibility(View.VISIBLE);
            hintHolderLn.startAnimation(AnimationUtils
                    .loadAnimation(getActivity(), R.anim.alpha_on));
        }
    };


    private void doBindService () {


        getActivity().bindService(new Intent(getActivity(), WearService.class),
                mConnection, Context.BIND_AUTO_CREATE);

        isBound = true;
    }

    private void doUnbindService () {

        if (isBound) {
            getActivity().unbindService(mConnection);

            isBound = false;
        }
    }


    @Override
    public void onPeerConnected(Node node) {
        Log.d ("[DEBUG] UserFragment - onPeerConnected", "Peer connected");

    }

    @Override
    public void onPeerDisconnected(Node node) {
        Log.d ("[DEBUG] UserFragment - onPeerDisconnected", "Peer disconnected");
    }
}
