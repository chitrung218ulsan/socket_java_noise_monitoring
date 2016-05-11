/**
 * 
 */
package ubicom.org.MongoHandle;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericXmlApplicationContext;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import ubicom.org.model.Home;
import ubicom.org.model.Room;
import ubicom.org.model.Node;
import ubicom.org.model.Building;
import com.mongodb.MongoException;

/**
 * @author ubicom
 *
 */
public class MongoHandle {

	private static final ApplicationContext ctx = new GenericXmlApplicationContext("SpringConfig.xml");
	private static final MongoOperations mongoOperation = (MongoOperations)ctx.getBean("mongoTemplate");
	
	
	public static Home getHome(String nodeId)
	{
		Query searchHome = new Query(Criteria.where("nodeId").is(nodeId).and("isDeleted").is(false));
		Home home = null;
		try{
			
		    home = mongoOperation.findOne(searchHome,Home.class);
			
			if(home != null){
				
				return home;
			}
			else
				return null;
		}
		catch(MongoException e){
			
			e.printStackTrace();
		}
		return home;
		
	}
	/*
	 * 
	 */
	public static Home getHomeByHomeNumber_Building(int homeNumber, String buildingId)
	{
		Query foundHome = new Query(Criteria.where("homeNumber").is(homeNumber).and("buildingId").is(buildingId).and("isDeleted").is(false));
		Home home = null;
		try{
			
		    home = mongoOperation.findOne(foundHome,Home.class);
			
			if(home != null && home.isDeleted() == false){
				
				return home;
			}
			else
				return null;
		}
		catch(MongoException e){
			
			e.printStackTrace();
		}
		return home;
	}
	/*
	 * 
	 */
	public static List<Node> getListOfNode()
	{
		List<Node> listNode = null;
		Query query = new Query();
		query.with(new Sort(Sort.Direction.ASC, "nodeNumber"));
		try{
			
			listNode = mongoOperation.find(query,Node.class);
			return listNode;
		}
		catch(MongoException e)
		{
			e.printStackTrace();
		}
		return listNode;
	}
	public static void updateNodeStatusInHome(String nodeId, String status)
	{
		Query searchHome = new Query(Criteria.where("nodeId").is(nodeId).and("isDeleted").is(false));
		
		try{
			mongoOperation.updateFirst(searchHome, Update.update("commStatus", status), Home.class);
		}
		catch(MongoException e){
			
			e.printStackTrace();
		}
	}
	
	/*
	 * Function: Getting Node from its id
	 */
	public static Node getNodeFromNodeId(int nodeNumber){
		Query searchNode = new Query(Criteria.where("nodeNumber").is(nodeNumber).and("isDeleted").is(false));
		Node node = null;
		try{
			
			node = mongoOperation.findOne(searchNode,Node.class);
			return node;
		}
		catch(MongoException e)
		{
			e.printStackTrace();
		}
		return node;
	}
	
	/*
	 * 
	 */
	public static Building getBuildingFromId(String buildingId)
	{
		Building foundBuilding = null;
		Query searchBuilding = new Query(Criteria.where("_id").is(buildingId).and("isDeleted").is(false));
		try{
			
			foundBuilding = mongoOperation.findOne(searchBuilding,Building.class);
			return foundBuilding;
		}
		catch(MongoException e)
		{
			e.printStackTrace();
		}
		
		return foundBuilding;
	}
	/*
	 * 
	 * 
	 */
	
	public static Node getNodeFromHome(Home home)
	{
		Query searchNode = new Query(Criteria.where("_id").is(home.getNodeId()).and("isDeleted").is(false));
		Node node = null;
		try{
			
			node = mongoOperation.findOne(searchNode,Node.class);
			return node;
		}
		catch(MongoException e)
		{
			e.printStackTrace();
		}
		return node;
		
	}
}
