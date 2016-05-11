package ubicom.org.model;

import org.springframework.data.mongodb.core.mapping.Document;


public class Room {
	
	
	private int nodeId;
	private double sound;
	private double vibration;
	private double nodeBattery;
	private double minSound; 
	private double minVibration; 
	private double maxSound; 
	private double maxVibration; 
	
	
	public Room(int nodeId, double sound,double vibration, double battery,
			double minSound, double minVibration, double maxSound, double maxVibration)
	{
		this.nodeId = nodeId;
		this.sound = sound;
		this.vibration = vibration;
		this.nodeBattery = battery;
		this.minSound = minSound;
		this.minVibration = minVibration;
		this.maxSound = maxSound;
		this.maxVibration = maxVibration;
	}
	
	
	public int getNodeId()
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
}
