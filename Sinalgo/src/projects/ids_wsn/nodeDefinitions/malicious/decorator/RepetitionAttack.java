package projects.ids_wsn.nodeDefinitions.malicious.decorator;
import projects.ids_wsn.nodeDefinitions.malicious.IAttack;
import projects.ids_wsn.nodes.messages.PayloadMsg;
import projects.ids_wsn.nodes.nodeImplementations.MaliciousNode;
import projects.ids_wsn.nodes.timers.RepetitionTimer;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

public class RepetitionAttack extends AttackDecorator implements IAttack {
	
	//How many packts the node will replay
	private Integer maxReplayMsg;
	
	

	public RepetitionAttack(IAttack attack) {
		super(attack);
		try {
			maxReplayMsg = Configuration.getIntegerParameter("Attack/Repetition/Quantity");
		} catch (CorruptConfigurationEntryException e) {
			Tools.appendToOutput("Key Attack/Repetition/Quantity not found");
			e.printStackTrace();
		}
		
	}

	public void doAttack(Node node, Message message) {
		performRepetitionAttack(node, message);
		super.doAttack(node, message);
		
	}

	private void performRepetitionAttack(Node node, Message message) {
		if (message instanceof PayloadMsg){
			PayloadMsg payloadMsg = (PayloadMsg) message;
			MaliciousNode malNode = (MaliciousNode) node;
			
			//The malicious node will repeat only the messages which he is the next Hop.
			if (malNode.isNodeNextHop(payloadMsg.nextHop)){			
				if (malNode.getCurrentAttacks() < maxReplayMsg){
					RepetitionTimer repTimer = new RepetitionTimer(payloadMsg,40);
					repTimer.startRelative(1, node);
					malNode.setCurrentAttacks(malNode.getCurrentAttacks()+1);
				}
				
			}
		}
		
	}

}
