package com.tuneurl.podcastplayer.adapter.actionbutton;

import android.content.Context;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import android.view.View;

import com.tuneurl.podcastplayer.core.storage.DBWriter;
import com.tuneurl.podcastplayer.model.feed.FeedItem;

import com.tuneurl.podcastplayer.R;

public class MarkAsPlayedActionButton extends ItemActionButton {

    public MarkAsPlayedActionButton(FeedItem item) {
        super(item);
    }

    @Override
    @StringRes
    public int getLabel() {
        return (item.hasMedia() ? R.string.mark_read_label : R.string.mark_read_no_media_label);
    }

    @Override
    @DrawableRes
    public int getDrawable() {
        return R.drawable.ic_check;
    }

    @Override
    public void onClick(Context context) {
        if (!item.isPlayed()) {
            DBWriter.markItemPlayed(item, FeedItem.PLAYED, true);
        }
    }

    @Override
    public int getVisibility() {
        return (item.isPlayed()) ? View.INVISIBLE : View.VISIBLE;
    }
}
