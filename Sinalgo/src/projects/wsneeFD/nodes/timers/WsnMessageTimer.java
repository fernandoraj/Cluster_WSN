package projects.wsneeFD.nodes.timers;

import projects.wsneeFD.nodes.messages.WsnMsg;
import projects.wsneeFD.nodes.nodeImplementations.SimpleNode;
import sinalgo.nodes.Node;
import sinalgo.nodes.timers.Timer;

public class WsnMessageTimer extends Timer {
	
	private WsnMsg message = null;
	private Node nextNode = null;
	
	public WsnMessageTimer(WsnMsg message)
	{
		this.message = message;
	}

	public WsnMessageTimer(WsnMsg message, Node nextNode)
	{
		this.message = message;
		this.nextNode = nextNode;
	}
	
	@Override
	public void fire()
	{
		if (nextNode != null)
		{
			((SimpleNode)node).send(message, nextNode);
		}
		else
		{
			// Metodo para disparar o temporizador (Timer)
			((SimpleNode)node).broadcast(message);
		}
	}

}
