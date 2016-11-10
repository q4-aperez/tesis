package edu.aperez.gridapp.jobs;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;

import edu.aperez.gridapp.R;
import edu.aperez.gridapp.job.Factorial;
import edu.aperez.gridapp.job.Fibonacci;
import edu.aperez.gridapp.model.Message;
import edu.aperez.gridapp.services.ConnectionService;
import edu.aperez.gridapp.util.Constants;
import edu.aperez.gridapp.util.Utils;

/**
 * Created by alex.perez on 07/09/2016.
 */
public class JobsController implements JobsContract.Controller {
    private final Utils mUtils;
    JobsFragment mView;
    private boolean mHasJobsPending;
    private JobsActivity mContext;

    public JobsController(JobsActivity context, @NonNull JobsFragment jobsFragment) {
        mView = jobsFragment;
        mView.setController(this);
        mContext = context;
        mUtils = new Utils(context);
    }

    @Override
    public void addJob(Message job) {
        mView.addJob(job);

        if (!mHasJobsPending) {
            mHasJobsPending = true;
            executePendingJobs();
        }
    }

    @Override
    public void toggleConnection() {
        mContext.toggleConnection();
    }

    private void executePendingJobs() {
        if (mHasJobsPending) {
            Message job = mView.getFirstJob();
            if (job != null) {
                mView.setExecutingJob(job);
                new ExecuteJob().execute(job);
            }
        } else {
            mView.setExecutingJob(null);
        }
    }

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
            ConnectionService connectionService = mContext.getConnectionService();
            if (connectionService != null) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
                Date resultDate = new Date();
                connectionService.sendMessage(mUtils.getSendMessageJSON(mContext.getString(R.string.result, result) + "\nTime: " + simpleDateFormat.format(resultDate)));
            }
            if (mView.getJobsCount() == 0) {
                mHasJobsPending = false;
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
