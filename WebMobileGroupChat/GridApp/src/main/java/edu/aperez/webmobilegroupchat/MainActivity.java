package edu.aperez.webmobilegroupchat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import edu.aperez.webmobilegroupchat.job.Factorial;
import edu.aperez.webmobilegroupchat.job.Fibonacci;
import edu.aperez.webmobilegroupchat.model.Message;
import info.androidhive.webgroupchat.other.Constants;
import info.androidhive.webgroupchat.other.Utils;

/**
 * Created by alexperez on 27/09/2015.
 */
public class MainActivity extends AppCompatActivity implements DeviceInformationService.JobCallback {

    private RecyclerView jobsList;
    private JobsAdapter jobsAdapter;
    private boolean hasJobsPending;
    private DeviceInformationService myService;
    private Utils utils;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

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

        Intent serviceIntent = new Intent(this, DeviceInformationService.class);
        startService(serviceIntent);
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE); //Binding to the service!
        utils = new Utils(this);
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

    @Override
    public void updateClient(final Message job) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                jobsAdapter.addJob(job);

                if (!hasJobsPending) {
//                    findViewById(R.id.current_job_data).setVisibility(View.VISIBLE);
                    hasJobsPending = true;
                    executePendingJobs();
                }
            }
        });
    }

    private void executePendingJobs() {
        TextView currentJob = (TextView) findViewById(R.id.current_job_data);
        if (hasJobsPending) {
            Message job = jobsAdapter.getFirstJob();
            if (job != null) {
                progressBar.setVisibility(View.VISIBLE);
                currentJob.setText(getString(R.string.job_data, job.getJob(), job.getValue()));
                new ExecuteJob().execute(job);
            }
        } else {
            progressBar.setVisibility(View.GONE);
            currentJob.setText(R.string.none);
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
//            Toast.makeText(MainActivity.this, "onServiceConnected called", Toast.LENGTH_SHORT).show();
            // We've binded to LocalService, cast the IBinder and get LocalService instance
            DeviceInformationService.LocalBinder binder = (DeviceInformationService.LocalBinder) service;
            myService = binder.getServiceInstance(); //Get instance of your service!
            myService.registerClient(MainActivity.this); //Activity register in the service as client for callabcks!
//            tvServiceState.setText("Connected to service...");
//            tbStartTask.setEnabled(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
//            Toast.makeText(MainActivity.this, "onServiceDisconnected called", Toast.LENGTH_SHORT).show();
//            tvServiceState.setText("Service disconnected");
//            tbStartTask.setEnabled(false);
        }
    };

    private class ExecuteJob extends AsyncTask<Message, Void, Long> {
        @Override
        protected Long doInBackground(Message... messages) {
            Message job = messages[0];
            Long result = 0L;
            switch (job.getJob().toLowerCase()) {
                case Constants.FIBONACCI:
                    result = new Fibonacci().calculate(Integer.valueOf(job.getValue()));
                    break;
                case Constants.FACTORIAL:
                    result = Factorial.calculate(Long.parseLong(job.getValue()));
                    break;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Long result) {
            if (myService != null) {
                myService.sendMessage(utils.getSendMessageJSON(getString(R.string.result, result)));
            }
            if (jobsAdapter.getItemCount() == 0) {
                hasJobsPending = false;
//                findViewById(R.id.current_job_data).setVisibility(View.GONE);
            }
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    executePendingJobs();
                }
            };
            Handler handler = new Handler();
            handler.postDelayed(runnable, 800);
        }
    }
}
