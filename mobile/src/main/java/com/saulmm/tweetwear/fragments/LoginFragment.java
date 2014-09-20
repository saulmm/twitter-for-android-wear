package com.saulmm.tweetwear.fragments;

import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import com.saulmm.tweetwear.R;
import com.saulmm.tweetwear.Utils;
import com.saulmm.tweetwear.helpers.TwitterHelper;
import com.saulmm.tweetwear.helpers.TwitterHelperListener;

import twitter4j.auth.RequestToken;

import static android.content.DialogInterface.OnDismissListener;
import static android.view.View.OnClickListener;

public class LoginFragment extends Fragment implements TwitterHelperListener {

    // UI Stuff
    private Dialog authDialog;
    private TextView errorMessageTv;
    private Button twitterLoginFragmentButton;
    private ProgressDialog pDialog;

    // Other stuff
    private boolean authOk;


    private TwitterHelper twHelper;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Utils.startServiceIfNeccessary (getActivity());
        View rootView = initUI(inflater);

        twHelper = new TwitterHelper();
        twHelper.setContext(getActivity());
        twHelper.setListener(this);
        twHelper.initTwitter();

        return rootView;
    }

    private View initUI(LayoutInflater inflater) {

        pDialog = new ProgressDialog(getActivity());
        pDialog.setMessage("Sign in with twitter...\nPlease wait");
        pDialog.setIndeterminate(true);
        pDialog.setCancelable(false);

        View rootView = inflater.inflate(R.layout.fragment_login, null); // TODO Check this

        twitterLoginFragmentButton  = (Button) rootView.findViewById(R.id.tw_login_fragment_button);
        errorMessageTv              = (TextView) rootView.findViewById(R.id.tw_login_error_msg);

        twitterLoginFragmentButton.setOnClickListener(onClickTwitterListener);
        return rootView;
    }


    private final OnClickListener onClickTwitterListener = new OnClickListener() {
        @Override
        public void onClick(View v) {

            errorMessageTv.setText("");
            pDialog.show();

            twHelper.setListener(LoginFragment.this);
            twHelper.startAuthorizationUrlTask();
        }
    };


    private final WebViewClient oauthWebClient = new WebViewClient() {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            Log.d("[DEBUG] GetTokenTask - onPageStarted", "Loading URL: " + url);

            if (url.startsWith("http://saulmm.com/")) {
                authDialog.dismiss();
                authOk = true;
                pDialog.show();
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {

            super.onPageFinished(view, url);

            if (url.contains("oauth_verifier")) {

                Uri uri = Uri.parse(url);
                String oauthVerifier = uri.getQueryParameter("oauth_verifier");

                twHelper.setOauthVerifier(oauthVerifier);
                twHelper.startAccessTokenTask();


            } else if (url.contains("denied")) {
                Log.e("[ERROR] GetTokenTask - onPageFinished", "[ERROR] Denied");
            }
        }
    };


    private final OnDismissListener onDismissDialogListener = new OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {

            // The user dismissed the dialog
            if (!authOk) {

                // Reinitialize the twitter client
                twHelper.initTwitter();
                pDialog.dismiss();
                String errorMsg = "You must be logged in to use twitter in android wear"; // TODO Hardcoded string
                showButtonError(errorMsg);
            }
        }
    };


    @Override
    public void onAuthorizationURLReceived(String url) {

        if (url != null) {
            authDialog = new Dialog(getActivity(), android.R.style.Theme_Light_NoTitleBar_Fullscreen);
            authDialog.setOnDismissListener(onDismissDialogListener);
            authDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            authDialog.setContentView(R.layout.dialog_twitter_authorization);

            WebView webview = (WebView) authDialog.findViewById(R.id.webv);

            webview.getSettings().setJavaScriptEnabled(true);
            webview.loadUrl("file://android_asset/index.html");
            webview.loadUrl(url);
            webview.setWebViewClient(oauthWebClient);


            authDialog.show();
            authDialog.setCancelable(true);

        } else {
            Log.e ("[ERROR] LoginFragment - onAuthorizationURLReceived", "Network Error or Invalid Credentials");
//            twitterLoginFragmentButton.setProgress(-1);

        }
    }

    @Override
    public void onAuthorizationURLFailed(String cause) {
        if (cause.equals ("401")) {
            String errorMsg = "Invalid twitter api keys"; // TODO harcoded string
            showButtonError (errorMsg);
        }
    }

    private void showButtonError(String errorMsg) {

        errorMessageTv.setText (errorMsg);
//        twitterLoginFragmentButton.setProgress(-1);
        twitterLoginFragmentButton.setOnClickListener(null);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//            twitterLoginFragmentButton.setProgress(0);
            twitterLoginFragmentButton.setOnClickListener(onClickTwitterListener);

            }
        }, 2000);
    }

    @Override
    public void onAccessTokenReceived() {

        authOk = true;
        twitterLoginFragmentButton.setEnabled(false);
        pDialog.dismiss();


        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.content_frame, new UserFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_ENTER_MASK);
        ft.remove(LoginFragment.this);
        ft.commit();

    }

    @Override
    public void onRequestTokenReceived(RequestToken rToken) {

        // I know that this is not the best way to save the request token
        twHelper.setRequestToken (rToken);
    }
}