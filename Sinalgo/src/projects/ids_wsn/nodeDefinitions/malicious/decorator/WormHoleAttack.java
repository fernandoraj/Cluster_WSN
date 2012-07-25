package projects.ids_wsn.nodeDefinitions.malicious.decorator;

import projects.ids_wsn.nodeDefinitions.BasicNode;
import projects.ids_wsn.nodeDefinitions.malicious.IAttack;
import projects.ids_wsn.nodes.messages.PayloadMsg;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

public class WormHoleAttack extends AttackDecorator implements IAttack {

	public WormHoleAttack(IAttack attack) {
		super(attack);
	}
	
	@Override
	public void doAttack(Node node, Message message) {
		peformWormHoleAttack(node, message);
		super.doAttack(node, message);
	}

	private void peformWormHoleAttack(Node node, Message message) {
		
		if (message instanceof PayloadMsg){
			PayloadMsg msg = (PayloadMsg) message.clone();
			//Node n = Tools.getRandomNode();
			Node n = Tools.getNodeByID(41);
			
			if ( (n instanceof BasicNode) && (n.ID != node.ID) && (n.ID != msg.sender.ID)){
				msg.nextHop = n;
				msg.imediateSender = node;
				node.sendDirect(msg, n);
				System.out.println("Node "+node.ID+": enviando pacote para o nó "+n.ID);
			}
			
			
		}
		
		
	}

}
