package ubicom.org.NoiseMonitoring;

import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import ubicom.org.websocket.WebSocketServer;
import ubicom.org.websocket.WebSocketService;
import ubicom.org.model.Home;
import ubicom.org.model.Node;
import ubicom.org.model.Room;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.mongodb.MongoException;

import ubicom.org.MongoHandle.MongoHandle;
/**
 * 
 *
 */
@SuppressWarnings("deprecation")
public class App 
{
	private ServerSocket serverSocket;
	private WebSocketServer webSocket;
    public static void main( String[] args )
    {
    	
    	
    	CommandLine commandLine;
    	
    	Option helpOption = Option.builder("h").longOpt("help").required(false).desc("shows this message").build();
    	
    	Option ipOption = Option.builder("i").longOpt("ipAddress").required(true).desc("ip address of the server").numberOfArgs(1).type(String.class).build();
    	
    	Option socketPortOption = Option.builder("sP").longOpt("socketPort").required(true).desc("the socket port").numberOfArgs(1).type(Number.class).build();
    	
    	Option webSocketPortOption = Option.builder("wsP").longOpt("websocketPort").required(true).desc("the web socket port").numberOfArgs(1).type(Number.class).build();
    	
    	Options options = new Options();
    	CommandLineParser parser = new DefaultParser();
    	options.addOption(helpOption);
    	options.addOption(ipOption);
    	options.addOption(socketPortOption);
    	options.addOption(webSocketPortOption);
    	
    	try {
    		InetAddress IP=InetAddress.getLocalHost();
    		
    		//System.out.println(IP);
    		
    		String ipAddress = IP.getHostAddress();
    		
    		System.out.println("Local Host IP: " + ipAddress);
    		
			commandLine = parser.parse(options,args);
			if(commandLine.hasOption("help")){
				HelpFormatter formatter = new HelpFormatter();
				formatter.printHelp("Please use", options);
			}
			else
			{
				
				
				int socketPort = ((Number)commandLine.getParsedOptionValue("socketPort")).intValue();
				
				int webSocketPort = ((Number)commandLine.getParsedOptionValue("websocketPort")).intValue();
				
				String serverIPAddress = (commandLine.getParsedOptionValue("ipAddress")).toString();
				
				System.out.println("Server IP: " + serverIPAddress);
				
				App serverApp = new App();
				
		    	serverApp.start(serverIPAddress,socketPort,webSocketPort);
		    	
		    	
		    	
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
    	catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	
        //System.out.println( "Hello World!" );
    }
    
    private void start(String ipAddress, int socketPort, int webSocketPort)
    {
    	serverSocket = new ServerSocket(ipAddress,socketPort); //9002
  
    	
    	Thread threadServerSocket = new Thread(serverSocket);
        threadServerSocket.start();
    	
    	 webSocket = new WebSocketServer(ipAddress, 0, webSocketPort); //9003
    	
    	 Thread threadWebServerSocket = new Thread(webSocket);
         threadWebServerSocket.start();
         
         
         //Call thread checking status of node every 2 minutes
         int interval = 2; 
         Date timeToRun = new Date(System.currentTimeMillis() + interval);
  		
  		 Timer timer = new Timer();
  		 
  		 try {
 			
 			timer.schedule(new TimerTask(){
 				public void run() {
 					
 					checkNodeStatus();
 				}
 			}, timeToRun, 3600000);
 		;
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 		}
         
        // Test websocket
         
        /*int interval = 2; 
 		Date timeToRun = new Date(System.currentTimeMillis() + interval);
 		
 		Timer timer = new Timer();
 		
 		
 		try {
 			
 			timer.schedule(new TimerTask(){
 				public void run() {
 					Random randValue1 = new Random();
 					Room room = new Room (1,randValue1.nextInt(20),randValue1.nextInt(50),randValue1.nextInt(100));
 					WebSocketService.pushSensorDataService(room);
 				}
 			}, timeToRun, 5000);
 		;
 		} 
 		catch (Exception e) {
 			e.printStackTrace();
 		}*/
 	
    }
    private void checkNodeStatus()
    {
    	try{
    		List<Node> listNode= MongoHandle.getListOfNode();
        	
        	if(listNode.size() > 0)
        	{
        		Node tempNode ;
        		for(int i = 0;i<listNode.size();i++)
        		{
        			tempNode = listNode.get(i);
        			
        			if(tempNode.isDeleted() == false)
        			{
        				String node_id = tempNode.getId();
        			
    	    			Home home = MongoHandle.getHome(node_id);
    	    			
    	    			if(home != null && home.isDeleted() == false)
    	    			{
    	    				Calendar curCalendar = GregorianCalendar.getInstance();
    	    				Date curDate = curCalendar.getTime();
    	    				int currentMinute = curDate.getMinutes();
    	    				curDate.setMinutes(currentMinute - 20);
    	    				
    	    				System.out.println(curDate);
    	    				
    	    				
    	    				Date lastDataUpdate = home.getLastUpdateData();
    	    				
    	    				if(curDate.after(lastDataUpdate)){
    	    					
    	    					System.out.println(tempNode.getNodeNumber() + " " + "off");
    	    					MongoHandle.updateNodeStatusInHome(node_id, "OFF");
    	    				}
    	    				else{
    	    					
    	    					System.out.println(tempNode.getNodeNumber() + " " + "on");
    	    					MongoHandle.updateNodeStatusInHome(node_id, "ON");
    	    				}
    	    				
    	    			}
        			}
        			
        			
        		}
        	}
    	}
    	catch(MongoException e)
    	{
    		e.printStackTrace();
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
    }
}
