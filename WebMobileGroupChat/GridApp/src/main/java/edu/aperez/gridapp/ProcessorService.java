package edu.aperez.gridapp;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.codebutler.android_websockets.WebSocketClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import info.androidhive.webgroupchat.other.ConnectionUtils;
import info.androidhive.webgroupchat.other.Utils;

public class ProcessorService extends Service {
    private WebSocketClient client;
    private Utils utils;

    @Override
    public void onCreate() {
        super.onCreate();

        utils = new Utils(getApplicationContext());
        client = ConnectionUtils.initializeWebSocket("Processor");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        client.send(utils.getSendMessageJSON(getInfo()));
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String getInfo() {
        StringBuffer sb = new StringBuffer();
        if (new File("/proc/cpuinfo").exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(new File("/proc/cpuinfo")));
                String aLine;
                while ((aLine = br.readLine()) != null) {
                    if (aLine.contains("processor") || aLine.contains("bogomips"))
                        sb.append(aLine + "\n");
                }
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

}