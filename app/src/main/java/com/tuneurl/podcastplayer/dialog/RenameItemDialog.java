package com.tuneurl.podcastplayer.dialog;

import android.app.Activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.view.View;
import androidx.appcompat.app.AlertDialog;

import com.tuneurl.podcastplayer.core.storage.DBWriter;
import com.tuneurl.podcastplayer.core.storage.NavDrawerData;
import com.tuneurl.podcastplayer.databinding.EditTextDialogBinding;
import com.tuneurl.podcastplayer.model.feed.Feed;
import com.tuneurl.podcastplayer.model.feed.FeedPreferences;

import com.tuneurl.podcastplayer.R;

public class RenameItemDialog {

    private final WeakReference<Activity> activityRef;
    private Feed feed = null;
    private NavDrawerData.DrawerItem drawerItem = null;

    public RenameItemDialog(Activity activity, Feed feed) {
        this.activityRef = new WeakReference<>(activity);
        this.feed = feed;
    }

    public RenameItemDialog(Activity activity, NavDrawerData.DrawerItem drawerItem) {
        this.activityRef = new WeakReference<>(activity);
        this.drawerItem = drawerItem;
    }

    public void show() {
        Activity activity = activityRef.get();
        if (activity == null) {
            return;
        }

        View content = View.inflate(activity, R.layout.edit_text_dialog, null);
        EditTextDialogBinding alertViewBinding = EditTextDialogBinding.bind(content);
        String title = feed != null ? feed.getTitle() : drawerItem.getTitle();

        alertViewBinding.urlEditText.setText(title);
        AlertDialog dialog = new AlertDialog.Builder(activity)
                .setView(content)
                .setTitle(feed != null ? R.string.rename_feed_label : R.string.rename_tag_label)
                .setPositiveButton(android.R.string.ok, (d, input) -> {
                    String newTitle = alertViewBinding.urlEditText.getText().toString();
                    if (feed != null) {
                        feed.setCustomTitle(newTitle);
                        DBWriter.setFeedCustomTitle(feed);
                    } else {
                        renameTag(newTitle);
                    }
                })
                .setNeutralButton(com.tuneurl.podcastplayer.core.R.string.reset, null)
                .setNegativeButton(com.tuneurl.podcastplayer.core.R.string.cancel_label, null)
                .show();

        // To prevent cancelling the dialog on button click
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(
                (view) -> alertViewBinding.urlEditText.setText(title));
    }

    private void renameTag(String title) {
        if (NavDrawerData.DrawerItem.Type.TAG == drawerItem.type) {
            List<FeedPreferences> feedPreferences = new ArrayList<>();
            for (NavDrawerData.DrawerItem item : ((NavDrawerData.TagDrawerItem) drawerItem).children) {
                feedPreferences.add(((NavDrawerData.FeedDrawerItem) item).feed.getPreferences());
            }

            for (FeedPreferences preferences : feedPreferences) {
                preferences.getTags().remove(drawerItem.getTitle());
                preferences.getTags().add(title);
                DBWriter.setFeedPreferences(preferences);
            }
        }
    }

}
