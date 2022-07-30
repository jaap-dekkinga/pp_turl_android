package com.tuneurl.podcastplayer.fragment.swipeactions;

import android.content.Context;

import androidx.fragment.app.Fragment;

import com.tuneurl.podcastplayer.core.storage.DBWriter;
import com.tuneurl.podcastplayer.model.feed.FeedItem;
import com.tuneurl.podcastplayer.model.feed.FeedItemFilter;

import com.tuneurl.podcastplayer.R;

public class MarkFavoriteSwipeAction implements SwipeAction {

    @Override
    public String getId() {
        return MARK_FAV;
    }

    @Override
    public int getActionIcon() {
        return R.drawable.ic_star;
    }

    @Override
    public int getActionColor() {
        return R.attr.icon_yellow;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.add_to_favorite_label);
    }

    @Override
    public void performAction(FeedItem item, Fragment fragment, FeedItemFilter filter) {
        DBWriter.toggleFavoriteItem(item);
    }

    @Override
    public boolean willRemove(FeedItemFilter filter, FeedItem item) {
        return filter.showIsFavorite || filter.showNotFavorite;
    }
}
