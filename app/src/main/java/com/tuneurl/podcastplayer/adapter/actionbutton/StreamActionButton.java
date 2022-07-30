package com.tuneurl.podcastplayer.adapter.actionbutton;

import android.content.Context;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.tuneurl.podcastplayer.core.preferences.UsageStatistics;
import com.tuneurl.podcastplayer.core.service.playback.PlaybackService;
import com.tuneurl.podcastplayer.core.util.NetworkUtils;
import com.tuneurl.podcastplayer.core.util.playback.PlaybackServiceStarter;
import com.tuneurl.podcastplayer.model.feed.FeedItem;
import com.tuneurl.podcastplayer.model.feed.FeedMedia;
import com.tuneurl.podcastplayer.model.playback.MediaType;

import com.tuneurl.podcastplayer.R;

import com.tuneurl.podcastplayer.dialog.StreamingConfirmationDialog;

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
