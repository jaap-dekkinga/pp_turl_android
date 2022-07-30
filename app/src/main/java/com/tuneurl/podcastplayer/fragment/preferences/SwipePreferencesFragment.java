package com.tuneurl.podcastplayer.fragment.preferences;

import android.os.Bundle;
import androidx.preference.PreferenceFragmentCompat;
import com.tuneurl.podcastplayer.R;
import com.tuneurl.podcastplayer.activity.PreferenceActivity;
import com.tuneurl.podcastplayer.dialog.SwipeActionsDialog;
import com.tuneurl.podcastplayer.fragment.CompletedDownloadsFragment;
import com.tuneurl.podcastplayer.fragment.FeedItemlistFragment;
import com.tuneurl.podcastplayer.fragment.InboxFragment;
import com.tuneurl.podcastplayer.fragment.QueueFragment;

public class SwipePreferencesFragment extends PreferenceFragmentCompat {
    private static final String PREF_SWIPE_QUEUE = "prefSwipeQueue";
    private static final String PREF_SWIPE_INBOX = "prefSwipeInbox";
    private static final String PREF_SWIPE_DOWNLOADS = "prefSwipeDownloads";
    private static final String PREF_SWIPE_FEED = "prefSwipeFeed";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_swipe);

        findPreference(PREF_SWIPE_QUEUE).setOnPreferenceClickListener(preference -> {
            new SwipeActionsDialog(requireContext(), QueueFragment.TAG).show(() -> { });
            return true;
        });
        findPreference(PREF_SWIPE_INBOX).setOnPreferenceClickListener(preference -> {
            new SwipeActionsDialog(requireContext(), InboxFragment.TAG).show(() -> { });
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
        ((PreferenceActivity) getActivity()).getSupportActionBar().setTitle(R.string.swipeactions_label);
    }

}
