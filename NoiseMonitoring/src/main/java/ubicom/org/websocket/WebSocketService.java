package ubicom.org.websocket;


import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import javax.websocket.OnClose;
import javax.websocket.Session;
import javax.websocket.CloseReason;
import javax.websocket.CloseReason.CloseCodes; 
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import org.json.JSONException;
import org.json.JSONObject;

import ubicom.org.model.Room;



import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/data_service")
public class WebSocketService {

	public static  Set<Session> clients = Collections.synchronizedSet(new HashSet<Session>());
	private static final Logger logger = LogManager.getLogger(WebSocketService.class.getName());
	@OnMessage
	public void onMessage(String message, Session session) throws IOException,
			InterruptedException {
	
	}

	@OnOpen
	public void onOpen(Session session) {
		logger.trace("Connected ... " + session.getId());
		clients.add(session);
	}

	@OnClose
	public void onClose(Session session, CloseReason closeReason) {
		
		logger.trace(String.format("Session %s closed because of %s", session.getId(), closeReason));
		
		clients.remove(session);
		
	}
	
	public static void pushSensorDataService(Room roomData) {
		
		ObjectMapper mapper = new ObjectMapper();
		StringWriter writer = new StringWriter();
		
		try{
			mapper.writeValue(writer, roomData);
			
		}catch(IOException ex){
			
		}
		
		String jsonStr = writer.toString();
		
		System.out.println(jsonStr);
		
		synchronized(clients){
			for(Session client : clients){
				try {
					if(client.isOpen()){
						
						client.getBasicRemote().sendText(jsonStr);
						
					}
					else
					{
						//logger.debug("socket closed");
						client.close();
						
					}
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					
					
				}
			}
		}
	}
	
	/**************************************************************************************/
	public static void pushEvent(String event) 
	{
		
			//System.out.println(event);
			
			synchronized(clients){
				for(Session client : clients){
					try {
						if(client.isOpen()){
							
							client.getBasicRemote().sendText(event);
							
						}
						else
						{
							//logger.debug("socket closed");
							client.close();
							
						}
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						
						
					}
				}
			}
		
	}
}
