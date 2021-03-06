package com.tuneurl.podcastplayer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.tuneurl.podcastplayer.core.ClientConfig;
import com.tuneurl.podcastplayer.core.service.download.DownloadRequestCreator;
import com.tuneurl.podcastplayer.core.service.download.DownloadService;
import com.tuneurl.podcastplayer.model.feed.Feed;

import java.util.Arrays;

import com.tuneurl.podcastplayer.R;

/**
 * Receives intents from AntennaPod Single Purpose apps
 */
public class SPAReceiver extends BroadcastReceiver{
    private static final String TAG = "SPAReceiver";

    public static final String ACTION_SP_APPS_QUERY_FEEDS = "com.tuneurl.podcastplayer.intent.SP_APPS_QUERY_FEEDS";
    private static final String ACTION_SP_APPS_QUERY_FEEDS_REPSONSE = "com.tuneurl.podcastplayer.intent.SP_APPS_QUERY_FEEDS_RESPONSE";
    private static final String ACTION_SP_APPS_QUERY_FEEDS_REPSONSE_FEEDS_EXTRA = "feeds";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!TextUtils.equals(intent.getAction(), ACTION_SP_APPS_QUERY_FEEDS_REPSONSE)) {
            return;
        }
        Log.d(TAG, "Received SP_APPS_QUERY_RESPONSE");
        if (!intent.hasExtra(ACTION_SP_APPS_QUERY_FEEDS_REPSONSE_FEEDS_EXTRA)) {
            Log.e(TAG, "Received invalid SP_APPS_QUERY_RESPONSE: Contains no extra");
            return;
        }
        String[] feedUrls = intent.getStringArrayExtra(ACTION_SP_APPS_QUERY_FEEDS_REPSONSE_FEEDS_EXTRA);
        if (feedUrls == null) {
            Log.e(TAG, "Received invalid SP_APPS_QUERY_REPSONSE: extra was null");
            return;
        }
        Log.d(TAG, "Received feeds list: " + Arrays.toString(feedUrls));
        ClientConfig.initialize(context);
        for (String url : feedUrls) {
            Feed f = new Feed(url, null);
            DownloadService.download(context, false, DownloadRequestCreator.create(f).build());
        }
        Toast.makeText(context, R.string.sp_apps_importing_feeds_msg, Toast.LENGTH_LONG).show();
    }
}
