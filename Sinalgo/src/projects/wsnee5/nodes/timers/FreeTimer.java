package projects.wsnee5.nodes.timers;

import projects.wsnee5.nodes.nodeImplementations.SinkNode;
import sinalgo.nodes.timers.Timer;

public class FreeTimer extends Timer {

	
	@Override
	public void fire() {
		((SinkNode)node).setNextNodesToNull();
	}

}
