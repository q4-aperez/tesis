package edu.aperez.gridapp.jobs;

import edu.aperez.gridapp.model.Message;

/**
 * Created by alex.perez on 07/09/2016.
 */
public interface JobsContract {

    interface Controller {

        void addJob(Message job);
    }

    interface View {

        void addJob(Message job);

        Message getFirstJob();

        void setExecutingJob(Message job);

        int getJobsCount();
    }
}
