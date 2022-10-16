package com.dekidea.hearact.fragment.swipeactions;

import android.content.Context;

import androidx.fragment.app.Fragment;

import com.dekidea.hearact.core.storage.DBWriter;
import com.dekidea.hearact.model.feed.FeedItem;
import com.dekidea.hearact.model.feed.FeedItemFilter;

import com.dekidea.hearact.R;

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
