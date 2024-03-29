package com.dekidea.hearact.core.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;

import com.dekidea.hearact.core.service.playback.PlaybackService;
import com.dekidea.hearact.core.ClientConfig;

/** Receives media button events. */
public class MediaButtonReceiver extends BroadcastReceiver {
	private static final String TAG = "MediaButtonReceiver";
	public static final String EXTRA_KEYCODE = "com.dekidea.hearact.core.service.extra.MediaButtonReceiver.KEYCODE";
	public static final String EXTRA_SOURCE = "com.dekidea.hearact.core.service.extra.MediaButtonReceiver.SOURCE";
	public static final String EXTRA_HARDWAREBUTTON = "com.dekidea.hearact.core.service.extra.MediaButtonReceiver.HARDWAREBUTTON";

	public static final String NOTIFY_BUTTON_RECEIVER = "com.dekidea.hearact.NOTIFY_BUTTON_RECEIVER";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "Received intent");
		if (intent == null || intent.getExtras() == null) {
			return;
		}
		KeyEvent event = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
		if (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount()==0) {
			ClientConfig.initialize(context);
			Intent serviceIntent = new Intent(context, PlaybackService.class);
			serviceIntent.putExtra(EXTRA_KEYCODE, event.getKeyCode());
			serviceIntent.putExtra(EXTRA_SOURCE, event.getSource());
			//detect if this is a hardware button press
			if (event.getEventTime() > 0 || event.getDownTime() > 0) {
				serviceIntent.putExtra(EXTRA_HARDWAREBUTTON, true);
			} else {
				serviceIntent.putExtra(EXTRA_HARDWAREBUTTON, false);
			}
			ContextCompat.startForegroundService(context, serviceIntent);
		}

	}

}
