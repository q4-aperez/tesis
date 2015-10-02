package com.q4tech.servicetest;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class MyStartServiceReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent service = new Intent(context, LocalWordService.class);
        context.startService(service);

        Log.e("ALGO", "ALGO MAS");
    }
}