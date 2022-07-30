package com.tuneurl.podcastplayer.fragment.swipeactions;

import android.content.Context;
import androidx.fragment.app.Fragment;

import com.tuneurl.podcastplayer.model.feed.FeedItem;
import com.tuneurl.podcastplayer.model.feed.FeedItemFilter;

import com.tuneurl.podcastplayer.R;
import com.tuneurl.podcastplayer.adapter.actionbutton.DownloadActionButton;

public class StartDownloadSwipeAction implements SwipeAction {

    @Override
    public String getId() {
        return START_DOWNLOAD;
    }

    @Override
    public int getActionIcon() {
        return R.drawable.ic_download;
    }

    @Override
    public int getActionColor() {
        return R.attr.icon_green;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.download_label);
    }

    @Override
    public void performAction(FeedItem item, Fragment fragment, FeedItemFilter filter) {
        if (!item.isDownloaded() && !item.getFeed().isLocalFeed()) {
            new DownloadActionButton(item)
                    .onClick(fragment.requireContext());
        }
    }

    @Override
    public boolean willRemove(FeedItemFilter filter, FeedItem item) {
        return false;
    }
}
