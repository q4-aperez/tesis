package edu.aperez.webmobilegroupchat;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import edu.aperez.webmobilegroupchat.model.Message;

/**
 * Created by alexperez on 14/04/2016.
 */
public class JobsAdapter extends RecyclerView.Adapter<JobsAdapter.JobViewHolder> {

    ArrayList jobs;

    public JobsAdapter() {
        jobs = new ArrayList();
    }

    @Override
    public JobViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.job_item, parent, false);
        return new JobViewHolder(v);
    }

    @Override
    public void onBindViewHolder(JobViewHolder holder, int position) {
        holder.job.setText("");
        holder.value.setText("");
    }

    @Override
    public int getItemCount() {
        return jobs.size();
    }

    protected class JobViewHolder extends RecyclerView.ViewHolder {
        TextView job, value;

        public JobViewHolder(View itemView) {
            super(itemView);

            job = (TextView) itemView.findViewById(R.id.job_title);
            value = (TextView) itemView.findViewById(R.id.job_value);
        }
    }

    public synchronized void addJob(Message job) {
        jobs.add(job);
        notifyItemInserted(jobs.size()-1);
    }
}
