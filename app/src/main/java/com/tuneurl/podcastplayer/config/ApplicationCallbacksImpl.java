package com.tuneurl.podcastplayer.config;


import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.tuneurl.podcastplayer.activity.StorageErrorActivity;
import com.tuneurl.podcastplayer.core.ApplicationCallbacks;
import com.tuneurl.podcastplayer.PodcastApp;

public class ApplicationCallbacksImpl implements ApplicationCallbacks {

    @Override
    public Application getApplicationInstance() {
        return PodcastApp.getInstance();
    }

    @Override
    public Intent getStorageErrorActivity(Context context) {
        return new Intent(context, StorageErrorActivity.class);
    }

}
