package edu.aperez.gridapp.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by alexperez on 05/11/2016.
 */

public class ConnectionStoppedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("Connection Service", "Restarting...");
        context.startService(new Intent(context, ConnectionService.class));;
    }
}
