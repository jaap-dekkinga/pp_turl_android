package com.dekidea.hearact.event.playback;

public class SpeedChangedEvent {
    private final float newSpeed;

    public SpeedChangedEvent(float newSpeed) {
        this.newSpeed = newSpeed;
    }

    public float getNewSpeed() {
        return newSpeed;
    }
}
