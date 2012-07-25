package projects.ids_wsn.nodes.messages;

import projects.ids_wsn.nodeDefinitions.malicious.Attacks;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class LocalInferenceMessage extends Message {

	private Node node;
	private Integer idMessage;
	private Node nodeSource;
	private Attacks attack;
	
	public LocalInferenceMessage() {}
	
	public LocalInferenceMessage(Node node, Integer idMessage, Node nodeSource, Attacks attack){
		this.node = node;
		this.idMessage = idMessage;
		this.nodeSource = nodeSource;
		this.attack = attack;
	}
	
	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public Integer getIdMessage() {
		return idMessage;
	}

	public void setIdMessage(Integer idMessage) {
		this.idMessage = idMessage;
	}

	public Node getNodeSource() {
		return nodeSource;
	}

	public void setNodeSource(Node nodeSource) {
		this.nodeSource = nodeSource;
	}

	public Attacks getAttack() {
		return attack;
	}

	public void setAttack(Attacks attack) {
		this.attack = attack;
	}

	@Override
	public Message clone() {
		LocalInferenceMessage newMessage = new LocalInferenceMessage(node, idMessage, nodeSource, attack);
		return newMessage;
	}

}
