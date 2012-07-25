package projects.ids_wsn.nodes.timers;
import projects.ids_wsn.nodes.nodeImplementations.BaseStation;
import sinalgo.nodes.timers.Timer;

/**
 * A timer to initially send a message. This timer
 * is used in synchronous simulation mode to handle user
 * input while the simulation is not running. 
 */
public class BaseStationRepeatMessageTimer extends Timer {
	private Integer interval;

	/**
	 * @param msg The message to send
	 */
	public BaseStationRepeatMessageTimer(Integer interval) {
		this.interval = interval;
	}
	
	@Override
	public void fire() {		
		BaseStation bs = (BaseStation) node;
		bs.prepareSendRouteMessage();
		if (interval > 0){			
			this.startRelative(interval, node);
		}
	}

}
