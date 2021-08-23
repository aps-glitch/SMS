package com.example.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import java.text.MessageFormat;

public class NotificationReceiver extends BroadcastReceiver {
        //Initializing variable for textview
        TextView phoneScreen;
        TextView messageScreen;

        //TextView passed in from the MainActivity
        public NotificationReceiver(EditText etMessage, EditText etPhone) {
            super();
            phoneScreen = etPhone;
            messageScreen = etMessage;
        }

        // OnReceive Methode
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs;
            StringBuilder text = new StringBuilder();
            StringBuilder text1 = new StringBuilder();

            if (bundle != null) { //to get the info of sms
                String format = bundle.getString("format");
                Object[] pdus = (Object[]) bundle.get("pdus");

                if (pdus != null) {
                    msgs = new SmsMessage[pdus.length];

                    for (int i = 0; i < msgs.length; i++) {
                        // Check the Android version.
                        boolean isVersionM =
                                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M);
                        // Check Android version and use appropriate createFromPdu.
                        if (isVersionM) {
                            // If Android version M or newer:
                            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                        } else {
                            // If Android version L or older:
                            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        }
                        //getting info in suitable variable
                        text1 = text1.append(msgs[i].getOriginatingAddress());
                        text = text.append(msgs[i].getMessageBody());
                    }
                }
            }
            //Assigning the variable in suitable View in the layout
            if (messageScreen != null && phoneScreen != null) {
                messageScreen.setText(MessageFormat.format("{0}\n{1}", messageScreen.getText().toString(), text));
                phoneScreen.setText(MessageFormat.format("{0}\n{1}", phoneScreen.getText().toString(), text1));
            }
        }
    }
