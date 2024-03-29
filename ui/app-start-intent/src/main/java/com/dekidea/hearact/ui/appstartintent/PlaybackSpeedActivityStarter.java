package com.dekidea.hearact.ui.appstartintent;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

/**
 * Launches the playback speed dialog activity of the app with specific arguments.
 * Does not require a dependency on the actual implementation of the activity.
 */
public class PlaybackSpeedActivityStarter {
    public static final String INTENT = "com.dekidea.hearact.intents.PLAYBACK_SPEED";
    private final Intent intent;
    private final Context context;

    public PlaybackSpeedActivityStarter(Context context) {
        this.context = context;
        intent = new Intent(INTENT);
        intent.setPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
    }

    public Intent getIntent() {
        return intent;
    }

    public PendingIntent getPendingIntent() {
        return PendingIntent.getActivity(context, R.id.pending_intent_playback_speed, getIntent(),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public void start() {
        context.startActivity(getIntent());
    }
}
