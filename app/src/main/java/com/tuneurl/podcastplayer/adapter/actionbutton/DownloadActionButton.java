package com.tuneurl.podcastplayer.adapter.actionbutton;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.tuneurl.podcastplayer.core.preferences.UsageStatistics;
import com.tuneurl.podcastplayer.core.service.download.DownloadRequestCreator;
import com.tuneurl.podcastplayer.core.service.download.DownloadService;
import com.tuneurl.podcastplayer.core.storage.DBWriter;
import com.tuneurl.podcastplayer.core.util.NetworkUtils;
import com.tuneurl.podcastplayer.model.feed.FeedItem;
import com.tuneurl.podcastplayer.model.feed.FeedMedia;

import com.tuneurl.podcastplayer.R;

public class DownloadActionButton extends ItemActionButton {

    public DownloadActionButton(FeedItem item) {
        super(item);
    }

    @Override
    @StringRes
    public int getLabel() {
        return R.string.download_label;
    }

    @Override
    @DrawableRes
    public int getDrawable() {
        return R.drawable.ic_download;
    }

    @Override
    public int getVisibility() {
        return item.getFeed().isLocalFeed() ? View.INVISIBLE : View.VISIBLE;
    }

    @Override
    public void onClick(Context context) {
        final FeedMedia media = item.getMedia();
        if (media == null || shouldNotDownload(media)) {
            return;
        }

        UsageStatistics.logAction(UsageStatistics.ACTION_DOWNLOAD);

        if (NetworkUtils.isEpisodeDownloadAllowed() || MobileDownloadHelper.userAllowedMobileDownloads()) {
            DownloadService.download(context, false, DownloadRequestCreator.create(item.getMedia()).build());
        } else if (MobileDownloadHelper.userChoseAddToQueue() && !item.isTagged(FeedItem.TAG_QUEUE)) {
            DBWriter.addQueueItem(context, item);
            Toast.makeText(context, R.string.added_to_queue_label, Toast.LENGTH_SHORT).show();
        } else {
            MobileDownloadHelper.confirmMobileDownload(context, item);
        }
    }

    private boolean shouldNotDownload(@NonNull FeedMedia media) {
        boolean isDownloading = DownloadService.isDownloadingFile(media.getDownload_url());
        return isDownloading || media.isDownloaded();
    }
}
