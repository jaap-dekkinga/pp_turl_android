package com.dekidea.tuneurl.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;

import com.dekidea.tuneurl.util.Constants;
import com.dekidea.tuneurl.util.TuneURLManager;

public class PhoneStateReceiver extends BroadcastReceiver implements Constants{

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();

        if (extras != null) {

            String state = extras.getString(TelephonyManager.EXTRA_STATE);

            if(state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)){

                TuneURLManager.stopScanning(context);
            }
            else if(state.equals(TelephonyManager.EXTRA_STATE_RINGING)){

            }
            else if(state.equals(TelephonyManager.EXTRA_STATE_IDLE)){

                int running_state = TuneURLManager.fetchIntSetting(context, Constants.SETTING_RUNNING_STATE, Constants.SETTING_RUNNING_STATE_STOPPED);

                if(running_state == SETTING_RUNNING_STATE_STARTED){

                    //TuneURLManager.startScanning(context);
                }
            }
        }
    }
}