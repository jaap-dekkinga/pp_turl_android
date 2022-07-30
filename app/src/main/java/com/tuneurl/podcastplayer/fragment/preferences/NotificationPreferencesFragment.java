package com.tuneurl.podcastplayer.fragment.preferences;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

import com.tuneurl.podcastplayer.core.sync.SynchronizationSettings;

import com.tuneurl.podcastplayer.R;
import com.tuneurl.podcastplayer.activity.PreferenceActivity;

public class NotificationPreferencesFragment extends PreferenceFragmentCompat {

    private static final String PREF_GPODNET_NOTIFICATIONS = "pref_gpodnet_notifications";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_notifications);
        setUpScreen();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((PreferenceActivity) getActivity()).getSupportActionBar().setTitle(R.string.notification_pref_fragment);
    }

    private void setUpScreen() {
        findPreference(PREF_GPODNET_NOTIFICATIONS).setEnabled(SynchronizationSettings.isProviderConnected());
    }
}
