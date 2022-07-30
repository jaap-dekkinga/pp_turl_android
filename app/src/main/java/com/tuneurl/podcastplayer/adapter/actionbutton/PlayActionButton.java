package com.tuneurl.podcastplayer.adapter.actionbutton;

import android.content.Context;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.tuneurl.podcastplayer.core.service.playback.PlaybackService;
import com.tuneurl.podcastplayer.core.storage.DBTasks;
import com.tuneurl.podcastplayer.core.util.playback.PlaybackServiceStarter;
import com.tuneurl.podcastplayer.model.feed.FeedItem;
import com.tuneurl.podcastplayer.model.feed.FeedMedia;
import com.tuneurl.podcastplayer.model.playback.MediaType;

import com.tuneurl.podcastplayer.R;

public class PlayActionButton extends ItemActionButton {

    public PlayActionButton(FeedItem item) {
        super(item);
    }

    @Override
    @StringRes
    public int getLabel() {
        return R.string.play_label;
    }

    @Override
    @DrawableRes
    public int getDrawable() {
        return R.drawable.ic_play_24dp;
    }

    @Override
    public void onClick(Context context) {
        FeedMedia media = item.getMedia();
        if (media == null) {
            return;
        }
        if (!media.fileExists()) {
            DBTasks.notifyMissingFeedMediaFile(context, media);
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
