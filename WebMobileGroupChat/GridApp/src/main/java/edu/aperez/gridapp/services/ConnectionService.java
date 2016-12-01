package edu.aperez.gridapp.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.codebutler.android_websockets.WebSocketClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;

import edu.aperez.gridapp.R;
import edu.aperez.gridapp.model.Message;
import edu.aperez.gridapp.util.Utils;
import edu.aperez.gridapp.util.WsConfig;

/**
 * Created by alexperez on 12/01/2016.
 */
public class ConnectionService extends Service {

    private Utils utils;
    private WebSocketClient client;
    private ConnectionCallbacks activity;
    private static final String TAG_SELF = "self", TAG_NEW = "new",
            TAG_MESSAGE = "message", TAG_EXIT = "exit";

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private final IBinder mBinder = new LocalBinder();
    private long lastPingReceivedTimestamp;
    private ArrayList<String> pendingMessages;

    @Override
    public void onCreate() {
        super.onCreate();

        utils = new Utils(this);

//        if (activity != null) {
//            client = initializeWebSocket(Build.MODEL.replaceAll(" ", ""));
//        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    //Here Activity register to the service as Callbacks client
    public void registerClient(ConnectionCallbacks activity) {
        this.activity = activity;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void initializeWebSocket(String serverIp, String name) {
        /**
         * Creating web socket client. This will have callback methods
         * */
        String url = WsConfig.URL_WEBSOCKET.replace("$SERVER_IP$", serverIp);
       client = new WebSocketClient(URI.create(url + name), new WebSocketClient.Listener() {
            @Override
            public void onConnect() {
                lastPingReceivedTimestamp = new Date().getTime();
                activity.showSnackbar(R.string.socket_connected);
                if (pendingMessages != null) {
                    for (String pendingMessage : pendingMessages) {
                        ConnectionService.this.client.send(pendingMessage);
                    }
                    pendingMessages.clear();
                }
            }

            /**
             * On receiving the message from web socket server
             * */
            @Override
            public void onMessage(String message) {
                parseMessage(message);

            }

            @Override
            public void onMessage(byte[] data) {
                // Message will be in JSON format
                parseMessage(bytesToHex(data));
            }

            /**
             * Called when the connection is terminated
             * */
            @Override
            public void onDisconnect(int code, String reason) {

            }

            @Override
            public void onError(Exception error) {
//                Log.e(TAG, "Error! : " + error);
//
//                showToast("Error! : " + error);
            }

        }, null);

//        client.connect();
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

//                Log.e(TAG, "Your session id: " + utils.getSessionId());

            } else if (flag.equalsIgnoreCase(TAG_MESSAGE)) {
                // if the flag is 'message', new message received
                if (jObj.getString("name").equals("admin")) {
                    GsonBuilder builder = new GsonBuilder();
                    Gson gson = builder.create();
                    try {
                        Message message = gson.fromJson(jObj.toString(), Message.class);
                        if (!message.getMessage().contains("ping")) {
                            activity.updateClient(message);
                        } else {
                            lastPingReceivedTimestamp = new Date().getTime();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }

        } catch (Exception e) {
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

    public void sendMessage(String jsonMessage) {
        if (client == null) {
            queueMessage(jsonMessage);
            return;
        }
        if (!client.isConnected() || lastPingTimeout()) {
            client.connect();
            queueMessage(jsonMessage);
        } else {
            client.send(jsonMessage);
        }
    }

    private void queueMessage(String jsonMessage) {
        if (pendingMessages == null) {
            pendingMessages = new ArrayList<>();
        }
        pendingMessages.add(jsonMessage);
    }

    private boolean lastPingTimeout() {
        long currentTimestamp = new Date().getTime();
        //if it's been moren than 60 seconds since the last message was received, reconnect
        if (lastPingReceivedTimestamp > 0 && currentTimestamp - lastPingReceivedTimestamp > 60000) {
            if (client.isConnected()) {
                client.disconnect();
            }
            return true;
        }
        return false;
    }

    public boolean isConnected() {
        if (client != null) {
            return client.isConnected();
        }
        return false;
    }

    public interface ConnectionCallbacks {
        void updateClient(Message job);

        void toggleConnectButtonText(boolean isConnected);

        void showSnackbar(int resId);
    }

    //returns the instance of the service
    public class LocalBinder extends Binder {
        public ConnectionService getServiceInstance() {
            return ConnectionService.this;
        }
    }

    public void disconnectClient() {
        client.disconnect();
        utils.storeSessionId(null);
        activity.showSnackbar(R.string.socket_disconnected);
        activity.toggleConnectButtonText(false);
    }

    public void connectClient() {
        if (client != null) {
            client.connect();
            activity.toggleConnectButtonText(true);
        }
    }

    @Override
    public void onDestroy() {
        Intent intent = new Intent("edu.aperez.groupchat");
        intent.putExtra("yourvalue", "torestore");
        sendBroadcast(intent);
    }
}
