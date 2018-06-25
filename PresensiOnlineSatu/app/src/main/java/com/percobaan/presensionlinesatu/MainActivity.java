package com.percobaan.presensionlinesatu;

import android.content.Context;
import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class MainActivity extends AppCompatActivity {
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textid);

        DetektorEmulator detektorEmulator = new DetektorEmulator(this);
        DetektorRoot detektorRoot = new DetektorRoot();
/*
        if(detektorEmulator.isEmulator()){
            textView.setText("Device yang anda gunakan adalah EMULATOR");
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Do something after 5s = 5000ms
                    finishAffinity();
                }
            }, 5000);


        }else {
            if(detektorRoot.isDeviceRooted()) {
                textView.setText("Device yang anda gunakan sudah di ROOT");
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after 5s = 5000ms
                        finishAffinity();
                    }
                }, 5000);
            }else {
                textView.setText("Terima Kasih");

                String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

                String type = "login";

                BackgroundWorker backgroundWorker = new BackgroundWorker(this);
                backgroundWorker.execute(type, id);

            }


        }
*/
        String id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        String type = "login";

        BackgroundWorker backgroundWorker = new BackgroundWorker(this);
        backgroundWorker.execute(type, id);

    }


}
