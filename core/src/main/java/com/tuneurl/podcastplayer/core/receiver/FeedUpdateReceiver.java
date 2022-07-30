package com.tuneurl.podcastplayer.core.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.tuneurl.podcastplayer.core.util.download.AutoUpdateManager;
import com.tuneurl.podcastplayer.core.ClientConfig;

/**
 * Refreshes all feeds when it receives an intent
 */
public class FeedUpdateReceiver extends BroadcastReceiver {

    private static final String TAG = "FeedUpdateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Received intent");
        ClientConfig.initialize(context);

        AutoUpdateManager.runOnce(context);
    }

}
