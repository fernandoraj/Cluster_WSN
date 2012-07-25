package projects.ids_wsn.nodeDefinitions.Monitor.decorator;

import java.util.ArrayList;
import java.util.List;

import projects.ids_wsn.nodeDefinitions.Monitor.DataMessage;
import projects.ids_wsn.nodeDefinitions.Monitor.IMonitor;
import projects.ids_wsn.nodeDefinitions.Monitor.Rules;
import projects.ids_wsn.nodeDefinitions.malicious.Attacks;
import projects.ids_wsn.nodes.messages.LocalInferenceMessage;
import sinalgo.nodes.Node;
import sinalgo.tools.Tools;

public class RepetitionRule extends RulesDecorator {

	public RepetitionRule(IMonitor monitor) {
		super(monitor);
	}
	
	@Override
	public void doInference() {
		applyRepetitionRule();				
		super.doInference();
	}
	
	private void applyRepetitionRule(){
		Integer tamBuffer = getDataMessage().size();
		List<Node> listTempNodes = new ArrayList<Node>();
		List<DataMessage> listTempDataMessage = new ArrayList<DataMessage>();
		DataMessage data1;
		DataMessage data2;
		
		for (int x=0; x<tamBuffer-1;x++){
			data1 = getDataMessage().get(x);
			
			if (listTempDataMessage.contains(data1)){
				continue;
			}
			
			for (int y=x+1; y<tamBuffer;y++){
				data2 = getDataMessage().get(y);
				if (data1.equals(data2)){
					listTempDataMessage.add(data1);
					break;
				}
			}	
		}
		listTempNodes = getNodesDontRetransmit(listTempDataMessage);
		setLocalMaliciousList(Rules.REPETITION, listTempNodes);
	}

	/**
	 * Descobrir se dentro das mensagens repetidas, existe alguma que caracteriza apenas
	 * uma retransmissao. Nesse caso, tais nós não pode ser marcados como suspeitos
	 * 
	 * @param listTempDataMessage
	 * @return lista de nós supeitos de realizar ataque de repetição
	 */
	private List<Node> getNodesDontRetransmit(List<DataMessage> listTempDataMessage) {
		Integer tamBuffer = listTempDataMessage.size();
		List<DataMessage> listDataMessage = listTempDataMessage;
		List<LocalInferenceMessage> listMessagesToSupervisor = new ArrayList<LocalInferenceMessage>();
		List<Node> listNodes = new ArrayList<Node>();
		DataMessage data1;
		DataMessage data2;
		
		//Vamos gerar uma mensagem do tipo LocalInferenceMessage para cada mensagem suspeita
		//Depois, iremos retirar as mensagens que foram confirmadas como retransmissão
		for (DataMessage dataMessage : listTempDataMessage){
			LocalInferenceMessage localMessage = new LocalInferenceMessage();
			localMessage.setAttack(Attacks.REPETITION);
			localMessage.setIdMessage(dataMessage.getIdMsg());
			localMessage.setNode(Tools.getNodeByID(dataMessage.getImediateSrc()));
			localMessage.setNodeSource(Tools.getNodeByID(dataMessage.getSource()));
			
			if (! listMessagesToSupervisor.contains(localMessage)){
				listMessagesToSupervisor.add(localMessage);
			}
		}
		
		for (int x=0; x<tamBuffer-1;x++){
			data1 = listTempDataMessage.get(x);
			
			for (int y=x+1; y<tamBuffer;y++){
				data2 = listTempDataMessage.get(y);
				if ((data1.getImediateDst().equals(data2.getImediateSrc())) &&
						(data1.getIdMsg().equals(data2.getIdMsg())) && 
						(data1.getSource().equals(data2.getSource()))){
					
					//Retirar Mensagens que foram confirmadas como retransmissão
					if (listDataMessage.contains(data2)){
						listDataMessage.remove(data2);
					}
					
					LocalInferenceMessage localMessage = new LocalInferenceMessage();
					localMessage.setAttack(Attacks.REPETITION);
					localMessage.setIdMessage(data2.getIdMsg());
					localMessage.setNode(Tools.getNodeByID(data2.getImediateSrc()));
					localMessage.setNodeSource(Tools.getNodeByID(data2.getSource()));
					
					//Retirar Mensagens que foram confirmadas como retransmissão
					if (listMessagesToSupervisor.contains(localMessage)){
						listMessagesToSupervisor.remove(localMessage);
					}
										
				}
			}			
		}
		//Gerando a lista de nós suspeitos locais (sem inferencia)
		for (LocalInferenceMessage localMessage : listMessagesToSupervisor){
			Node node = localMessage.getNode();
			if (!listNodes.contains(node)){
				listNodes.add(node);
			}
		}
		
		return listNodes;
	}

}
