package com.dekidea.hearact.fragment.preferences;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;

import com.dekidea.hearact.activity.MainActivity;
import com.dekidea.hearact.core.sync.SynchronizationSettings;

import com.dekidea.hearact.R;

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
        ((MainActivity) getActivity()).getSupportActionBar().setTitle(R.string.notification_pref_fragment);
    }

    private void setUpScreen() {
        findPreference(PREF_GPODNET_NOTIFICATIONS).setEnabled(SynchronizationSettings.isProviderConnected());
    }
}
