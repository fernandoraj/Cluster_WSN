package projects.wsnSccs.nodes.timers;

import sinalgo.nodes.timers.Timer;
import projects.wsnSccs.nodes.messages.WsnMsg;
import projects.wsnSccs.nodes.nodeImplementations.*;

public class WsnMessageTimer extends Timer {
	
	private WsnMsg message = null;
	
	public WsnMessageTimer(WsnMsg message){
		this.message = message;
	}

	@Override
	public void fire() {
		// Metodo para disparar o temporizador (Timer)
		((SimpleNode)node).broadcast(message);
	}

}
