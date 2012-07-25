package projects.ids_wsn.nodes.messages;

import java.util.ArrayList;
import java.util.List;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

/**
 * The message used to determine a route between two nodes using
 * incremental flooding.
 * This message requires the read-only policy. 
 */
public class FloodFindFuzzy extends Message {

	/**
	 * The TTL for this message when it's being sent as a find-msg
	 */
	public int ttl;
	
	/**
	 * The Reference to the BaseStation
	 */
	public Node baseStation;
	
	/**
	 * The node who is forwarding the flood message
	 */
	public Node forwardingNode;
	
	/**
	 * The immediate Source of the message.
	 */
	public Node immediateSource;
	
	/**
	 * The source of the message
	 */
	public Node source;
	
	/**
	 * The immediate Destination of the message.
	 */
	public Node immediateDestination;
	
	
	/**
	 * Number of hops to BaseStation 
	 */
	public int hopsToBaseStation;
	
	/**
	 * Index Sequence ID of this message 
	 */
	public int sequenceID; 
	
	/**
	 * Index of the  route
	 */
	public int index;
	
	/**
	 * BasicStation GlobalID. Necessary when the Basic wants to send a new broadcast
	 */
	public int broadcastID;
	
	/**
	 * The lowest node energy collected during the packet routing
	 */
	public float energy;
	
	/**
	 * The list of Nodes that the sender users as routes. We will use this to avoid that a node A uses, as route, a node B 
	 * that already uses A as route 
	 */
	public List<Integer> idNodesRoutes;
	
	/**
	 * Default constructor. 
	 */
	public FloodFindFuzzy(int seqID, Node baseStation, Node immediateSource, Node forwardingNode, Node source, Integer index, Node dst, int broadcastID) {
		ttl = 20; // initial TTL
		hopsToBaseStation = 0;
		sequenceID = seqID;
		this.baseStation = baseStation;
		this.forwardingNode = forwardingNode;
		this.immediateSource = immediateSource;
		this.source = source;
		this.index = index;
		this.broadcastID = broadcastID;
		this.immediateDestination = dst;
		this.idNodesRoutes = new ArrayList<Integer>();
	}
	
	@Override
	public Message clone() {
		FloodFindFuzzy msg = new FloodFindFuzzy(this.sequenceID, this.baseStation, this.immediateSource, this.forwardingNode, this.source, this.index, this.immediateDestination, this.broadcastID);
		msg.ttl = this.ttl;
		msg.hopsToBaseStation = this.hopsToBaseStation;
		msg.forwardingNode = this.forwardingNode;
		msg.energy = this.energy;
		msg.idNodesRoutes = this.idNodesRoutes;
		return msg;
	}
	
	@Override
	public String toString() {
		return "SeqID: "+this.sequenceID+" /BS: "+this.baseStation+" / FwdNode: "+this.forwardingNode+" / iSource: "+this.immediateSource;
	}
}