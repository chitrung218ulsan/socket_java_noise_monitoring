package ubicom.org.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

@Document(collection="Home")
public class Home {

	@Id
	private String id;
	private String buildingId;
	private int floor;
	private int homeNumber;
	private String name;
	private String telNumber;
	private boolean isDeleted;
	
	private String nodeId;
	private int sound; //average
	private int vibration; //average
	private int minSound; 
	private int minVibration; 
	private int maxSound; 
	private int maxVibration; 
	private int nodeBattery;
	
	@DateTimeFormat(iso=ISO.DATE_TIME)
	private Date lastDataUpdate;
	
	public Home(String nodeId, String buildingId, int sound,int vibration, int nodeBattery,int minSound, int minVibration,
			int maxSound, int maxVibration)
	{
		this.buildingId = buildingId;
		this.nodeId = nodeId;
		this.sound = sound;
		this.vibration = vibration;
		this.nodeBattery = nodeBattery;
		this.minSound = minSound;
		this.minVibration = minVibration;
		this.maxSound = maxSound;
		this.maxVibration = maxVibration;
	}
	public String getId(){
		
		return id;
	}
public String getBuildingId(){
		
		return buildingId;
	}
	public int getFloor(){
		
		return floor;
	}
	public int getHomeNumber(){
		
		return homeNumber;
	}
	public String getName(){
		
		return name;
	}
	public String getTelNumber(){
		
		return telNumber;
	}
	public String getNodeId()
	{
		return nodeId;
	}
	public double getSound()
	{
		return sound;
	}
	public double getVibration()
	{
		return vibration;
	}
	public double getbattery()
	{
		return nodeBattery;
	}
	public double getMinSound()
	{
		return minSound;
	}
	public double getMinVibration(){
		
		return minVibration;
	}
	public double getMaxSound()
	{
		return maxSound;
		
	}
	public double getMaxVibration(){
		
		return maxVibration;
	}
	public Date getLastUpdateData(){
		
		return lastDataUpdate;
	}
	public boolean isDeleted(){
		return isDeleted;
	}
}
