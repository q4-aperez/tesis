package edu.aperez.webmobilegroupchat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by alexperez on 27/09/2015.
 */
public class MainActivity extends AppCompatActivity {

    private RecyclerView jobsList;
    private JobsAdapter jobsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupJobsList();

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

    private void setupJobsList() {
        jobsList = (RecyclerView) findViewById(R.id.jobs_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        jobsList.setHasFixedSize(true);

        // use a linear layout manager
        jobsList.setLayoutManager(new LinearLayoutManager(this));

        // specify an adapter
        jobsAdapter = new JobsAdapter();
        jobsList.setAdapter(jobsAdapter);
    }
}
