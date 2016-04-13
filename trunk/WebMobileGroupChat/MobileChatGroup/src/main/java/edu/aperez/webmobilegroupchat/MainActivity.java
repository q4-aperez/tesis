package edu.aperez.webmobilegroupchat;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by alexperez on 27/09/2015.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        //Processor receiver
//        AlarmManager service = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        Intent intent = new Intent(this, ProcessorReceiver.class);
//        PendingIntent pending = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
//        // fetch every 30 seconds
//        // InexactRepeating allows Android to optimize the energy consumption
//        long secondsFromNow = System.currentTimeMillis() + 5 * 1000;
//        service.setInexactRepeating(AlarmManager.RTC_WAKEUP,
//                secondsFromNow, 30000, pending);
//
//        //Battery Receiver
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
//        BatteryReceiver batteryReceiver = new BatteryReceiver();
//        registerReceiver(batteryReceiver, intentFilter);

        startService(new Intent(this, DeviceInformationService.class));
    }
}
