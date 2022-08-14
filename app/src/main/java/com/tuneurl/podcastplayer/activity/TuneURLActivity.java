package com.tuneurl.podcastplayer.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.dekidea.tuneurl.api.APIData;
import com.dekidea.tuneurl.util.Constants;
import com.dekidea.tuneurl.util.TuneURLManager;
import com.tuneurl.podcastplayer.R;

public class TuneURLActivity extends AppCompatActivity implements Constants {

    private APIData apiData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setShowWhenLocked(true);
        setTurnScreenOn(true);
        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        keyguardManager.requestDismissKeyguard(this, null);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_tune_urlactivity);

        try{

            apiData = getIntent().getParcelableExtra(APIDATA);
        }
        catch (Exception e){

            e.printStackTrace();
        }

        showTuneURLOptions(apiData);
    }


    @Override
    public void onResume() {

        super.onResume();
    }


    public void showTuneURLOptions(final APIData apiData) {

        try {

            String type = "Type: " + apiData.getType();
            String description = apiData.getDescription();
            String name = apiData.getName();
            String info = apiData.getInfo();

            TextView title = findViewById(R.id.title);
            TextView details = findViewById(R.id.details);
            TextView button_open = findViewById(R.id.button_open);
            TextView button_ignore = findViewById(R.id.button_ignore);

            button_open.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    doAction("yes", apiData);
                }
            });

            button_ignore.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    doAction("no", apiData);
                }
            });
        }
        catch (Exception e){

            e.printStackTrace();
        }
    }


    public void doAction(String user_response, APIData apiData){

        if(apiData != null) {

            String action = apiData.getType();
            String date = apiData.getDate();

            if (ACTION_POLL.equals(action)) {

                TuneURLManager.postPollAnswer(this, user_response, apiData.getDescription(), apiData.getDate());

                //TuneURLManager.startScanning(this);
            }
            else {

                if (USER_RESPONSE_YES.equals(user_response)) {

                    if (ACTION_SAVE_PAGE.equals(action)) {

                        saveInfo(apiData);

                        //TuneURLManager.startScanning(this);
                    }
                    else if (ACTION_OPEN_PAGE.equals(action)) {

                        TuneURLManager.addRecordOfInterest(this, String.valueOf(apiData.getId()), INTEREST_ACTION_ACTED, date);

                        openPage( apiData);

                        //TuneURLManager.startScanning(this);
                    }
                    else if (ACTION_PHONE.equals(action)) {

                        TuneURLManager.addRecordOfInterest(this, String.valueOf(apiData.getId()), INTEREST_ACTION_ACTED, date);

                        callPhone(apiData);
                    }
                    else {

                        //TuneURLManager.startScanning(this);
                    }
                }
                else {

                    //TuneURLManager.startScanning(this);
                }
            }
        }

        this.finish();
    }


    private void openPage(APIData data){

        try{

            String url = data.getInfo();

            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(i);
        }
        catch(Exception e){

            e.printStackTrace();
        }
    }


    private void saveInfo(APIData data){

        openPage(data);
    }


    private void callPhone(APIData data){

        try {

            String phone_number = data.getInfo();

            if (phone_number != null) {

                try {

                    Intent i = new Intent(Intent.ACTION_CALL);

                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    String uri = "tel:" + phone_number.trim();

                    i.setData(Uri.parse(uri));

                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {

                        //TuneURLManager.stopScanning(this);

                        startActivity(i);
                    }
                    else{

                        //TuneURLManager.startScanning(this);
                    }
                }
                catch (Exception e) {

                    e.printStackTrace();
                }
            }
        }
        catch (Exception e){

            e.printStackTrace();
        }
    }
}