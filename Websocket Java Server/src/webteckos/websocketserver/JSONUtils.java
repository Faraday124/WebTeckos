package webteckos.websocketserver;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Iwo Paprota
 *
 */

public class JSONUtils implements JsonUtilsInterface {

	/**
	 * Flags to identify the kind of json response on client side
	 */
	protected static final String FLAG_SELF = "self", FLAG_REQUEST = "request", FLAG_RESPONSE = "response";

	@Override
	public String getClientDetailsJson(String sessionId, String message) {
		String json = null;

		try {
			JSONObject jObj = new JSONObject();
			jObj.put("flag", FLAG_SELF);
			jObj.put("sessionId", sessionId);
			jObj.put("message", message);

			json = jObj.toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}

	@Override
	public String getRequest(String sessionId, String fromName, String subject, String phone, String message,
			String extras) {
		String json = null;

		try {
			JSONObject jObj = new JSONObject();
			jObj.put("flag", FLAG_REQUEST);
			jObj.put("subject", subject);
			jObj.put("sessionId", sessionId);
			jObj.put("name", fromName);
			if (message != null)
				jObj.put("message", message);
			if (phone != null)
				jObj.put("phone", phone);
			if (extras != null)
				jObj.put("extras", extras);
			json = jObj.toString();

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}

	@Override
	public String getResponse(String sessionId, String fromName, String subject, String message) {
		String json = null;

		try {
			JSONObject jObj = new JSONObject();
			jObj.put("flag", FLAG_RESPONSE);
			jObj.put("subject", subject);
			jObj.put("sessionId", sessionId);
			jObj.put("name", fromName);
			jObj.put("message", message);
			json = jObj.toString();

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json;
	}
}
