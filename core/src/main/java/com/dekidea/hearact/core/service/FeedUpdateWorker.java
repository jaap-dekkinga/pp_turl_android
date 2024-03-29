package com.dekidea.hearact.core.service;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.dekidea.hearact.core.preferences.UserPreferences;
import com.dekidea.hearact.core.storage.DBTasks;
import com.dekidea.hearact.core.util.download.AutoUpdateManager;
import com.dekidea.hearact.core.ClientConfig;
import com.dekidea.hearact.core.util.NetworkUtils;

public class FeedUpdateWorker extends Worker {

    private static final String TAG = "FeedUpdateWorker";

    public static final String PARAM_RUN_ONCE = "runOnce";

    public FeedUpdateWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @Override
    @NonNull
    public Result doWork() {
        final boolean isRunOnce = getInputData().getBoolean(PARAM_RUN_ONCE, false);
        Log.d(TAG, "doWork() : isRunOnce = " + isRunOnce);
        ClientConfig.initialize(getApplicationContext());

        if (NetworkUtils.networkAvailable() && NetworkUtils.isFeedRefreshAllowed()) {
            DBTasks.refreshAllFeeds(getApplicationContext(), false);
        } else {
            Log.d(TAG, "Blocking automatic update: no wifi available / no mobile updates allowed");
        }

        if (!isRunOnce && UserPreferences.isAutoUpdateTimeOfDay()) {
            // WorkManager does not allow to set specific time for repeated tasks.
            // We repeatedly schedule a OneTimeWorkRequest instead.
            AutoUpdateManager.restartUpdateAlarm(getApplicationContext());
        }

        return Result.success();
    }
}
