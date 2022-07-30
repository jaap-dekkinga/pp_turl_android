package com.tuneurl.podcastplayer.event.settings;

import com.tuneurl.podcastplayer.model.feed.VolumeAdaptionSetting;

public class VolumeAdaptionChangedEvent {
    private final VolumeAdaptionSetting volumeAdaptionSetting;
    private final long feedId;

    public VolumeAdaptionChangedEvent(VolumeAdaptionSetting volumeAdaptionSetting, long feedId) {
        this.volumeAdaptionSetting = volumeAdaptionSetting;
        this.feedId = feedId;
    }

    public VolumeAdaptionSetting getVolumeAdaptionSetting() {
        return volumeAdaptionSetting;
    }

    public long getFeedId() {
        return feedId;
    }
}
