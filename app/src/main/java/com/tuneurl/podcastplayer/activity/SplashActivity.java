package com.tuneurl.podcastplayer.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dekidea.tuneurl.util.TuneURLManager;
import com.tuneurl.podcastplayer.storage.database.PodDBAdapter;

import com.tuneurl.podcastplayer.R;
import com.tuneurl.podcastplayer.error.CrashReportWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

        initializeResources();

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
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
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

    private static final String SETTING_TRIGGER_FILE_PATH = "com.dekidea.tuneurl.SETTING_TRIGGER_FILE_PATH";
    private static final String DEFAULT_TUNEURL_API_BASE_URL = "http://ec2-54-213-252-225.us-west-2.compute.amazonaws.com";
    private static final String DEFAULT_SEARCH_FINGERPRINT_URL = "https://pnz3vadc52.execute-api.us-east-2.amazonaws.com/dev/search-fingerprint";
    private static final String DEFAULT_POLL_API_URL = "http://pollapiwebservice.us-east-2.elasticbeanstalk.com/api/pollapi";
    private static final String DEFAULT_INTERESTS_API_URL = "https://65neejq3c9.execute-api.us-east-2.amazonaws.com/interests";
    private static final String SETTING_TUNEURL_API_BASE_URL = "com.dekidea.tuneurl.SETTING_TUNEURL_API_BASE_URL";
    private static final String SETTING_SEARCH_FINGERPRINT_URL = "com.dekidea.tuneurl.SETTING_SEARCH_FINGERPRINT_URL";
    private static final String SETTING_POLL_API_URL = "com.dekidea.tuneurl.SETTING_POLL_API_URL";
    private static final String SETTING_INTERESTS_API_URL = "com.dekidea.tuneurl.SETTING_INTERESTS_API_URL";

    private void initializeResources(){

        String reference_file_path = TuneURLManager.fetchStringSetting(this, SETTING_TRIGGER_FILE_PATH, null);

        if(reference_file_path == null || reference_file_path.isEmpty()) {

            TuneURLManager.updateStringSetting(this, SETTING_TUNEURL_API_BASE_URL, DEFAULT_TUNEURL_API_BASE_URL);
            TuneURLManager.updateStringSetting(this, SETTING_SEARCH_FINGERPRINT_URL, DEFAULT_SEARCH_FINGERPRINT_URL);
            TuneURLManager.updateStringSetting(this, SETTING_POLL_API_URL, DEFAULT_POLL_API_URL);
            TuneURLManager.updateStringSetting(this, SETTING_INTERESTS_API_URL, DEFAULT_INTERESTS_API_URL);

            reference_file_path = installReferenceWavFile(this, R.raw.trigger_audio, "trigger_audio.raw");

            TuneURLManager.updateStringSetting(this, SETTING_TRIGGER_FILE_PATH, reference_file_path);
        }
    }


    private String installReferenceWavFile(Context context, int raw_resource, String file_name){

        String output_file_path = null;

        InputStream input_stream = null;

        try {

            input_stream = context.getApplicationContext().getResources().openRawResource(raw_resource);

            String file_path = getExternalFilesDir(null).getPath() + "/" + file_name;

            boolean success =  writeFile(input_stream, file_path);

            if(success){

                output_file_path = file_path;
            }
        }
        catch (Exception e) {

            e.printStackTrace();
        }
        finally {

            try {

                input_stream.close();
            }
            catch (IOException e) {

                e.printStackTrace();
            }
        }

        return output_file_path;
    }


    private boolean writeFile(InputStream input_stream, String output_file_path){

        boolean success = false;

        OutputStream output_stream = null;

        try {

            File out_file = new File(output_file_path);
            if (!out_file.exists()) {

                output_stream = new FileOutputStream(output_file_path);

                byte[] buffer = new byte[1024];
                int length;

                while ((length = input_stream.read(buffer)) != -1) {

                    output_stream.write(buffer, 0, length);
                }

                output_stream.flush();

                success = true;
            }
        }
        catch(Exception e){

            e.printStackTrace();
        }
        finally {

            try {

                if(output_stream != null){

                    output_stream.close();
                }
            }
            catch (IOException e) {

                e.printStackTrace();
            }
        }

        return success;
    }
}
