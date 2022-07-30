package com.tuneurl.podcastplayer.fragment.swipeactions;

import android.content.Context;

import androidx.fragment.app.Fragment;

import com.tuneurl.podcastplayer.core.storage.DBWriter;
import com.tuneurl.podcastplayer.model.feed.FeedItem;
import com.tuneurl.podcastplayer.model.feed.FeedItemFilter;

import com.tuneurl.podcastplayer.R;

public class AddToQueueSwipeAction implements SwipeAction {

    @Override
    public String getId() {
        return ADD_TO_QUEUE;
    }

    @Override
    public int getActionIcon() {
        return R.drawable.ic_playlist;
    }

    @Override
    public int getActionColor() {
        return R.attr.colorAccent;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.add_to_queue_label);
    }

    @Override
    public void performAction(FeedItem item, Fragment fragment, FeedItemFilter filter) {
        if (!item.isTagged(FeedItem.TAG_QUEUE)) {
            DBWriter.addQueueItem(fragment.requireContext(), item);
        } else {
            new RemoveFromQueueSwipeAction().performAction(item, fragment, filter);
        }
    }

    @Override
    public boolean willRemove(FeedItemFilter filter, FeedItem item) {
        return filter.showQueued || filter.showNew;
    }
}
