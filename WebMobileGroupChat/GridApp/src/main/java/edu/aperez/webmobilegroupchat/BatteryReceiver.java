package edu.aperez.webmobilegroupchat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by alex.perez on 29/10/2015.
 */
public class BatteryReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, BatteryService.class);
        context.startService(service);
    }
}
