package com.dekidea.hearact.fragment.preferences.about;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;

import com.dekidea.hearact.BuildConfig;
import androidx.preference.PreferenceFragmentCompat;

import com.dekidea.hearact.activity.MainActivity;
import com.dekidea.hearact.core.util.IntentUtils;
import com.google.android.material.snackbar.Snackbar;

import com.dekidea.hearact.R;

public class AboutFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_about);

        findPreference("about_version").setSummary(String.format(
                "%s (%s)", BuildConfig.VERSION_NAME, BuildConfig.FLAVOR));
        findPreference("about_version").setOnPreferenceClickListener((preference) -> {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(getString(R.string.bug_report_title),
                    findPreference("about_version").getSummary());
            clipboard.setPrimaryClip(clip);
            Snackbar.make(getView(), R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT).show();
            return true;
        });

        findPreference("about_privacy_policy").setOnPreferenceClickListener((preference) -> {
            IntentUtils.openInBrowser(getContext(), "https://REMAINS TO BE SET");
            return true;
        });

        findPreference("about_licenses").setOnPreferenceClickListener((preference) -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_view, new LicensesFragment())
                    .addToBackStack(getString(R.string.licenses)).commit();
            return true;
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) getActivity()).setSelectedFragmentTitle(getString(R.string.about_pref));
    }
}
