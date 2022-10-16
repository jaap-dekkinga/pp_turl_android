package com.dekidea.hearact.adapter.actionbutton;

import android.content.Context;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import com.dekidea.hearact.R;
import com.dekidea.hearact.model.feed.FeedItem;
import com.dekidea.hearact.model.feed.FeedMedia;
import com.dekidea.hearact.core.util.FeedItemUtil;
import com.dekidea.hearact.core.util.IntentUtils;

import com.dekidea.hearact.core.service.playback.PlaybackService;

public class PauseActionButton extends ItemActionButton {

    public PauseActionButton(FeedItem item) {
        super(item);
    }

    @Override
    @StringRes
    public int getLabel() {
        return R.string.pause_label;
    }

    @Override
    @DrawableRes
    public int getDrawable() {
        return R.drawable.ic_pause;
    }

    @Override
    public void onClick(Context context) {
        FeedMedia media = item.getMedia();
        if (media == null) {
            return;
        }

        if (FeedItemUtil.isCurrentlyPlaying(media)) {
            IntentUtils.sendLocalBroadcast(context, PlaybackService.ACTION_PAUSE_PLAY_CURRENT_EPISODE);
        }
    }
}
