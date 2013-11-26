package projects.wsnee.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import projects.wsnee.nodes.messages.WsnMsg;
import projects.wsnee.nodes.messages.WsnMsgResponse;
import projects.wsnee.nodes.timers.WsnMessageTimer;
import projects.wsnee.utils.ArrayList2d;
import projects.wsnee.utils.Utils;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;

public class SinkNode extends SimpleNode 
{

	/**
	 * Número de dados sensoriados por time slot (Tamanho do time slot inicial) <p>
	 * [Eng] Number of sensed data per time slot (initial time slot size)
	 */
	private Integer sizeTimeSlot = 100;
	
	/**
	 * Quantidade de rounds (ciclos) a ser saltado para cada leitura sequencial dos sensores, no caso de uso da abordagem de ClusterHeads (ACS=True) <p>
	 * [Eng] Number of rounds (cycles) to be jumped for each sequential sensor reading in the case of using the approach of ClusterHeads (ACS=True)
	 */
	public static final int sensorTimeSlot = 1;
	
	/**
	 * Número de dados sensoriados por time slot (Tamanho do time slot) <p>
	 * [Eng] Number of sensed data per time slot (time slot size)
	 */
	private Integer sizeTimeUpdate = 5;
	
	/**
	 * Tipo de dado a ser sensoriado (lido nos nós sensores), que pode ser: "t"=temperatura, "h"=humidade, "l"=luminosidade ou "v"=voltagem <p>
	 * [Eng] Type of data to be sensed (read in the sensor nodes), which can be: "t" = temperature, "h" = humidity, "l" = brightness or "v" = voltage
	 */
	private String dataSensedType = "t";
	
	/**
	 * Percentual do limiar de erro temporal aceitável para as leituras dos nós sensores, que pode estar entre 0.0 (não aceita erros) e 1.0 (aceita todo e qualquer erro) <p>
	 * [Eng] Percentage of temporal acceptable error threshold for the readings of sensor nodes, which may be between 0.0 (accepts no errors) and 1.0 (accepts any error)
	 */
	private double thresholdError = 0.05; // te
	
	/**
	 * Limite de diferença de magnitude aceitável (erro espacial) para as leituras dos nós sensores /--que pode estar entre 0.0 (não aceita erros) e 1.0 (aceita todo e qualquer erro) <p>
	 * [Eng] Limit of acceptable magnitude difference (spatial error) for the readings of sensor nodes / - which can be between 0.0 (no errors accepted) and 1.0 (accepts any error)
	 */
	private double spacialThresholdError = 1.5;
	
	/**
	 * Percentual mínimo do número de rounds iguais das medições de 2 sensores para que os mesmos sejam classificados no mesmo cluster <p>
	 * [Eng] Minimum percentage of the number of equal measurement rounds of 2 sensors so that they are classified in the same cluster
	 */
//	private double equalRoundsThreshold = 0.5;
	
	/**
	 * Percentual mínimo das medições de 2 sensores (no mesmo round) a ficar dentro dos limiares aceitáveis para que os mesmos sejam classificados no mesmo cluster <p>
	 * [Eng] Minimum percentage of measurements of two sensors (in the same round) to stay within the acceptable thresholds for them to be classified in the same cluster
	 */
//	private double metaThreshold = 0.5;
	
	/**
	 * Distância máxima aceitável para a formação de clusters. Se for igual a zero (0,0), não considerar tal limite (distância) <p>
	 * [Eng] Maximum distance acceptable to the formation of clusters. If it is equal to zero (0.0), ignoring
	 */
	private double maxDistance = 8.0; //8.0;
	
	/**
	 * Número total de nós sensores presentes na rede <p>
	 * [Eng] Total number of sensor nodes in the network
	 */
	private static int numTotalOfSensors = 54;

	/**
	 * Indica que o sink node sinaliza para todos os outros nós que deve ficar continuamente com sensoriamento (usando Cluster Heads) <p>
	 * [Eng] Indicates that sink node signalize to all other nodes must continuously sensing (using Cluster Heads)
	 */
	private boolean allSensorsMustContinuoslySense = true; // ACS: false = Representative Nodes; true = Cluster Heads
	
	/**
	 * Emblema para indicar que o sink ainda não agrupou todos nós para a primeira vez <p>
	 * [Eng] Flag to indicate that the sink still not clustered all nodes for the first time
	 */
	private boolean stillNonclustered = true;
	
	/**
	 * Array 2D (clusters) a partir de sensores (Mensagens de sensores = WsnMsgResponse) <p>
	 * [Eng] Array 2D (clusters) from sensors (Messages from sensors = WsnMsgResponse)
	 */
	private static ArrayList2d<WsnMsgResponse> messageGroups;
	
	private ArrayList2d<WsnMsgResponse> newCluster;
	
	private ArrayList2d<WsnMsgResponse> nodesToReceiveDataReading;
	
	/**
	 * "Lista Negra" é uma lista de mensagens (nós de origem) já recebidos pelo sink (e removido os nodesToReceiveDataReading) <p>
	 * [Eng] "BlackList" is the list of messages (source nodes) already received by sink (and removed from nodesToReceiveDataReading)
	 */
	private ArrayList<WsnMsgResponse> blackList;
	
	/**
	 * Número de mensagens recebidas pelo nó sink de todos os outros nós sensores <p> 
	 * [Eng] Number of messages received by sink node from all other sensors nodes
	 */
	private int numMessagesReceived = 0;
	
	/**
	 * Número de rounds (ciclos) para reagrupamento (reclustering) dos sensores no caso de uso de Nós Representativos <p>
	 * Number of rounds (cycles) for reclustering of the sensors in use cases of Representatives Nodes
	 */
//	private int numRoundsForReclustering = 30;
	
	/**
	 * Número de mensagens de erros de predição recebidos pelo sink node de todos os outros nós sensores <p>
	 * [Eng] Number of messages of error prediction received by sink node from all other sensors nodes
	 */
	private int numMessagesOfErrorPredictionReceived = 0;
	
	/**
	 * Número de mensagens do espaço horário terminado recebido pelo sink node de todos os outros nós sensores <p>
	 * [Eng] Number of messages of time slot finished received by sink node from all other sensors nodes
	 */
	private int numMessagesOfTimeSlotFinishedReceived = 0;
	
	/**
	 * Número de mensagens do espaço horário terminado recebido pelo sink node de todos os outros nós sensores <p>
	 * [Eng] Number of messages of time slot finished received by sink node from all other sensors nodes
	 */
	private int numMessagesOfLowBatteryReceived = 0;
	
	private int expectedNumberOfSensors = 0;
	private int numMessagesExpectedReceived = 0;
	
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
				
				if (wsnMsgResp.typeMsg == 2) // Se é uma mensagem de um Nó Representativo / Cluster Head que excedeu o #máximo de erros de predição
				{
					numMessagesOfErrorPredictionReceived++;
					
// CASO O CLUSTER PRECISE SOFRER UM SPLIT, UMA MENSAGEM SOLICITANDO UM NOVO ENVIO DE DADOS PARA O SINK DEVE SER ENVIADA PARA CADA UM DOS NÓS DO CLUSTER 
					
					//int lineFromCluster = identifyCluster(wsnMsgResp);
					
					
					
					
					
					int lineFromCluster = searchAndReplaceNodeInClusterByMessage(wsnMsgResp);
					if (lineFromCluster >= 0)
					{
						expectedNumberOfSensors += sendSenseRequestMessageToAllSensorsInCluster(messageGroups, lineFromCluster);
						triggerSplitFromCluster(lineFromCluster);
					}
				
// CASO O NÓ QUE TENHA ENVIADO A MsgResp SEJA UM CLUSTER HEAD???
					if (wsnMsgResp.target != null)
					{
						Utils.printForDebug("Inside the code => if (wsnMsgResp.target != null)");						
					}
					//receiveMessage(wsnMsgResp, null); // Recebe a mensagem, para recálculo dos coeficientes e reenvio dos mesmos àquele nó sensor (Nó Representativo), mantendo o número de predições a serem executadas como complemento do total calculado inicialmente, ou seja, NÃO reinicia o ciclo de time slot daquele cluster
				}
				
				else if (wsnMsgResp.typeMsg == 3) // Se é uma mensagem de um Nó Representativo que excedeu o #máximo de predições (timeSlot)
				{
					numMessagesOfTimeSlotFinishedReceived++;
					
					int lineFromClusterNode = searchAndReplaceNodeInClusterByMessage(wsnMsgResp); // Procura a linha (cluster) da mensagem recebida e atualiza a mesma naquela linha
					
					if (lineFromClusterNode >= 0) // Se a linha da mensagem recebida for encontrada
					{
						classifyNodesByAllParams(messageGroups);
						
						WsnMsgResponse wsnMsgResponseRepresentative = messageGroups.get(lineFromClusterNode, 0); // Get the (new) Representative Node (or Cluster Head)
						int numSensors = messageGroups.getNumCols(lineFromClusterNode);
						Utils.printForDebug("Cluster / Line number = "+lineFromClusterNode+"\n");
						wsnMsgResponseRepresentative.calculatesTheSizeTimeSlotFromRepresentativeNode(sizeTimeSlot, numSensors);
						
						receiveMessage(wsnMsgResponseRepresentative, null);
					}
					// PAREI AQUI!!! - Fazer testes para verificar se os clusters estão sendo reconfigurados quando um No Repres. finaliza seu time slot e atualiza o status de sua bateria!
				} // else if (wsnMsgResp.typeMsg == 3)
				
				else if (wsnMsgResp.typeMsg == 4) // Se é uma mensagem de um Nó Representativo/Cluster Head cujo nível da bateria está abaixo do mínimo (SimpleNode.minBatLevelInClusterHead)
				{
					numMessagesOfLowBatteryReceived++;
					
					if (allSensorsMustContinuoslySense) { // Se é uma mensagem de um Cluster Head // Se for um ClusterHead (ClusterHead != null)
						
						int lineFromClusterNode = searchAndReplaceNodeInClusterByMessage(wsnMsgResp); // Procura a linha (cluster) da mensagem recebida e atualiza a mesma naquela linha
						
						if (lineFromClusterNode >= 0) // Se a linha da mensagem recebida for encontrada
						{
							classifyNodesByAllParams(messageGroups); // Reclassifica todos os nós do cluster atual - cujo Cluster Head teve decaimento do nível de bateria
							
							int numSensors = messageGroups.getNumCols(lineFromClusterNode);
							Node chNode = (messageGroups.get(lineFromClusterNode, 0)).source; // Get the (new) Cluster Head from the current cluster/line

							Utils.printForDebug("Cluster / Line number = "+lineFromClusterNode+"; ClusterHead / IDnumber = "+chNode.ID+"; #Sensors = "+numSensors);
							for (int col=0; col < numSensors; col++) // For each node from that cluster (in messageGroups), it must communicate who is the new ClusterHead
							{
								WsnMsgResponse wsnMsgResponseCurrent = messageGroups.get(lineFromClusterNode, col); // Get the Node
								
								wsnMsgResponseCurrent.sizeTimeSlot = sensorTimeSlot; // If all sensor nodes in Cluster must continuosly sense, so the sizeTimeSlot will be the sensorTimeSlot
								
								receiveMessage(wsnMsgResponseCurrent, chNode);
							} // end for (int col=0; col < numSensors; col++)
						} // end if (lineFromClusterNode >= 0)
					
					
					} // end if (allSensorsMustContinuoslySense)
					
					// Se for um Nó Representativo (ClusterHead == null) - VERIFICAR ESTE CÓDIGO!!!
					else { //if (!allSensorsMustContinuoslySense) { // Se é uma mensagem de um Nó Representativo
						int lineFromClusterNode = searchAndReplaceNodeInClusterByMessage(wsnMsgResp); // Procura a linha (cluster) da mensagem recebida e atualiza a mesma naquela linha
						
						if (lineFromClusterNode >= 0) // Se a linha da mensagem recebida for encontrada
						{
							classifyNodesByAllParams(messageGroups);
							
							WsnMsgResponse wsnMsgResponseRepresentative = messageGroups.get(lineFromClusterNode, 0); // Get the (new) Representative Node (or Cluster Head)
							int numSensors = messageGroups.getNumCols(lineFromClusterNode);
							Utils.printForDebug("Cluster / Line number = "+lineFromClusterNode+"\n");
							wsnMsgResponseRepresentative.calculatesTheSizeTimeSlotFromRepresentativeNode(sizeTimeSlot, numSensors);						
							receiveMessage(wsnMsgResponseRepresentative, null);
						} // end if (lineFromClusterNode >= 0)
					}  // end else
				} // end else if (wsnMsgResp.typeMsg == 4)
				
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
							classifyNodesByAllParams(messageGroups);
							Global.clustersCount = messageGroups.getNumRows(); // It sets the number of clusters (lines in messageGroups) to the Global.clustersCount attribute
							setClustersFromNodes(messageGroups);
							
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
											wsnMsgResponseCurrent.sizeTimeSlot = sensorTimeSlot; // If all sensor nodes in Cluster must continuosly sense, so the sizeTimeSlot doesn't matter
											
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
						
//						System.out.println("CHEGOU O NODE ID "+wsnMsgResp.source.ID+" NO SINK!");
						
						if (newCluster == null) // If a new cluster (temp) has not yet been created (instanciated)
						{
							newCluster = new ArrayList2d<WsnMsgResponse>(); // Instanciate him
							newCluster.ensureCapacity(expectedNumberOfSensors);
							newCluster.add(wsnMsgResp, 0); // Adds the new response message sensor to new cluster

						} // end if (newCluster == null)
						else // If already there is a new cluster (created)
						{
							addNodeInClusterClassifiedByMessage(newCluster, wsnMsgResp);
						} // end else if (newCluster == null)

						if (blackList == null) { // If BlackList is not created / instanciate yet...
							blackList = new ArrayList<WsnMsgResponse>(); // Instanciate him
						}
						blackList.add(wsnMsgResp); // ... até aqui
						
						expectedNumberOfSensors--;

						if (removeNodeAndChecksIfDataReceivedFromAllNodesInCluster(nodesToReceiveDataReading, wsnMsgResp))
//							if (numMessagesExpectedReceived >= expectedNumberOfSensors) // If all messagesResponse (from all nodes in Cluster to be splited) already done received
						{
							classifyNodesByAllParams(newCluster);
							
							setClustersFromNodes(newCluster);

							//NESTE PONTO, É PRECISO MANDAR MENSAGEM PARA OS NOVOS NÓS REPRESENTATIVOS PARA QUE OS MESMOS INICIEM UMA NOVA FASE (Novo ciclo de sensoriamento)
							
							// (CICLO) DE SENSORIAMENTO
							ArrayList<Integer> linesToBeUnified = new ArrayList<Integer>();
							for (int line = 0; line < newCluster.getNumRows(); line++) // For each line (group/cluster) from newCluster
							{
								if (isAllNodesInThisClusterLineInList(newCluster, line, blackList)) {

									linesToBeUnified.add(line);

									int numSensors = newCluster.getNumCols(line);
									Utils.printForDebug("Cluster / Line number = "+line);

									if (!allSensorsMustContinuoslySense) { // If only the representative nodes must sensing (Representative nodes approach)
										WsnMsgResponse wsnMsgResponseRepresentative = newCluster.get(line, 0); // Get the Representative Node (or Cluster Head)
										wsnMsgResponseRepresentative.calculatesTheSizeTimeSlotFromRepresentativeNode(sizeTimeSlot, numSensors);
										receiveMessage(wsnMsgResponseRepresentative, null);
									} // end if (!allSensorsMustContinuoslySense)
									
									else { // If all nodes in cluters must sensing, and not only the representative nodes (Cluster heads approach)
										Node chNode = (newCluster.get(line, 0)).source; // Cluster Head from the current cluster/line
										for (int col=0; col < numSensors; col++) // For each colunm from that line in newCluster
										{
											WsnMsgResponse wsnMsgResponseCurrent = newCluster.get(line, col); // Get the current node
											wsnMsgResponseCurrent.sizeTimeSlot = sensorTimeSlot; // If all sensor nodes in each cluster must continuosly sense, so the sizeTimeSlot doesn't matter
											receiveMessage(wsnMsgResponseCurrent, chNode);
										} // end for (int col=0; col < numSensors; col++)
									} // end else if (!allSensorsMustContinuoslySense)
									
									
								}
							} // end for (int line=0; line < newCluster.getNumRows(); line++)
							
							unifyClusters(messageGroups, newCluster, linesToBeUnified, blackList); // TESTAR SE MÉTODO FUNCIONA CORRETAMENTE!!!???
							Global.clustersCount = messageGroups.getNumRows(); // It sets the number of clusters (lines in messageGroups) to the Global.clustersCount attribute

						} // end if (removeNodeAndChecksIfDataReceivedFromAllNodesInCluster(nodesToReceiveDataReading, wsnMsgResp))
						// end if (numMessagesExpectedReceived >= expectedNumberOfSensors)
						
					} // end else if (stillNonclustered)
					
				} // end else
			} // end if (message instanceof WsnMsg)
		} //end while (inbox.hasNext())
	} //end handleMessages()
	
	/**
	 * Testa se todos os nós em número de linha "row" do "tempCluster" estão dentro do "blkList", se o sink(sumidouro) já recebeu respostas de mensagens de todos os nós do respectivo Cluster<p> 
	 * [Eng]Test if all nodes in line number "row" of "tempCluster" are in "blkList", ie, if the sink have already received message responses from all nodes in the respective Cluster 
	 * @param tempCluster Novo GroupCluster com os nós para serem "Unidos" <b> tempCluster </b><p>
	 * [Eng] @param tempCluster New GroupCluster with the nodes to be "merged" <b> tempCluster </b>
	 * @param row Linha do Novo GroupCluster a ser testada <b> row </b><p>
	 * [Eng] @param row Line from the New GroupCluster to be tested <b> row </b>
	 * @param blkList Lista Negra com os nós que já receberam do sink <b> blkList </b><p>
	 * [Eng] @param blkList Black List with the nodes that have already received by the sink <b> blkList </b>
	 * @return Verdadeiro se todos os nós em linha "linha" de "tempCluster" estão na "blkList" <p>
	 * [Eng] @return True if all nodes in line number "row" of "tempCluster" are in "blkList"
	 */
	private boolean isAllNodesInThisClusterLineInList(ArrayList2d<WsnMsgResponse> tempCluster, int row, ArrayList<WsnMsgResponse> blkList) {
		if (tempCluster == null) { // If there isn't a message group yet
			System.out.println("ERROR in isAllNodesInThisClusterLineInList method: There isn't tempCluster object instanciated yet!");
			return false;
		}
		else if (blkList == null) { // If there isn't a black list yet
			System.out.println("ERROR in isAllNodesInThisClusterLineInList method: There isn't blkList object instanciated yet!");
			return false;
		}
		else {
			for (int i = 0; i < tempCluster.get(row).size(); i++) {
				boolean found = false;
				int j = 0;
				while (!(found) && (j < blkList.size())) {
					if (isEqualNodeSourceFromMessages(tempCluster.get(row,i), blkList.get(j))) {
						found = true;
					}
					else {
						j++;
					}
				}
				if (!found) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Ele classifica os 'clusters de nós' pela 'energia residual' e pelos 'saltos para o sink' e imprime as configuração do cluster antes, durante e após nova ordem <p>
	 * [Eng] It classify the 'clusters nodes' by 'residual energy' and by 'hops to sink' and prints cluster configuration before, during and after new order
	 * @param cluster Grupo de mensagens (que representam agrupamentos) que serão classificados de acordo com parâmetros ("energia residual 'e'saltos para o sink')<b> cluster </b><p>
	 * [Eng] @param Group of messages (representing clusters) which will be classified according with params ('residual energy' and 'hops to sink') <b> cluster </b>
	 */
	void classifyNodesByAllParams(ArrayList2d<WsnMsgResponse> cluster) {
		Utils.printForDebug("@ @ @ MessageGroups BEFORE classification:\n");
		printClusterArray2d(cluster);
		
		classifyRepresentativeNodesByResidualEnergy(cluster); // (Re)classifica os nós dos clusters por energia residual
		
		Utils.printForDebug("@ @ @ MessageGroups AFTER FIRST classification:\n");
		printClusterArray2d(cluster);
		
		classifyRepresentativeNodesByHopsToSink(cluster); // (Re)classifica os nós dos clusters por saltos até o sink node 
		
		Utils.printForDebug("@ @ @ MessageGroups AFTER SECOND classification:\n"); 
		printClusterArray2d(cluster);
	} // end classifyNodesByAllParams(ArrayList2d<WsnMsgResponse> cluster)
	
	/**
	 * Ele define todos os clusters para cada WsnMsgResponse representando os nós de origem<p>
	 * [Eng]It sets all clusters for each WsnMsgResponse representing the source nodes  
	 * 
	 * @param clusterGroup  Grupo de mensagens (representando agrupamentos) que têm os clusters configurados <b> clusterGroup</b><p>
	 * [Eng] @param clusterGroup Group of messages (representing clusters) to have the clusters configurated <b> clusterGroup </b>
	 */
	void setClustersFromNodes(ArrayList2d<WsnMsgResponse> clusterGroup) {
		if (clusterGroup != null) // If there is a message group created
		{
			for (int line=0; line < clusterGroup.getNumRows(); line++)
			{
				Cluster currentCluster = new Cluster(clusterGroup.get(line, 0).source);
				currentCluster.setMembers( convertArrayListMsgResponsesToArrayListNodes(clusterGroup.get(line)) );
				for (int col=0; col < clusterGroup.getNumCols(line); col++)
				{
					WsnMsgResponse currentWsnMsgResp = clusterGroup.get(line, col);
					currentWsnMsgResp.cluster = currentCluster;
				} // end for (int col=0; col < clusterGroup.getNumCols(line); col++)
			} // end for (int line=0; line < clusterGroup.getNumRows(); line++)
		} // end if (clusterGroup != null)
	} // end setClustersFromNodes(ArrayList2d<WsnMsgResponse> clusterGroup)
	
	/**
	 * Ele recebe um ArrayList de WsnMsgResponse e retorna um ArrayList de nó com nós de origem destas mensagens<p>
	 * [Eng] It receives an ArrayList of WsnMsgResponse and returns an ArrayList of Node with source nodes from that messages
	 * @param lineMessages ArrayList de WsnMsgResponse <b>lineMessages</b><p>
	 * [Eng] @param lineMessages ArrayList of WsnMsgResponse <b>lineMessages</b>
	 * @return ArrayList de nós de origem <p>
	 * [Eng] @return ArrayList of source nodes
	 */
	ArrayList<Node> convertArrayListMsgResponsesToArrayListNodes(ArrayList<WsnMsgResponse> lineMessages) {
		ArrayList<Node> tempNodes = null;
		if (lineMessages != null) {
			tempNodes = new ArrayList<Node>(); 
			for (int cont=0; cont < lineMessages.size(); cont++) {
				tempNodes.add(lineMessages.get(cont).source);
			}
		}
		return tempNodes;
	} // end convertArrayListMsgResponsesToArrayListNodes(ArrayList<WsnMsgResponse> lineMessages)
	
	/**
	 * Adiciona os clusters (linhas) de "tempClusterGiver" (em "rowIndexes" linhas) dentro do "tempClusterReceiver", retire os elementos de "lista negra" e remove estas linhas de "tempClusterGiver" no final <p>
	 * [Eng] Adds the clusters (lines) from "tempClusterGiver" (in "rowIndexes" lines) inside the "tempClusterReceiver", remove the elements from "blackList" and removes this lines from "tempClusterGiver" at the end
	 * @param tempClusterReceiver Estrutura do cluster que receberá os sensores / cluster a partir da estrutura tempClusterGiver <b>tempClusterReceiver</b><p>
	 * [Eng] @param tempClusterReceiver Cluster structure that will receive the sensors/clusters from the tempClusterGiver structure <b>tempClusterReceiver</b>
	 * @param tempClusterGiver Estrutura do cluster que vai dar os sensores / cluster à estrutura tempClusterReceiver <b> tempClusterGiver </b><p>
	 * [Eng] @param tempClusterGiver Cluster structure that will give the sensors/clusters to the tempClusterReceiver structure <b> tempClusterGiver </b>
	 * @param rowIndexes Índices de linha de "tempClusterGiver" para ser movido para "tempClusterReceiver" <b> rowIndexes </b><p>
	 * [Eng] @param rowIndexes Line indexes from "tempClusterGiver" to be moved to "tempClusterReceiver" <b> rowIndexes </b>
	 * @param blackList Lista que os elementos serão removidos da <b>lista negra</b><p>
	 * [Eng] @param blackList List which the elements will be removed from <b> blackList </b>
	 */
	private void unifyClusters(ArrayList2d<WsnMsgResponse> tempClusterReceiver, ArrayList2d<WsnMsgResponse> tempClusterGiver, ArrayList<Integer> rowIndexes, ArrayList<WsnMsgResponse> blackList)
	{
		int rowReceiver, rowGiver = 0, col;
		rowReceiver = tempClusterReceiver.getNumRows();
		while (rowGiver < rowIndexes.size())
		{
			col = 0;
			while (col < tempClusterGiver.getNumCols(rowIndexes.get(rowGiver)))
			{
				removeFromList(blackList, tempClusterGiver.get(rowIndexes.get(rowGiver), col));
				tempClusterReceiver.add(tempClusterGiver.get(rowIndexes.get(rowGiver), col), rowReceiver);
				col++;
			} // end while (col < tempClusterGiver.getNumCols(rowIndexes.get(rowGiver)))
			rowGiver++;
			rowReceiver++;
		} // end while (rowGiver < rowIndexes.size())
		for (int i = (rowIndexes.size()-1); i >= 0; i--) {
			tempClusterGiver.remove(rowIndexes.get(i));
		}
		
	} // end unifyClusters(ArrayList2d<WsnMsgResponse> tempClusterReceiver, ArrayList2d<WsnMsgResponse> tempClusterGiver)
	
	/**
	 * Ele remove o "msgToRemoveFromList" da lista "list" (lista negra) que passou por parâmetro <p>
	 * [Eng] It removes the "msgToRemoveFromList" from the list "list" (blackList) passed by param
	 * @param list Um objeto ArrayList (WsnMsgResponse) <b> list </b><p>
	 * [Eng] @param list An ArrayList(WsnMsgResponse) object <b> list </b>
	 * @param msgToRemoveFromList Um objeto WsnMsgResponse <b> msgToRemoveFromList </b><p>
	 * [Eng] @param msgToRemoveFromList A WsnMsgResponse object <b> msgToRemoveFromList </b>
	 */
	private void removeFromList(ArrayList<WsnMsgResponse> list, WsnMsgResponse msgToRemoveFromList) {
		if (list == null) { // 
			System.out.println("ERROR in removeFromList method: There isn't listToRemove object instanciated!");
		} // end if (listToRemove == null)
		else if (msgToRemoveFromList == null) { 
			System.out.println("ERROR in removeFromList method: There isn't msgToRemove object instanciated!");
		} // end else if (msgToRemove == null)
		else {
			int cont = 0;
			while (cont < list.size())
			{
				if (isEqualNodeSourceFromMessages(list.get(cont), msgToRemoveFromList)) {
					list.remove(cont);
				} // end if (isEqualNodeSourceFromMessages(listToRemove.get(cont), msgToRemove))
				cont++;
			} // end while (cont < listToRemove.size())
		} // end else
	} // end removeFromList(ArrayList<WsnMsgResponse> listToRemove, WsnMsgResponse msgToRemove)
	
	/**
	 * Ele aciona o processo de dividir um cluster, através da exclusão (retirar) a partir da linha do antigo cluster - para ser separado <p>
	 * [Eng] It triggers the process to split a cluster, through the exclusion (remove) from the line from old cluster - to be splitted
	 * @param lineFromCluster Número da linha do cluster para ser dividido <b> lineFromCluster </b><p>
	 * [Eng] @param lineFromCluster Line number from cluster to be divided <b> lineFromCluster </b>
	 */
	private void triggerSplitFromCluster(int lineFromCluster)
	{
		// IDEIA: O nó representativo acabou de enviar seus últimos dados de leitura (sensoriamento), então ele não precisa enviar novamente
		// Remover cluster (nós do cluster) de messageGroups
		nodesToReceiveDataReading = ensuresArrayList2d(nodesToReceiveDataReading);
		
		if (messageGroups != null && nodesToReceiveDataReading != null) {
			Utils.printForDebug("Antes: messageGroups.numRows = "+messageGroups.getNumRows()+" and nodesToReceiveDataReading.numRows = "+nodesToReceiveDataReading.getNumRows());
			Utils.printForDebug("messageGroups");
			printClusterArray(messageGroups);
			messageGroups.transferRowTo(lineFromCluster, nodesToReceiveDataReading);
			Utils.printForDebug("Depois: messageGroups.numRows = "+messageGroups.getNumRows()+" and nodesToReceiveDataReading.numRows = "+nodesToReceiveDataReading.getNumRows());
			Utils.printForDebug("messageGroups");
			printClusterArray(messageGroups);
			Utils.printForDebug("nodesToReceiveDataReading");
			printClusterArray(nodesToReceiveDataReading);
		}
		else {
			System.out.print("Round = "+Global.currentTime+": ");
			if (messageGroups == null) {
				System.out.println("messageGroup = NULL ! ");
			}
			if (nodesToReceiveDataReading == null) {
				System.out.println("nodesToReceiveDataReading = NULL !");
			}
		}


		//messageGroups.remove(lineFromCluster);
		
		// Armazenar quantos nós existiam neste cluster, para saber quando todos terminaram de responder
		// Identificá-los pelo clusterHeadID
	} // end triggerSplitFromCluster(int lineFromCluster)
	
	/**
	 * Quando um nó sensor envia uma mensagem para sink indicando uma novidade, o sink deve atirar / enviar mensagens para todos os sensores desse cluster exigem novas leituras de sensores para verificar a similaridade atual das leituras dos cluster de sensores <p>
	 * [Eng] When a sensor node sends a message to sink indicating a novelty, the sink must shoot/send messages to all sensors in that cluster requiring new readings from sensors to verify the current similarity of the readings from cluster`s sensors 
	 * @param tempCluster Cluster (acertamento de cluster) para ser utilizado como base - A estrutura é realmente um acertamento de cluster, em que cada cluster é representado por uma linha diferente <b> tempCluster </b><p>
	 * [Eng] @param tempCluster Cluster (set of clusters) to be used as base - The structure actually is a set of clusters, where each cluster is represented by a different line <b> tempCluster </b>
	 * @param lineFromCluster Número da linha do cluster atual <b> lineFromCluster </b><p>
	 * [Eng] @param lineFromCluster Line number from current cluster <b> lineFromCluster </b>
	 * @return Número de sensores (#colunas) no cluster / linha de lineFromCluster <p>
	 * [Eng] @return Number of sensors (#columns) in the cluster/line from lineFromCluster
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
				//currentWsnMsgResp.hopsToTarget--;

				WsnMsg wsnMessage = new WsnMsg(1, this, currentWsnMsgResp.source, this, 1, sizeTimeUpdate, dataSensedType);
								
				wsnMessage.removeCoefs(); // Identifies this message as requesting sensing and not sending coefficients
				wsnMessage.setPathToSenderNode(currentWsnMsgResp.clonePath(), currentWsnMsgResp.hopsToTarget); // Sets the path (route) to destination node (source) - same "currentWsnMsgResp.origem"
//				System.out.println("Mensagem solicitando dados enviada para o node ID = "+currentWsnMsgResp.source.ID);
				
				wsnMessage.hopsToTarget--;
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
	 * Ele identifica qual cluster (número da linha), onde o "newWsnMsgResp" está<p>
	 * [Eng] It identifies which cluster (line number) where the "newWsnMsgResp" is 
	 * @param newWsnMsgResp MessageResponse representando o nó sensorial para ser localizado na messageGroups <b> newWsnMsgResp </b><p>
	 * [Eng] @param newWsnMsgResp MessageResponse representing the sensor node to be localized in messageGroups <b> newWsnMsgResp </b>
	 * @return Número da linha (cluster) de mensagem passada, caso contrário, retorna -1 indicando que não existe essa mensagem ("newWsnMsgResp") em qualquer cluster <p>
	 * [Eng] @return Line number (cluster) from message passed by; otherwise, returns -1 indicating that there is no such message("newWsnMsgResp") in any cluster
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
	 * Ele identifica qual cluster(número da linha), onde o "representativeNode" está <p>
	 * [Eng] It identifies which cluster (line number) where the "representativeNode" is 
	 * @param representativeNode Nó representando o nó sensor representante a ser localizado no messageGroups <b> representativeNode </b><p>
	 * [Eng] @param representativeNode Node representing the representative sensor node to be localized in messageGroups <b> representativeNode </b>
	 * @return Número da linha (cluster) do nó passado, caso contrário, retorna -1 indicando que não existe tal nó ("representativeNode") em qualquer cluster<p>
	 * [Eng] @return Line number (cluster) from node passed by; otherwise, returns -1 indicating that there is no such node("representativeNode") in any cluster
	 */ 
	private static int identifyCluster(Node representativeNode)
	{
		int lineCLuster = -1;
		if (messageGroups == null) // If there isn't a message group yet
		{
			Utils.printForDebug("ERROR in identifyCluster(Node) method: There isn't messageGroups object instanciated yet!");
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
					Node currentNode = messageGroups.get(line, col).source;
					if (isEqualNode(currentNode, representativeNode))
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
	 * Ele remove o nó (newWsnMsgResp.source passado por parâmetro ) do grupo/cluster indicado por "tempCluster" e indicar se o grupo tornou-se vazio <p>
	 * [Eng] It removes the node (newWsnMsgResp.source passed by param) from the group/cluster indicated by "tempCluster" and indicate if the cluster became empty
	 * @param tempCluster Grupo de aglomerados que o nó (newWsnMsgResp.source) que serão removidos <b> tempCluster </b><p>
	 * [Eng] @param tempCluster Group of clusters which the node (newWsnMsgResp.source) will be removed <b> tempCluster </b>
	 * @param newWsnMsgResp  Mensagem que contém o nó (newWsnMsgResp.source), que será removido do grupo de aglomerado <b> newWsnMsgResp </b><p>
	 * [Eng] @param newWsnMsgResp Message that contains the node (newWsnMsgResp.source) which will be removed from group of cluster <b> newWsnMsgResp </b>
	 * @return Se o cluster desta mensagem / nó ficou vazio após a remoção do nó "newWsnMsgResp.source" <p>
	 * [Eng] @return If the cluster of this message / node became empty after removal of the node in "newWsnMsgResp.source"
	 */
	private boolean removeNodeAndChecksIfDataReceivedFromAllNodesInCluster(ArrayList2d<WsnMsgResponse> tempCluster, WsnMsgResponse newWsnMsgResp)
	{
		boolean receivedAll = false;
		if (tempCluster == null) // If there isn't a message group yet
		{
			Utils.printForDebug("ERROR in removeNodeAndChecksIfDataReceivedFromAllNodesInCluster method: There isn't tempCluster object instanciated yet!");
		}
		else if (newWsnMsgResp == null) {
			Utils.printForDebug("ERROR in removeNodeAndChecksIfDataReceivedFromAllNodesInCluster method: The newWsnMsgResp object received is NULL!");
		}
		else
		{
			boolean found = false;
			int line = 0, col = 0;
			while ((!found) && (line < tempCluster.getNumRows()))
			{
				col = 0;
				while ((!found) && (col < tempCluster.getNumCols(line)))
				{
					WsnMsgResponse currentWsnMsgResp = tempCluster.get(line, col);
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
				if (tempCluster.getNumCols(line) == 1) { // It means that this is the last node in this line (cluster), and it will be removed so the cluster will be empty
					tempCluster.remove(line);
					receivedAll = true; // Then the sink have received all data node message from the current cluster
/*					
					if (tempCluster.getNumRows() == 0) { // If the group (total cluster) became empty
						receivedAll = true; // Then the sink have received all data node message 
					}
*/
				}
				else {
					tempCluster.remove(line,col);
				}
			}
		}
		return receivedAll;
	} // end removeNodeAndChecksIfDataReceivedFromAllNodesInCluster(ArrayList2d<WsnMsgResponse> tempCluster, WsnMsgResponse newWsnMsgResp)

	
	/**
	 * Ele seleciona o nó representante para cada linha (cluster) dos sensores pela energia residual máxima e coloca-lo na primeira posição (em linha) <p>
	 * [Eng] It selects the Representative Node for each line (cluster) from sensors by the max residual energy and puts him in the first position (in line)
	 * @param tempCluster cluster(ArrayList), que terá os sensores ordenados (linha por linha) pela energia residual máxima <b> tempCluster </b><p>
	 * [Eng] @param tempCluster Cluster (ArrayList) which will have the sensors ordered (line by line) by the max residual energy <b> tempCluster </b>
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
	 * Ele classifica os nós para cada linha (cluster) dos sensores pela mínima distância (em número de saltos) para o sink entre eles que têm a mesma energia residual máximo e coloca-lo na primeira posição (em linha) <p>
	 * [Eng] It classifies the Nodes for each line (cluster) from sensors by the min distance (in number of hops) to sink among them who have the same max residual energy and puts him in the first position (in line)
	 * @param tempCluster Cluster(ArrayList), que terá os sensores ordenados (linha por linha - como um segundo critério) pelo número mínimo de saltos para o sink <b> tempCluster </b><p>
	 * [Eng] Cluster (ArrayList) which will have the sensors ordered (line by line - as a second criterion) by the min number of hops to sink <b> tempCluster </b>
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
	 * Ele imprime e colore os nós pelos clusters (parâmetro) formados <p>
	 * [Eng] It prints and colore nodes by the clusters (param) formed
	 * @param cluster
	 */
	private void printClusterArray2d(ArrayList2d<WsnMsgResponse> cluster)
	{
		if (cluster != null) // If there is a message group created
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
			for (int line=0; line < cluster.getNumRows(); line++)
			{
				for (int col=0; col < cluster.getNumCols(line); col++)
				{
					WsnMsgResponse currentWsnMsgResp = cluster.get(line, col);
					currentWsnMsgResp.source.setColor(currentRandomColor);
					Utils.printForDebug("Line = "+line+", Col = "+col+": NodeID = "+currentWsnMsgResp.source.ID+" BatLevel = "+currentWsnMsgResp.batLevel+" Round = "+((SimpleNode)currentWsnMsgResp.source).lastRoundRead);
				}
				Utils.printForDebug("\n");
				codColor += 1;
				codColor = (codColor < 29 ? codColor : 0);
				currentRandomColor = arrayColor[codColor];
//				currentRandomColor = new Color(codColor);
			}
			Utils.printForDebug("Number of Lines / Clusters = "+cluster.getNumRows()+"\n");
		}
	} // end printClusterArray2d(ArrayList2d<WsnMsgResponse> cluster)

	/**
	 * Ela imprime nó por nó, cada cluster (Parâmetro) formado <p>
	 * [Eng] It prints node by node by each cluster (param) formed
	 */
	private void printClusterArray(ArrayList2d<WsnMsgResponse> cluster)
	{
		if (cluster != null) // If there is a message group created
		{
			for (int line=0; line < cluster.getNumRows(); line++)
			{
				Utils.printForDebug("Line = "+line+": ");
				for (int col=0; col < cluster.getNumCols(line); col++)
				{
					WsnMsgResponse currentWsnMsgResp = cluster.get(line, col);
					Utils.printForDebug("NodeID = "+currentWsnMsgResp.source.ID+" ");
//					System.out.print("Line = "+line+", Col = "+col+": NodeID = "+currentWsnMsgResp.source.ID+" ");
//					System.out.println("Line = "+line+", Col = "+col+": NodeID = "+currentWsnMsgResp.source.ID+" BatLevel = "+currentWsnMsgResp.batLevel+" Round = "+((SimpleNode)currentWsnMsgResp.source).lastRoundRead);
				}
				Utils.printForDebug("");
			}
			Utils.printForDebug("Number of Lines / Clusters = "+cluster.getNumRows()+"\n");
		}
	} // end printClusterArray(ArrayList2d<WsnMsgResponse> cluster)
	
	/**
	 * Muda a mensagem de posição "index" para a primeira posição [0] nesta linha do array<p>
	 * [Eng] Change the message from "index" position for the first position [0] in that line from array
	 * @param line
	 * @param index
	 */
	private void changeMessagePositionInLine(int line, int index)
	{
		messageGroups.move(line, index, 0);
	} // end changeMessagePositionInLine(int line, int index)
	
	/**
	 * Adiciona o objeto WsnMsgResponse (newWsnMsgResp), passado pelo parâmetro, na linha correta ("Cluster") a partir do tempCluster (ArrayList2d), de acordo com a Medida dissimilaridade
	 * PS:. Cada linha tempCluster (ArrayList2d de objetos WsnMsgResponse) representa um cluster de sensores (WsnMsgResponse.origem)
	 * classificada pela dissimilaridade Medida a partir de dados sensoriados, armazenado em WsnMsgResponse.dataRecordItens <p>
	 * [Eng] Adds the WsnMsgResponse object (newWsnMsgResp), passed by parameter, in the correct line ("Cluster") from the tempCluster (ArrayList2d) according with the Dissimilarity Measure 
	 * PS:. Each line in tempCluster (ArrayList2d of objects WsnMsgResponse) represents a cluster of sensors (WsnMsgResponse.origem), 
	 * classified by Dissimilarity Measure from yours data sensed, stored on WsnMsgResponse.dataRecordItens
	 *  
	 * @param tempCluster ArrayList2d de sensores, organizados em clusters (linha por linha) <b> tempCluster </b><p>
	 * [Eng] ArrayList2d from sensors, organized as clusters (line by line) <b> tempCluster </b>
	 * @param newWsnMsgResp Mensagem a ser utilizada para classificar o nó sensor <b> newWsnMsgResp </b><p>
	 * [Eng] Message to be used for classify the sensor node <b> newWsnMsgResp </b>
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
	} // end addNodeInClusterClassifiedByMessage(ArrayList2d<WsnMsgResponse> tempCluster, WsnMsgResponse newWsnMsgResp)

	/**
	 * Procura o objeto WsnMsgResponse com o mesmo nó de origem, na posição correta em "Cluster" das messageGroups (ArrayList2d)
	 * e substituí-lo com o que é passado por parâmetro
	 * PS:. Cada linha "messageGroups" (ArrayList2d de objetos WsnMsgResponse) representa um cluster de sensores (WsnMsgResponse.source) <p>
	 * classificada pela dissimilaridade Medida a partir de dados sensoriados, armazenada em WsnMsgResponse.dataRecordItens <p>
	 * [Eng] Search the WsnMsgResponse object with the same source node, in the correct position in "Cluster" from the messageGroups (ArrayList2d) 
	 * and replace him with the one passed by parameter
	 * PS.: Each line in "messageGroups" (ArrayList2d of objects WsnMsgResponse) represents a cluster of sensors (WsnMsgResponse.source), 
	 * classified by Dissimilarity Measure from yours data sensed, stored on WsnMsgResponse.dataRecordItens
	 *  
	 * @param newWsnMsgResp Mensagem a ser utilizada para classificar o nó sensor <b> newWsnMsgResp </b><p>
	 * [Eng] @param newWsnMsgResp Message to be used for classify the sensor node <b> newWsnMsgResp </b>
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
			WsnMsgResponse currentWsnMsgResp = null;
			while ((!found) && (line < messageGroups.getNumRows()))
			{
				col = 0;
				while ((!found) && (col < messageGroups.getNumCols(line)))
				{
					currentWsnMsgResp = messageGroups.get(line, col);
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
				newWsnMsgResp.cluster = currentWsnMsgResp.cluster;
				messageGroups.set(line, col, newWsnMsgResp); // It sets the new message "wsnMsgResp" in the line and col (cluster) of messageGroup 
			}
		}
		return lineCLuster;
	} // end searchAndReplaceNodeInClusterByMessage(WsnMsgResponse newWsnMsgResp)

	
	/**
	 * Testa se a distância entre os nós sensores que enviaram tais mensagens (currentWsnMsg e newWsnMsg) é menor ou igual a máxima distância possível (maxDistance) para os dois nós estarem no mesmo cluster <p>
	 * [Eng] Tests if the distance between the sensor nodes send those messages (currentWsnMsg e newWsnMsg) is less than or equal to the maximum possible distance (maxDistance) to the two nodes are in the same cluster
	 * @param currentWsnMsg Mensagem atual já classificada no cluster <b> currentWsnMsg </b><p>
	 * [Eng] Current message already classified in cluster <b> currentWsnMsg </b>
	 * @param newWsnMsg Nova mensagem a ser classificada no cluster <b> newWsnMsg </b><p>
	 * [Eng] @param newWsnMsg New message to be classified in the cluster<b> newWsnMsg </b>
	 * @return Retorna "true" caso a distância entre os nós sensores que enviaram as mensagens não ultrapassa o limite máximo, "false" caso contrário <p>
	 * [Eng] @return Returns "true" if the distance between the sensor nodes that have sent messages does not exceed the maximum limit, "false" otherwise
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
	
	private static boolean isEqualNode(Node currentNode, Node newNode)
	{
		return (currentNode == newNode);
	} // end isEqualNodeSourceFromMessages(WsnMsgResponse currentWsnMsg, WsnMsgResponse newWsnMsg)
	
	/**
	 * Ele testa se há divergência (falta de similaridade) entre os dois conjunto de medida de 2 Sensores trazidos por 2 mensagens <p>
	 * [Eng] It tests if there is dissimilarity (lack of similarity) between the 2 set of measure from 2 sensor brought by the 2 messages
	 * @param currentWsnMsg Representa a mensagem atual do grupo de mensagens (messageGroups) em um "<WsnMsgResponse> ArrayList2d" estrutura <p>
	 * [Eng] @param currentWsnMsg Represents the current message from the group of messages (messageGroups) in a "ArrayList2d<WsnMsgResponse>" structure <b> currentWsnMsg </b>
	 * @param newWsnMsg Representa a mensagem chegou recentemente no nó sorvedouro, enviado a partir do nó sensor fonte <b> newWsnMsg </b><p>
	 * [Eng] @param newWsnMsg Represents the recently arrived message in the sink node, sent from the source sensor node <b> newWsnMsg </b>
	 * @return Verdadeiro caso as duas mensagens são diferentes, ou seja, de clusters diferentes (ou "grupos"); falsos, caso contrário <p>
	 * [Eng] @return True case the two messages are DISsimilar, i.e., from different clusters (or "groups"); False, otherwise
	 */
	
	private boolean testSimilarityMeasureWithPairRounds(WsnMsgResponse currentWsnMsg, WsnMsgResponse newWsnMsg)
	{
//		boolean sameSize = true
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

/*	
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
*/	
	
	/**
	 * Recebe a mensagem passada, lê os parâmetros (itens) no dataRecordItens,
	 * calcula os coeficientes A e B de acordo com estes parâmetros e envia tais
	 * coeficientes para o nó sensor de origem <p>
	 * [Eng] Receives the message, reads the parameters (items) in
	 * dataRecordItens, calculates the coefficients A and B according to these
	 * parameters and sends these coefficients for the sensor node of origin
	 * 
	 * @param wsnMsgResp Mensagem recebida com os parâmetros a serem lidos <b> wsnMsgResp </b><p>
	 * [Eng] @param wsnMsgResp Received message with the parameters to be read <b> wsnMsgResp </b>
	 * @param clusterHeadNode Indica(seta) o ClusterHead daquele cluster <b> clusterHeadNode </b><p>
	 * [Eng] @param clusterHeadNode Indicates(sets) that the cluster clusterhead  <b> clusterHeadNode </b>
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
	 * Calcula e retorna a média aritmética dos valores reais passados <p>
	 * [Eng] Computes and returns the arithmetic mean of the actual values ??passed
	 * @param values Array de valores reais de entrada <b> values </b><p>
	 * [Eng] @param values Array of real values ??input <b> values </b>
	 * @return Média dos valores reais de entrada <p>
	 * [Eng] @return Mean of the actual values ??of input
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
	 * Calcula o coeficiente B da equação de regressão <p>
	 * [Eng] Calculates the coefficient B of the regression equation
	 * @param valores Array de valores (grandezas) das medições dos sensores <p>
	 * [Eng] @param Array of values ??(magnitudes) of the measurements of the sensors
	 * @param tempos Array de tempos das medições dos sensores <p>
	 * [Eng] @param Array times of the measurements of the sensors
	 * @param mediaValores Média dos valores lidos pelos sensores <b> mediaValores </b><p>
	 * [Eng] @param mediaValores Mean of values ??read by the sensors <b> mediaValores </b>
	 * @param mediaTempos Média dos tempos de leitura dos valores pelos sensores <b> mediaTempos </b><p>
	 * [Eng] @param mediaTempos Mean time reading the values ??from sensors <b> mediaTempos </b>
	 * @return Valor do coeficiente B da equação de regressão <p>
	 * [Eng] @return Value of the coefficient B of the regression equation
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
	 * Calcula o coeficiente A da equação de regressão <p>
	 * [Eng] Calculate coefficient A of the regression equation
	 * @param mediaValores Média dos valores lidos pelos sensores <b> mediaValores </b><p>
	 * [Eng] @param mediaValores Mean of values ??read by the sensors <b> mediaValores </b>
	 * @param mediaTempos Média dos tempos de leitura dos valores pelos sensores <b> mediaTempos </b><p>
	 * [Eng] @param mediaTempos Mean time reading the values ??from sensors <b> mediaTempos </b>
	 * @param B Valor do coeficiente B da equação de regressão <b> B </b><p>
	 * [Eng] @param Value of the coefficient B of the regression equation <b> B </b>
	 * @return Valor do coeficiente A <p>
	 * [Eng] @return Value of the coefficient A
	 */
	private double calculaA(double mediaValores, double mediaTempos, double B)
	{
		return (mediaValores - B*mediaTempos);
	} // end calculaA(double mediaValores, double mediaTempos, double B)
	
	/**
	 * Cria uma nova mensagem (WsnMsg) para envio dos coeficientes recebidos através dos parâmetros, e a envia para o próximo nó no caminho até o nó de origem da mensagem (wsnMsgResp.origem) <p>
	 * [Eng] Creates a new message (WsnMsg) for sending coefficients received through the parameters, and sends it to the next node on the path until the source node of the message (wsnMsgResp.origem)
	 * @param wsnMsgResp Mensagem de resposta enviada do nó de origem para o nó sink, que agora enviará os (novos) coeficientes calculados para o nó de origem <b> wsnMsgResp </b><p>
	 * [Eng] Response message sent from the source node to the sink node, which now sends the (new) coefficients calculated for the source node <b> wsnMsgResp </b>
	 * @param coeficienteA Valor do coeficiente A da equação de regressão <b> coeficienteA </b><p>
	 * [Eng] Value of the coefficient A of the regression equation <b> coeficienteA </b>
	 * @param coeficienteB Valor do coeficiente B da equação de regressão <b> coeficienteB </b><p>
	 * [Eng] Value of the coefficient B of the regression equation <b> coeficienteB </b>
	 */
	private void sendCoefficients(WsnMsgResponse wsnMsgResp, double coeficienteA, double coeficienteB, Node clusterHeadNode)
	{
		WsnMsg wsnMessage = new WsnMsg(1, this, wsnMsgResp.source , this, 1, wsnMsgResp.sizeTimeSlot, dataSensedType, thresholdError, clusterHeadNode);
		// WsnMsg wsnMessage = new WsnMsg(1, this, wsnMsgResp.source , this, 1, wsnMsgResp.sizeTimeSlot, dataSensedType, thresholdError);
		wsnMessage.setCoefs(coeficienteA, coeficienteB);
		wsnMessage.setPathToSenderNode(wsnMsgResp.clonePath(), wsnMsgResp.hopsToTarget);
		wsnMessage.hopsToTarget--;
		sendToNextNodeInPath(wsnMessage);
	} // end sendCoefficients(WsnMsgResponse wsnMsgResp, double coeficienteA, double coeficienteB, Node clusterHeadNode)
	
	/**
	 * Retorna todos os nós sensores no mesmo cluster do nó representante <p>
	 * [Eng] Returns all sensor nodes in the same cluster from representative node
	 * @param rn Nó representante <b> rn </b><p> 
	 * [Eng] @param rn Representative Node <b> rn </b>
	 * @return Todos nós sensores no mesmo cluster <p>
	 * [Eng] @return All sensor nodes in the same cluster
	 */
	public static Node[] getNodesFromThisCluster(Node rn) {
		Node[] nodes = null;
		ArrayList<WsnMsgResponse> messages;
		int numClusterLine = identifyCluster(rn);
		int cont = 0;
		if (numClusterLine >= 0) {
			messages = messageGroups.get(numClusterLine);
			nodes = new Node[messages.size()];
			for (WsnMsgResponse message : messages) {
				nodes[cont] = message.source;
				cont++;
			}
		}
		return nodes;
	}
	
	public ArrayList2d<WsnMsgResponse> ensuresNodesToReceiveDataReading() {
		if (nodesToReceiveDataReading == null) {// If there isn't a message group yet, then it does create one and adds the message to it
			nodesToReceiveDataReading = new ArrayList2d<WsnMsgResponse>();
	 		//NodesToReceiveDataReading.ensureCapacity(numTotalOfSensors); // Ensure the capacity as the total number of sensors (nodes) in the data set
		}
		return nodesToReceiveDataReading;
	}

	public ArrayList2d<WsnMsgResponse> ensuresArrayList2d(ArrayList2d<WsnMsgResponse> array2d) {
		if (array2d == null) {// If there isn't a message group yet, then it does create one and adds the message to it
			array2d = new ArrayList2d<WsnMsgResponse>();
	 		//NodesToReceiveDataReading.ensureCapacity(numTotalOfSensors); // Ensure the capacity as the total number of sensors (nodes) in the data set
		}
		return array2d;
	}

}
