package com.dekidea.hearact.adapter.actionbutton;

import android.content.Context;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.dekidea.hearact.core.preferences.UserPreferences;
import com.dekidea.hearact.core.service.download.DownloadService;
import com.dekidea.hearact.core.storage.DBWriter;
import com.dekidea.hearact.model.feed.FeedItem;
import com.dekidea.hearact.model.feed.FeedMedia;

import com.dekidea.hearact.R;

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
