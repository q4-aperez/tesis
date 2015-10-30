package edu.aperez.webmobilegroupchat;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.codebutler.android_websockets.WebSocketClient;

import info.androidhive.webgroupchat.other.ConnectionUtils;
import info.androidhive.webgroupchat.other.Utils;

/**
 * Created by alex.perez on 29/10/2015.
 */
public class BatteryService extends Service {
    private WebSocketClient client;
    private Utils utils;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        utils = new Utils(getApplicationContext());
        client = ConnectionUtils.initializeWebSocket("Battery");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        final int batteryPct = (int) (level / (float) scale * 100);

        //Delay to wait for connection setup
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (client != null && client.isConnected()) {
                    client.send(utils.getSendMessageJSON(batteryPct + "%"));
                }
            }
        }, 1000);

        return Service.START_NOT_STICKY;
    }
}
