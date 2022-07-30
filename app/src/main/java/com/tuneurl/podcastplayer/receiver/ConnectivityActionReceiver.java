package com.tuneurl.podcastplayer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.text.TextUtils;
import android.util.Log;

import com.tuneurl.podcastplayer.core.ClientConfig;
import com.tuneurl.podcastplayer.core.util.NetworkUtils;

public class ConnectivityActionReceiver extends BroadcastReceiver {
    private static final String TAG = "ConnectivityActionRecvr";

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (TextUtils.equals(intent.getAction(), ConnectivityManager.CONNECTIVITY_ACTION)) {
            Log.d(TAG, "Received intent");

            ClientConfig.initialize(context);
            NetworkUtils.networkChangedDetected();
        }
    }
}
