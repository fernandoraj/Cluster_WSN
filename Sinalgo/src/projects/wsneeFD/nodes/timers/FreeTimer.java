package projects.wsneeFD.nodes.timers;

import projects.wsneeFD.nodes.nodeImplementations.SinkNode;
import sinalgo.nodes.timers.Timer;

public class FreeTimer extends Timer {

	
	@Override
	public void fire() {
		((SinkNode)node).setNextNodesToNull();
	}

}
