package com.q4tech.servicetest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

public class LocalWordService extends Service {
    private final IBinder mBinder = new MyBinder();
    private ArrayList<String> list = new ArrayList<String>();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

//        Random random = new Random();
//        if (random.nextBoolean()) {
//            list.add("Linux");
//        }
//        if (random.nextBoolean()) {
//            list.add("Android");
//        }
//        if (random.nextBoolean()) {
//            list.add("iPhone");
//        }
//        if (random.nextBoolean()) {
//            list.add("Windows7");
//        }
//        if (list.size() >= 20) {
//            list.remove(0);
//        }

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float) scale;

        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL;
        String chargeStatus;
        if(isCharging)
            chargeStatus = "charging";
        else
            chargeStatus = "discharging";

        list.add(list.size() + ": " + getInfo() + "\nBattery " + (int)(batteryPct*100)+"% and "+chargeStatus);

        return Service.START_NOT_STICKY;
    }

    private String getInfo() {
        StringBuffer sb = new StringBuffer();
//        sb.append("abi: ").append(Build.CPU_ABI).append("\n");
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

    @Override
    public IBinder onBind(Intent arg0) {
        return mBinder;
    }

    public class MyBinder extends Binder {
        LocalWordService getService() {
            return LocalWordService.this;
        }
    }

    public List<String> getWordList() {
        return list;
    }

}