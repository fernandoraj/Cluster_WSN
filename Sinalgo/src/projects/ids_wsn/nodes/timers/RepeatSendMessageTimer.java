package projects.ids_wsn.nodes.timers;

import projects.ids_wsn.nodeDefinitions.BasicNode;
import sinalgo.nodes.timers.Timer;

public class RepeatSendMessageTimer extends Timer {
	private Integer interval;
	
	public RepeatSendMessageTimer(Integer interval){
		this.interval = interval;		
	}

	@Override
	public void fire() {
		BasicNode n = (BasicNode)node;
		n.getRouting().sendMessage(10);
		this.startRelative(interval, n);		
	}

}
