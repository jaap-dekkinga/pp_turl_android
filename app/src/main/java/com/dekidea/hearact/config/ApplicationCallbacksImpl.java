package com.dekidea.hearact.config;


import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.dekidea.hearact.activity.StorageErrorActivity;
import com.dekidea.hearact.core.ApplicationCallbacks;
import com.dekidea.hearact.App;

public class ApplicationCallbacksImpl implements ApplicationCallbacks {

    @Override
    public Application getApplicationInstance() {
        return App.getInstance();
    }

    @Override
    public Intent getStorageErrorActivity(Context context) {
        return new Intent(context, StorageErrorActivity.class);
    }

}
