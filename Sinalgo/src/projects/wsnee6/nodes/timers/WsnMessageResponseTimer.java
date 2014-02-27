package projects.wsnee6.nodes.timers;

import projects.wsnee6.nodes.messages.WsnMsgResponse;
import projects.wsnee6.nodes.nodeImplementations.SimpleNode;
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
		((SimpleNode)node).send(message, proximoNoAteEstacaoBase);
	}

}
