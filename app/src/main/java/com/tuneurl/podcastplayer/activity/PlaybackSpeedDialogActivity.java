package com.tuneurl.podcastplayer.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;

import com.tuneurl.podcastplayer.core.preferences.UserPreferences;
import com.tuneurl.podcastplayer.dialog.VariableSpeedDialog;

public class PlaybackSpeedDialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(UserPreferences.getTranslucentTheme());
        super.onCreate(savedInstanceState);
        VariableSpeedDialog speedDialog = new InnerVariableSpeedDialog();
        speedDialog.show(getSupportFragmentManager(), null);
    }

    public static class InnerVariableSpeedDialog extends VariableSpeedDialog {
        @Override
        public void onDismiss(@NonNull DialogInterface dialog) {
            super.onDismiss(dialog);
            getActivity().finish();
        }
    }
}
