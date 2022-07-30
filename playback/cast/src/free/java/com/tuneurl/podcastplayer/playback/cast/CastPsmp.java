package com.tuneurl.podcastplayer.playback.cast;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.tuneurl.podcastplayer.playback.base.PlaybackServiceMediaPlayer;

/**
 * Stub implementation of CastPsmp for Free build flavour
 */
public class CastPsmp {
    @Nullable
    public static PlaybackServiceMediaPlayer getInstanceIfConnected(@NonNull Context context,
                                        @NonNull PlaybackServiceMediaPlayer.PSMPCallback callback) {
        return null;
    }
}
