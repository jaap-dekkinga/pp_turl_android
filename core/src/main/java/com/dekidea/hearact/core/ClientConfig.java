package com.dekidea.hearact.core;

import android.content.Context;

import com.dekidea.hearact.core.preferences.PlaybackPreferences;
import com.dekidea.hearact.core.preferences.SleepTimerPreferences;
import com.dekidea.hearact.core.preferences.UsageStatistics;
import com.dekidea.hearact.core.preferences.UserPreferences;
import com.dekidea.hearact.core.service.download.AntennapodHttpClient;
import com.dekidea.hearact.core.util.NetworkUtils;
import com.dekidea.hearact.core.util.gui.NotificationUtils;
import com.dekidea.hearact.net.ssl.SslProviderInstaller;
import com.dekidea.hearact.storage.database.PodDBAdapter;

import java.io.File;

/**
 * Stores callbacks for core classes like Services, DB classes etc. and other configuration variables.
 * Apps using the core module of AntennaPod should register implementations of all interfaces here.
 */
public class ClientConfig {

    /**
     * Should be used when setting User-Agent header for HTTP-requests.
     */
    public static String USER_AGENT;

    public static ApplicationCallbacks applicationCallbacks;

    public static DownloadServiceCallbacks downloadServiceCallbacks;

    private static boolean initialized = false;

    public static synchronized void initialize(Context context) {
        if (initialized) {
            return;
        }
        PodDBAdapter.init(context);
        UserPreferences.init(context);
        UsageStatistics.init(context);
        PlaybackPreferences.init(context);
        SslProviderInstaller.install(context);
        NetworkUtils.init(context);
        AntennapodHttpClient.setCacheDirectory(new File(context.getCacheDir(), "okhttp"));
        AntennapodHttpClient.setProxyConfig(UserPreferences.getProxyConfig());
        SleepTimerPreferences.init(context);
        NotificationUtils.createChannels(context);
        initialized = true;
    }
}
