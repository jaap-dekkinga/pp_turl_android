package com.dekidea.hearact.core.feed.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dekidea.hearact.model.feed.FeedItem;
import com.dekidea.hearact.model.feed.FeedMedia;
import com.dekidea.hearact.model.playback.Playable;
import com.dekidea.hearact.core.preferences.UserPreferences;

/**
 * Utility class to use the appropriate image resource based on {@link UserPreferences}.
 */
public final class ImageResourceUtils {

    private ImageResourceUtils() {
    }

    /**
     * returns the image location, does prefer the episode cover if available and enabled in settings.
     */
    @Nullable
    public static String getEpisodeListImageLocation(@NonNull Playable playable) {
        if (UserPreferences.getUseEpisodeCoverSetting()) {
            return playable.getImageLocation();
        } else {
            return getFallbackImageLocation(playable);
        }
    }

    /**
     * returns the image location, does prefer the episode cover if available and enabled in settings.
     */
    @Nullable
    public static String getEpisodeListImageLocation(@NonNull FeedItem feedItem) {
        if (UserPreferences.getUseEpisodeCoverSetting()) {
            return feedItem.getImageLocation();
        } else {
            return getFallbackImageLocation(feedItem);
        }
    }

    @Nullable
    public static String getFallbackImageLocation(@NonNull Playable playable) {
        if (playable instanceof FeedMedia) {
            FeedMedia media = (FeedMedia) playable;
            FeedItem item = media.getItem();
            if (item != null && item.getFeed() != null) {
                return item.getFeed().getImageUrl();
            } else {
                return null;
            }
        } else {
            return playable.getImageLocation();
        }
    }

    @Nullable
    public static String getFallbackImageLocation(@NonNull FeedItem feedItem) {
        if (feedItem.getFeed() != null) {
            return feedItem.getFeed().getImageUrl();
        } else {
            return null;
        }
    }
}
