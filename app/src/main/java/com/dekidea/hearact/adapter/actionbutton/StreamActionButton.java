package com.dekidea.hearact.adapter.actionbutton;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.dekidea.hearact.core.preferences.UsageStatistics;
import com.dekidea.hearact.core.service.playback.PlaybackService;
import com.dekidea.hearact.core.util.NetworkUtils;
import com.dekidea.hearact.core.util.playback.PlaybackServiceStarter;
import com.dekidea.hearact.model.feed.FeedItem;
import com.dekidea.hearact.model.feed.FeedMedia;
import com.dekidea.hearact.model.playback.MediaType;

import com.dekidea.hearact.R;

import com.dekidea.hearact.dialog.StreamingConfirmationDialog;

public class StreamActionButton extends ItemActionButton {

    public StreamActionButton(FeedItem item) {
        super(item);
    }

    @Override
    @StringRes
    public int getLabel() {
        return R.string.stream_label;
    }

    @Override
    @DrawableRes
    public int getDrawable() {
        return R.drawable.ic_stream;
    }

    @Override
    public void onClick(Context context) {
        final FeedMedia media = item.getMedia();
        if (media == null) {
            return;
        }
        UsageStatistics.logAction(UsageStatistics.ACTION_STREAM);

        if (!NetworkUtils.isStreamingAllowed()) {
            new StreamingConfirmationDialog(context, media).show();
            return;
        }
        new PlaybackServiceStarter(context, media)
                .callEvenIfRunning(true)
                .start();

        if (media.getMediaType() == MediaType.VIDEO) {
            context.startActivity(PlaybackService.getPlayerActivityIntent(context, media));
        }
    }
}
