package ubicom.org.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="Node")
public class Node {
	@Id
	private String _id;
	private String name;
	private int nodeNumber;
	private boolean isDeleted;
	public Node(String name, int nodeNumber)
	{
		this.name = name;
		this.nodeNumber = nodeNumber;
		
	}
	
	public String getId()
	{
		return _id;
	}
	public String getName(){
		return name;
	}
	public int getNodeNumber(){
		return nodeNumber;
	}
	public boolean isDeleted(){
		
		return isDeleted;
	}
}
