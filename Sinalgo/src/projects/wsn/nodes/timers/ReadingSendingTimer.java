package projects.wsn.nodes.timers;

import projects.wsn.nodes.nodeImplementations.SimpleNode;
import sinalgo.nodes.timers.Timer;

public class ReadingSendingTimer extends Timer {

	private String dataType;
	
	public ReadingSendingTimer(String dataSensedType)
	{
		this.dataType = dataSensedType;
	}
	
	@Override
	public void fire() {
		((SimpleNode)node).makeSensorReadingAndSendingLoop(dataType);
	}

}
