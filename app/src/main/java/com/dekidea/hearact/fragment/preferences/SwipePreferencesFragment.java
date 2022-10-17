package com.dekidea.hearact.fragment.preferences;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import com.dekidea.hearact.R;
import com.dekidea.hearact.activity.MainActivity;
import com.dekidea.hearact.dialog.SwipeActionsDialog;
import com.dekidea.hearact.fragment.CompletedDownloadsFragment;
import com.dekidea.hearact.fragment.FeedItemlistFragment;
import com.dekidea.hearact.fragment.InboxFragment;
import com.dekidea.hearact.fragment.QueueFragment;

public class SwipePreferencesFragment extends PreferenceFragmentCompat {
    private static final String PREF_SWIPE_QUEUE = "prefSwipeQueue";
    private static final String PREF_SWIPE_DOWNLOADS = "prefSwipeDownloads";
    private static final String PREF_SWIPE_FEED = "prefSwipeFeed";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_swipe);

        findPreference(PREF_SWIPE_QUEUE).setOnPreferenceClickListener(preference -> {
            new SwipeActionsDialog(requireContext(), QueueFragment.TAG).show(() -> { });
            return true;
        });
        findPreference(PREF_SWIPE_DOWNLOADS).setOnPreferenceClickListener(preference -> {
            new SwipeActionsDialog(requireContext(), CompletedDownloadsFragment.TAG).show(() -> { });
            return true;
        });
        findPreference(PREF_SWIPE_FEED).setOnPreferenceClickListener(preference -> {
            new SwipeActionsDialog(requireContext(), FeedItemlistFragment.TAG).show(() -> { });
            return true;
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        ((MainActivity) getActivity()).setSelectedFragmentTitle(getString(R.string.swipeactions_label));
    }

}
