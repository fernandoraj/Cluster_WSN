package projects.ids_wsn.nodes.timers;
import sinalgo.nodes.messages.Message;
import sinalgo.nodes.timers.Timer;

/**
 * A timer to initially send a message. This timer
 * is used in synchronous simulation mode to handle user
 * input while the simulation is not running. 
 */
public class BaseStationMessageTimer extends Timer {
	private Message msg = null; // the msg to send

	/**
	 * @param msg The message to send
	 */
	public BaseStationMessageTimer(Message msg) {
		this.msg = msg;
	}
	
	@Override
	public void fire() {		
		node.broadcast(msg);
	}

}
