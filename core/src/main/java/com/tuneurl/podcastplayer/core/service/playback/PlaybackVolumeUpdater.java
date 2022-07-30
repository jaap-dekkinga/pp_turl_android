package com.tuneurl.podcastplayer.core.service.playback;

import com.tuneurl.podcastplayer.model.feed.FeedMedia;
import com.tuneurl.podcastplayer.model.feed.FeedPreferences;
import com.tuneurl.podcastplayer.model.feed.VolumeAdaptionSetting;
import com.tuneurl.podcastplayer.model.playback.Playable;
import com.tuneurl.podcastplayer.playback.base.PlaybackServiceMediaPlayer;
import com.tuneurl.podcastplayer.playback.base.PlayerStatus;

class PlaybackVolumeUpdater {

    public void updateVolumeIfNecessary(PlaybackServiceMediaPlayer mediaPlayer, long feedId,
                                        VolumeAdaptionSetting volumeAdaptionSetting) {
        Playable playable = mediaPlayer.getPlayable();

        if (playable instanceof FeedMedia) {
            updateFeedMediaVolumeIfNecessary(mediaPlayer, feedId, volumeAdaptionSetting, (FeedMedia) playable);
        }
    }

    private void updateFeedMediaVolumeIfNecessary(PlaybackServiceMediaPlayer mediaPlayer, long feedId,
                                                  VolumeAdaptionSetting volumeAdaptionSetting, FeedMedia feedMedia) {
        if (feedMedia.getItem().getFeed().getId() == feedId) {
            FeedPreferences preferences = feedMedia.getItem().getFeed().getPreferences();
            preferences.setVolumeAdaptionSetting(volumeAdaptionSetting);

            if (mediaPlayer.getPlayerStatus() == PlayerStatus.PLAYING) {
                forceUpdateVolume(mediaPlayer);
            }
        }
    }

    private void forceUpdateVolume(PlaybackServiceMediaPlayer mediaPlayer) {
        mediaPlayer.pause(false, false);
        mediaPlayer.resume();
    }

}
