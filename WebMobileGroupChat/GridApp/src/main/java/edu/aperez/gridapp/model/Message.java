package edu.aperez.gridapp.model;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;

/**
 * Created by alexperez on 14/04/2016.
 */
public class Message {

    @SerializedName("flag")
    private String flag;
    @SerializedName("name")
    private String name;
    @SerializedName("sessionId")
    private String sessionId;
    @SerializedName("message")
    private String message;

    private String job;
    private String value;
    private Long receivedTime;
    private Long finishedTime;
    private Long startTime;

    public Message() {
        receivedTime = Calendar.getInstance().getTime().getTime();
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getJob() {
        if (TextUtils.isEmpty(job)) {
            String[] data = message.split(";");
            job = data[0];
        }
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getValue() {
        if (TextUtils.isEmpty(value)) {
            String[] data = message.split(";");
            value = data[1];
        }
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getWaitTime() {
        if (finishedTime != null) {
            return finishedTime - receivedTime;
        } else {
            Long currentTime = Calendar.getInstance().getTime().getTime();
            return currentTime - receivedTime;
        }
    }

    public Long getExecutionTime() {
        if (finishedTime != null) {
            return finishedTime - startTime;
        } else {
            Long currentTime = Calendar.getInstance().getTime().getTime();
            return currentTime - startTime;
        }
    }

    public void setFinishedTime(Long finishedTime) {
        this.finishedTime = finishedTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }
}
