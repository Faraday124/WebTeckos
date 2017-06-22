package webteckos.websocketserver;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Maps;

/**
 * @author Iwo Paprota
 *
 */
@ServerEndpoint("/websocket")
public class SocketServer {

	/**
	 * Set to store all the live sessions
	 */
	private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());

	/**
	 * Mapping between session and person name
	 */
	private static final HashMap<String, String> nameSessionPair = new HashMap<String, String>();

	private JsonUtilsInterface jsonUtils = new JSONUtils();

	/**
	 * Getting query params
	 * 
	 * @param query
	 * @return
	 */
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

	@OnOpen
	public void onOpen(Session session) {

		System.out.println(session.getId() + " has opened a connection");

		Map<String, String> queryParams = getQueryMap(session.getQueryString());

		String name = "";

		if (queryParams.containsKey("name")) {

			name = queryParams.get("name");
			try {
				name = URLDecoder.decode(name, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			// Mapping client name and session id
			nameSessionPair.put(session.getId(), name);
		}

		session.setMaxBinaryMessageBufferSize(9000000);
		session.setMaxTextMessageBufferSize(9000000);

		// Adding session to session list
		sessions.add(session);

		try {
			// Sending session id to the client that just connected
			session.getBasicRemote().sendText(jsonUtils.getClientDetailsJson(session.getId(), "Your session details"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@OnMessage
	public void onMessage(String jsonMessage, Session session) {

		System.out.println("Message from " + session.getId() + ": " + jsonMessage);

		String message = null;
		String flag = null;
		String subject = null;
		String phone = null;
		String extras = null;
		try {

			JSONObject jObj = new JSONObject(jsonMessage);

			flag = jObj.getString("flag");
			subject = jObj.getString("subject");

			if (flag.equals(JSONUtils.FLAG_REQUEST)) {
				if (subject.equals("sms-body") || subject.equals("sms-remove") || subject.equals("phonebook-delete")
						|| subject.equals("phonebook-new")) {
					phone = jObj.getString("phone");
				}
				if (subject.equals("sms-send")) {
					phone = jObj.getString("phone");
					message = jObj.getString("message");
				}
				if (subject.equals("phonebook-edit") || subject.equals("files-download")
						|| subject.equals("files-delete")) {
					extras = jObj.getString("extras");
				}

				sendRequest(session.getId(), nameSessionPair.get(session.getId()), subject, phone, message, extras);

			} else if (flag.equals(JSONUtils.FLAG_RESPONSE)) {

				message = jObj.getString("message");
				sendResponse(session.getId(), nameSessionPair.get(session.getId()), subject, message);
			}

		} catch (

		JSONException e)

		{
			e.printStackTrace();
		}
	}

	@OnMessage
	public void onMessage(byte[] bytes, Session session) {

		sendFile(bytes);
	}

	@OnClose
	public void onClose(Session session) {

		System.out.println("Session " + session.getId() + " has ended");
		nameSessionPair.remove(session.getId());
		sessions.remove(session);
	}

	private void sendFile(byte[] bytes) {
		for (Session s : sessions) {
			try {
				System.out.println("Sending File...");
				ByteBuffer byteBuffer = ByteBuffer.allocate(bytes.length);
				byteBuffer.put(bytes);
				byteBuffer.flip();
				s.getBasicRemote().sendBinary(byteBuffer);
			} catch (IOException e) {
				System.out.println("error in sending. " + s.getId() + ", " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	private void sendRequest(String sessionId, String name, String subject, String phone, String message,
			String extras) {
		String destinationId = getDestinationId(name, sessionId);
		if (destinationId != null) {
			for (Session s : sessions) {
				if (s.getId() == destinationId) {
					String json = null;
					json = jsonUtils.getRequest(sessionId, name, subject, phone, message, extras);

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
	}

	private void sendResponse(String sessionId, String name, String subject, String message) {

		String destinationId = getDestinationId(name, sessionId);
		if (destinationId != null) {
			for (Session s : sessions) {
				if (s.getId() == destinationId) {
					String json = null;
					json = jsonUtils.getResponse(sessionId, name, subject, message);
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
	}

	private String getDestinationId(String name, String sessionId) {
		String destinationId = null;
		for (Entry<String, String> e : nameSessionPair.entrySet()) {
			String key = e.getKey();
			String value = e.getValue();
			if (value.equals(name) && key != sessionId) {
				destinationId = key;
			}
		}
		return destinationId;
	}
}