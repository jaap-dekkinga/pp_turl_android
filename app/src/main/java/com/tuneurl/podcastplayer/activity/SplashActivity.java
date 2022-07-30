package com.tuneurl.podcastplayer.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ProgressBar;

import com.tuneurl.podcastplayer.storage.database.PodDBAdapter;

import com.tuneurl.podcastplayer.R;
import com.tuneurl.podcastplayer.error.CrashReportWriter;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Shows the app logo while waiting for the main activity to start.
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash);

        Completable.create(subscriber -> {
            // Trigger schema updates
            PodDBAdapter.getInstance().open();
            PodDBAdapter.getInstance().close();
            subscriber.onComplete();
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(
            () -> {
                Intent intent = new Intent(SplashActivity.this, MainActivity2.class);
                startActivity(intent);
                overridePendingTransition(0, 0);
                finish();
            }, error -> {
                error.printStackTrace();
                CrashReportWriter.write(error);
                Toast.makeText(this, error.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                finish();
            });
    }
}
