package com.tuneurl.podcastplayer.dialog;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;

import com.tuneurl.podcastplayer.core.preferences.UserPreferences;
import com.tuneurl.podcastplayer.core.util.playback.PlaybackServiceStarter;
import com.tuneurl.podcastplayer.model.playback.Playable;

import com.tuneurl.podcastplayer.R;

public class StreamingConfirmationDialog {
    private final Context context;
    private final Playable playable;

    public StreamingConfirmationDialog(Context context, Playable playable) {
        this.context = context;
        this.playable = playable;
    }

    public void show() {
        new AlertDialog.Builder(context)
                .setTitle(R.string.stream_label)
                .setMessage(R.string.confirm_mobile_streaming_notification_message)
                .setPositiveButton(R.string.confirm_mobile_streaming_button_once, (dialog, which) -> stream())
                .setNegativeButton(R.string.confirm_mobile_streaming_button_always, (dialog, which) -> {
                    UserPreferences.setAllowMobileStreaming(true);
                    stream();
                })
                .setNeutralButton(R.string.cancel_label, null)
                .show();
    }

    private void stream() {
        new PlaybackServiceStarter(context, playable)
                .callEvenIfRunning(true)
                .shouldStreamThisTime(true)
                .start();
    }
}
