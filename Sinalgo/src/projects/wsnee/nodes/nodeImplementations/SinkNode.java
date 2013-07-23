package projects.wsnee.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;
import java.util.Set;

import projects.wsnee.nodes.messages.WsnMsg;
import projects.wsnee.nodes.messages.WsnMsgResponse;
import projects.wsnee.nodes.timers.WsnMessageTimer;
import projects.wsnee.utils.ArrayList2d;
import projects.wsnee.utils.Utils;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;
import sinalgo.nodes.Node;

public class SinkNode extends SimpleNode 
{

	/**
	 * Número de dados sensoriados por time slot (Tamanho do time slot)
	 * Number of sensed data per time slot (time slot size)
	 */
	private Integer sizeTimeSlot = 100;
	
	/**
	 * Número de dados sensoriados por time slot (Tamanho do time slot)
	 * Number of sensed data per time slot (time slot size)
	 */
	private Integer sizeTimeUpdate = 5;
	
	/**
	 * Tipo de dado a ser sensoriado (lido nos nós sensores), que pode ser: "t"=temperatura, "h"=humidade, "l"=luminosidade ou "v"=voltagem
	 * Type of data to be sensed (read in the sensor nodes), which can be: "t" = temperature, "h" = humidity, "l" = brightness or "v" = voltage
	 */
	private String dataSensedType = "t";
	
	/**
	 * Percentual do limiar de erro temporal aceitável para as leituras dos nós sensores, que pode estar entre 0.0 (não aceita erros) e 1.0 (aceita todo e qualquer erro)
	 * Percentage of temporal acceptable error threshold for the readings of sensor nodes, which may be between 0.0 (accepts no errors) and 1.0 (accepts any error)
	 */
	private double thresholdError = 0.05;
	
	/**
	 * Limite de diferença de magnitude aceitável (erro espacial) para as leituras dos nós sensores /--que pode estar entre 0.0 (não aceita erros) e 1.0 (aceita todo e qualquer erro)
	 * Limit of acceptable magnitude difference (spatial error) for the readings of sensor nodes / - which can be between 0.0 (no errors accepted) and 1.0 (accepts any error)
	 */
	private double spacialThresholdError = 1.5;
	
	/**
	 * Percentual mínimo do número de rounds iguais das medições de 2 sensores para que os mesmos sejam classificados no mesmo cluster
	 * Minimum percentage of the number of equal measurement rounds of 2 sensors so that they are classified in the same cluster
	 */
	private double equalRoundsThreshold = 0.5;
	
	/**
	 * Percentual mínimo das medições de 2 sensores (no mesmo round) a ficar dentro dos limiares aceitáveis para que os mesmos sejam classificados no mesmo cluster
	 * Minimum percentage of measurements of two sensors (in the same round) to stay within the acceptable thresholds for them to be classified in the same cluster
	 */
	private double metaThreshold = 0.5;
	
	/**
	 * Distância máxima aceitável para a formação de clusters. Se for igual a zero (0,0), não considerar tal limite (distância)
	 * Maximum distance acceptable to the formation of clusters. If it is equal to zero (0.0), ignoring
	 */
	private double maxDistance = 8.0; //8.0;
	
	/**
	 * Número total de nós sensores presentes na rede
	 * Total number of sensor nodes in the network
	 */
	private static int numTotalOfSensors = 54;
	
	/**
	 * Array 2D (clusters) from sensors (Messages from sensors = WsnMsgResponse).
	 */
	private ArrayList2d<WsnMsgResponse> messageGroups;
	
	private ArrayList2d<WsnMsgResponse> newCluster;
	
	/**
	 * Number of messages received by sink node from all other sensors nodes
	 */
	private int numMessagesReceived = 0;
	
	/**
	 * Number of messages of error prediction received by sink node from all other sensors nodes
	 */
	private int numMessagesOfErrorPredictionReceived = 0;
	
	/**
	 * Number of messages of time slot finished received by sink node from all other sensors nodes
	 */
	private int numMessagesOfTimeSlotFinishedReceived = 0;
	
	/**
	 * Indicates that sink node signalize to all other nodes must continuously sensing (naive using Cluster Heads)
	 */
	private boolean allSensorsMustContinuoslySense = true;
	
	/**
	 * Flag to indicate that the sink still not clustered all nodes for the first time
	 */
	private boolean stillNonclustered = true;
	
	private int expectedNumberOfSensors, numMessagesExpectedReceived = 0;
	
	public SinkNode()
	{
		super();
		this.setColor(Color.RED);
		Utils.printForDebug("The size of time slot is "+sizeTimeSlot);
		Utils.printForDebug("The type of data sensed is "+dataSensedType);
		Utils.printForDebug("The threshold of error (max error) is "+thresholdError);
		Utils.printForDebug("The size of sliding window is "+SimpleNode.slidingWindowSize);
		Utils.printForDebug("The maximum distance between sensors in the same cluster is "+maxDistance);
		Utils.printForDebug("The status for continuos sense is "+allSensorsMustContinuoslySense);
		
//		if(LogL.ROUND_DETAIL){
			Global.log.logln("\nThe size of time slot is "+sizeTimeSlot);
			Global.log.logln("The type of data sensed is "+dataSensedType);
			Global.log.logln("The threshold of error (max error) is "+thresholdError);
			Global.log.logln("The size of sliding window is "+SimpleNode.slidingWindowSize+"\n");
//		}
	} // end SinkNode()

	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		String text = "S";
		super.drawNodeAsSquareWithText(g, pt, highlight, text, 1, Color.WHITE);
	} // end draw(Graphics g, PositionTransformation pt, boolean highlight)
	
	@NodePopupMethod(menuText="Definir Sink como Raiz de Roteamento")
	public void construirRoteamento()
	{
		this.nextNodeToBaseStation = this;
		//WsnMsg wsnMessage = new WsnMsg(1, this, null, this, 0); //Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo
		WsnMsg wsnMessage = new WsnMsg(1, this, null, this, 0, sizeTimeSlot, dataSensedType);
		
		// The numberOfMessagesOverAll and sensorReadingsCount attribs are used to define a param from energy consumption of the overall network
		Utils.printForDebug("Global.numberOfMessagesOverAll = "+Global.numberOfMessagesOverAll);
		Utils.printForDebug("Global.sensorReadingsCount = "+Global.sensorReadingsCount);
		
		WsnMessageTimer timer = new WsnMessageTimer(wsnMessage);
		timer.startRelative(1, this);
	} // end construirRoteamento()
	
	@Override
	public void handleMessages(Inbox inbox) 
	{
		while (inbox.hasNext())
		{
			Message message = inbox.next();
			
			Utils.printForDebug("Global.numberOfMessagesOverAll = "+Global.numberOfMessagesOverAll);
			Utils.printForDebug("Global.sensorReadingsCount = "+Global.sensorReadingsCount);
			
			if (message instanceof WsnMsgResponse)
			{
				this.setColor(Color.YELLOW);
				WsnMsgResponse wsnMsgResp = (WsnMsgResponse) message;
				
				Utils.printForDebug("@ @ @ Message Received by SINK from the NodeID = "+wsnMsgResp.source.ID +" with MsgType = "+wsnMsgResp.typeMsg+"\n");
				
				if (wsnMsgResp.typeMsg == 2) // Se é uma mensagem de um Nó Representativo que excedeu o #máximo de erros de predição
				{
					numMessagesOfErrorPredictionReceived++;
					
// CASO O CLUSTER PRECISE SOFRER UM SPLIT, UMA MENSAGEM SOLICITANDO UM NOVO ENVIO DE DADOS PARA O SINK DEVE SER ENVIADA PARA CADA UM DOS NÓS DO CLUSTER 
					
					int lineFromCluster = identifyCluster(wsnMsgResp);
					if (lineFromCluster >= 0)
					{
						expectedNumberOfSensors = sendSenseRequestMessageToAllSensorsInCluster(messageGroups, lineFromCluster);
						triggerSplitFromCluster(lineFromCluster);
					}
				
// CASO O NÓ QUE TENHA ENVIADO A MsgResp SEJA UM CLUSTER HEAD???
					if (wsnMsgResp.target != null)
					{
						
						Utils.printForDebug("Inside the code => if (wsnMsgResp.target != null)");
						
						
						
						
						
						
					}
					
					receiveMessage(wsnMsgResp, null); // Recebe a mensagem, para recálculo dos coeficientes e reenvio dos mesmos àquele nó sensor (Nó Representativo), mantendo o número de predições a serem executadas como complemento do total calculado inicialmente, ou seja, NÃO reinicia o ciclo de time slot daquele cluster
				}
				
				else if (wsnMsgResp.typeMsg == 3) // Se é uma mensagem de um Nó Representativo que excedeu o #máximo de predições (timeSlot)
				{
					numMessagesOfTimeSlotFinishedReceived++;
					
					int lineFromClusterNode = searchAndReplaceNodeInClusterByMessage(wsnMsgResp); // Procura a linha (cluster) da mensagem recebida e atualiza a mesma naquela linha
					
					if (lineFromClusterNode >= 0) // Se a linha da mensagem recebida for encontrada
					{
						Utils.printForDebug("@ @ @ MessageGroups BEFORE classification:\n");
						printMessageGroupsArray2d();
						
						classifyRepresentativeNodesByResidualEnergy(messageGroups); // (Re)classifica os nós dos clusters por energia residual
						
						Utils.printForDebug("@ @ @ MessageGroups AFTER FIRST classification:\n");
						printMessageGroupsArray2d();
						
						classifyRepresentativeNodesByHopsToSink(messageGroups); // (Re)classifica os nós dos clusters por saltos até o sink node 
						
						Utils.printForDebug("@ @ @ MessageGroups AFTER SECOND classification:\n");
						printMessageGroupsArray2d();
						
						WsnMsgResponse wsnMsgResponseRepresentative = messageGroups.get(lineFromClusterNode, 0); // Get the (new) Representative Node (or Cluster Head)
						int numSensors = messageGroups.getNumCols(lineFromClusterNode);
						Utils.printForDebug("Cluster / Line number = "+lineFromClusterNode+"\n");
						wsnMsgResponseRepresentative.calculatesTheSizeTimeSlotFromRepresentativeNode(sizeTimeSlot, numSensors);
						
						receiveMessage(wsnMsgResponseRepresentative, null);
					}
					// PAREI AQUI!!! - Fazer testes para verificar se os clusters estão sendo reconfigurados quando um No Repres. finaliza seu time slot e atualiza o status de sua bateria!
				} // else if (wsnMsgResp.typeMsg == 3)
				
				else // If it is a message from a (Representative) node containing reading (sense) data
				{
					
// ALTERAR NESTE PONTO PARA VERIFICAR QUANDO UMA MENSAGEM DE RESPOSTA A UMA REQUISIÇÃO FOR RECEBIDA PARA REALIZAR POSSÍVEL SPLIT DE CLUSTER 
					numMessagesReceived++;
					
					if (stillNonclustered) // If the sink still not clustered all nodes for the first time
					{
						if (messageGroups == null) // If there isn't a message group yet, then it does create one and adds the message to it
						{
							messageGroups = new ArrayList2d<WsnMsgResponse>();
							messageGroups.ensureCapacity(numTotalOfSensors); // Ensure the capacity as the total number of sensors (nodes) in the data set
							
							messageGroups.add(wsnMsgResp, 0); // Add the initial message to the group (ArrayList2d of WsnMsgResponse)
						}
						else // If there is a message group (SensorCluster), then adds the wsnMsgResp representing a sensor to group, classifing this message/sensor in correct cluster/line
						{
							addNodeInClusterClassifiedByMessage(messageGroups, wsnMsgResp);
						}
						
						if (numMessagesReceived >= numTotalOfSensors) // In this point, clusters should be "closed", and the sensors inside them being classified
						{
							Utils.printForDebug("@ @ @ MessageGroups BEFORE classification:\n");
							printMessageGroupsArray2d();
							
							classifyRepresentativeNodesByResidualEnergy(messageGroups);
							
							Utils.printForDebug("@ @ @ MessageGroups AFTER FIRST classification:\n");
							printMessageGroupsArray2d();
							
							classifyRepresentativeNodesByHopsToSink(messageGroups);
							
							Utils.printForDebug("@ @ @ MessageGroups AFTER SECOND classification:\n");
							printMessageGroupsArray2d();
							
							stillNonclustered = false;
						 	
							if (messageGroups != null) // If there is a message group created
							{
								if (!allSensorsMustContinuoslySense) // If only the representative nodes must sensing
								{
									for (int line=0; line < messageGroups.getNumRows(); line++) // For each line (group/cluster) from messageGroups
									{
										WsnMsgResponse wsnMsgResponseRepresentative = messageGroups.get(line, 0); // Get the Representative Node (or Cluster Head)
										int numSensors = messageGroups.getNumCols(line);
										Utils.printForDebug("Cluster / Line number = "+line);
										wsnMsgResponseRepresentative.calculatesTheSizeTimeSlotFromRepresentativeNode(sizeTimeSlot, numSensors);
										receiveMessage(wsnMsgResponseRepresentative, null);
									} // for (int line=0; line < messageGroups.getNumRows(); line++)
								} // if (!allSensorsMustContinuoslySense)
								else // If all nodes in cluters must sensing, and not only the representative nodes
								{
									
									for (int line=0; line < messageGroups.getNumRows(); line++) // For each line (group/cluster) from messageGroups
									{
										int numSensors = messageGroups.getNumCols(line);
										Node chNode = (messageGroups.get(line, 0)).source; // Cluster Head from the current cluster/line
										Utils.printForDebug("Cluster / Line number = "+line+"; ClusterHead / IDnumber = "+chNode.ID+"; #Sensors = "+numSensors);
										for (int col=0; col < numSensors; col++) // For each colunm from that line in messageGroups
										{
											WsnMsgResponse wsnMsgResponseCurrent = messageGroups.get(line, col); // Get the Node
											
											//wsnMsgResponseCurrent.calculatesTheSizeTimeSlotFromRepresentativeNode(sizeTimeSlot, numSensors);
											wsnMsgResponseCurrent.sizeTimeSlot = 1; // If all sensor nodes in Cluster must continuosly sense, so the sizeTimeSlot doesn't matter
											
											receiveMessage(wsnMsgResponseCurrent, chNode);
										}
									}
									
								} // end else
							} // end if (messageGroups != null)
						} // end if (numMessagesReceived >= numTotalOfSensors)
					} // end if (stillNonclustered)
					
					else // otherwise, if the sink have already been clustered all nodes for the first time
					{
						numMessagesExpectedReceived++;
						
						if (newCluster == null) // If a new cluster (temp) has not yet been created (instanciated)
						{
							newCluster = new ArrayList2d<WsnMsgResponse>(); // Instanciate him
							newCluster.ensureCapacity(expectedNumberOfSensors);
							newCluster.add(wsnMsgResp, 0); // Adds the new response message sensor to new cluster 
						} // end if (newCluster == null)
						else
						{
							addNodeInClusterClassifiedByMessage(newCluster, wsnMsgResp);
							
							if (numMessagesExpectedReceived >= expectedNumberOfSensors) // If all messagesResponse (from all nodes in Cluster to be splited) already done received
							{
								classifyRepresentativeNodesByResidualEnergy(newCluster);
								
								classifyRepresentativeNodesByHopsToSink(newCluster);

								//NESTE PONTO, É PRECISO MANDAR MENSAGEM PARA OS NOVOS NÓS REPRESENTATIVOS PARA QUE OS MESMOS INICIEM UMA NOVA FASE (Novo ciclo de sensoriamento)
								
								// (CICLO) DE SENSORIAMENTO
								if (newCluster != null) // If there is a message group created
								{
									if (!allSensorsMustContinuoslySense) // If only the representative nodes must sensing
									{
										for (int line=0; line < newCluster.getNumRows(); line++) // For each line (group/cluster) from newCluster
										{
											WsnMsgResponse wsnMsgResponseRepresentative = newCluster.get(line, 0); // Get the Representative Node (or Cluster Head)
											int numSensors = newCluster.getNumCols(line);
											Utils.printForDebug("Cluster / Line number = "+line);
											wsnMsgResponseRepresentative.calculatesTheSizeTimeSlotFromRepresentativeNode(sizeTimeSlot, numSensors);
											receiveMessage(wsnMsgResponseRepresentative, null);
										} // end for (int line=0; line < newCluster.getNumRows(); line++)
									} // end if (!allSensorsMustContinuoslySense)
									
									else // If all nodes in cluters must sensing, and not only the representative nodes
									{
										
										for (int line=0; line < newCluster.getNumRows(); line++) // For each line (group/cluster) from messageGroups
										{
											int numSensors = newCluster.getNumCols(line);
											Node chNode = (newCluster.get(line, 0)).source; // Cluster Head from the current cluster/line
											Utils.printForDebug("Cluster / Line number = "+line+"; ClusterHead / IDnumber = "+chNode.ID+"; #Sensors = "+numSensors);
											for (int col=0; col < numSensors; col++) // For each colunm from that line in newCluster
											{
												WsnMsgResponse wsnMsgResponseCurrent = newCluster.get(line, col); // Get the Node
												
												//wsnMsgResponseCurrent.calculatesTheSizeTimeSlotFromRepresentativeNode(sizeTimeSlot, numSensors);
												wsnMsgResponseCurrent.sizeTimeSlot = 1; // If all sensor nodes in each cluster must continuosly sense, so the sizeTimeSlot doesn't matter
												
												receiveMessage(wsnMsgResponseCurrent, chNode);
											} // end for (int col=0; col < numSensors; col++)
										} // end for (int line=0; line < newCluster.getNumRows(); line++)
										
									} // else
								} // end if (newCluster != null)
																	
								unifyClusters(messageGroups, newCluster); // TESTAR SE MÉTODO FUNCIONA CORRETAMENTE!!!???
								
								numMessagesExpectedReceived = 0;
								newCluster = null;
								
 							} // end if (numMessagesExpectedReceived >= expectedNumberOfSensors)
						} // end else
					}
					
				} // end else
			} // end if (message instanceof WsnMsg)
		} //end while (inbox.hasNext())
	} //end handleMessages()
	
	/**
	 * Adds the clusters (lines) from tempClusterGiver in the tempClusterReceiver
	 * @param tempClusterReceiver Cluster structure that will receive the sensors/clusters from the tempClusterGiver structure
	 * @param tempClusterGiver Cluster structure that will give the sensors/clusters to the tempClusterReceiver structure
	 */
	private void unifyClusters(ArrayList2d<WsnMsgResponse> tempClusterReceiver, ArrayList2d<WsnMsgResponse> tempClusterGiver)
	{
		int rowReceiver, rowGiver = 0, col;
		rowReceiver = tempClusterReceiver.getNumRows();
		while (rowGiver < tempClusterGiver.getNumRows())
		{
			col = 0;
			while (col < tempClusterGiver.getNumCols(rowGiver))
			{
				tempClusterReceiver.add(tempClusterGiver.get(rowGiver, col), rowReceiver);
				col++;
			} // end while (col < tempClusterGiver.getNumCols(rowGiver))
			rowGiver++;
			rowReceiver++;
		} // end while (rowGiver < tempClusterGiver.getNumRows())
	} // end unifyClusters(ArrayList2d<WsnMsgResponse> tempClusterReceiver, ArrayList2d<WsnMsgResponse> tempClusterGiver)
	
	/**
	 * It triggers the process to split a cluster, through the exclusion (remove) from the line from old cluster - to be splited
	 * @param lineFromCluster Line number from cluster to be divided
	 */
	private void triggerSplitFromCluster(int lineFromCluster)
	{
		// IDEIA: O nó representativo acabou de enviar seus últimos dados de leitura (sensoriamento), então ele não precisa enviar novamente
		// Remover cluster (nós do cluster) de messageGroups
		messageGroups.remove(lineFromCluster);
		// Armazenar quantos nós existiam neste cluster, para saber quando todos terminaram de responder
		// Identificá-los pelo clusterHeadID
	} // end triggerSplitFromCluster(int lineFromCluster)
	
	/**
	 * When a sensor node sends a message to sink indicating a novelty, the sink must shoot/send messages to all sensors in that cluster requiring new readings from sensors to verify the current similarity of the readings from cluster`s sensors 
	 * @param tempCluster Cluster (set of clusters) to be used as base - The structure actually is a set of clusters, where each cluster is represented by a different line
	 * @param lineFromCluster Line number from current cluster
	 * @return Number of sensors (#columns) in the cluster/line from lineFromCluster
	 */
	private int sendSenseRequestMessageToAllSensorsInCluster(ArrayList2d<WsnMsgResponse> tempCluster, int lineFromCluster)
	{
		int numSensorsInThisCluster = 0;
		if (tempCluster == null) // If there isn't a message group yet
		{
			Utils.printForDebug("ERROR in sendSenseRequestMessageToAllCluster method: There isn't tempCluster object instanciated yet!");
		}
		else
		{
			int col = 0;
			numSensorsInThisCluster = tempCluster.getNumCols(lineFromCluster);
			while (col < numSensorsInThisCluster)
			{
				WsnMsgResponse currentWsnMsgResp = tempCluster.get(lineFromCluster, col);
				
				WsnMsg wsnMessage = new WsnMsg(1, this, currentWsnMsgResp.source, this, 1, sizeTimeUpdate, dataSensedType);
								
				wsnMessage.removeCoefs(); // Identifies this message as requesting sensing and not sending coefficients
				wsnMessage.setPathToSenderNode(currentWsnMsgResp.clonePath()); // Sets the path (route) to destination node (source) - same "currentWsnMsgResp.origem"
				
				sendToNextNodeInPath(wsnMessage);
/*				
				WsnMessageTimer timer = new WsnMessageTimer(wsnMessage);
				timer.startRelative(1, this);
*/
				col++;
			} // end while (col < numSensorsInThisCluster)
		}
		return numSensorsInThisCluster;
	} // end sendSenseRequestMessageToAllSensorsInCluster(ArrayList2d<WsnMsgResponse> tempCluster, int lineFromCluster)
	
	/**
	 * It identifies which cluster (line number) where the "newWsnMsgResp" is 
	 * @param newWsnMsgResp MessageResponse representing the sensor node to be localized in messageGroups
	 * @return Line number (cluster) from message passed by; otherwise, returns -1 indicating that there is no such message("newWsnMsgResp") in any cluster
	 */
	private int identifyCluster(WsnMsgResponse newWsnMsgResp)
	{
		int lineCLuster = -1;
		if (messageGroups == null) // If there isn't a message group yet
		{
			Utils.printForDebug("ERROR in identifyCluster method: There isn't messageGroups object instanciated yet!");
		}
		else
		{
			boolean found = false;
			int line = 0, col = 0;
			while ((!found) && (line < messageGroups.getNumRows()))
			{
				col = 0;
				while ((!found) && (col < messageGroups.getNumCols(line)))
				{
					WsnMsgResponse currentWsnMsgResp = messageGroups.get(line, col);
					if (isEqualNodeSourceFromMessages(currentWsnMsgResp, newWsnMsgResp))
					{
						found = true;
					}
					else
					{
						col++;
					}
				}
				if (!found)
				{
					line++;
				}
			}
			if (found)
			{
				lineCLuster = line;
			}
		}
		return lineCLuster;
	} // end identifyCluster(WsnMsgResponse newWsnMsgResp)
	
	/**
	 * It selects the Representative Node for each line (cluster) from sensors by the max residual energy and puts him in the first position (in line)
	 * @param tempCluster Cluster (ArrayList) which will have the sensors ordered (line by line) by the max residual energy
	 */
	private void classifyRepresentativeNodesByResidualEnergy(ArrayList2d<WsnMsgResponse> tempCluster)
	{
		if (tempCluster != null) // If there is a message group created
		{
			for (int line=0; line < tempCluster.getNumRows(); line++)
			{
				double maxBatLevel = 0.0;
				int maxBatLevelIndexInThisLine = 0;
				int bubbleLevel = 0;
				while (bubbleLevel < (tempCluster.getNumCols(line) - 1))
				{
					for (int col=bubbleLevel; col < tempCluster.getNumCols(line); col++)
					{
						WsnMsgResponse currentWsnMsgResp = tempCluster.get(line, col);
						double currentBatLevel = currentWsnMsgResp.batLevel;
						int currentIndex = col;
						if (currentBatLevel > maxBatLevel)
						{
							maxBatLevel = currentBatLevel;
							maxBatLevelIndexInThisLine = currentIndex;
						} // end if (currentBatLevel > maxBatLevel)
					} // end for (int col=bubbleLevel; col < tempCluster.getNumCols(line); col++)
					if (maxBatLevelIndexInThisLine != 0)
					{
						tempCluster.move(line, maxBatLevelIndexInThisLine, bubbleLevel);//changeMessagePositionInLine(line, maxBatLevelIndexInThisLine);
					} // end if (maxBatLevelIndexInThisLine != 0)
					bubbleLevel++;
					maxBatLevel = 0.0;
					maxBatLevelIndexInThisLine = 0;
				} // end while (bubbleLevel < (tempCluster.getNumCols(line) - 1))
			} // end for (int line=0; line < tempCluster.getNumRows(); line++)
		} // end if (tempCluster != null)
	} // end classifyRepresentativeNodesByResidualEnergy(ArrayList2d<WsnMsgResponse> tempCluster)
	
	/**
	 * It classifies the Nodes for each line (cluster) from sensors by the min distance (in number of hops) to sink among them who have the same max residual energy and puts him in the first position (in line)
	 * @param tempCluster Cluster (ArrayList) which will have the sensors ordered (line by line - as a second criterion) by the min number of hops to sink 
	 */
	private void classifyRepresentativeNodesByHopsToSink(ArrayList2d<WsnMsgResponse> tempCluster)
	{
		if (tempCluster != null) // If there is a message group created
		{
			for (int line=0; line < tempCluster.getNumRows(); line++)
			{
				if (tempCluster.get(line, 0) != null)
				{
					int minIndexNumHopsToSink = 0;
					boolean sameBatLevel = false;
					WsnMsgResponse firstWsnMsgRespInLine = tempCluster.get(line, 0);
					int col=1;
					while ((col < tempCluster.getNumCols(line)) && (tempCluster.get(line, col).batLevel == firstWsnMsgRespInLine.batLevel) && (tempCluster.get(line, col).hopsToTarget < firstWsnMsgRespInLine.hopsToTarget) )
					{
						minIndexNumHopsToSink = col;
						sameBatLevel = true;
						col++;
					} // end while ((col < tempCluster.getNumCols(line)) && (tempCluster.get(line, col).batLevel == firstWsnMsgRespInLine.batLevel) && (tempCluster.get(line, col).hopsToTarget < firstWsnMsgRespInLine.hopsToTarget) )
					if (sameBatLevel)
					{
						changeMessagePositionInLine(line, minIndexNumHopsToSink);
					} // end if (sameBatLevel)
				} // end if (tempCluster.get(line, 0) != null)
			} // end for (int line=0; line < tempCluster.getNumRows(); line++)
		} // end if (tempCluster != null)
	} // end classifyRepresentativeNodesByHopsToSink(ArrayList2d<WsnMsgResponse> tempCluster)
	
	/**
	 * It prints and colore nodes by the clusters (message groups) formed
	 */
	private void printMessageGroupsArray2d()
	{
		if (messageGroups != null) // If there is a message group created
		{
			int codColor = 0;
			Color[] arrayColor = {
					Color.CYAN, 
					Color.DARK_GRAY, 
					Color.GRAY, 
					Color.MAGENTA, 
					Color.ORANGE, 
					Color.GREEN, 
					Color.BLUE, 
					Color.PINK, 
					Color.LIGHT_GRAY, 
					new Color(25,25,112), 
					new Color(16,78,139), 
					new Color(0,199,140), 
					new Color(189,183,107), 
					new Color(205,133,0), 
					new Color(176,23,31), 
					new Color(139,71,137), 
					new Color(138,43,226), 
					new Color(132,112,255), 
					new Color(100,149,237), 
					new Color(95,158,160), 
					new Color(0,206,209), 
					new Color(61,145,64), 
					new Color(255,0,0), 
					new Color(198,113,113),
					new Color(128,0,0),
					new Color(113,113,198),
					new Color(234,234,234),
					new Color(238,99,99),
					Color.BLACK};
			Color currentRandomColor = arrayColor[codColor]; 
//			Color currentRandomColor = new Color(codColor); 
			for (int line=0; line < messageGroups.getNumRows(); line++)
			{
				for (int col=0; col < messageGroups.getNumCols(line); col++)
				{
					WsnMsgResponse currentWsnMsgResp = messageGroups.get(line, col);
					currentWsnMsgResp.source.setColor(currentRandomColor);
					Utils.printForDebug("Line = "+line+", Col = "+col+": NodeID = "+currentWsnMsgResp.source.ID+" BatLevel = "+currentWsnMsgResp.batLevel+" Round = "+((SimpleNode)currentWsnMsgResp.source).lastRoundRead);
				}
				Utils.printForDebug("\n");
				codColor += 1;
				codColor = (codColor < 29 ? codColor : 0);
				currentRandomColor = arrayColor[codColor];
//				currentRandomColor = new Color(codColor);
			}
			Utils.printForDebug("Number of Lines / Clusters = "+messageGroups.getNumRows()+"\n");
		}
	} // end printMessageGroupsArray2d()
	
	/**
	 * Change the message from "index" position for the first position [0] in that line from array
	 * @param line
	 * @param index
	 */
	private void changeMessagePositionInLine(int line, int index)
	{
		messageGroups.move(line, index, 0);
	} // end changeMessagePositionInLine(int line, int index)
	
	/**
	 * Adds the WsnMsgResponse object (newWsnMsgResp), passed by parameter, in the correct line ("Cluster") from the tempCluster (ArrayList2d) according with the Dissimilarity Measure 
	 * PS.: Each line in tempCluster (ArrayList2d of objects WsnMsgResponse) represents a cluster of sensors (WsnMsgResponse.origem), 
	 * classified by Dissimilarity Measure from yours data sensed, stored on WsnMsgResponse.dataRecordItens
	 *  
	 * @param tempCluster ArrayList2d from sensors, organized as clusters (line by line) 
	 * @param newWsnMsgResp Message to be used for classify the sensor node
	 */
	private void addNodeInClusterClassifiedByMessage(ArrayList2d<WsnMsgResponse> tempCluster, WsnMsgResponse newWsnMsgResp)
	{
		boolean found = false;
		int line = 0;
		while ((!found) && (line < tempCluster.getNumRows()))
		{
			int col = 0;
			boolean continueThisLine = true;
			while ((continueThisLine) && (col < tempCluster.getNumCols(line)))
			{
				WsnMsgResponse currentWsnMsgResp = tempCluster.get(line, col);
				if (testDistanceBetweenSensorPositions(currentWsnMsgResp, newWsnMsgResp))
				{
// DANDO ERRO NO TESTE DE DIST.
					if (testSimilarityMeasureWithPairRounds(currentWsnMsgResp, newWsnMsgResp)) // If this (new)message (with sensor readings) already is dissimilar to current message
					{
						continueThisLine = true; // Then this (new)message doesn't belong to this cluster / line / group
					}
					else
					{
						continueThisLine = false;
					}
				}
				else
				{
					continueThisLine = false;
				}
				col++;
			}
			if ((continueThisLine) && (col == tempCluster.getNumCols(line)))
			{
				found = true;
				tempCluster.add(newWsnMsgResp, line);
			}
			else
			{
				line++;
			}
		}
		if (!found)
		{
			tempCluster.add(newWsnMsgResp, tempCluster.getNumRows()); // It adds the new message "wsnMsgResp" in a new line (cluster) of messageGroup 
		}
/*				
			for (int line = 0; line < tempCluster.getNumRows(); line++)
			{
				for (int col = 0; col < tempCluster.getNumCols(line); col++)
				{
					
				}
			}
*/			
	} // end addNodeInClusterClassifiedByMessage(ArrayList2d<WsnMsgResponse> tempCluster, WsnMsgResponse newWsnMsgResp)

	/**
	 * Search the WsnMsgResponse object with the same source node, in the correct position in "Cluster" from the messageGroups (ArrayList2d) and replace him with the one passed by parameter  
	 * PS.: Each line in "messageGroups" (ArrayList2d of objects WsnMsgResponse) represents a cluster of sensors (WsnMsgResponse.origem), 
	 * classified by Dissimilarity Measure from yours data sensed, stored on WsnMsgResponse.dataRecordItens
	 *  
	 * @param wsnMsgResp Message to be used for classify the sensor node
	 */
	private int searchAndReplaceNodeInClusterByMessage(WsnMsgResponse newWsnMsgResp)
	{
		int lineCLuster = -1;
		if (messageGroups == null) // If there isn't a message group yet
		{
			Utils.printForDebug("ERROR in searchAndReplaceNodeInClusterByMessage method: There isn't messageGroups object instanciated yet!");
		}
		else
		{
			boolean found = false;
			int line = 0, col = 0;
			while ((!found) && (line < messageGroups.getNumRows()))
			{
				col = 0;
				while ((!found) && (col < messageGroups.getNumCols(line)))
				{
					WsnMsgResponse currentWsnMsgResp = messageGroups.get(line, col);
					if (isEqualNodeSourceFromMessages(currentWsnMsgResp, newWsnMsgResp))
					{
						found = true;
					}
					else
					{
						col++;
					}
				}
				if (!found)
				{
					line++;
				}
			}
			if (found)
			{
				lineCLuster = line;
				messageGroups.set(line, col, newWsnMsgResp); // It sets the new message "wsnMsgResp" in the line and col (cluster) of messageGroup 
			}
		}
		return lineCLuster;
	} // end searchAndReplaceNodeInClusterByMessage(WsnMsgResponse newWsnMsgResp)

	
	/**
	 * Testa se a distância entre os nós sensores que enviaram tais mensagens (currentWsnMsg e newWsnMsg) é menor ou igual a máxima distância possível (maxDistance) para os dois nós estarem no mesmo cluster
	 * @param currentWsnMsg Mensagem atual já classificada no cluster
	 * @param newWsnMsg Nova mensagem a ser classificada no cluster
	 * @return Retorna "verdadeiro" caso a distância entre os nós sensores que enviaram as mensagens não ultrapassa o limite máximo, "falso" caso contrário
	 */
	private boolean testDistanceBetweenSensorPositions(WsnMsgResponse currentWsnMsg, WsnMsgResponse newWsnMsg)
	{
		boolean distanceOK = false;
		if (maxDistance > 0.0)
		{
			if (currentWsnMsg.spatialPos != null && newWsnMsg.spatialPos != null && currentWsnMsg.spatialPos.distanceTo(newWsnMsg.spatialPos) <= maxDistance)
			{
				distanceOK = true;
			}
/*
			else
			{
				distanceOK = false;
			}
*/
			// distanceOK = (currentWsnMsg.spatialPos.distanceTo(newWsnMsg.spatialPos) <= maxDistance); //Another form for the "if" (above) 
		}
		else if (maxDistance == 0.0) // Case the distance between sensors should be ignored
		{
			distanceOK = true;
		}
		return distanceOK;
	} // end testDistanceBetweenSensorPositions(WsnMsgResponse currentWsnMsg, WsnMsgResponse newWsnMsg)
	
	private boolean isEqualNodeSourceFromMessages(WsnMsgResponse currentWsnMsg, WsnMsgResponse newWsnMsg)
	{
		return (currentWsnMsg.source.ID == newWsnMsg.source.ID);
	} // end isEqualNodeSourceFromMessages(WsnMsgResponse currentWsnMsg, WsnMsgResponse newWsnMsg)
	
/*
	private boolean testDissimilarityMeasureWithoutPairRounds(WsnMsgResponse currentWsnMsg, WsnMsgResponse newWsnMsg)
	{
		return true;
	}
*/
	
	/**
	 * It tests if there is dissimilarity (lack of similarity) between the 2 set of measure from 2 sensor brought by the 2 messages
	 * @param currentWsnMsg Represents the current message from the group of messages (messageGroups) in a "ArrayList2d<WsnMsgResponse>" structure
	 * @param newWsnMsg Represents the recently arrived message in the sink node, sent from the source sensor node
	 * @return True case the two messages are DISsimilar, i.e., from different clusters (or "groups"); False, otherwise
	 */
	
	private boolean testSimilarityMeasureWithPairRounds(WsnMsgResponse currentWsnMsg, WsnMsgResponse newWsnMsg)
	{
//		boolean sameSize = true;
		boolean mSimilarityMagnitude = false;
		boolean tSimilarityTrend = false;
		
		int currentSize;
		double[] currentValues;
		if (currentWsnMsg.dataRecordItens != null) {
			currentSize = currentWsnMsg.dataRecordItens.size();
			currentValues = new double[currentSize];
		}
		else {
			return (mSimilarityMagnitude && tSimilarityTrend);
		}
/*
		double[] currentTimes = new double[currentSize];
		char[] currentTypes = new char[currentSize];
		double[] currentBatLevel = new double[currentSize];
*/
		int[] currentRound = new int[currentSize];
		
		//Data read from current sensor (from ArrayList2d)
		currentValues = currentWsnMsg.getDataRecordValues();
/*
		currentTimes = currentWsnMsg.getDataRecordTimes();
		currentTypes = currentWsnMsg.getDataRecordTypes();
		currentBatLevel = currentWsnMsg.getDataRecordBatLevels();
*/
		currentRound = currentWsnMsg.getDataRecordRounds();

		int newSize;
		double[] newValues;
		if (newWsnMsg.dataRecordItens != null) {
			newSize = newWsnMsg.dataRecordItens.size();
			newValues = new double[newSize];
		}
		else {
			return (mSimilarityMagnitude && tSimilarityTrend);
		}
/*
		double[] newTimes = new double[newSize];
		char[] newTypes = new char[newSize];
		double[] newBatLevel = new double[newSize];
*/
		int[] newRound = new int[newSize];
		
		//Data read from new sensor (from message received)
		newValues = newWsnMsg.getDataRecordValues();
/*
		newTimes = newWsnMsg.getDataRecordTimes();
		newTypes = newWsnMsg.getDataRecordTypes();
		newBatLevel = newWsnMsg.getDataRecordBatLevels();
*/
		newRound = newWsnMsg.getDataRecordRounds();

		HashMap<Integer, Double> hashCurrentMsg, hashNewMsg;
		
		hashCurrentMsg = new HashMap<Integer, Double>();
		hashNewMsg = new HashMap<Integer, Double>();
		
		// Populates 2 HashMaps with the values from currentWsnMsg and newWsnMsg
		for (int i=0,j=0; (i < currentSize || j < newSize); i++, j++)
		{
			if (i < currentSize)
			{	
				hashCurrentMsg.put(currentRound[i], currentValues[i]);
			}
			if (j < newSize)
			{
				hashNewMsg.put(newRound[j], newValues[j]);
			}
		}


//		int maxSizeOf2Msg = Math.max(currentSize, newSize);		
		
//		int numDissimilarity = 0;
		
		int numEqualKeys = 0;
		
		double sumDifs = 0.0;
		
		Set<Integer> keys = hashCurrentMsg.keySet();
		for (Integer key : keys)
		{
			if (hashNewMsg.containsKey(key))
			{
				numEqualKeys++;
				double curValue = hashCurrentMsg.get(key);
				double newValue = hashNewMsg.get(key);
				sumDifs += Math.abs(curValue - newValue);
			}
		}

		if ((numEqualKeys > 0) && (sumDifs/numEqualKeys <= spacialThresholdError))
		{
			mSimilarityMagnitude = true;
//			return mSimilarityMagnitude;
		}
/*
		if ((numEqualKeys >= equalRoundsThreshold * maxSizeOf2Msg) && (numDissimilarity >= metaThreshold * numEqualKeys))
		{
			mSimilarityMagnitude = true;
			return mSimilarityMagnitude;
		}
*/		
		double contN1 = 0.0;
		double contN = currentSize; // = newSize; // Total size of sensed values from node
		for (int i=1,j=1; (i < currentSize && j < newSize); i++, j++)
		{
			double difX, difY;			
			difX = (currentValues[i] - currentValues[i-1]);
			difY = (newValues[j] - newValues[j-1]);
			if ((difX * difY) >= 0)
			{
				contN1++;
			}
		}
		if ((contN > 0.0) && (contN1/contN >= thresholdError))
		{
			tSimilarityTrend = true;
//			return tDissimilarityTrendFound;
		}
		
/*
		if (currentSize != newSize)
		{
			sameSize = false; // Size from (2) data sets are different
		}
		
		if (sameSize && compareDataSetValuesPairToPair(currentValues, newValues, currentSize))
		{
			
		}
*/	
		return (mSimilarityMagnitude && tSimilarityTrend);
	} // end testSimilarityMeasureWithPairRounds(WsnMsgResponse currentWsnMsg, WsnMsgResponse newWsnMsg)

	private boolean compareDataSetValuesPairToPair(double[] valuesC, double[] valuesN, int size)
	{
		boolean ok = true;
		int cont = 0;
		while (ok && (cont<size))
		{
			if (Math.abs(valuesC[cont] - valuesN[cont]) > spacialThresholdError)
			{
				ok = false;
			}
			cont++;
		}
		return ok;
	} // end compareDataSetValuesPairToPair(double[] valuesC, double[] valuesN, int size)
	
	/**
	 * Recebe a mensagem passada, lê os parâmetros (itens) no dataRecordItens,
	 * calcula os coeficientes A e B de acordo com estes parâmetros e envia tais
	 * coeficientes para o nó sensor de origem
	 * <p>
	 * [Eng] Receives the message, reads the parameters (items) in
	 * dataRecordItens, calculates the coefficients A and B according to these
	 * parameters and sends these coefficients for the sensor node of origin
	 * 
	 * @param wsnMsgResp
	 *            Mensagem recebida com os parâmetros a serem lidos
	 * @param clusterHeadNode
	 *            Indica(seta) o ClusterHead daquele cluster
	 */
	private void receiveMessage(WsnMsgResponse wsnMsgResp, Node clusterHeadNode)
	{
		if (wsnMsgResp != null && wsnMsgResp.dataRecordItens != null)
		{
			int size = wsnMsgResp.dataRecordItens.size();
			double[] valores = new double[size];
			double[] tempos = new double[size];
//			char[] tipos = new char[size];
			//Dados lidos do sensor correspondente
			valores = wsnMsgResp.getDataRecordValues();
			tempos = wsnMsgResp.getDataRecordTimes();
//			tipos = wsnMsgResp.getDataRecordTypes();
			//Coeficientes de regressão linear com os vetores acima
			double coeficienteA, coeficienteB;
			double mediaTempos, mediaValores;
			//Médias dos valores de leitura e tempos
			mediaTempos = calculaMedia(tempos);
			mediaValores = calculaMedia(valores);
			//Cálculos dos coeficientes de regressão linear com os vetores acima
			coeficienteB = calculaB(valores, tempos, mediaValores, mediaTempos);
			coeficienteA = calculaA(mediaValores, mediaTempos, coeficienteB);
			sendCoefficients(wsnMsgResp, coeficienteA, coeficienteB, clusterHeadNode);
		}
	} // end receiveMessage(WsnMsgResponse wsnMsgResp, Node clusterHeadNode)
	
	/**
	 * Calcula e retorna a média aritmética dos valores reais passados
	 * @param values Array de valores reais de entrada 
	 * @return Média dos valores reais de entrada
	 */
	private double calculaMedia(double[] values)
	{
		double mean = 0, sum = 0;
		for (int i=0; i<values.length; i++)
		{
			sum += values[i];
		}
		if (values.length > 0)
		{
			mean = sum/values.length;
		}
		return mean;
	} // end calculaMedia(double[] values)
			
	/**
	 * Calcula o coeficiente B da equação de regressão
	 * @param valores Array de valores (grandezas) das medições dos sensores
	 * @param tempos Array de tempos das medições dos sensores
	 * @param mediaValores Média dos valores
	 * @param mediaTempos Média dos tempos
	 * @return Valor do coeficiente B da equação de regressão
	 */
	private double calculaB(double[] valores, double[] tempos, double mediaValores, double mediaTempos)
	{
		double numerador = 0.0, denominador = 0.0, x;
		for (int i = 0; i < tempos.length; i++)
		{
			x = tempos[i] - mediaTempos;
			numerador += x*(valores[i] - mediaValores);
			denominador += x*x;
		}
		if (denominador != 0)
		{
			return (numerador/denominador);
		}
		return 0.0;
	} // end calculaB(double[] valores, double[] tempos, double mediaValores, double mediaTempos)
	
	/**
	 * Calcula o coeficiente A da equação de regressão
	 * @param mediaValores Média dos valores lidos pelos sensores
	 * @param mediaTempos Média dos tempos de leitura dos valores pelos sensores
	 * @param B Valor do coeficiente B da equação de regressão
	 * @return Valor do coeficiente A
	 */
	private double calculaA(double mediaValores, double mediaTempos, double B)
	{
		return (mediaValores - B*mediaTempos);
	} // end calculaA(double mediaValores, double mediaTempos, double B)
	
	/**
	 * Cria uma nova mensagem (WsnMsg) para envio dos coeficientes recebidos
	 * através dos parâmetros, e a envia para o próximo nó no caminho até o nó
	 * de origem da mensagem (wsnMsgResp.origem)
	 * 
	 * @param wsnMsgResp
	 *            Mensagem de resposta enviada do nó de origem para o nó sink,
	 *            que agora enviará os (novos) coeficientes calculados para o nó
	 *            de origem
	 * @param coeficienteA
	 *            Valor do coeficiente A da equação de regressão
	 * @param coeficienteB
	 *            Valor do coeficiente B da equação de regressão
	 */
	private void sendCoefficients(WsnMsgResponse wsnMsgResp, double coeficienteA, double coeficienteB, Node clusterHeadNode)
	{
		WsnMsg wsnMessage = new WsnMsg(1, this, wsnMsgResp.source , this, 1, wsnMsgResp.sizeTimeSlot, dataSensedType, thresholdError, clusterHeadNode);
		// WsnMsg wsnMessage = new WsnMsg(1, this, wsnMsgResp.source , this, 1, wsnMsgResp.sizeTimeSlot, dataSensedType, thresholdError);
		wsnMessage.setCoefs(coeficienteA, coeficienteB);
		wsnMessage.setPathToSenderNode(wsnMsgResp.clonePath());
		sendToNextNodeInPath(wsnMessage);
	} // end sendCoefficients(WsnMsgResponse wsnMsgResp, double coeficienteA, double coeficienteB, Node clusterHeadNode)
}
