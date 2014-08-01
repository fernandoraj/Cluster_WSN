package projects.wsneeFD.nodes.timers;

import projects.wsneeFD.nodes.messages.WsnMsgResponse;
import projects.wsneeFD.nodes.nodeImplementations.SimpleNode;
import sinalgo.nodes.timers.Timer;

public class SelectionTimer extends Timer {

	private WsnMsgResponse wsnMsgResp;

	public SelectionTimer(WsnMsgResponse wsnMsgResp) //(int[] dataSensedTypes, double[] coefsA, double[] coefsB, double[] maxErrors)
	{
		this.wsnMsgResp = wsnMsgResp;
	}
	
	@Override
	public void fire() {
		((SimpleNode)node).triggerSelection(wsnMsgResp); //(dataTypes, As, Bs, errors);
	}

}
