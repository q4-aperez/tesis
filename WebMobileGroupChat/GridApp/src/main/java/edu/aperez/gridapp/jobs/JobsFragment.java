package edu.aperez.gridapp.jobs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import edu.aperez.gridapp.JobsAdapter;
import edu.aperez.gridapp.R;
import edu.aperez.gridapp.model.Message;

/**
 * Created by alex.perez on 07/09/2016.
 */
public class JobsFragment extends Fragment implements JobsContract.View {

    private JobsContract.Controller mController;
    private View mRoot;
    private RecyclerView mJobsList;
    private JobsAdapter mJobsAdapter;
    private ProgressBar mProgressBar;
    private TextView mConnectButton;
    private TextView mCurrentJob;
    private EditText mServerIp;

    public static JobsFragment newInstance() {

        Bundle args = new Bundle();

        JobsFragment fragment = new JobsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void setController(JobsContract.Controller controller) {
        this.mController = controller;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.jobs_fragment, container, false);

        mProgressBar = (ProgressBar) mRoot.findViewById(R.id.progress_bar);
        mConnectButton = (TextView) mRoot.findViewById(R.id.connect_button);
        mCurrentJob = (TextView) mRoot.findViewById(R.id.current_job_data);
        mServerIp = (EditText) mRoot.findViewById(R.id.server_ip);
        mRoot.findViewById(R.id.set_server_ip).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((JobsActivity) getActivity()).initializeWebSocket(mServerIp.getText().toString());
            }
        });

        setupJobsList();

        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mController.toggleConnection();
            }
        });

        return mRoot;
    }

    private void setupJobsList() {
        mJobsList = (RecyclerView) mRoot.findViewById(R.id.jobs_list);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mJobsList.setHasFixedSize(true);

        // use a linear layout manager
        mJobsList.setLayoutManager(new LinearLayoutManager(getActivity()));

        // specify an adapter
        mJobsAdapter = new JobsAdapter();
        mJobsList.setAdapter(mJobsAdapter);
    }

    @Override
    public void addJob(Message job) {
        mJobsAdapter.addJob(job);
    }

    @Override
    public Message getFirstJob() {
        return mJobsAdapter.getFirstJob();
    }

    @Override
    public void setExecutingJob(@Nullable Message job) {
        if (job != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            mCurrentJob.setText(getString(R.string.job_data, job.getJob(), job.getValue()));
        } else {
            mProgressBar.setVisibility(View.GONE);
            mCurrentJob.setText(R.string.none);
        }
    }

    @Override
    public int getJobsCount() {
        return mJobsAdapter.getItemCount();
    }

    @Override
    public void showSnackbar(int resId) {
        Snackbar.make(mRoot, resId, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void toggleConnect(boolean isConnected) {
        if (isConnected) {
            mConnectButton.setText(R.string.disconnect);
            mConnectButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.red_button));
        } else {
            mConnectButton.setText(R.string.connect);
            mConnectButton.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.green_button));
        }
    }
}
