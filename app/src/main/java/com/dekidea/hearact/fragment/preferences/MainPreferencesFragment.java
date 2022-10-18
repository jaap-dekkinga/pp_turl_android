package com.dekidea.hearact.fragment.preferences;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.bytehamster.lib.preferencesearch.SearchConfiguration;
import com.bytehamster.lib.preferencesearch.SearchPreference;
import com.dekidea.hearact.activity.MainActivity;
import com.dekidea.hearact.fragment.preferences.about.AboutFragment;

import com.dekidea.hearact.R;

public class MainPreferencesFragment extends PreferenceFragmentCompat {

    public static final String TAG = "MainPreferencesFragment";
    private static final String PREF_SCREEN_USER_INTERFACE = "prefScreenInterface";
    private static final String PREF_SCREEN_PLAYBACK = "prefScreenPlayback";
    private static final String PREF_SCREEN_NETWORK = "prefScreenNetwork";
    private static final String PREF_SCREEN_SYNCHRONIZATION = "prefScreenSynchronization";
    private static final String PREF_SCREEN_STORAGE = "prefScreenStorage";
    private static final String PREF_DOCUMENTATION = "prefDocumentation";
    private static final String PREF_VIEW_FORUM = "prefViewForum";
    private static final String PREF_SEND_BUG_REPORT = "prefSendBugReport";
    private static final String PREF_CATEGORY_PROJECT = "project";
    private static final String PREF_ABOUT = "prefAbout";
    private static final String PREF_NOTIFICATION = "notifications";
    private static final String PREF_CONTRIBUTE = "prefContribute";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        addPreferencesFromResource(R.xml.preferences);
        setupMainScreen();
        setupSearch();
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) getActivity()).setSelectedFragmentTitle(getString(R.string.settings_label));
    }

    private void setupMainScreen() {
        findPreference(PREF_SCREEN_USER_INTERFACE).setOnPreferenceClickListener(preference -> {
            ((MainActivity) getActivity()).openScreen(R.xml.preferences_user_interface);
            return true;
        });
        findPreference(PREF_SCREEN_PLAYBACK).setOnPreferenceClickListener(preference -> {
            ((MainActivity) getActivity()).openScreen(R.xml.preferences_playback);
            return true;
        });
        findPreference(PREF_SCREEN_NETWORK).setOnPreferenceClickListener(preference -> {
            ((MainActivity) getActivity()).openScreen(R.xml.preferences_network);
            return true;
        });
        findPreference(PREF_SCREEN_SYNCHRONIZATION).setOnPreferenceClickListener(preference -> {
            ((MainActivity) getActivity()).openScreen(R.xml.preferences_synchronization);
            return true;
        });
        findPreference(PREF_SCREEN_STORAGE).setOnPreferenceClickListener(preference -> {
            ((MainActivity) getActivity()).openScreen(R.xml.preferences_storage);
            return true;
        });
        findPreference(PREF_NOTIFICATION).setOnPreferenceClickListener(preference -> {
            ((MainActivity) getActivity()).openScreen(R.xml.preferences_notifications);
            return true;
        });
        findPreference(PREF_ABOUT).setOnPreferenceClickListener(
                preference -> {
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.main_view, new AboutFragment())
                            .addToBackStack(getString(R.string.about_pref)).commit();
                    return true;
                }
        );
    }

    private void setupSearch() {

        SearchPreference searchPreference = findPreference("searchPreference");
        SearchConfiguration config = searchPreference.getSearchConfiguration();
        config.setActivity((AppCompatActivity) getActivity());
        config.setFragmentContainerViewId(R.id.main_view);
        config.setBreadcrumbsEnabled(true);

        config.index(R.xml.preferences_user_interface)
                .addBreadcrumb(MainActivity.getTitleOfPage(R.xml.preferences_user_interface));
        config.index(R.xml.preferences_playback)
                .addBreadcrumb(MainActivity.getTitleOfPage(R.xml.preferences_playback));
        config.index(R.xml.preferences_network)
                .addBreadcrumb(MainActivity.getTitleOfPage(R.xml.preferences_network));
        config.index(R.xml.preferences_storage)
                .addBreadcrumb(MainActivity.getTitleOfPage(R.xml.preferences_storage));
        config.index(R.xml.preferences_import_export)
                .addBreadcrumb(MainActivity.getTitleOfPage(R.xml.preferences_storage))
                .addBreadcrumb(MainActivity.getTitleOfPage(R.xml.preferences_import_export));
        config.index(R.xml.preferences_autodownload)
                .addBreadcrumb(MainActivity.getTitleOfPage(R.xml.preferences_network))
                .addBreadcrumb(R.string.automation)
                .addBreadcrumb(MainActivity.getTitleOfPage(R.xml.preferences_autodownload));
        config.index(R.xml.preferences_synchronization)
                .addBreadcrumb(MainActivity.getTitleOfPage(R.xml.preferences_synchronization));
        config.index(R.xml.preferences_notifications)
                .addBreadcrumb(MainActivity.getTitleOfPage(R.xml.preferences_notifications));
        config.index(R.xml.feed_settings)
                .addBreadcrumb(MainActivity.getTitleOfPage(R.xml.feed_settings));
        config.index(R.xml.preferences_swipe)
                .addBreadcrumb(MainActivity.getTitleOfPage(R.xml.preferences_user_interface))
                .addBreadcrumb(MainActivity.getTitleOfPage(R.xml.preferences_swipe));
    }
}
