package projects.ids_wsn.nodes.messages;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

/**
 * A message used to transport data from a sender to a receiver
 */
public class MonitorMsg extends PayloadMsg {
	
	public Node baseStation; // BaseStation: node who should receive this msg
	public Node nextHop;
	public Node imediateSender;
	public Node sender; // The node who has crated the msg
	public int sequenceNumber; // a number to identify this msg, set by the sender 
	//public RetryPayloadMessageTimer ackTimer; // The timer set on the sender that will fire if there is no ACK returning from the destination 
	public Integer value = 0;
	public Integer ttl;
	
	public Integer keyUpdateSegment;
	public Integer packetAuthenticationSegment;
	
	/**
	 * The immediate Source of the message.
	 */
	public Node immediateSource;
	
	
	public MonitorMsg(Node destination, Node sender) {
		super(destination, sender);
		keyUpdateSegment = 0;
		packetAuthenticationSegment = 0;
	}
		
	@Override
	public boolean equals(Object obj) {
		MonitorMsg msg = (MonitorMsg) obj;
		
		if (msg.baseStation == null){
			if ((msg.sender.equals(this.sender)) && (msg.value.equals(this.value))){
				return true;
			}else{
				return false;
			}			
		}else if ((msg.baseStation.equals(this.baseStation)) && (msg.sender.equals(this.sender)) &&
				(msg.value.equals(this.value))){
				return true;
			}else{
				return false;
		}
	}
	
	/**
	 * Constructor
	 * If nextHop is null then the message shall be received by all the neigboring nodes
	 * @param destination The node to send this msg to
	 * @param sender The sender who sends this msg
	 */
	public MonitorMsg(Node destination, Node sender, Node nextHop, Node immediateSender) {
		super(destination, sender, nextHop, immediateSender);
		keyUpdateSegment = 0;
		packetAuthenticationSegment = 0;
	}
	
	@Override
	public Message clone() {
		MonitorMsg newMessage = new MonitorMsg(this.baseStation, this.sender, this.nextHop, this.imediateSender);
		newMessage.sequenceNumber = this.sequenceNumber;
		newMessage.value = this.value;
		newMessage.ttl = this.ttl;
		newMessage.immediateSource = this.immediateSource;
		newMessage.keyUpdateSegment = this.keyUpdateSegment;
		newMessage.packetAuthenticationSegment = this.packetAuthenticationSegment;
		
		return newMessage;
	}
	
	@Override
	public String toString() {
		return "Packet: Seq. Number: "+sequenceNumber+" - Sender: "+sender+" - Next hop: "+nextHop;
	}

}
