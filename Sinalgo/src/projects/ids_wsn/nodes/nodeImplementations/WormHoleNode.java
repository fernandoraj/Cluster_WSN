package projects.ids_wsn.nodes.nodeImplementations;

import java.awt.Color;

import projects.ids_wsn.nodeDefinitions.BasicNode;
import projects.ids_wsn.nodeDefinitions.malicious.IAttack;
import projects.ids_wsn.nodeDefinitions.malicious.decorator.WormHoleAttack;
import projects.ids_wsn.nodes.messages.PayloadMsg;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;

public class WormHoleNode extends BasicNode implements IAttack {

	@Override
	public void init() {
		setMyColor(Color.DARK_GRAY);
		this.setRadioIntensity(100.0);
		
		super.init();
	}
	
	public void doAttack(Node node, Message message) {}
	
	
	
	@Override
	public Boolean beforeSendingMessage(Message message) {
		
		if (message instanceof PayloadMsg){
			IAttack attack = new WormHoleAttack(this);
			attack.doAttack(this, message);
		
			return Boolean.FALSE;
		}else{
			return Boolean.TRUE;
		}
			
	}

}
