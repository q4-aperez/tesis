package info.androidhive.webmobilegroupchat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Maps;

@ServerEndpoint("/chat")
public class SocketServer {
	// set to store all the live sessions
	private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());

	// Mapping between session and person name
	private static final HashMap<String, String> nameSessionPair = new HashMap<String, String>();

	// Admin session
	private Session adminSession;

	private JSONUtils jsonUtils = new JSONUtils();

	// Getting query params
	public static Map<String, String> getQueryMap(String query) {
		Map<String, String> map = Maps.newHashMap();
		if (query != null) {
			String[] params = query.split("&");
			for (String param : params) {
				String[] nameval = param.split("=");
				map.put(nameval[0], nameval[1]);
			}
		}
		return map;
	}

	/**
	 * Called when a socket connection opened
	 */
	@OnOpen
	public void onOpen(Session session) {

		System.out.println(session.getId() + " has opened a connection");

		Map<String, String> queryParams = getQueryMap(session.getQueryString());

		String name = "";

		if (queryParams.containsKey("name")) {
			// Getting client name via query param
			name = queryParams.get("name");
			try {
				name = URLDecoder.decode(name, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			if (nameSessionPair.containsValue(name)) {
				System.out.println(name + " is already logged in");

				Iterator it = nameSessionPair.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry pair = (Map.Entry) it.next();
					if (pair.getValue().equals(name)) {
						it.remove(); // avoids a ConcurrentModificationException
					}
				}
				Iterator<Session> it2 = sessions.iterator();
				while (it2.hasNext()) {
					if (it2.next().getQueryString().contains(name)) {
						it2.remove();
					}
				}
			}

			// Mapping client name and session id
			nameSessionPair.put(session.getId(), name);
			if (name.equals("admin")) {
				adminSession = session;
			}
		}

		// Adding session to session list
		sessions.add(session);

		try {
			// Sending session id to the client that just connected
			session.getBasicRemote().sendText(jsonUtils.getClientDetailsJson(session.getId(), "Your session details"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Notifying all the clients about new person joined
		sendMessageToAll(session.getId(), name, " joined the grid!", true, false);

	}

	/**
	 * method called when new message received from any client
	 * 
	 * @param message
	 *            JSON message from client
	 */
	@OnMessage
	public void onMessage(String message, Session session) {

		System.out.println("Message from " + session.getId() + ": " + message);

		String msg = null;

		// Parsing the json and getting message
		try {
			JSONObject jObj = new JSONObject(message);
			msg = jObj.getString("message");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// Sending the message to a specific client
		if (msg.contains("@")) {
			String[] messageParts = msg.split("@");
			String destination = messageParts[0];
			String messageToSend = messageParts[1];
			for (Session s : sessions) {
				if (nameSessionPair.get(s.getId()).equals(destination)) {
					String json = null;

					json = jsonUtils.getSendAllMessageJson(session.getId(), nameSessionPair.get(session.getId()),
							messageToSend);

					try {
						System.out.println("Sending Message to device: " + session.getId() + ", " + json);

						s.getBasicRemote().sendText(json);
					} catch (IOException e) {
						System.out.println("error in sending. " + s.getId() + ", " + e.getMessage());
						e.printStackTrace();
					}
					break;
				}
			}
		} else {
			// Sending the message to server admin
			try {
				if (adminSession == null) {
					setAdminSession();
				}
				if (adminSession.isOpen()) {
					String json = jsonUtils.getSendAllMessageJson(session.getId(), nameSessionPair.get(session.getId()),
							msg);
					adminSession.getBasicRemote().sendText(json);
					System.out.println("Sending Message to admin: " + session.getId() + ", " + json);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void setAdminSession() {
		for (Session s : sessions) {
			if (nameSessionPair.get(s.getId()).equals("admin")) {
				adminSession = s;
				break;
			}
		}
	}

	/**
	 * Method called when a connection is closed
	 */
	@OnClose
	public void onClose(Session session) {

		System.out.println("Session " + session.getId() + " has ended");

		// Getting the client name that exited
		String name = nameSessionPair.get(session.getId());

		// removing the session from sessions list
		sessions.remove(session);

		// Notifying all the clients about person exit
		sendMessageToAll(session.getId(), name, " left the grid!", false, true);

	}

	/**
	 * Method to send message to all clients
	 * 
	 * @param sessionId
	 * @param message
	 *            message to be sent to clients
	 * @param isNewClient
	 *            flag to identify that message is about new person joined
	 * @param isExit
	 *            flag to identify that a person left the conversation
	 */
	private void sendMessageToAll(String sessionId, String name, String message, boolean isNewClient, boolean isExit) {

		// Looping through all the sessions and sending the message individually
		for (Session s : sessions) {
			String json = null;

			// Checking if the message is about new client joined
			if (isNewClient) {
				json = jsonUtils.getNewClientJson(sessionId, name, message, sessions.size());

			} else if (isExit) {
				// Checking if the person left the conversation
				json = jsonUtils.getClientExitJson(sessionId, name, message, sessions.size());
			} else {
				// Normal chat conversation message
				json = jsonUtils.getSendAllMessageJson(sessionId, name, message);
			}

			try {
				System.out.println("Sending Message To: " + sessionId + ", " + json);

				s.getBasicRemote().sendText(json);
			} catch (IOException e) {
				System.out.println("error in sending. " + s.getId() + ", " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
