package edu.aperez.gridapp.jobs;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import edu.aperez.gridapp.R;
import edu.aperez.gridapp.model.Message;
import edu.aperez.gridapp.services.BatteryReceiver;
import edu.aperez.gridapp.services.BatteryService;
import edu.aperez.gridapp.services.ConnectionService;
import edu.aperez.gridapp.services.InformationSender;
import edu.aperez.gridapp.services.ProcessorReceiver;
import edu.aperez.gridapp.services.ProcessorService;
import edu.aperez.gridapp.util.Utils;

/**
 * Created by alex.perez on 07/09/2016.
 */
public class JobsActivity extends AppCompatActivity implements ConnectionService.ConnectionCallbacks, InformationSender {

    private ConnectionService connectionService;
    private ProcessorService processorService;
    private BatteryService batteryService;
    private boolean mIsConnected;

    private ServiceConnection serverConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've binded to LocalService, cast the IBinder and get LocalService instance
            ConnectionService.LocalBinder binder = (ConnectionService.LocalBinder) service;
            connectionService = binder.getServiceInstance(); //Get instance of your service!
            connectionService.registerClient(JobsActivity.this); //Activity register in the service as client for callbacks!
            mIsConnected = connectionService.isConnected();
            toggleConnect(mIsConnected);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {

        }
    };

    private ServiceConnection proccesorConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've binded to LocalService, cast the IBinder and get LocalService instance
            ProcessorService.LocalBinder binder = (ProcessorService.LocalBinder) service;
            processorService = binder.getServiceInstance(); //Get instance of your service!
            processorService.registerClient(JobsActivity.this); //Activity register in the service as client for callbacks!
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private ServiceConnection batteryConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've binded to LocalService, cast the IBinder and get LocalService instance
            BatteryService.LocalBinder binder = (BatteryService.LocalBinder) service;
            batteryService = binder.getServiceInstance(); //Get instance of your service!
            batteryService.registerClient(JobsActivity.this); //Activity register in the service as client for callbacks!
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };
    private JobsFragment mJobsFragment;
    private JobsController mJobsController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_fragment_activity);

        // Set up the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.app_name);

        mJobsFragment = (JobsFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);

        if (mJobsFragment == null) {
            mJobsFragment = JobsFragment.newInstance();
            Utils.addFragmentToActivity(getSupportFragmentManager(), mJobsFragment, R.id.contentFrame);
        }

        // Create the presenter
        mJobsController = new JobsController(this, mJobsFragment);
        setupServices();
    }

    private void setupServices() {
        //Processor receiver
        AlarmManager service = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent processorRecIntent = new Intent(this, ProcessorReceiver.class);
        PendingIntent pending = PendingIntent.getBroadcast(this, 0, processorRecIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        // fetch every 30 seconds
        // InexactRepeating allows Android to optimize the energy consumption
        long secondsFromNow = System.currentTimeMillis() + 5 * 1000;
        service.setInexactRepeating(AlarmManager.RTC_WAKEUP, secondsFromNow, 30000, pending);
        Intent processorIntent = new Intent(this, ProcessorService.class);
        startService(processorIntent);
        bindService(processorIntent, proccesorConnection, Context.BIND_AUTO_CREATE);

        //Battery Receiver
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        BatteryReceiver batteryReceiver = new BatteryReceiver();
        registerReceiver(batteryReceiver, intentFilter);
        Intent batteryIntent = new Intent(this, BatteryService.class);
        startService(batteryIntent);
        bindService(batteryIntent, batteryConnection, Context.BIND_AUTO_CREATE);

        Intent connectionIntent = new Intent(this, ConnectionService.class);
        startService(connectionIntent);
        bindService(connectionIntent, serverConnection, Context.BIND_AUTO_CREATE); //Binding to the service!
    }

    @Override
    public void updateClient(final Message job) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mJobsController.addJob(job);
            }
        });
    }

    @Override
    public void toggleConnect(boolean isConnected) {
        this.isConnected = isConnected;
        if (isConnected) {
            connectButton.setText(R.string.disconnect);
            connectButton.setBackground(ContextCompat.getDrawable(this, R.drawable.red_button));
        } else {
            connectButton.setText(R.string.connect);
            connectButton.setBackground(ContextCompat.getDrawable(this, R.drawable.green_button));
        }
    }

    @Override
    public void showSnackbar(int resId) {
        Snackbar.make(view, resId, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void sendInfo(String jsonInfo) {

    }

    public ConnectionService getConnectionService() {
        return connectionService;
    }
}
