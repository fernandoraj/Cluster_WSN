package projects.ids_wsn.nodeDefinitions.malicious;

import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public interface IAttack {
	
	public void doAttack(Node node, Message message);
}
