package edu.aperez.gridapp;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import info.androidhive.webgroupchat.other.Utils;

/**
 * Created by alex.perez on 29/10/2015.
 */
public class BatteryService extends Service {
    private Utils utils;
    private final IBinder mBinder = new LocalBinder();
    private InformationSender activity;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        utils = new Utils(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (activity != null) {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = registerReceiver(null, ifilter);

            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);


            final int batteryPct = (int) (level / (float) scale * 100);
            activity.sendInfo(utils.getSendMessageJSON(batteryPct + "%"));

        }

        return Service.START_STICKY;
    }

    public void registerClient(InformationSender mainActivity) {
        this.activity = mainActivity;
    }

    //returns the instance of the service
    public class LocalBinder extends Binder {
        public BatteryService getServiceInstance() {
            return BatteryService.this;
        }
    }

}
