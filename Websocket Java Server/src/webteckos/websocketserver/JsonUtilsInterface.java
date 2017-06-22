/**
 * 
 */
package webteckos.websocketserver;

/**
 * @author Iwo Paprota
 *
 */
public interface JsonUtilsInterface {

	/**
	 * Retrieves client details
	 * 
	 * @param sessionId
	 * @param message
	 * @return Formatted String
	 */
	public String getClientDetailsJson(String sessionId, String message);

	/**
	 * Retrieves the request
	 * 
	 * @param sessionId
	 * @param fromName
	 * @param subject
	 * @param phone
	 * @param message
	 * @param extras
	 * @return Formatted String
	 */
	public String getRequest(String sessionId, String fromName, String subject, String phone, String message,
			String extras);

	/**
	 * Retrieves the response
	 * 
	 * @param sessionId
	 * @param fromName
	 * @param subject
	 * @param message
	 * @return Formatted String
	 */
	public String getResponse(String sessionId, String fromName, String subject, String message);

}
