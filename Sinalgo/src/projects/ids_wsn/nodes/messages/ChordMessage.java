package projects.ids_wsn.nodes.messages;

import projects.ids_wsn.enumerators.ChordMessageType;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class ChordMessage extends Message{

	private Integer sequenceID;
	private Node sender;
	private Node target;
	private Node immediateSender;
	private ChordMessageType chordMessageType;
	
	public ChordMessage(){
		
	}
	
	public ChordMessage(Node sender, ChordMessageType chordMessageType, Integer sequenceID) {
		this(sender, null, chordMessageType, sequenceID);
	}
	
	public ChordMessage(Node sender, Node target, ChordMessageType chordMessageType, Integer sequenceID){
		this.sequenceID = sequenceID;
		this.sender = sender;
		this.immediateSender = sender;
		this.target = target;
		this.chordMessageType = chordMessageType;
	}

	public ChordMessageType getChordMessageType() {
		return chordMessageType;
	}
	
	public void setChordMessageType(ChordMessageType chordMessageType) {
		this.chordMessageType = chordMessageType;
	}

	public Node getSender() {
		return sender;
	}
	
	public void setSender(Node sender) {
		this.sender = sender;
	}
	
	public Node getTarget() {
		return target;
	}
	
	public void setTarget(Node target) {
		this.target = target;
	}

	public Node getImmediateSender() {
		return immediateSender;
	}
	
	public void setImmediateSender(Node immediateSender) {
		this.immediateSender = immediateSender;
	}
	
	public Integer getSequenceID() {
		return sequenceID;
	}
	
	public void setSequenceID(Integer sequenceID) {
		this.sequenceID = sequenceID;
	}

	@Override
	public Message clone() {
		return new ChordMessage(this.sender, this.target, this.chordMessageType, this.sequenceID);
	}
}
