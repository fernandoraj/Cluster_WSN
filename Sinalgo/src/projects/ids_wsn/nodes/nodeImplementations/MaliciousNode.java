package projects.ids_wsn.nodes.nodeImplementations;

import java.awt.Color;

import projects.ids_wsn.nodeDefinitions.BasicNode;
import projects.ids_wsn.nodeDefinitions.malicious.IAttack;
import projects.ids_wsn.nodeDefinitions.malicious.decorator.RepetitionAttack;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class MaliciousNode extends BasicNode implements IAttack {

	//How many packtes the node is current replaying
	protected Integer currentAttacks;
	
	@Override
	public void init() {
		currentAttacks = 0;
		setMyColor(Color.DARK_GRAY);
		super.init();
	}
	
	@Override
	protected void postProcessingMessage(Message message) {
		super.postProcessingMessage(message);
		
		IAttack attack1 = new RepetitionAttack(this);
		attack1.doAttack(this, message);
	}
	
	public void doAttack(Node node, Message message) {
		
	}
	public void setCurrentAttacks(Integer currentAttacks) {
		this.currentAttacks = currentAttacks;
	}
	public Integer getCurrentAttacks() {
		return currentAttacks;
	}

}
