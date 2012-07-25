package projects.ids_wsn.nodes.nodeImplementations;

import java.awt.Color;

import projects.ids_wsn.nodeDefinitions.BasicNode;
import projects.ids_wsn.nodeDefinitions.malicious.IAttack;
import projects.ids_wsn.nodeDefinitions.malicious.decorator.ExhaustionAttack;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class ExhaustionNode extends BasicNode implements IAttack {
	
	@Override
	public void init() {
		setMyColor(Color.DARK_GRAY);
		super.init();
	}
	
	@Override
	public void postStep() {
		IAttack attack = new ExhaustionAttack(this);
		attack.doAttack(this, null);
	}
	
	public void doAttack(Node node, Message message) {
		
	}
	
	
}
