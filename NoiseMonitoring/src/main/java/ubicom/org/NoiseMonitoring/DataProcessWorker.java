package ubicom.org.NoiseMonitoring;

import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.nio.ByteBuffer ;

import ubicom.org.MongoHandle.MongoHandle;
import ubicom.org.model.Node;
import ubicom.org.model.Room;
import ubicom.org.model.Home;
import ubicom.org.model.Building;
import ubicom.org.websocket.WebSocketService;


/*
 * Data definition for socket
 * 
 * messageType(1byte) - NumberOfEntries(1byte) - NodeId(1byte) - depth(1byte) - data(4bytes)[devEC, roomStatus, roomTemp, remainVoltage] - CRC
 * If the CRC is correct, server responses with "ACK"
 */


public class DataProcessWorker implements Runnable {
	
	@SuppressWarnings("rawtypes")
	private LinkedList queue =  new LinkedList();
	
	
	@SuppressWarnings("unchecked")
	public void processData(ServerSocket server, SocketChannel socket, byte[] data, int count )
	{
		//logger.debug("process data");
		byte[] dataCopy = new byte[count];
		System.arraycopy(data, 0, dataCopy, 0,count);
		synchronized(queue){
			queue.add(new ServerDataEvent(server,socket,dataCopy) );
			queue.notify();
		}
	} 
	public void run(){
		ServerDataEvent dataEvent;
		 while(true)
		 {
			 // Wait for data to become available
			 
			 synchronized(queue)
			 {
				 while(queue.isEmpty()){
					 try{
						 queue.wait();
					 }catch(InterruptedException e){
						               
					 }
				 }
				 
				 dataEvent = (ServerDataEvent)queue.remove(0);
			 }
			
			 
			 
			 ServerSocket serverSocket = dataEvent.server;
			 
			 SocketChannel socketChannel = dataEvent.socket;
			 
			 
			 byte[] check_no_array = Arrays.copyOf(dataEvent.data, 2);
			 //short check_no1 = (short)((dataEvent.data[0]<<8) | (dataEvent.data[1]));
			 short check_no1=(short)( ((check_no_array[1]&0xFF)<<8) | (check_no_array[0]&0xFF) );
			 int month = (int) dataEvent.data[2] & 0xff;
			 int day = (int)dataEvent.data[3] & 0xff;
			 int hour = (int)dataEvent.data[4] & 0xff;
			 int minute = (int)dataEvent.data[5] & 0xff;
			 
			 short dataNumber = (short)( ((dataEvent.data[7]&0xFF)<<8) | (dataEvent.data[6]&0xFF) );
			 
			 
			 
			 System.out.println("checkNo: " + check_no1);
			 System.out.println("month: " + month);
			 System.out.println("day: " + day);
			 System.out.println("hour: " + hour);
			 System.out.println("minute: " + minute);
			 System.out.println("dataNumber: " + dataNumber);
			 
			 
			 int start = 9;
			 
			 short device_id;
			 int command;
			 int battery;
			 short vib_min;
			 short vib_aver;
			 short vib_max;
			 short noise_min;
			 short noise_aver;
			 short noise_max;
			 
			 for(int i=0;i<dataNumber;i++)
			 {
				 //check_no = (short)( ((dataEvent.data[9+(i*20)]&0xFF)<<8) | (dataEvent.data[8+(i*20)]&0xFF) );
				 device_id =  (short)( ((dataEvent.data[9+(i*20)]&0xFF)<<8) | (dataEvent.data[8+(i*20)]&0xFF) );
				 command = (int)dataEvent.data[10+i*20] & 0xff;
				 battery = (int)dataEvent.data[11+i*20] & 0xff;
				 /*vib_max = (short)( ((dataEvent.data[13+(i*20)]&0xFF)<<8) | (dataEvent.data[12+(i*20)]&0xFF) );
				 vib_aver = (short)( ((dataEvent.data[15+(i*20)]&0xFF)<<8) | (dataEvent.data[14+(i*20)]&0xFF) );
				 vib_min = (short)( ((dataEvent.data[17+(i*20)]&0xFF)<<8) | (dataEvent.data[16+(i*20)]&0xFF) );
				 
				 noise_max = (short)( ((dataEvent.data[19+(i*20)]&0xFF)<<8) | (dataEvent.data[18+(i*20)]&0xFF) );
				 noise_aver = (short)( ((dataEvent.data[21+(i*20)]&0xFF)<<8) | (dataEvent.data[20+(i*20)]&0xFF) );
				 noise_min = (short)( ((dataEvent.data[23+(i*20)]&0xFF)<<8) | (dataEvent.data[22+(i*20)]&0xFF) );*/
				 
				 noise_max = (short)( ((dataEvent.data[13+(i*20)]&0xFF)<<8) | (dataEvent.data[12+(i*20)]&0xFF) );
				 noise_aver = (short)( ((dataEvent.data[15+(i*20)]&0xFF)<<8) | (dataEvent.data[14+(i*20)]&0xFF) );
				 noise_min = (short)( ((dataEvent.data[17+(i*20)]&0xFF)<<8) | (dataEvent.data[16+(i*20)]&0xFF) );
				 
				 vib_max = (short)( ((dataEvent.data[19+(i*20)]&0xFF)<<8) | (dataEvent.data[18+(i*20)]&0xFF) );
				 vib_aver = (short)( ((dataEvent.data[21+(i*20)]&0xFF)<<8) | (dataEvent.data[20+(i*20)]&0xFF) );
				 vib_min = (short)( ((dataEvent.data[23+(i*20)]&0xFF)<<8) | (dataEvent.data[22+(i*20)]&0xFF) );
				 
				 
				 /*System.out.println("Data: " + i);
				
				 System.out.println("device_id: " + device_id);
				 System.out.println("command: " + command);
				 System.out.println("battery: " + battery);
				 
				 */
				 System.out.println("vib_min: " + vib_min);
				 System.out.println("vib_aver: " + vib_aver);
				 
				 System.out.println("vib_max: " + vib_max);
				 System.out.println("noise_min: " + noise_min);
				 System.out.println("noise_aver: " + noise_aver);
				 System.out.println("noise_max: " + noise_max);
				 
				 Node node = MongoHandle.getNodeFromNodeId(device_id);
				 if(node !=null)
				 {
					// System.out.println("Node _id: " + node.getId());
					 
					 MongoHandle.updateNodeStatusInHome(node.getId(), "ON");
					 
					 //For sending alarm message
					 
					 Home foundHome = MongoHandle.getHome(node.getId());
					 
					 //System.out.println("Home: " + foundHome.getHomeNumber());
					 
					 if(foundHome != null)
					 {
						 Building building = MongoHandle.getBuildingFromId(foundHome.getBuildingId());
						 if(building != null)
						 {
							 int dangerSoundThreshold = building.getDangerSoundThreshold();
							 int dangerVibThreshold = building.getDangerVibThreshold();
							 
							 int warningSoundThreshold = building.getWarningSoundThreshold();
							 int warningVibThreshold = building.getWarningVibThreshold();
							 
							 double temp_noise_check = (double)(noise_aver/10);
							 double temp_vib_check = (double)(vib_aver/10);
							 if(temp_noise_check >= dangerSoundThreshold && temp_vib_check >= dangerVibThreshold)
							 {
							 
								 // checking the home which making noise
								 
								 //Getting upper Home
								 int checkedHomeNumber = foundHome.getHomeNumber() - 100;
								 
								 System.out.println("Checking home for making noise: " + checkedHomeNumber);
								 System.out.println("Building Id: " + building.getId());
								 
								 Home checkedHome = MongoHandle.getHomeByHomeNumber_Building(checkedHomeNumber, building.getId());
								 //System.out.println("checked_Home: " + checkedHome.getBuildingId());
								 if(checkedHome != null)
								 {
									 if(checkedHome.getSound() >= warningSoundThreshold && checkedHome.getVibration() >= warningVibThreshold)
									 
									 {
										 // This home is the source of making noise
										 //sending alarm message to this home
										 Node node_checked_home = MongoHandle.getNodeFromHome(checkedHome);
										 
										 short nodeId_checked_home = (short)node_checked_home.getNodeNumber();
										 
										 
										 //if(device_id == 260 || device_id == 261 || device_id == 261 || device_id == 263)
										 {
											 System.out.println("Sending Alarm message to node: " + nodeId_checked_home );
										 
											 byte[] alarm_message = new byte[24];
											 
											 short send_check_no = 4660;
											 alarm_message[0] = (byte)(send_check_no & 0x00FF) ;
											 alarm_message[1] = (byte)((send_check_no & 0xFF00) >> 8) ;
											 
											 //Device_Id
											 alarm_message[2] = (byte) (device_id & 0x00FF) ;
											 alarm_message[3] = (byte)((device_id & 0xFF00) >> 8) ;		
											 
											 alarm_message[4] = 3;
											 
											 alarm_message[5] = 2;
											 
											 //Time
											 alarm_message[6] = 0x00;
											 alarm_message[7] = 0x00;
											 alarm_message[8] = 0x00;
											 alarm_message[9] = 0x00;
											 alarm_message[10] = 0x00;
											 alarm_message[11] = 0x00;
											 alarm_message[12] = 0x00;
											 alarm_message[13] = 0x00;
											 alarm_message[14] = 0x00;
											 alarm_message[15] = 0x00;
											 
											 //Reserve
											 alarm_message[16] = 0x00;
											 alarm_message[17] = 0x00;
											 alarm_message[18] = 0x00;
											 alarm_message[19] = 0x00;
											 alarm_message[20] = 0x00;
											 alarm_message[21] = 0x00;
											 
											 //Check
											 alarm_message[22] = (byte)(send_check_no & 0x00FF) ;
											 alarm_message[23] = (byte)((send_check_no & 0xFF00) >> 8) ;
											 
											 serverSocket.send(socketChannel, alarm_message);
										 }
										 
									 }
								 }
							 }
						 }
					 }
				 }
				 double temp_noise_aver = ((double)noise_aver/10);
				 double temp_vib_aver = ((double)vib_aver/10);
				 double temp_noise_min = ((double)noise_min/10);
				 double temp_vib_min = ((double)vib_min/10);
				 double temp_noise_max =((double)noise_max/10);
				 double temp_vib_max = ((double)vib_max/10);
				 
				/* System.out.println("temp_noise_aver: " + temp_noise_aver);
				 System.out.println("temp_vib_aver: " + temp_vib_aver);
				 
				 System.out.println("temp_noise_min: " + temp_noise_min);
				 System.out.println("temp_vib_min: " + temp_vib_min);
				 System.out.println("temp_noise_max: " + temp_noise_max);
				 System.out.println("temp_vib_max: " + temp_vib_max);*/
				 
				 
				 //Room room = new Room (device_id,noise_aver,vib_aver,battery,noise_min,vib_min,noise_max,vib_max);
				 Room room = new Room (device_id,temp_noise_aver,temp_vib_aver,battery,temp_noise_min,temp_vib_min,temp_noise_max,temp_vib_max);
				 WebSocketService.pushSensorDataService(room);
				 
			 }
			 
			 
			 
			
			 
		 }// while(true)
		 
		 
	}
}