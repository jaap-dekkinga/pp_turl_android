package com.tuneurl.podcastplayer.core.feed.util;

import android.util.Log;
import com.tuneurl.podcastplayer.model.feed.Feed;
import com.tuneurl.podcastplayer.model.feed.FeedItem;
import com.tuneurl.podcastplayer.model.feed.FeedMedia;
import com.tuneurl.podcastplayer.model.playback.MediaType;
import com.tuneurl.podcastplayer.core.preferences.PlaybackPreferences;
import com.tuneurl.podcastplayer.core.preferences.UserPreferences;
import com.tuneurl.podcastplayer.model.playback.Playable;

import com.tuneurl.podcastplayer.model.feed.FeedPreferences;

/**
 * Utility class to use the appropriate playback speed based on {@link PlaybackPreferences}
 */
public final class PlaybackSpeedUtils {
    private static final String TAG = "PlaybackSpeedUtils";

    private PlaybackSpeedUtils() {
    }

    /**
     * Returns the currently configured playback speed for the specified media.
     */
    public static float getCurrentPlaybackSpeed(Playable media) {
        float playbackSpeed = FeedPreferences.SPEED_USE_GLOBAL;
        MediaType mediaType = null;

        if (media != null) {
            mediaType = media.getMediaType();
            playbackSpeed = PlaybackPreferences.getCurrentlyPlayingTemporaryPlaybackSpeed();

            if (playbackSpeed == FeedPreferences.SPEED_USE_GLOBAL && media instanceof FeedMedia) {
                FeedItem item = ((FeedMedia) media).getItem();
                if (item != null) {
                    Feed feed = item.getFeed();
                    if (feed != null && feed.getPreferences() != null) {
                        playbackSpeed = feed.getPreferences().getFeedPlaybackSpeed();
                    } else {
                        Log.d(TAG, "Can not get feed specific playback speed: " + feed);
                    }
                }
            }
        }

        if (playbackSpeed == FeedPreferences.SPEED_USE_GLOBAL) {
            playbackSpeed = UserPreferences.getPlaybackSpeed(mediaType);
        }

        return playbackSpeed;
    }
}
