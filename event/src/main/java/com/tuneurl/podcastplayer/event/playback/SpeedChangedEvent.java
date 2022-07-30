package com.tuneurl.podcastplayer.event.playback;

public class SpeedChangedEvent {
    private final float newSpeed;

    public SpeedChangedEvent(float newSpeed) {
        this.newSpeed = newSpeed;
    }

    public float getNewSpeed() {
        return newSpeed;
    }
}
