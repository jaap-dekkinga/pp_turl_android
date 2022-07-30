package com.tuneurl.podcastplayer.activity;

import android.os.Bundle;
import android.text.TextUtils;
import androidx.appcompat.app.AppCompatActivity;

import com.tuneurl.podcastplayer.core.preferences.UserPreferences;
import com.tuneurl.podcastplayer.core.service.download.DownloadRequest;
import com.tuneurl.podcastplayer.core.service.download.DownloadService;
import com.tuneurl.podcastplayer.core.storage.DBReader;
import com.tuneurl.podcastplayer.core.storage.DBWriter;
import com.tuneurl.podcastplayer.model.feed.FeedMedia;
import com.tuneurl.podcastplayer.model.feed.FeedPreferences;

import com.tuneurl.podcastplayer.R;

import com.tuneurl.podcastplayer.dialog.AuthenticationDialog;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import org.apache.commons.lang3.Validate;


/**
 * Shows a username and a password text field.
 * The activity MUST be started with the ARG_DOWNlOAD_REQUEST argument set to a non-null value.
 */
public class DownloadAuthenticationActivity extends AppCompatActivity {

    /**
     * The download request object that contains information about the resource that requires a username and a password.
     */
    public static final String ARG_DOWNLOAD_REQUEST = "request";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(UserPreferences.getTranslucentTheme());
        super.onCreate(savedInstanceState);

        Validate.isTrue(getIntent().hasExtra(ARG_DOWNLOAD_REQUEST), "Download request missing");
        DownloadRequest request = getIntent().getParcelableExtra(ARG_DOWNLOAD_REQUEST);

        new AuthenticationDialog(this, R.string.authentication_label, true, "", "") {
            @Override
            protected void onConfirmed(String username, String password) {
                Completable.fromAction(
                        () -> {
                            request.setUsername(username);
                            request.setPassword(password);

                            if (request.getFeedfileType() == FeedMedia.FEEDFILETYPE_FEEDMEDIA) {
                                long mediaId = request.getFeedfileId();
                                FeedMedia media = DBReader.getFeedMedia(mediaId);
                                if (media != null) {
                                    FeedPreferences preferences = media.getItem().getFeed().getPreferences();
                                    if (TextUtils.isEmpty(preferences.getPassword())
                                            || TextUtils.isEmpty(preferences.getUsername())) {
                                        preferences.setUsername(username);
                                        preferences.setPassword(password);
                                        DBWriter.setFeedPreferences(preferences);
                                    }
                                }
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(() -> {
                            DownloadService.download(DownloadAuthenticationActivity.this, false, request);
                            finish();
                        });
            }

            @Override
            protected void onCancelled() {
                finish();
            }
        }.show();
    }
}
