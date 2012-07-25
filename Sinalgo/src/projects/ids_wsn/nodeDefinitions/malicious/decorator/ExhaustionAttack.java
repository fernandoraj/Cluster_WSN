package projects.ids_wsn.nodeDefinitions.malicious.decorator;
import projects.ids_wsn.nodeDefinitions.malicious.IAttack;
import projects.ids_wsn.nodes.nodeImplementations.ExhaustionNode;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;

public class ExhaustionAttack extends AttackDecorator implements IAttack {

	private Integer numberOfMessagesToSend = 1;
	private Integer timerToSendMessages = 1;
	private ExhaustionNode malNode = null;
	
	public ExhaustionAttack(IAttack attack) {
		super(attack);
		
		try {
			String number = Configuration.getStringParameter("Attack/Exhaustion/Quantity");
			numberOfMessagesToSend = Integer.valueOf(number);
		} catch (CorruptConfigurationEntryException e) {
			Tools.appendToOutput("Attack/Exhaustion/Quantity Key Not Found");
			e.printStackTrace();
		}
		
		try {
			String number = Configuration.getStringParameter("Attack/Exhaustion/Timer");
			timerToSendMessages = Integer.valueOf(number);
		} catch (CorruptConfigurationEntryException e) {
			Tools.appendToOutput("Attack/Exhaustion/Timer Key Not Found");
			e.printStackTrace();
		}
	}
	
	@Override
	public void doAttack(Node node, Message message) {
		malNode = (ExhaustionNode) node;
		performRepetitionAttack(node, message);
		super.doAttack(node, message);
	}
	
	private void performRepetitionAttack(Node node, Message message) {
		if ((Tools.getGlobalTime()+node.ID) % 200 == 0){
			Integer timer = timerToSendMessages;
			for (int x=0; x<numberOfMessagesToSend; x++){
				malNode.getRouting().sendMessageWithTimer(10, timer);					
				timer = timer + timerToSendMessages; 
			}
		}
		
	}

	
	

}
