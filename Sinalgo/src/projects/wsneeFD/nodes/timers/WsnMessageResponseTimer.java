package projects.wsneeFD.nodes.timers;

import projects.wsneeFD.nodes.messages.WsnMsgResponse;
import projects.wsneeFD.nodes.nodeImplementations.SimpleNode;
import projects.wsneeFD.nodes.nodeImplementations.SinkNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;

public class WsnMessageResponseTimer extends Timer {
	
	private WsnMsgResponse message = null;
	private Node proximoNoAteEstacaoBase = null;
	
	public WsnMessageResponseTimer(WsnMsgResponse message, Node pNAEB)
	{
		this.message = message;
		this.proximoNoAteEstacaoBase = pNAEB;
	}

	@Override
	public void fire() {
		// Metodo para disparar o temporizador (Timer)
		if (SinkNode.rPPMIntraNode && !((SimpleNode)this.proximoNoAteEstacaoBase).rPPMIntraNodeLocal){ // If rPPMIntraNode mode is ON and this node (that call this method) still didn't calculate the rPPM, then active the flag (rPPMIntraNodeLocal) for do it. 
			((SimpleNode)this.proximoNoAteEstacaoBase).rPPMIntraNodeLocal = true;
		}
		((SimpleNode)node).send(message, proximoNoAteEstacaoBase);
	}

}
