package edu.aperez.gridapp.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import edu.aperez.gridapp.util.Utils;

public class ProcessorService extends Service {
    private Utils utils;
    private InformationSender activity;
    private final IBinder mBinder = new LocalBinder();

    @Override
    public void onCreate() {
        super.onCreate();

        utils = new Utils(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (activity != null) {
            activity.sendInfo(utils.getSendMessageJSON(getInfo()));
        }
        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    private String getInfo() {
        StringBuffer sb = new StringBuffer();
        if (new File("/proc/cpuinfo").exists()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(new File("/proc/cpuinfo")));
                String aLine;
                int processorCount = 0;
                while ((aLine = br.readLine()) != null) {
                    if (aLine.contains("processor")) {
                        processorCount++;
                    } else if (aLine.toLowerCase().contains("bogomips")) {
                        sb.append(aLine + "\n");
                    }
                }
                sb.append("processor count: " + processorCount + "\n");
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    public void registerClient(InformationSender mainActivity) {
        this.activity = mainActivity;
    }

    //returns the instance of the service
    public class LocalBinder extends Binder {
        public ProcessorService getServiceInstance() {
            return ProcessorService.this;
        }
    }

    @Override
    public void onDestroy() {
        Intent intent = new Intent("edu.aperez.processor");
        intent.putExtra("yourvalue", "torestore");
        sendBroadcast(intent);
    }
}