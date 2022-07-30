package com.tuneurl.podcastplayer.core.sync;

import android.content.Context;
import android.content.SharedPreferences;

import com.tuneurl.podcastplayer.core.preferences.UserPreferences;
import com.tuneurl.podcastplayer.core.sync.queue.SynchronizationQueueSink;
import com.tuneurl.podcastplayer.core.ClientConfig;

/**
 * Manages preferences for accessing gpodder.net service and other sync providers
 */
public class SynchronizationCredentials {

    private SynchronizationCredentials() {
    }

    private static final String PREF_NAME = "gpodder.net";
    private static final String PREF_USERNAME = "com.tuneurl.podcastplayer.preferences.gpoddernet.username";
    private static final String PREF_PASSWORD = "com.tuneurl.podcastplayer.preferences.gpoddernet.password";
    private static final String PREF_DEVICEID = "com.tuneurl.podcastplayer.preferences.gpoddernet.deviceID";
    private static final String PREF_HOSTNAME = "prefGpodnetHostname";

    private static SharedPreferences getPreferences() {
        return ClientConfig.applicationCallbacks.getApplicationInstance()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static String getUsername() {
        return getPreferences().getString(PREF_USERNAME, null);
    }

    public static void setUsername(String username) {
        getPreferences().edit().putString(PREF_USERNAME, username).apply();
    }

    public static String getPassword() {
        return getPreferences().getString(PREF_PASSWORD, null);
    }

    public static void setPassword(String password) {
        getPreferences().edit().putString(PREF_PASSWORD, password).apply();
    }

    public static String getDeviceID() {
        return getPreferences().getString(PREF_DEVICEID, null);
    }

    public static void setDeviceID(String deviceID) {
        getPreferences().edit().putString(PREF_DEVICEID, deviceID).apply();
    }

    public static String getHosturl() {
        return getPreferences().getString(PREF_HOSTNAME, null);
    }

    public static void setHosturl(String value) {
        getPreferences().edit().putString(PREF_HOSTNAME, value).apply();
    }

    public static synchronized void clear(Context context) {
        setUsername(null);
        setPassword(null);
        setDeviceID(null);
        SynchronizationQueueSink.clearQueue(context);
        UserPreferences.setGpodnetNotificationsEnabled();
    }
}
