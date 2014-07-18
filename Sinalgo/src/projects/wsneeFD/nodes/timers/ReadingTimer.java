package projects.wsneeFD.nodes.timers;

import projects.wsneeFD.nodes.messages.WsnMsgResponse;
import projects.wsneeFD.nodes.nodeImplementations.SimpleNode;
import sinalgo.nodes.timers.Timer;

public class ReadingTimer extends Timer {

	private WsnMsgResponse wsnMsgResp;
	private Integer sizeTimeSlot;
	private int[] dataSensedTypes;
	
	public ReadingTimer(WsnMsgResponse wsnMsgResp, Integer sizeTimeSlot, int[] dataSensedTypes)
	{
		this.wsnMsgResp = wsnMsgResp;
		this.sizeTimeSlot = sizeTimeSlot;
		this.dataSensedTypes = dataSensedTypes;
	}
	
	@Override
	public void fire() {
		((SimpleNode)node).triggerReading(wsnMsgResp, sizeTimeSlot, dataSensedTypes);
	}

}
