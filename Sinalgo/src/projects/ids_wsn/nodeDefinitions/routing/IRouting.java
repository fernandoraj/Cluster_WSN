package projects.ids_wsn.nodeDefinitions.routing;

import projects.ids_wsn.nodeDefinitions.BasicNode;
import projects.ids_wsn.nodes.messages.PayloadMsg;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public interface IRouting {
	public void sendMessage(Integer value);
	public void sendMessage(Message message);
	public void sendMessageWithTimer(Message message, Integer timer);
	public void sendMessageWithTimer(Integer value, Integer timer);
	public void sendBroadcast(Message message);
	public void receiveMessage(Message message);
	public void setNode(BasicNode node);
	public Node getBestRoute(Node destino);
	public void printRoutingTable();
	public void sendChordMessage(PayloadMsg message);
	
	/**
	 * Return the sink node. 
	 * If the network uses multiple sinks, this method
	 * must return the Sink of the best route
	 * 
	 */
	public Node getSinkNode();
	
	/**
	 * This method will check weather the destination Node is the Source Node next hop or not.
	 * 
	 * @param destination Node
	 * @return Boolen
	 */
	public Boolean isNodeNextHop(Node destination);
	
}
