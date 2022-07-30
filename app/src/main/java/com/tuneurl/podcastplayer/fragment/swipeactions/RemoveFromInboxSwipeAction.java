package com.tuneurl.podcastplayer.fragment.swipeactions;

import android.content.Context;

import androidx.fragment.app.Fragment;

import com.tuneurl.podcastplayer.model.feed.FeedItem;
import com.tuneurl.podcastplayer.model.feed.FeedItemFilter;

import com.tuneurl.podcastplayer.R;
import com.tuneurl.podcastplayer.menuhandler.FeedItemMenuHandler;

public class RemoveFromInboxSwipeAction implements SwipeAction {

    @Override
    public String getId() {
        return REMOVE_FROM_INBOX;
    }

    @Override
    public int getActionIcon() {
        return R.drawable.ic_check;
    }

    @Override
    public int getActionColor() {
        return R.attr.icon_purple;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.remove_inbox_label);
    }

    @Override
    public void performAction(FeedItem item, Fragment fragment, FeedItemFilter filter) {
        if (item.isNew()) {
            FeedItemMenuHandler.markReadWithUndo(fragment, item, FeedItem.UNPLAYED, willRemove(filter, item));
        }
    }

    @Override
    public boolean willRemove(FeedItemFilter filter, FeedItem item) {
        return filter.showNew;
    }
}
