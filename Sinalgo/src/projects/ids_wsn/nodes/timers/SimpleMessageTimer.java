package projects.ids_wsn.nodes.timers;
import projects.ids_wsn.nodeDefinitions.BasicNode;
import projects.ids_wsn.nodeDefinitions.energy.EnergyMode;
import sinalgo.nodes.messages.Message;
import sinalgo.nodes.timers.Timer;

/**
 * A timer to initially send a message. This timer
 * is used in synchronous simulation mode to handle user
 * input while the simulation is not running. 
 */
public class SimpleMessageTimer extends Timer {
	Message msg = null; // the msg to send

	/**
	 * @param msg The message to send
	 */
	public SimpleMessageTimer(Message msg) {
		this.msg = msg;
	}
	
	@Override
	public void fire() {
		//System.out.println("send the message");		
		BasicNode basicNode = (BasicNode)node;
		
		
		//If the node is dead, don't do anything
		if (basicNode.getIsDead()){
			return;
		}
		
		if (basicNode.beforeSendingMessage(msg)) {		
			node.broadcast(msg);
		
			basicNode.afterSendingMessage(msg);
			//Spent energy due to the send of the message
			basicNode.getBateria().spend(EnergyMode.SEND);
		}
	}
}
