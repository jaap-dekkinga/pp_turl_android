package com.tuneurl.podcastplayer.core;

import android.content.Context;

import com.tuneurl.podcastplayer.core.preferences.PlaybackPreferences;
import com.tuneurl.podcastplayer.core.preferences.SleepTimerPreferences;
import com.tuneurl.podcastplayer.core.preferences.UsageStatistics;
import com.tuneurl.podcastplayer.core.preferences.UserPreferences;
import com.tuneurl.podcastplayer.core.service.download.AntennapodHttpClient;
import com.tuneurl.podcastplayer.core.util.NetworkUtils;
import com.tuneurl.podcastplayer.core.util.gui.NotificationUtils;
import com.tuneurl.podcastplayer.net.ssl.SslProviderInstaller;
import com.tuneurl.podcastplayer.storage.database.PodDBAdapter;

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
