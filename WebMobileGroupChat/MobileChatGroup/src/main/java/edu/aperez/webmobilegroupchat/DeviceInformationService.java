package edu.aperez.webmobilegroupchat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.codebutler.android_websockets.WebSocketClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;

import edu.aperez.webmobilegroupchat.job.Factorial;
import edu.aperez.webmobilegroupchat.job.Fibonacci;
import edu.aperez.webmobilegroupchat.model.Message;
import info.androidhive.webgroupchat.other.Utils;
import info.androidhive.webgroupchat.other.WsConfig;

/**
 * Created by alexperez on 12/01/2016.
 */
public class DeviceInformationService extends Service {

    private Utils utils;
    private WebSocketClient client;
    private static final String TAG_SELF = "self", TAG_NEW = "new",
            TAG_MESSAGE = "message", TAG_EXIT = "exit";

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    @Override
    public void onCreate() {
        super.onCreate();

        utils = new Utils(this);

        client = initializeWebSocket(Installation.id(this));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public WebSocketClient initializeWebSocket(String name) {
        /**
         * Creating web socket client. This will have callback methods
         * */
        WebSocketClient client = new WebSocketClient(URI.create(WsConfig.URL_WEBSOCKET + name), new WebSocketClient.Listener() {
            @Override
            public void onConnect() {

            }

            /**
             * On receiving the message from web socket server
             * */
            @Override
            public void onMessage(String message) {
//                Log.d(TAG, String.format("Got string message! %s", message));

                parseMessage(message);

            }

            @Override
            public void onMessage(byte[] data) {
//                Log.d(TAG, String.format("Got binary message! %s", bytesToHex(data)));

                // Message will be in JSON format
                parseMessage(bytesToHex(data));
            }

            /**
             * Called when the connection is terminated
             * */
            @Override
            public void onDisconnect(int code, String reason) {

//                String message = String.format(Locale.US, "Disconnected! Code: %d Reason: %s", code, reason);

//                showToast(message);

                // clear the session id from shared preferences
                utils.storeSessionId(null);
            }

            @Override
            public void onError(Exception error) {
//                Log.e(TAG, "Error! : " + error);
//
//                showToast("Error! : " + error);
            }

        }, null);

        client.connect();

        return client;
    }

    /**
     * Parsing the JSON message received from server The intent of message will
     * be identified by JSON node 'flag'. flag = self, message belongs to the
     * person. flag = new, a new person joined the conversation. flag = message,
     * a new message received from server. flag = exit, somebody left the
     * conversation.
     */
    private void parseMessage(final String msg) {

        try {
            JSONObject jObj = new JSONObject(msg);

            // JSON node 'flag'
            String flag = jObj.getString("flag");

            // if flag is 'self', this JSON contains session id
            if (flag.equalsIgnoreCase(TAG_SELF)) {

                String sessionId = jObj.getString("sessionId");

                // Save the session id in shared preferences
                utils.storeSessionId(sessionId);
//
//                Log.e(TAG, "Your session id: " + utils.getSessionId());

            } else if (flag.equalsIgnoreCase(TAG_NEW)) {
                // If the flag is 'new', new person joined the room
//                String name = jObj.getString("name");
//                String message = jObj.getString("message");

                // number of people online
//                String onlineCount = jObj.getString("onlineCount");

//                showToast(name + message + ". Currently " + onlineCount
//                        + " people online!");

            } else if (flag.equalsIgnoreCase(TAG_MESSAGE)) {
                // if the flag is 'message', new message received
                if (jObj.getString("name").equals("admin")) {
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    try {
                        Message message = gson.fromJson(jObj.toString(), Message.class);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
//                    String[] job = jObj.getString("message").split(";");
//                    Integer value = Integer.valueOf(job[1]);
//                    switch (job[0]) {
//                        case "fibonacci":
//                            Long fibResult = new Fibonacci().calculate(value);
//                            client.send(utils.getSendMessageJSON("Resultado: " + fibResult));
//                            break;
//                        case "factorial":
//                            Long facResult = Factorial.calculate(value);
//                            client.send(utils.getSendMessageJSON("Resultado: " + facResult));
//                            break;
//                    }
                }

            } else if (flag.equalsIgnoreCase(TAG_EXIT)) {
                // If the flag is 'exit', somebody left the conversation
//                String name = jObj.getString("name");
//                String message = jObj.getString("message");

//                showToast(name + message);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
