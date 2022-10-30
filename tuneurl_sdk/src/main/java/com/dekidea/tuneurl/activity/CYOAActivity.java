package com.dekidea.tuneurl.activity;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.dekidea.tuneurl.R;
import com.dekidea.tuneurl.api.CYOA;
import com.dekidea.tuneurl.util.Constants;
import com.dekidea.tuneurl.util.TimeUtils;
import com.dekidea.tuneurl.util.TuneURLManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class CYOAActivity extends AppCompatActivity implements Constants {

    private static final long DEFAULT_CLOSE_INTERVAL = 3000L;

    private LinearLayout optionsLayout;

    private String tuneurl_id;
    private String default_mp3_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        }

        KeyguardManager keyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
        keyguardManager.requestDismissKeyguard(this, null);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_cyoa_activity);

        optionsLayout = (LinearLayout) findViewById(R.id.optionsLayout);

        try{

            tuneurl_id = getIntent().getStringExtra(TUNEURL_ID);
            default_mp3_url = getIntent().getStringExtra(DEFAULT_MP3_URL);
            String result  = getIntent().getStringExtra(TUNEURL_RESULT);

            processCYOAResult(result);
        }
        catch (Exception e){

            e.printStackTrace();
        }
    }


    @Override
    public void onResume() {

        super.onResume();

        scheduleDefaultClose();
    }


    @Override
    public void onPause() {

        super.onPause();
    }


    private void scheduleDefaultClose(){

        TimerTask timerTask = new TimerTask() {

            public void run() {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        playFile(default_mp3_url);
                    }
                });
            }
        };

        Timer timer = new Timer();
        timer.schedule(timerTask, DEFAULT_CLOSE_INTERVAL);
    }


    public void doAction(String user_response){

        TuneURLManager.addRecordOfInterest(this, tuneurl_id, INTEREST_ACTION_ACTED, TimeUtils.getCurrentTimeAsFormattedString());

        this.finish();
    }


    private void processCYOAResult(String result){

        try {

            ArrayList<CYOA> CYOAArray = new ArrayList<CYOA>();

            JsonObject object = new JsonParser().parse(result).getAsJsonObject();
            JsonArray jsonArray = object.getAsJsonArray("result");

            if (jsonArray != null && jsonArray.size() > 0) {

                for (int i = 0; i < jsonArray.size(); i++) {

                    try {

                        JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();

                        long tuneurl_id = jsonObject.get("tuneurl_id").getAsLong();
                        String option = jsonObject.get("options").getAsString();
                        String mp3_url = jsonObject.get("mp3_url").getAsString();

                        CYOA cyoa = new CYOA(tuneurl_id, option, mp3_url);

                        CYOAArray.add(cyoa);
                    }
                    catch (Exception e) {

                        e.printStackTrace();
                    }
                }

                showOptions(CYOAArray);
            }
            else {

                //Do something
            }
        }
        catch (Exception e){

            e.printStackTrace();

            //Do something
        }
    }


    private void showOptions(ArrayList<CYOA> CYOAArray){

        if(CYOAArray != null && !CYOAArray.isEmpty()){

            try{

                for(CYOA option: CYOAArray){

                    Button button = getOptionButton(option);

                    if(button != null) {

                        optionsLayout.addView(button);
                    }
                }
            }
            catch (Exception e){

                e.printStackTrace();
            }
        }
    }


    private Button getOptionButton(final CYOA option){

        try {

            Button button = new Button(this);

            button.setPadding(10, 10, 10, 10);
            button.setTextSize(24);
            button.setBackgroundResource(R.drawable.round_button_open);
            button.setText(option.getOption());
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    playFile(option.getMp3Url());
                }
            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(40, 40, 40, 40);
            button.setLayoutParams(params);

            return button;
        }
        catch (Exception e){

            e.printStackTrace();
        }

        return null;
    }


    private void playFile(final String mp3_url){


    }
}
