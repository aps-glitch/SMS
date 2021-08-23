package com.example.sms;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public  class MainActivity extends AppCompatActivity {
    //initialize variable
    private static final String TAG = "IT472";
    final int MY_PERMISSIONS_REQUEST_SEND_SMS = 13;
    final int MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 14;


    ImageButton btSend;
    //broadcast receiver to check sent sms
    BroadcastReceiver confirmSentBR = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "in confirmSentRB");
            //all other codes are error
            if (getResultCode() == Activity.RESULT_OK) {
                Toast.makeText(getBaseContext(),
                        "SMS sent successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getBaseContext(),
                        "Error: SMS was not sent", Toast.LENGTH_SHORT).show();
            }
        }

    };
    //broadcast receiver to check delivered sms
    BroadcastReceiver confirmDeliveryBR = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "in confirmDeliverRB");
            //all other codes are error
            if (getResultCode() == Activity.RESULT_OK) {
                Toast.makeText(getBaseContext(),
                        "SMS delivered successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getBaseContext(),
                        "Error: SMS was not delivered",
                        Toast.LENGTH_SHORT).show();
            }

        }

    };


    private NotificationReceiver smsReceiver;
    private EditText etPhone, etMessage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //assign variable
        etPhone = findViewById(R.id.et_phone);
        etMessage = findViewById(R.id.et_message);
        btSend = findViewById(R.id.bt_send);

        //register broadcast receivers
        IntentFilter sentIntentFilter = new IntentFilter("android.provider.Telephony.SMS_SENT");
        registerReceiver(confirmSentBR, sentIntentFilter);

        IntentFilter deliveredIntentFilter = new IntentFilter("android.provider.Telephony.SMS_DELIVER");
        registerReceiver(confirmDeliveryBR, deliveredIntentFilter);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.SEND_SMS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Call requestPermissions
                // here to request the missing permissions, and then override
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission.

                requestPermissions(new String[]{Manifest.permission.SEND_SMS},
                        MY_PERMISSIONS_REQUEST_SEND_SMS);

            } else {
                registerReceiveMessageReceiver();
            }
        }


        btSend.setOnClickListener(v -> {
            //check condition
            if (ContextCompat.checkSelfPermission(MainActivity.this
                    , Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {
                //When permission is granted
                //Create methode
                sendAllowedMessage();
                etPhone.setText(null);
                etMessage.setText(null);
            } else {
                //when permission not granted
                //request permission
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.SEND_SMS, Manifest.permission.RECEIVE_SMS}, 100);
            }
        });
    }




    private void registerReceiveMessageReceiver() {
        Log.d(TAG, "MainActivity: registerReceiveMessageReceiver");

        IntentFilter incomingSmsIntentFilter =
                new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        //display refers to the TextView on the UI
        smsReceiver = new NotificationReceiver(etMessage, etPhone);
        registerReceiver(smsReceiver, incomingSmsIntentFilter);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "onRequestPermissionResult for request code " + requestCode);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_SEND_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // task you need to do.
                    Log.d(TAG, "onRequestPermissionResult SEND_SMS granted");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (checkSelfPermission(Manifest.permission.RECEIVE_SMS)
                                != PackageManager.PERMISSION_GRANTED) {
                            // Call ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then override
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission.

                            Log.d(TAG, "onRequestPermissionResult request RECEIVE_SMS");
                            requestPermissions(new String[]{Manifest.permission.RECEIVE_SMS},
                                    MY_PERMISSIONS_REQUEST_RECEIVE_SMS);

                        } else {
                            registerReceiveMessageReceiver();
                        }
                    }
                    //sendAllowedMessage();
                } else {
                    // permission denied. Disable the
                    // functionality that depends on this permission.
                    //do nothing in this case
                    Toast.makeText(getBaseContext(), "Cannot send message due to denied permission", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            case MY_PERMISSIONS_REQUEST_RECEIVE_SMS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // task you need to do.
                    Log.d(TAG, "onRequestPermissionResult RECEIVE_SMS granted");
                    registerReceiveMessageReceiver();
                } else {
                    Log.d(TAG, "onRequestPermissionResult RECEIVE_SMS denied");

                }
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    private void sendAllowedMessage() {
        String sPhone = etPhone.getText().toString().trim();
        String sMessage = etMessage.getText().toString().trim();

        PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent("SMS SENT"), 0);
        PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent("SMS DELIVERED"), 0);

        if (!sPhone.equals("") && !sMessage.equals("")) {
            //when fields are not equal to blank
            SmsManager smsManager = SmsManager.getDefault();
            //send text message
            smsManager.sendTextMessage(sPhone, null, sMessage, sentPI, deliveredPI);
            //display toast
            Toast.makeText(getApplicationContext(), "SMS sent successfully!", Toast.LENGTH_LONG).show();
        } else {
            //when field is blank
            //display toast
            Toast.makeText(getApplicationContext(), "enter value", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //unregister receivers
        unregisterReceiver(confirmSentBR);
        unregisterReceiver(confirmDeliveryBR);

        if (smsReceiver != null) {
            unregisterReceiver(smsReceiver);
        }
    }

    public void sendMessage(View view) {
        sendAllowedMessage();
    }

}



