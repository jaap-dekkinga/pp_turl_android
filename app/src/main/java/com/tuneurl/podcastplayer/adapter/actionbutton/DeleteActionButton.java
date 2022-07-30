package com.tuneurl.podcastplayer.adapter.actionbutton;

import android.content.Context;
import android.view.View;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.tuneurl.podcastplayer.core.storage.DBWriter;
import com.tuneurl.podcastplayer.model.feed.FeedItem;
import com.tuneurl.podcastplayer.model.feed.FeedMedia;

import com.tuneurl.podcastplayer.R;

public class DeleteActionButton extends ItemActionButton {

    public DeleteActionButton(FeedItem item) {
        super(item);
    }

    @Override
    @StringRes
    public int getLabel() {
        return R.string.delete_label;
    }

    @Override
    @DrawableRes
    public int getDrawable() {
        return R.drawable.ic_delete;
    }

    @Override
    public void onClick(Context context) {
        final FeedMedia media = item.getMedia();
        if (media == null) {
            return;
        }
        DBWriter.deleteFeedMediaOfItem(context, media.getId());
    }

    @Override
    public int getVisibility() {
        return (item.getMedia() != null && item.getMedia().isDownloaded()) ? View.VISIBLE : View.INVISIBLE;
    }
}
