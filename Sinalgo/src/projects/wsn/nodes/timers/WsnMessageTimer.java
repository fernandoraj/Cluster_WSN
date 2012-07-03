package projects.wsn.nodes.timers;

import sinalgo.nodes.timers.Timer;
import projects.wsn.nodes.messages.WsnMsg;
import projects.wsn.nodes.nodeImplementations.*;

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
