package projects.ids_wsn.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class BeaconMessage extends Message {

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
	 * Number of hops to BaseStation 
	 */
	public int hopsToBaseStation;
	
	/**
	 * Sequence ID of this message 
	 */
	public int sequenceID; 
	
	/**
	 * The lowest node energy collected during the packet routing
	 */
	public float energy;
	
	/**
	 * Default constructor. 
	 */
	public BeaconMessage(int seqID, Node baseStation, Node immediateSource, Node forwardingNode, Node source) {
		ttl = 3; // initial TTL
		hopsToBaseStation = 0;
		sequenceID = seqID;
		this.baseStation = baseStation;
		this.forwardingNode = forwardingNode;
		this.immediateSource = immediateSource;
		this.source = source;
	}
	
	@Override
	public Message clone() {
		BeaconMessage msg = new BeaconMessage(this.sequenceID, this.baseStation, this.immediateSource, this.forwardingNode, this.source);
		msg.ttl = this.ttl;
		msg.hopsToBaseStation = this.hopsToBaseStation;
		msg.forwardingNode = this.forwardingNode;
		msg.energy = this.energy;
		return msg;
	}
	
	@Override
	public String toString() {
		return "SeqID: "+this.sequenceID+" /BS: "+this.baseStation+" / FwdNode: "+this.forwardingNode+" / iSource: "+this.immediateSource;
	}
}