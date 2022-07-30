package com.tuneurl.podcastplayer.fragment.swipeactions;

import android.content.Context;
import androidx.fragment.app.Fragment;

import com.tuneurl.podcastplayer.core.storage.DBWriter;
import com.tuneurl.podcastplayer.model.feed.FeedItem;
import com.tuneurl.podcastplayer.model.feed.FeedItemFilter;

import com.tuneurl.podcastplayer.R;

public class DeleteSwipeAction implements SwipeAction {

    @Override
    public String getId() {
        return DELETE;
    }

    @Override
    public int getActionIcon() {
        return R.drawable.ic_delete;
    }

    @Override
    public int getActionColor() {
        return R.attr.icon_red;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.delete_episode_label);
    }

    @Override
    public void performAction(FeedItem item, Fragment fragment, FeedItemFilter filter) {
        if (!item.isDownloaded()) {
            return;
        }
        DBWriter.deleteFeedMediaOfItem(fragment.requireContext(), item.getMedia().getId());
    }

    @Override
    public boolean willRemove(FeedItemFilter filter, FeedItem item) {
        return filter.showDownloaded && item.isDownloaded();
    }
}
