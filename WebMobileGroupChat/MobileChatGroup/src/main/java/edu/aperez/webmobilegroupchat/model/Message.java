package edu.aperez.webmobilegroupchat.model;

import com.google.gson.annotations.SerializedName;

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
}
