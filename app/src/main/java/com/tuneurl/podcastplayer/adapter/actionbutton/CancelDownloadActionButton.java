package com.tuneurl.podcastplayer.adapter.actionbutton;

import android.content.Context;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.tuneurl.podcastplayer.core.preferences.UserPreferences;
import com.tuneurl.podcastplayer.core.service.download.DownloadService;
import com.tuneurl.podcastplayer.core.storage.DBWriter;
import com.tuneurl.podcastplayer.model.feed.FeedItem;
import com.tuneurl.podcastplayer.model.feed.FeedMedia;

import com.tuneurl.podcastplayer.R;

public class CancelDownloadActionButton extends ItemActionButton {

    public CancelDownloadActionButton(FeedItem item) {
        super(item);
    }

    @Override
    @StringRes
    public int getLabel() {
        return R.string.cancel_download_label;
    }

    @Override
    @DrawableRes
    public int getDrawable() {
        return R.drawable.ic_cancel;
    }

    @Override
    public void onClick(Context context) {
        FeedMedia media = item.getMedia();
        DownloadService.cancel(context, media.getDownload_url());
        if (UserPreferences.isEnableAutodownload()) {
            item.disableAutoDownload();
            DBWriter.setFeedItem(item);
        }
    }
}
