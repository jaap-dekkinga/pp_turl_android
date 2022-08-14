package com.tuneurl.podcastplayer.fragment.preferences;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.bytehamster.lib.preferencesearch.SearchConfiguration;
import com.bytehamster.lib.preferencesearch.SearchPreference;
import com.tuneurl.podcastplayer.activity.MainActivity;
import com.tuneurl.podcastplayer.core.util.IntentUtils;
import com.tuneurl.podcastplayer.fragment.preferences.about.AboutFragment;

import com.tuneurl.podcastplayer.R;
import com.tuneurl.podcastplayer.activity.BugReportActivity;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

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

        // If you are writing a spin-off, please update the details on screens like "About" and "Report bug"
        // and afterwards remove the following lines. Please keep in mind that AntennaPod is licensed under the GPL.
        // This means that your application needs to be open-source under the GPL, too.
        // It must also include a prominent copyright notice.
        int packageHash = getContext().getPackageName().hashCode();
        if (packageHash != 1790437538 && packageHash != -1190467065) {
            findPreference(PREF_CATEGORY_PROJECT).setVisible(false);
            Preference copyrightNotice = new Preference(getContext());
            copyrightNotice.setIcon(R.drawable.ic_info_white);
            copyrightNotice.getIcon().mutate()
                    .setColorFilter(new PorterDuffColorFilter(0xffcc0000, PorterDuff.Mode.MULTIPLY));
            copyrightNotice.setSummary("This application is based on AntennaPod."
                    + " The AntennaPod team does NOT provide support for this unofficial version."
                    + " If you can read this message, the developers of this modification"
                    + " violate the GNU General Public License (GPL).");
            findPreference(PREF_CATEGORY_PROJECT).getParent().addPreference(copyrightNotice);
        } else if (packageHash == -1190467065) {
            Preference debugNotice = new Preference(getContext());
            debugNotice.setIcon(R.drawable.ic_info_white);
            debugNotice.getIcon().mutate()
                    .setColorFilter(new PorterDuffColorFilter(0xffcc0000, PorterDuff.Mode.MULTIPLY));
            debugNotice.setOrder(-1);
            debugNotice.setSummary("This is a development version of AntennaPod and not meant for daily use");
            findPreference(PREF_CATEGORY_PROJECT).getParent().addPreference(debugNotice);
        }
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
                            .replace(R.id.settingsContainer, new AboutFragment())
                            .addToBackStack(getString(R.string.about_pref)).commit();
                    return true;
                }
        );
        findPreference(PREF_DOCUMENTATION).setOnPreferenceClickListener(preference -> {
            IntentUtils.openInBrowser(getContext(), getLocalizedWebsiteLink() + "/documentation/");
            return true;
        });
        findPreference(PREF_VIEW_FORUM).setOnPreferenceClickListener(preference -> {
            IntentUtils.openInBrowser(getContext(), "https://forum.antennapod.org/");
            return true;
        });
        findPreference(PREF_CONTRIBUTE).setOnPreferenceClickListener(preference -> {
            IntentUtils.openInBrowser(getContext(), getLocalizedWebsiteLink() + "/contribute/");
            return true;
        });
        findPreference(PREF_SEND_BUG_REPORT).setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(getActivity(), BugReportActivity.class));
            return true;
        });
    }

    private String getLocalizedWebsiteLink() {
        try (InputStream is = getContext().getAssets().open("website-languages.txt")) {
            String[] languages = IOUtils.toString(is, StandardCharsets.UTF_8.name()).split("\n");
            String deviceLanguage = Locale.getDefault().getLanguage();
            if (ArrayUtils.contains(languages, deviceLanguage) && !"en".equals(deviceLanguage)) {
                return "https://antennapod.org/" + deviceLanguage;
            } else {
                return "https://antennapod.org";
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setupSearch() {
        SearchPreference searchPreference = findPreference("searchPreference");
        SearchConfiguration config = searchPreference.getSearchConfiguration();
        config.setActivity((AppCompatActivity) getActivity());
        config.setFragmentContainerViewId(R.id.settingsContainer);
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
