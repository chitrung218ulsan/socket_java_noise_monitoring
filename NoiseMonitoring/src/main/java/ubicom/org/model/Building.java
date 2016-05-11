package ubicom.org.model;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="Building")
public class Building {
	@Id
	private String _id;
	
	private String apartmentId;
	private String name;
	private String type;
	private String representative;
	private String manager;
	private int numOfFloors;
	private int numHousePerFloor;
	private int warningSoundThreshold;
	private int dangerSoundThreshold;
	private int warningVibThreshold;
	private int dangerVibThreshold;
	private String remarks;
	private String createdBy;
	private boolean isDeleted;
	
	public Building(String _id, String apartmentId, String name, String type, String representative, String manager, 
			int numOfFloors, int numHousePerFloor, int warningSoundThreshold, int dangerSoundThreshold, int warningVibThreshold,
			int dangerVibThreshold, String remarks,String createdBy,boolean isDeleted)
	{
		this._id = _id;
		this.apartmentId = apartmentId;
		this.name = name;
		this.type = type;
		this.representative = representative;
		this.manager = manager;
		this.numOfFloors = numOfFloors;
		this.numHousePerFloor = numHousePerFloor;
		this.warningSoundThreshold = warningSoundThreshold;
		this.dangerSoundThreshold = dangerSoundThreshold;
		this.warningVibThreshold = warningVibThreshold;
		this.dangerVibThreshold = dangerVibThreshold;
		this.remarks = remarks;
		this.createdBy = createdBy;
		this.isDeleted = isDeleted;
		
	}
	
	public String getId() {
		return this._id;
	}
	public String getApartmentId(){
		return this.apartmentId;
	}
	public String getName(){
		return this.name;
	}
	public String getType(){
		return this.type;
	}
	public String getRepresentative(){
		return this.representative;
	}
	public String getManager(){
		return this.manager;
	}
	public int getNumOfFloors(){
		return this.numOfFloors;
	}
	public int getNumHousePerFloor(){
		return this.numHousePerFloor;
	}
	public int getWarningSoundThreshold(){
		return this.warningSoundThreshold;
	}
	public int getDangerSoundThreshold(){
		return this.dangerSoundThreshold;
	}
	public int getWarningVibThreshold(){
		return this.warningVibThreshold;
	}
	public int getDangerVibThreshold(){
		return this.dangerVibThreshold;
	}
	public String getRemarks(){
		return this.remarks;
	}
	public String getCreatedBy(){
		return this.createdBy;
	}
	public boolean getIsDeleted(){
		return this.isDeleted;
	}
}
