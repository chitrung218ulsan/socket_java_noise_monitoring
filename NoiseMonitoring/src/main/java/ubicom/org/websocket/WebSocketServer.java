package ubicom.org.websocket;

import java.io.BufferedReader;
import java.io.InputStreamReader; 

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.tyrus.server.Server;



/*
 * Reference: How to build Java WebSocket Applications Using the JSR 356 AP
 */

public class WebSocketServer implements Runnable{
	
	
	
	private Server webServiceServer;
	
	 private static final Logger logger = LogManager.getLogger(WebSocketServer.class.getName());
	
	public WebSocketServer(String host, int portConfig, int portDataService){
		//server = new Server(host, port, "/websockets", WebSocket.class);
		
		
		logger.trace("initializing web socket server ADDRESS: " + host + " PORT " + portDataService);
		webServiceServer = new Server(host, portDataService, "/websockets", null ,WebSocketService.class);
		
	}
	
	public void run(){
		 try {
			 
             
             webServiceServer.start();
             
         } catch (Exception e) {
             throw new RuntimeException(e);
         } 
	}
	
	
	public void stopWebServiceServer(){
		
		webServiceServer.stop();
	}
}
