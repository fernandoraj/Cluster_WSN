package projects.wsneeFD.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import projects.wsneeFD.nodes.messages.WsnMsg;
import projects.wsneeFD.nodes.messages.WsnMsgResponse;
import projects.wsneeFD.nodes.timers.FreeTimer;
import projects.wsneeFD.nodes.timers.WsnMessageTimer;
import projects.wsneeFD.utils.ArrayList2d;
import projects.wsneeFD.utils.FD3BigInt;
import projects.wsneeFD.utils.Utils;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;
import sinalgo.runtime.Runtime;
//import java.util.Iterator;
//import sinalgo.nodes.timers.Timer;
//import sinalgo.nodes.TimerCollection;

public class SinkNode extends SimpleNode 
{
	/**
	 * Número de dados sensoriados por time slot (Tamanho do time slot inicial) <br>
	 * [Eng] Number of sensed data per time slot (initial time slot size)
	 */
	private Integer sizeTimeSlot = 70; 	// This value would be, at least, 10 ^ N, N = quantity of dimensions from data, 
										// i.e., number of dimensions from "dataSensedTypes", e.g., if dataSensedTypes
										// = {4,5} => N = 2 => sizeTimeSlot >= 10^2 = 100.
	
	/**
	 * Número de dados sensoriados por time slot (Tamanho do time slot inicial) <br>
	 * [Eng] Number of sensed data per time slot (initial time slot size)
	 */
	private Integer sizeTimeSlotForMerge = 25;
	
	/**
	 * Indica que o sink node sinaliza para todos os outros nós que deve ficar continuamente com sensoriamento (usando Cluster Heads) <p>
	 * [Eng] Indicates that sink node signalize to all other nodes must continuously sensing (using Cluster Heads)
	 */
	private boolean allSensorsMustContinuoslySense = true; // ACS: false = Representative Nodes; true = Cluster Heads
	
	/**
	 * Indica quando o modo de clusterização usando Dimensão Fractal está ligado (ativo = true)
	 * [Eng] Indicates whether the Fractal Dimension clustering is in ON mode (active = true)
	 */
	private final boolean FDmodeOn = true;

	/**
	 * Quantidade de rounds (ciclos) a ser saltado para cada leitura sequencial dos sensores, no caso de uso da abordagem de ClusterHeads (ACS=True) <br>
	 * [Eng] Number of rounds (cycles) to be jumped for each sequential sensor reading in the case of using the approach of ClusterHeads (ACS=True)
	 */
	public static final int sensorTimeSlot = 1;
	
	/**
	 * Número de dados sensoriados por time slot (Tamanho do time slot) <br>
	 * [Eng] Number of sensed data per time slot (time slot size)
	 */
	private Integer sizeTimeUpdate = 5;
	
	/**
	 * Tipos de dados a serem sensoriados (lidos nos nós sensores), que, para os dados do "Intel Lab Data", podem ser: temperatura = 4, 
	 * umidade = 5, luminosidade = 6 ou voltagem = 7. <br>
	 * [Eng] Types of data to be sensed (read in the sensor nodes), which, for Intel Lab Data, can be: 
	 * temperature = 4; humidity = 5; brightness("lum") = 6 or voltage = 7;
	 */
	public static int[] dataSensedTypes = {4}; //{4}; //{4,5}; //{4,5,6};
	
	/**
	 * Percentual do limiar de erro temporal aceitável para as leituras dos nós sensores, que pode estar entre 0.0 (não aceita erros) e 1.0 (aceita todo e qualquer erro) <br>
	 * [Eng] Percentage of temporal acceptable error threshold for the readings of sensor nodes, which may be between 0.0 (accepts no errors) and 1.0 (accepts any error)
	 */
	private double[] thresholdErrors = {0.05}; //{0.05}; //{0.05,0.05}; //{0.05,0.05,0.05}; // thresholdErr: 0.05 = 5%
	
	/**
	 * Limite de diferença de magnitude aceitável (erro espacial) para as leituras dos nós sensores /--que pode estar entre 0.0 (não aceita erros) e 1.0 (aceita todo e qualquer erro) <br>
	 * [Eng] Limit of acceptable magnitude difference (spatial error) for the readings of sensor nodes / - which can be between 0.0 (no errors accepted) and 1.0 (accepts any error)
	 */
	private double[] spacialThresholdErrors = {1.5}; //{1.5}; //{1.5, 1.5}; //{2.0, 2.5, 70.0};
	
	/**
	 * Percentual mínimo do número de rounds iguais das medições de 2 sensores para que os mesmos sejam classificados no mesmo cluster <br>
	 * [Eng] Minimum percentage of the number of equal measurement rounds of 2 sensors so that they are classified in the same cluster
	 */
//	private double equalRoundsThreshold = 0.5;
	
	/**
	 * Percentual mínimo das medições de 2 sensores (no mesmo round) a ficar dentro dos limiares aceitáveis para que os mesmos sejam classificados no mesmo cluster <br>
	 * [Eng] Minimum percentage of measurements of two sensors (in the same round) to stay within the acceptable thresholds for them to be classified in the same cluster
	 */
//	private double metaThreshold = 0.5;
	
	/**
	 * Distância máxima aceitável para a formação de clusters. Se for igual a zero (0,0), não considerar tal limite (distância) <br>
	 * [Eng] Maximum distance acceptable to the formation of clusters. If it is equal to zero (0.0), ignoring
	 */
	private double maxDistance = 0.0; //8.0;
	
	/**
	 * Número total de nós sensores presentes na rede <br>
	 * [Eng] Total number of sensor nodes in the network
	 */
	private static int numTotalOfSensors = 54;

	
	/**
	 * Emblema para indicar que o sink ainda não agrupou todos nós para a primeira vez <p>
	 * [Eng] Flag to indicate that the sink still not clustered all nodes for the first time
	 */
	private boolean stillNonclustered = true;
	
	/**
	 * Array 2D (clusters) de sensores (Sensores = SimpleNode) <p>
	 * [Eng] Array 2D (clusters) from sensors (Sensors = SimpleNode)
	 */
	private static ArrayList2d<Double, SimpleNode> nodeGroups;
	
	private ArrayList2d<Double, SimpleNode> newCluster;
	
	private ArrayList2d<Double, SimpleNode> nodesToReceiveDataReading;
	
	/**
	 * "Lista Negra" é uma lista de mensagens (nós de origem) já recebidos pelo sink (e removido os nodesToReceiveDataReading) <p>
	 * [Eng] "BlackList" is the list of messages (source nodes) already received by sink (and removed from nodesToReceiveDataReading)
	 */
//	private ArrayList<SimpleNode> blackList;
	
	/**
	 * Número de mensagens recebidas pelo nó sink de todos os outros nós sensores <p> 
	 * [Eng] Number of messages received by sink node from all other sensors nodes
	 */
	private int numMessagesReceived = 0;
	
	/**
	 * Número de rounds (ciclos) para reagrupamento (reclustering) dos sensores no caso de uso de Nós Representativos <br>
	 * [Eng] Number of rounds (cycles) for reclustering of the sensors in use cases of Representatives Nodes
	 */
//	private int numRoundsForReclustering = 30;

	/**
	 * Número de mensagens de resposta esperado já recebidos até o momento <p>
	 * Number of messages response expected already received until the moment
	 */
	private int numMessagesExpectedReceived = 0;

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
	
//	private int expectedNumberOfSensors = 0;
	
	
	private boolean canReceiveMsgResponseError = false;
	
	private double minimumOccupancyRatePerCluster = 1.35; // MORPC: #TotalSensors = 54 / #CLusters = 40 => 54/40 = 1.35
	
	public SinkNode() {
		super();
		this.setColor(Color.RED);
		System.out.println("The number of sensors is "+numTotalOfSensors);
		System.out.println("The status for continuos sense is "+allSensorsMustContinuoslySense);
		if (allSensorsMustContinuoslySense) {
			System.out.println("Using Cluster Head approach... ACS = true");
		}
		else {
			System.out.println("Using Representative Nodes approach...  ACS = false");
		}
		System.out.println("The sensor delay is "+SimpleNode.limitPredictionError);
		System.out.println("The cluster delay is "+SimpleNode.maxErrorsPerCluster);
		for (int numTypes = 0; numTypes < thresholdErrors.length; numTypes++) {
			System.out.println("The threshold of error (max error) is "+thresholdErrors[numTypes]+" for data type (SensedType) in position "+dataSensedTypes[numTypes]);
		}
		System.out.println("The size of time slot is "+sizeTimeSlot);
		System.out.println("The size of time slot for merge is "+sizeTimeSlotForMerge);
		for (int numTypes = 0; numTypes < spacialThresholdErrors.length; numTypes++) {
			System.out.println("The spacial threshold of error (spacialThresholdError) is "+spacialThresholdErrors[numTypes]+" for data type (SensedType) in position "+dataSensedTypes[numTypes]);
		}
		System.out.println("The size of sliding window is "+SimpleNode.slidingWindowSize);
		System.out.println("The maximum distance between sensors in the same cluster is "+maxDistance);
		System.out.println("The minimum occupancy rate per cluster (for Merge) is "+minimumOccupancyRatePerCluster);
//		System.out.println("The type of data sensed is "+dataSensedType);

		
//		if(LogL.ROUND_DETAIL){
			Global.log.logln("\nThe size of time slot is "+sizeTimeSlot);
//			Global.log.logln("The type of data sensed is "+dataSensedType);
			for (int numTypes = 0; numTypes < thresholdErrors.length; numTypes++) {
				Global.log.logln("The threshold of error (max error) is "+thresholdErrors[numTypes]+" for data type (SensedType) in position "+dataSensedTypes[numTypes]);
			}
			Global.log.logln("The size of sliding window is "+SimpleNode.slidingWindowSize+"\n");
//		}
		construirRoteamento(); // Equivale ao clique com o botão direito sobre o SinkNode e a escolha da opção "Definir Sink como Raiz de Roteamento"
	} // end SinkNode()

	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		String text = "S";
		super.drawNodeAsSquareWithText(g, pt, highlight, text, 1, Color.WHITE);
	} // end draw(Graphics g, PositionTransformation pt, boolean highlight)
	
	@NodePopupMethod(menuText="Definir Sink como Raiz de Roteamento")
	public void construirRoteamento() {
		this.nextNodeToBaseStation = this;
		WsnMsg wsnMessage = new WsnMsg(1, this, null, this, 0, sizeTimeSlot, dataSensedTypes);
		
		// The numberOfMessagesOverAll and sensorReadingsCount attribs are used to define a param from energy consumption of the overall network
		Utils.printForDebug("Global.numberOfMessagesOverAll = "+Global.numberOfMessagesOverAll);
		Utils.printForDebug("Global.sensorReadingsCount = "+Global.sensorReadingsCount);
		
		WsnMessageTimer timer = new WsnMessageTimer(wsnMessage);
		timer.startRelative(1, this);
	} // end construirRoteamento()
	
	@Override
	public void handleMessages(Inbox inbox) {
		while (inbox.hasNext()) {
			Message message = inbox.next();
			
			Utils.printForDebug("Global.numberOfMessagesOverAll = "+Global.numberOfMessagesOverAll);
			Utils.printForDebug("Global.sensorReadingsCount = "+Global.sensorReadingsCount);
			
			if (message instanceof WsnMsgResponse) {
				this.setColor(Color.YELLOW);
				WsnMsgResponse wsnMsgResp = (WsnMsgResponse) message;
				
				Utils.printForDebug("@ @ @ Message Received by SINK from the NodeID = "+wsnMsgResp.source.ID +" with MsgType = "+wsnMsgResp.typeMsg+"\n");
				
				if (canReceiveMsgResponseError) { // If the other sensor nodes still getting data to send to sink calculates the Equation Regression Coeffs. - e.g.: During the Merge Process Operation

					if (wsnMsgResp.typeMsg == 2 || wsnMsgResp.typeMsg == 3) { // Se é uma mensagem de um Nó Representativo / Cluster Head que excedeu o #máximo de erros de predição

/*
						 Neste caso (wsnMsgResp.typeMsg == 2 || wsnMsgResp.typeMsg == 3), os clusters atuais estão armazenados em "nodeGroups", só que ocorreu um erro de
						 predição em algum nó (wsnMsgResp.typeMsg == 2) ou algum nó esgotou o seu tempo de predição (wsnMsgResp.typeMsg == 3). Desta forma, primeiramente 
						 (1) o nó sensor que enviou novos dados junto com a mensagem de erro irá substituir o antigo nó que o representava em "nodeGroups" (linha: 
						 int lineFromCluster = searchAndReplaceNodeInCluster((SimpleNode)wsnMsgResp.source);
						 ), então, a seguir (2), iremos remover de "nodeGroups" todo o cluster do nó que enviou novos dados (com mensagem de erro) e o transferir para 
						 a estrutura "nodesToReceiveDataReading" (linha:
						 triggerSplitFromCluster(lineFromCluster);
						 ). No passo seguinte (3), iremos transferir todos os nós de "nodesToReceiveDataReading" para a estrutura "newCluster" (linha:
						 addNodesInNewCluster(nodesToReceiveDataReading, newCluster);
						 ), dividindo cada nó em clusters (linhas) de acordo com o cluster correto ao qual ele deveria pertencer, ou seja, pode acontecer um split (divisão) 
						 dos nós que estavam em um mesmo cluster. Em seguida (4), classificamos os nós dentro da estrutura "newCluster" (linha:
						 classifyNodesByAllParams(newCluster);
						 ), para que os nós representativos / cluster heads ocupem a primeira posição (coluna) de cada cluster. O próximo passo (5) é fazer a auto-
						 -configuração dos clusters dos nós em "newCluster", para que cada nó configure corretamente o atributo "myCluster" (linha:
						 setClustersFromNodes(newCluster);
						 ), indicando quais os sensores presentes em seu próprio cluster. O passo seguinte (6) é tratar a mensagem recebida propriamente "receiveMessage()"
						 enviando os novos coeficientes para os devidos nós dentro do laço "for" (linha:
						 for (int line = 0; line < newCluster.getNumRows(); line++)
						 ). Finalmente (7), unir os novos clusters (newCluster) com os cluster já existentes (nodeGroups) através do método "unifyClusters()" (linha:
						 unifyClusters(nodeGroups, newCluster);
						 ), mas mantendo a separação (de linhas) entre tais clusters, ou seja, tal método faz somente uma espécie de 'Append' entre os dois grupos de 
						 clusters.
*/
						if (wsnMsgResp.typeMsg == 2) {
							numMessagesOfErrorPredictionReceived++;
						}
						if (wsnMsgResp.typeMsg == 3) {
							numMessagesOfTimeSlotFinishedReceived++;
						}
						//numMessagesExpectedReceived++;
						
						/*
						 * Criar métodos AQUI para:
						 * 0) Clonar o cluster de sensores
						 * 1) Inserir os novos dados recebidos em todos os clusters;
						 * 2) Recalcular as dimensões fractais de todos os "novos" clusters;
						 * 3) Verificar qual cluster sofreu menor diferença da nova dimensão fractal para a antiga (anterior);
						 * 4) Adicionar o(s) novo(s) nó(s) para o cluster selecionado no passo anterior!
						 * 5) Estudar demais alterações necessárias e possíveis efeitos colaterais...
						 */
						
						//TODO: TO BE TESTED! (18/04/2014)
						if (FDmodeOn) { // Se estiver no modo Dimensão Fractal!
							SimpleNode currentNode = (SimpleNode)wsnMsgResp.source;
							ArrayList2d<Double, SimpleNode> cloneCluster = nodeGroups.clone2();
							insertNewNodeInClusters(cloneCluster, currentNode);
							//classifyNodesByAllParams(cloneCluster); // Investigar se é necessário (1)
							//setClustersFromNodes(cloneCluster); // Investigar se é necessário (1)
							
							System.out.println("\nAntes de calcular FD: cloneCluster:");
							System.out.println(cloneCluster);

							//Calculates the Fractal Dimension (Capacity) of each cluster and saves it as "key" of each cluster (ArrayList)
							System.out.println("");
							for (int i = 0; i < cloneCluster.getNumRows(); i++) {
								cloneCluster.setKey(i, FD3BigInt.calculatesFractalDimensions(cloneCluster.get(i)));
							    System.out.println("Fractal Dimension of cluster "+i+" = "+cloneCluster.getKey(i));
							}
							System.out.println("\nDepois de calcular FD: cloneCluster:");
							System.out.println(cloneCluster);

							System.out.println("Aqui: nodeGroups:");
							System.out.println(nodeGroups);
							
							//TODO: TEORICAMENTE, ESTÁ FALTANDO APENAS OS PASSOS 3) 4) E 5):
							/* 3) Verificar qual cluster sofreu menor diferença da nova dimensão fractal para a antiga (anterior);
							 * 4) Adicionar o(s) novo(s) nó(s) para o cluster selecionado no passo anterior!
							 * 5) Estudar demais alterações necessárias e possíveis efeitos colaterais...
							 */
							
							
							
						}
						else {
							
							// COLOCAR AS PRÓXIMAS LINHAS (MARCADAS*) PARA DENTRO DESTE ELSE!
							
						}
// *DAQUI...
						int lineFromCluster = searchAndReplaceNodeInCluster((SimpleNode)wsnMsgResp.source); // (1)
						if (lineFromCluster >= 0) {
	//						expectedNumberOfSensors += sendSenseRequestMessageToAllSensorsInCluster(nodeGroups, lineFromCluster);
							triggerSplitFromCluster(lineFromCluster); // (2)
						}
						newCluster = ensuresArrayList2d(newCluster);
						
						addNodesInNewCluster(nodesToReceiveDataReading, newCluster); // (3)
						classifyNodesByAllParams(newCluster); // (4)
						setClustersFromNodes(newCluster); // (5)
						nodesToReceiveDataReading = new ArrayList2d<Double, SimpleNode>();
							
						for (int line = 0; line < newCluster.getNumRows(); line++) // For each line (group/cluster) from newCluster // (6)
						{
							int numSensors = newCluster.getNumCols(line);
							Utils.printForDebug("Cluster / Line number = "+line);
	
							if (!allSensorsMustContinuoslySense) { // If only the representative nodes must sensing (Representative nodes approach)
								SimpleNode representativeNode = newCluster.get(line, 0); // Get the Representative Node (or Cluster Head)
								representativeNode.myCluster.sizeTimeSlot = calculatesTheSizeTimeSlotFromRepresentativeNode(sizeTimeSlot, numSensors);
								receiveMessage(representativeNode, null);
							} // end if (!allSensorsMustContinuoslySense)
							
							else { // If all nodes in cluters must sensing, and not only the representative nodes (Cluster heads approach)
								Node chNode = newCluster.get(line, 0); // Cluster Head from the current cluster/line
								for (int col=0; col < numSensors; col++) // For each colunm from that line in newCluster
								{
									SimpleNode currentNode = newCluster.get(line, col); // Get the current node
									currentNode.myCluster.sizeTimeSlot = sensorTimeSlot; // If all sensor nodes in each cluster must continuosly sense, so the sizeTimeSlot doesn't matter
									receiveMessage(currentNode, chNode);
								} // end for (int col=0; col < numSensors; col++)
							} // end else if (!allSensorsMustContinuoslySense)
								
						} // end for (int line=0; line < newCluster.getNumRows(); line++)
						
						unifyClusters(nodeGroups, newCluster); // (7) // TESTAR SE MÉTODO FUNCIONA CORRETAMENTE!!!???
// *... ATÉ AQUI!
						Global.clustersCount = nodeGroups.getNumRows(); // It sets the number of clusters (lines in messageGroups) to the Global.clustersCount attribute
						
						// TODO: Test if it must br done currentNumberOfActiveSensors = (numTotalOfSensors - numMessagesOfLowBatteryReceived)
						if (((double)numTotalOfSensors / (double)Global.clustersCount) <= minimumOccupancyRatePerCluster) { // Begin MERGE operation
							canReceiveMsgResponseError = false;
							// TODO: To be tested!
							nodeGroups = null;
							Global.clustersCount = 0;
							stillNonclustered = true;
//							System.out.println("    ***  ENTROU no MERGE ! Round = "+Global.currentTime);
							
							for(Node n : Runtime.nodes) {
								((SimpleNode)n).startMerge();
							}

							FreeTimer ft = new FreeTimer();
							ft.startRelative(1, this);
							
							sizeTimeSlot = sizeTimeSlotForMerge; // Sugestão do Prof. Everardo em reunião no dia 12/12/2013 : sizeTimeSlotForMerge = 2
							
							WsnMsg wsnMessage = new WsnMsg(1, this, null, this, 0, sizeTimeSlot, dataSensedTypes);

							WsnMessageTimer timer = new WsnMessageTimer(wsnMessage);
							timer.startRelative(2, this);
							numMessagesReceived = 0;
						}
						
					}
/*
					else if (wsnMsgResp.typeMsg == 3) {// Se é uma mensagem de um Nó Representativo que excedeu o #máximo de predições (timeSlot) => Incorporado ao caso anterior

					} // else if (wsnMsgResp.typeMsg == 3)
*/					
					else if (wsnMsgResp.typeMsg == 4) { // Se é uma mensagem de um Nó Representativo/Cluster Head cujo nível da bateria está abaixo do mínimo (SimpleNode.minBatLevelInClusterHead)
						numMessagesOfLowBatteryReceived++;
						
						if (allSensorsMustContinuoslySense) { // Se é uma mensagem de um Cluster Head // Se for um ClusterHead (ClusterHead != null)
							
							int lineFromClusterNode = searchAndReplaceNodeInCluster((SimpleNode)wsnMsgResp.source); // Procura a linha (cluster) da mensagem recebida e atualiza a mesma naquela linha
							
							if (lineFromClusterNode >= 0) { // Se a linha da mensagem recebida for encontrada
								classifyNodesByAllParams(nodeGroups); // Reclassifica todos os nós do cluster atual - cujo Cluster Head teve decaimento do nível de bateria
								
								int numSensors = nodeGroups.getNumCols(lineFromClusterNode);
								Node chNode = (nodeGroups.get(lineFromClusterNode, 0)); // Get the (new) Cluster Head from the current cluster/line
	
								Utils.printForDebug("Cluster / Line number = "+lineFromClusterNode+"; ClusterHead / IDnumber = "+chNode.ID+"; #Sensors = "+numSensors);
								for (int col=0; col < numSensors; col++) { // For each node from that cluster (in messageGroups), it must communicate who is the new ClusterHead
									SimpleNode nodeCurrent = nodeGroups.get(lineFromClusterNode, col); // Get the Node
									
									nodeCurrent.myCluster.sizeTimeSlot = sensorTimeSlot; // If all sensor nodes in Cluster must continuosly sense, so the sizeTimeSlot will be the sensorTimeSlot
									
									receiveMessage(nodeCurrent, chNode);
								} // end for (int col=0; col < numSensors; col++)
							} // end if (lineFromClusterNode >= 0)
						} // end if (allSensorsMustContinuoslySense)
						
						// Se for um Nó Representativo (ClusterHead == null) - VERIFICAR ESTE CÓDIGO!!!
						else { //if (!allSensorsMustContinuoslySense) { // Se é uma mensagem de um Nó Representativo
							int lineFromClusterNode = searchAndReplaceNodeInCluster((SimpleNode)wsnMsgResp.source); // Procura a linha (cluster) da mensagem recebida e atualiza a mesma naquela linha
							
							if (lineFromClusterNode >= 0) // Se a linha da mensagem recebida for encontrada
							{
								classifyNodesByAllParams(nodeGroups);
								
								SimpleNode representativeNode = nodeGroups.get(lineFromClusterNode, 0); // Get the (new) Representative Node (or Cluster Head)
								int numSensors = nodeGroups.getNumCols(lineFromClusterNode);
								Utils.printForDebug("Cluster / Line number = "+lineFromClusterNode+"\n");
								representativeNode.myCluster.sizeTimeSlot = calculatesTheSizeTimeSlotFromRepresentativeNode(sizeTimeSlot, numSensors);						
								receiveMessage(representativeNode, null);
							} // end if (lineFromClusterNode >= 0)
						}  // end else
					} // end else if (wsnMsgResp.typeMsg == 4)
				} // end if (canReceiveMsgResponseError)
				
				else if (wsnMsgResp.typeMsg != 2 && wsnMsgResp.typeMsg != 3 && wsnMsgResp.typeMsg != 4) { // If it is a message from a (Representative) node containing reading (sense) data
				
					numMessagesReceived++;
					
					if (stillNonclustered) { // If the sink still not clustered all nodes for the first time
						// ((SimpleNode)wsnMsgResp.source).hopsToTarget = wsnMsgResp.hopsToTarget; // TESTAR AQUI!!!
						((SimpleNode)wsnMsgResp.source).setPathToSenderNode(wsnMsgResp.clonePath(), wsnMsgResp.hopsToTarget);
						
						if (nodeGroups == null) { // If there isn't a message group yet, then it does create one and adds the message to it
							nodeGroups = new ArrayList2d<Double, SimpleNode>();
							nodeGroups.ensureCapacity(numTotalOfSensors); // Ensure the capacity as the total number of sensors (nodes) in the data set
							Double initialFracDim = new Double(0.0);
							nodeGroups.add((SimpleNode)wsnMsgResp.source, 0, initialFracDim); // Add the initial sensor node(SimpleNode) to the group 0 - line 0 (ArrayList2d of SimpleNode)
						}
						else { // If there is a message group (SensorCluster), then adds the wsnMsgResp representing a sensor to group, classifing this message/sensor in correct cluster/line
							addNodeInCluster(nodeGroups, (SimpleNode)wsnMsgResp.source);
						}
						
						if (numMessagesReceived >= numTotalOfSensors) { // In this point, clusters should be "closed", and the sensors inside them being classified
							classifyNodesByAllParams(nodeGroups);
							Global.clustersCount = nodeGroups.getNumRows(); // It sets the number of clusters (lines in messageGroups) to the Global.clustersCount attribute
							setClustersFromNodes(nodeGroups);
							
							//TODO:FDmodeON to be checked
							if (FDmodeOn) {
								//Calculates the Fractal Dimension (Capacity) of each cluster and saves it as "key" of each cluster (ArrayList)
								for (int i = 0; i < nodeGroups.getNumRows(); i++) {
								    nodeGroups.setKey(i, FD3BigInt.calculatesFractalDimensions(nodeGroups.get(i)));
								    System.out.println("Fractal Dimension of cluster "+i+" = "+nodeGroups.getKey(i));
								}
								System.out.println("Tehee");
								System.out.println(nodeGroups);
							}
//							printClusterArray(nodeGroups);
							
							stillNonclustered = false;
							canReceiveMsgResponseError = true;
						 	
							if (nodeGroups != null) { // If there is a message group created
								if (!allSensorsMustContinuoslySense) { // If only the representative nodes must sensing
									for (int line=0; line < nodeGroups.getNumRows(); line++) { // For each line (group/cluster) from messageGroups
										SimpleNode representativeNode = nodeGroups.get(line, 0); // Get the Representative Node (or Cluster Head)
										int numSensors = nodeGroups.getNumCols(line);
										Utils.printForDebug("Cluster / Line number = "+line);
										representativeNode.myCluster.sizeTimeSlot = calculatesTheSizeTimeSlotFromRepresentativeNode(sizeTimeSlot, numSensors);
										receiveMessage(representativeNode, null);
									} // for (int line=0; line < messageGroups.getNumRows(); line++)
								} // if (!allSensorsMustContinuoslySense)
								else {// If all nodes in cluters must sensing, and not only the representative nodes
									for (int line=0; line < nodeGroups.getNumRows(); line++) { // For each line (group/cluster) from messageGroups
										int numSensors = nodeGroups.getNumCols(line);
										Node chNode = (nodeGroups.get(line, 0)); // Cluster Head from the current cluster/line
										Utils.printForDebug("Cluster / Line number = "+line+"; ClusterHead / IDnumber = "+chNode.ID+"; #Sensors = "+numSensors);
										for (int col=0; col < numSensors; col++) { // For each colunm from that line in messageGroups
											SimpleNode nodeCurrent = nodeGroups.get(line, col); // Get the Node
											
											//wsnMsgResponseCurrent.calculatesTheSizeTimeSlotFromRepresentativeNode(sizeTimeSlot, numSensors);
											nodeCurrent.myCluster.sizeTimeSlot = sensorTimeSlot; // If all sensor nodes in Cluster must continuosly sense, so the sizeTimeSlot doesn't matter
											
											receiveMessage(nodeCurrent, chNode);
										}
									}
								} // end else
							} // end if (messageGroups != null)
						} // end if (numMessagesReceived >= numTotalOfSensors)
					} // end if (stillNonclustered)
					
					else { // otherwise, if the sink have already been clustered all nodes for the first time
						numMessagesExpectedReceived++;
					} // end else if (stillNonclustered)
					
				} // end else
			} // end if (message instanceof WsnMsg)
		} //end while (inbox.hasNext())
	} //end handleMessages()
	
	/**
	 * Chama o método "freeNextNode ()" para cada nó sensor na rede para apagar (definido como null) o atributo "nextNodeToBaseStation" e
     * definir o "nextNodeToBaseStation" do nó sink (this) para si mesmo (this) <p>
	 * [Eng] Calls the "freeNextNode()" method for each sensor node in the network for clear (set as null) the "nextNodeToBaseStation" attribute and
	 * set the "nextNodeToBaseStation" from sink node (this) to itself (this)
	 */
	public void setNextNodesToNull() {
		for(Node n : Runtime.nodes) {
			((SimpleNode)n).freeNextNode();
		} // end for(Node n : Runtime.nodes)
		this.nextNodeToBaseStation = this;
	} // end setNextNodesToNull()
	
	/**
	 * Testa se todos os nós em número de linha "row" do "tempCluster" estão dentro do "blkList", se o sink(sumidouro) já recebeu respostas de mensagens de todos os nós do respectivo Cluster<p> 
	 * [Eng]Test if all nodes in line number "row" of "tempCluster" are in "blkList", ie, if the sink have already received message responses from all nodes in the respective Cluster 
	 * @param tempCluster Novo GroupCluster com os nós para serem "Unidos" <p>
	 * [Eng] New GroupCluster with the nodes to be "merged" 
	 * @param row Linha do Novo GroupCluster a ser testada <p>
	 * [Eng] Line from the New GroupCluster to be tested 
	 * @param blkList Lista Negra com os nós que já receberam do sink <p>
	 * [Eng] Black List with the nodes that have already received by the sink 
	 * @return Verdadeiro se todos os nós em linha "linha" de "tempCluster" estão na "blkList" <p>
	 * [Eng] True if all nodes in line number "row" of "tempCluster" are in "blkList"
	 */
/*
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
*/
	
	/**
	 * Ele classifica os 'clusters de nós' pela 'energia residual' e pelos 'saltos para o sink' e imprime as configuração do cluster antes, durante e após nova ordem <p>
	 * [Eng] It classify the 'clusters nodes' by 'residual energy' and by 'hops to sink' and prints cluster configuration before, during and after new order
	 * @param cluster Grupo de mensagens (que representam clusters) que serão classificados de acordo com parâmetros ("energia residual 'e'saltos para o sink')<p>[Eng] Group of messages (representing clusters) which will be classified according with params ('residual energy' and 'hops to sink') 
	 */
	void classifyNodesByAllParams(ArrayList2d<Double, SimpleNode> cluster) {
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
	 * Ele define todos os clusters para cada SimpleNode representando os nós de origem<p>[Eng] It sets all clusters for each SimpleNode representing the source nodes  
	 * @param clusterGroup  Grupo de mensagens (representando clusters) que têm os clusters configurados <p>[Eng] Group of messages (representing clusters) to have the clusters configurated 
	 */
	void setClustersFromNodes(ArrayList2d<Double, SimpleNode> clusterGroup) {
		if (clusterGroup != null) { // If there is a node group created
			for (int line=0; line < clusterGroup.getNumRows(); line++) {
				Cluster currentCluster = new Cluster(clusterGroup.get(line, 0));
				currentCluster.setMembers( clusterGroup.get(line) );
				for (int col=0; col < clusterGroup.getNumCols(line); col++) {
					SimpleNode currentNode = clusterGroup.get(line, col);
					currentNode.myCluster = currentCluster;
				} // end for (int col=0; col < clusterGroup.getNumCols(line); col++)
			} // end for (int line=0; line < clusterGroup.getNumRows(); line++)
		} // end if (clusterGroup != null)
	} // end setClustersFromNodes(ArrayList2d<WsnMsgResponse> clusterGroup)
	
	/**
	 * Ele recebe um ArrayList de WsnMsgResponse e retorna um ArrayList de nó com nós de origem destas mensagens<p>
	 * [Eng] It receives an ArrayList of WsnMsgResponse and returns an ArrayList of Node with source nodes from that messages
	 * @param lineMessages ArrayList de WsnMsgResponse <p>[Eng] ArrayList of WsnMsgResponse
	 * @return ArrayList de nós de origem <p>[Eng] ArrayList of source nodes
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
	 * @param tempClusterReceiver Estrutura do cluster que receberá os sensores / cluster a partir da estrutura tempClusterGiver <p>[Eng] Cluster structure that will receive the sensors/clusters from the tempClusterGiver structure 
	 * @param tempClusterGiver Estrutura do cluster que vai dar os sensores / cluster à estrutura tempClusterReceiver <p>[Eng] Cluster structure that will give the sensors/clusters to the tempClusterReceiver structure 
	 */
	private void unifyClusters(ArrayList2d<Double, SimpleNode> tempClusterReceiver, ArrayList2d<Double, SimpleNode> tempClusterGiver) {
		int rowReceiver, rowGiver = 0, col;
		rowReceiver = tempClusterReceiver.getNumRows();
		while (rowGiver < tempClusterGiver.getNumRows()) {
			col = 0;
			while (col < tempClusterGiver.getNumCols(rowGiver)) {
				tempClusterReceiver.add(tempClusterGiver.get(rowGiver, col), rowReceiver);
				col++;
			} // end while (col < tempClusterGiver.getNumCols(rowIndexes.get(rowGiver)))
			rowGiver++;
			rowReceiver++;
		} // end while (rowGiver < rowIndexes.size())
		for (int i = (tempClusterGiver.getNumRows()-1); i >= 0; i--) {
			tempClusterGiver.remove(i);
		} // end for (int i = (tempClusterGiver.getNumRows()-1); i >= 0; i--)
	} // end unifyClusters(ArrayList2d<Double, SimpleNode> tempClusterReceiver, ArrayList2d<Double, SimpleNode> tempClusterGiver)
	
	/**
	 * Ele remove o "msgToRemoveFromList" da lista "list" (lista negra) que passou por parâmetro <p>
	 * [Eng] It removes the "msgToRemoveFromList" from the list "list" (blackList) passed by param
	 * @param list Um objeto ArrayList (WsnMsgResponse) <p>[Eng] An ArrayList(WsnMsgResponse) object 
	 * @param msgToRemoveFromList Um objeto WsnMsgResponse <p>[Eng] A WsnMsgResponse object 
	 */
/*
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
*/
	
	/**
	 * Aciona o processo de divisão de cluster, através da retirada da linha do cluster em "nodeGroups", e transferência do mesmo para "nodesToReceiveDataReading"<p>
	 * [Eng] It triggers the process to split a cluster, through the removal from the line from cluster in "nodeGroups" - and transfer to "nodesToReceiveDataReading"
	 * @param lineFromCluster Número da linha do cluster para ser dividido <p>[Eng] Line number from cluster to be divided
	 */
	private void triggerSplitFromCluster(int lineFromCluster) {
		// IDEIA: O nó representativo acabou de enviar seus últimos dados de leitura (sensoriamento), então ele não precisa enviar novamente
		// Remover cluster (nós do cluster) de messageGroups
		nodesToReceiveDataReading = ensuresArrayList2d(nodesToReceiveDataReading);
		
		if (nodeGroups != null && nodesToReceiveDataReading != null) {
/*			
			Utils.printForDebug("Antes: messageGroups.numRows = "+nodeGroups.getNumRows()+" and nodesToReceiveDataReading.numRows = "+nodesToReceiveDataReading.getNumRows());
			Utils.printForDebug("messageGroups");
			printClusterArray(nodeGroups);
*/			
			nodeGroups.transferRowTo(lineFromCluster, nodesToReceiveDataReading);
/*
			Utils.printForDebug("Depois: messageGroups.numRows = "+nodeGroups.getNumRows()+" and nodesToReceiveDataReading.numRows = "+nodesToReceiveDataReading.getNumRows());
			Utils.printForDebug("messageGroups");
			printClusterArray(nodeGroups);
			Utils.printForDebug("nodesToReceiveDataReading");
			printClusterArray(nodesToReceiveDataReading);
*/
		}
		else {
			System.out.print("Round = "+Global.currentTime+": ");
			if (nodeGroups == null) {
				System.out.println("nodeGroups = NULL ! ");
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
	 * @param tempCluster Cluster (acertamento de cluster) para ser utilizado como base - A estrutura é realmente um acertamento de cluster, em que cada cluster é representado por uma linha diferente <p>[Eng] Cluster (set of clusters) to be used as base - The structure actually is a set of clusters, where each cluster is represented by a different line 
	 * @param lineFromCluster Número da linha do cluster atual <p>[Eng] Line number from current cluster 
	 * @return Número de sensores (#colunas) no cluster / linha de lineFromCluster <p>[Eng] Number of sensors (#columns) in the cluster/line from lineFromCluster
	 */
	private int sendSenseRequestMessageToAllSensorsInCluster(ArrayList2d<Double, SimpleNode> tempCluster, int lineFromCluster) {
		int numSensorsInThisCluster = 0;
		if (tempCluster == null) { // If there isn't a message group yet
			Utils.printForDebug("ERROR in sendSenseRequestMessageToAllCluster method: There isn't tempCluster object instanciated yet!");
		}
		else {
			int col = 0;
			numSensorsInThisCluster = tempCluster.getNumCols(lineFromCluster);
			while (col < numSensorsInThisCluster) {
				SimpleNode currentNode = tempCluster.get(lineFromCluster, col);
				//currentWsnMsgResp.hopsToTarget--;

				WsnMsg wsnMessage = new WsnMsg(1, this, currentNode, this, 1, sizeTimeUpdate, dataSensedTypes);
								
				wsnMessage.removeCoefs(); // Identifies this message as requesting sensing and not sending coefficients
				wsnMessage.setPathToSenderNode(currentNode.getPathToSenderNode(), currentNode.hopsToTarget); // Sets the path (route) to destination node (source) - same "currentWsnMsgResp.origem"
//				System.out.println("Mensagem solicitando dados enviada para o node ID = "+currentWsnMsgResp.source.ID);
				
				if (wsnMessage.hopsToTarget > 0) {
					wsnMessage.hopsToTarget--;					
				}
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
	 * Identifica qual cluster (número da linha), onde o "newNode" está<p>
	 * [Eng] It identifies which cluster (line number) where the "newNode" is 
	 * @param newNode SimpleNode representando o nó sensor para ser localizado no nodeGroups <p>[Eng]SimpleNode representing the sensor node to be localized in nodeGroups 
	 * @return Número da linha (cluster) do sensor passado como parâmetro, caso contrário, retorna -1 indicando que não existe esse nó sesnor ("newNode") em nenhum cluster <p>[Eng] Line number (cluster) from node passed by; otherwise, returns -1 indicating that there is no such node("newNode") in any cluster
	 */
	private static int identifyCluster(SimpleNode newNode) {
		int lineCLuster = -1;
		if (nodeGroups == null) { // If there isn't a message group yet
			Utils.printForDebug("ERROR in identifyCluster(SimpleNode) method: There isn't nodeGroups object instanciated yet!");
		}
		else {
			boolean found = false;
			int line = 0, col = 0;
			while ((!found) && (line < nodeGroups.getNumRows())) {
				col = 0;
				while ((!found) && (col < nodeGroups.getNumCols(line))) {
					SimpleNode currentNode = nodeGroups.get(line, col);
					if (isEqualNode(currentNode, newNode)) {
						found = true;
					}
					else {
						col++;
					}
				}
				if (!found) {
					line++;
				}
			}
			if (found) {
				lineCLuster = line;
			}
		}
		return lineCLuster;
	} // end identifyCluster(SimpleNode newNode)

	/**
	 * Ele remove o nó (newWsnMsgResp.source passado por parâmetro ) do grupo/cluster indicado por "tempCluster" e indicar se o grupo tornou-se vazio <p>
	 * [Eng] It removes the node (newWsnMsgResp.source passed by param) from the group/cluster indicated by "tempCluster" and indicate if the cluster became empty
	 * @param tempCluster Grupo de aglomerados que o nó (newWsnMsgResp.source) que serão removidos <p>[Eng] Group of clusters which the node (newWsnMsgResp.source) will be removed 
	 * @param newWsnMsgResp  Mensagem que contém o nó (newWsnMsgResp.source), que será removido do grupo de aglomerado <p>[Eng] Message that contains the node (newWsnMsgResp.source) which will be removed from group of cluster 
	 * @return Se o cluster desta mensagem / nó ficou vazio após a remoção do nó "newWsnMsgResp.source" <p>[Eng] If the cluster of this message / node became empty after removal of the node in "newWsnMsgResp.source"
	 */
/*
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
				}
				else {
					tempCluster.remove(line,col);
				}
			}
		}
		return receivedAll;
	} // end removeNodeAndChecksIfDataReceivedFromAllNodesInCluster(ArrayList2d<WsnMsgResponse> tempCluster, WsnMsgResponse newWsnMsgResp)
*/
	
	/**
	 * Ele seleciona o nó representante para cada linha (cluster) dos sensores pela energia residual máxima e coloca-lo na primeira posição (em linha) <p>
	 * [Eng] It selects the Representative Node for each line (cluster) from sensors by the max residual energy and puts him in the first position (in line)
	 * @param tempCluster cluster(ArrayList), que terá os sensores ordenados (linha por linha) pela energia residual máxima <p>[Eng] Cluster (ArrayList) which will have the sensors ordered (line by line) by the max residual energy 
	 */
	private void classifyRepresentativeNodesByResidualEnergy(ArrayList2d<Double, SimpleNode> tempCluster) {
		if (tempCluster != null) { // If there is a message group created 
			for (int line=0; line < tempCluster.getNumRows(); line++) {
				double maxBatLevel = 0.0;
				int maxBatLevelIndexInThisLine = 0;
				int bubbleLevel = 0;
				while (bubbleLevel < (tempCluster.getNumCols(line) - 1)) {
					for (int col=bubbleLevel; col < tempCluster.getNumCols(line); col++) {
						SimpleNode currentWsnMsgResp = tempCluster.get(line, col);
						double currentBatLevel = currentWsnMsgResp.lastBatLevel;
						int currentIndex = col;
						if (currentBatLevel > maxBatLevel)
						{
							maxBatLevel = currentBatLevel;
							maxBatLevelIndexInThisLine = currentIndex;
						} // end if (currentBatLevel > maxBatLevel)
					} // end for (int col=bubbleLevel; col < tempCluster.getNumCols(line); col++)
					if (maxBatLevelIndexInThisLine != 0) {
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
	 * @param tempCluster Cluster(ArrayList), que terá os sensores ordenados (linha por linha - como um segundo critério) pelo número mínimo de saltos para o sink <p>[Eng] Cluster (ArrayList) which will have the sensors ordered (line by line - as a second criterion) by the min number of hops to sink 
	 */
	private void classifyRepresentativeNodesByHopsToSink(ArrayList2d<Double, SimpleNode> tempCluster) {
		if (tempCluster != null) { // If there is a message group created 
			for (int line=0; line < tempCluster.getNumRows(); line++) {
				if (tempCluster.get(line, 0) != null) {
					int minIndexNumHopsToSink = 0;
					boolean sameBatLevel = false;
					SimpleNode firstNodeInLine = tempCluster.get(line, 0);
					int col=1;
					while ((col < tempCluster.getNumCols(line)) && (tempCluster.get(line, col).lastBatLevel == firstNodeInLine.lastBatLevel) && (tempCluster.get(line, col).hopsToTarget < firstNodeInLine.hopsToTarget) ) {
						minIndexNumHopsToSink = col;
						sameBatLevel = true;
						col++;
					} // end while ((col < tempCluster.getNumCols(line)) && (tempCluster.get(line, col).batLevel == firstWsnMsgRespInLine.batLevel) && (tempCluster.get(line, col).hopsToTarget < firstWsnMsgRespInLine.hopsToTarget) )
					if (sameBatLevel) {
						changePositionInLine(line, minIndexNumHopsToSink);
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
	private void printClusterArray2d(ArrayList2d<Double, SimpleNode> cluster) {
		if (cluster != null) { // If there is a message group created
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
			for (int line=0; line < cluster.getNumRows(); line++) {
				for (int col=0; col < cluster.getNumCols(line); col++) {
					SimpleNode currentNode = cluster.get(line, col);
					currentNode.setColor(currentRandomColor);
					Utils.printForDebug("Line = "+line+", Col = "+col+": NodeID = "+currentNode.ID+" BatLevel = "+currentNode.lastBatLevel+" Round = "+currentNode.lastRoundRead);
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
	private void printClusterArray(ArrayList2d<Double, SimpleNode> cluster) {
		if (cluster != null) { // If there is a message group created
			for (int line=0; line < cluster.getNumRows(); line++) {
				Utils.printForDebug("Line = "+line+": ");
				System.out.println("Line = "+line+": ");
				for (int col=0; col < cluster.getNumCols(line); col++) {
					SimpleNode currentNode = cluster.get(line, col);
					Utils.printForDebug("NodeID = "+currentNode.ID+" ");
					System.out.println("NodeID = "+currentNode.ID+" ");
				}
				Utils.printForDebug("");
				System.out.println("");
			}
			Utils.printForDebug("Number of Lines / Clusters = "+cluster.getNumRows()+"\n");
			System.out.println("Number of Lines / Clusters = "+cluster.getNumRows()+"\n");
		}
	} // end printClusterArray(ArrayList2d<WsnMsgResponse> cluster)
	
	
	/**
	 * Muda a mensagem de posição "index" para a primeira posição [0] nesta linha do array<p>
	 * [Eng] Change the message from "index" position for the first position [0] in that line from array
	 * @param line 
	 * @param index 
	 */
	private void changePositionInLine(int line, int index) {
		nodeGroups.move(line, index, 0);
	} // end changeMessagePositionInLine(int line, int index)
	
	/**
	 * Adiciona o objeto SimpleNode (newNode), passado como parâmetro, na linha correta ("Cluster") a partir do tempCluster (ArrayList2d), 
	 * de acordo com a Medida de Dissimilaridade
	 * PS:. Cada linha tempCluster (ArrayList2d de objetos SimpleNode) representa um cluster de sensores
	 * classificada pela Medida de Dissimilaridade a partir de dados sensoriados, armazenado em WsnMsgResponse.dataRecordItens <p>
	 * [Eng] Adds the WsnMsgResponse object (newWsnMsgResp), passed by parameter, in the correct line ("Cluster") from the tempCluster (ArrayList2d) according with the Dissimilarity Measure 
	 * PS:. Each line in tempCluster (ArrayList2d of objects WsnMsgResponse) represents a cluster of sensors, 
	 * classified by Dissimilarity Measure from yours data sensed, stored on WsnMsgResponse.dataRecordItens
	 *  
	 * @param tempCluster ArrayList2d de sensores, organizados em clusters (linha por linha) <p>
	 * [Eng] ArrayList2d from sensors, organized as clusters (line by line) 
	 * @param newNode Nó sensor utilizado para classificação  <p>
	 * [Eng] Message to be used for classify the sensor node 
	 */
	private void addNodeInCluster(ArrayList2d<Double, SimpleNode> tempCluster, SimpleNode newNode) {
		boolean found = false;
		int line = 0;
		while ((!found) && (line < tempCluster.getNumRows()))
		{
			int col = 0;
			boolean continueThisLine = true;
			while ((continueThisLine) && (col < tempCluster.getNumCols(line)))
			{
				SimpleNode currentNode = tempCluster.get(line, col);
				if (testDistanceBetweenSensorPositions(currentNode, newNode))
				{
					if (testSimilarityMeasureWithPairRounds(currentNode, newNode)) // If this (new)message (with sensor readings) already is dissimilar to current message
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
				Double fracDim = tempCluster.getKey(line);
				tempCluster.add(newNode, line, fracDim);
			}
			else
			{
				line++;
			}
		}
		if (!found)
		{
			Double newFractalDim = new Double(0.0);
			tempCluster.add(newNode, tempCluster.getNumRows(), newFractalDim); // It adds the new message "wsnMsgResp" in a new line (cluster) of messageGroup 
		}
	} // end addNodeInClusterClassifiedByMessage(ArrayList2d<WsnMsgResponse> tempCluster, WsnMsgResponse newWsnMsgResp)
	
	/**
	 * Adiciona os nós contidos em "nodesReceived", passados por parâmetro, na linha correta ("Cluster") de newCluster (ArrayList2d), de acordo com a Medida de Similaridade
     * PS:. Cada linha de "newCluster" (ArrayList2d de objetos SimpleNode) representa um conjunto de sensores (cluster),
     * classificado pela Medida de Similaridade a partir de dados de sensoriamento, armazenados em SimpleNode.dataRecordItens <p>
	 * [Eng] Adds the nodes in nodesReceived, passed by parameter, in the correct line ("Cluster") from the newCluster (ArrayList2d) according with the Similarity Measure 
	 * PS.: Each line in newCluster (ArrayList2d of objects SimpleNode) represents a cluster of sensors, 
	 * classified by Similarity Measure from yours data sensed, stored on SimpleNode.dataRecordItens
	 *  
	 * @param nodesReceived Cluster de nós sensores com dados recebidos pelo sink
	 * @param newCluster Cluster de sensores que irá receber e agrupar corretamente os sensores com novos dados recebidos
	 */
	private void addNodesInNewCluster(ArrayList2d<Double, SimpleNode> nodesReceived, ArrayList2d<Double, SimpleNode> newCluster)
	{
		if (nodesReceived == null) {
			Utils.printForDebug("ERROR in addNodesInCluster method: nodesReceived == null!");
		} // end if (nodesReceived == null)
		else {
			for (int row=0; row < nodesReceived.getNumRows(); row++) {
				for (int colu=0; colu < nodesReceived.getNumCols(row); colu++) {
					boolean found = false;
					int line = 0;
					SimpleNode nodeReceived = nodesReceived.get(row, colu);
//					SimpleNode nodeReceived = nodesReceived.remove(row, colu);
					while ((!found) && (line < newCluster.getNumRows())) {
						int col = 0;
						boolean continueThisLine = true;
						while ((continueThisLine) && (col < newCluster.getNumCols(line))) {
							SimpleNode currentNewNode = newCluster.get(line, col);
							if (testDistanceBetweenSensorPositions(currentNewNode, nodeReceived)) {
								if (testSimilarityMeasureWithPairRounds(currentNewNode, nodeReceived)) { // If this (new)message (with sensor readings) already is dissimilar to current message
									continueThisLine = true; // Then this (new)message doesn't belong to this cluster / line / group
								} // end if (testSimilarityMeasureWithPairRounds(currentNewNode, nodeReceived))
								else {
									continueThisLine = false;
								} // end else from if (testSimilarityMeasureWithPairRounds(currentNewNode, nodeReceived))
							} // end if (testDistanceBetweenSensorPositions(currentNewNode, nodeReceived))
							else {
								continueThisLine = false;
							} // end else from if (testDistanceBetweenSensorPositions(currentNewNode, nodeReceived))
							col++;
						} // end while ((continueThisLine) && (col < newCluster.getNumCols(line)))
						if ((continueThisLine) && (col == newCluster.getNumCols(line))) {
							found = true;
							newCluster.add(nodeReceived, line);
						} // end if ((continueThisLine) && (col == newCluster.getNumCols(line)))
						else {
							line++;
						} // end else from if ((continueThisLine) && (col == newCluster.getNumCols(line)))
					} // end 
					if (!found) {
						newCluster.add(nodeReceived, newCluster.getNumRows()); // It adds the new message "wsnMsgResp" in a new line (cluster) of messageGroup 
					} // end if (!found)
				} // end for (int colu=0; colu < nodesReceived.getNumCols(row); colu++)
			} // end for (int row=0; row < nodesReceived.getNumRows(); row++)
			//nodesReceived.removeAll(); 
		} // end else from if (nodesReceived == null)
	} // end addNodeInClusterClassifiedByMessage(ArrayList2d<WsnMsgResponse> tempCluster, WsnMsgResponse newWsnMsgResp)

	/**
	 * Procura o objeto SimpleNode, na posição correta em "Cluster" de "nodeGroups" (ArrayList2d)
	 * e substitui com o que é passado por parâmetro
	 * PS:. Cada linha de "nodeGroups" (ArrayList2d de objetos SimpleNode) representa um cluster de sensores (SimpleNode) 
	 * classificada pela Medida de Dissimilaridade a partir dos dados sensoriados, armazenados em SimpleNode.dataRecordItens <p>
	 * [Eng] Search the SimpleNode object, in the correct position in "Cluster" from the "nodeGroups" (ArrayList2d) 
	 * and replace him with the one passed by parameter
	 * PS.: Each line in "nodeGroups" (ArrayList2d of objects SimpleNode) represents a cluster of sensors (SimpleNode), 
	 * classified by Dissimilarity Measure from yours data sensed, stored on SimpleNode.dataRecordItens
	 *  
	 * @param newNode Nó sensor a ser classificado  <p>[Eng] Sensor node to be classified
	 */
	private int searchAndReplaceNodeInCluster(SimpleNode newNode) {
		int lineCLuster = -1;
		if (nodeGroups == null) { // If there isn't a message group yet
			Utils.printForDebug("ERROR in searchAndReplaceNodeInCluster method: There isn't nodeGroups object instanciated yet!");
		} // end if (nodeGroups == null)
		else {
			boolean found = false;
			int line = 0, col = 0;
			SimpleNode currentNode = null;
			while ((!found) && (line < nodeGroups.getNumRows())) {
				col = 0;
				while ((!found) && (col < nodeGroups.getNumCols(line))) {
					currentNode = nodeGroups.get(line, col);
					if (isEqualNode(currentNode, newNode)) {
						found = true;
					} // end if (isEqualNode(currentNode, newNode))
					else {
						col++;
					} // end else from if (isEqualNode(currentNode, newNode))
				}
				if (!found) {
					line++;
				} // end if (!found)
			}
			if (found) {
				lineCLuster = line;
				newNode.myCluster = currentNode.myCluster;
				nodeGroups.set(line, col, newNode); // It sets the new node "newNode" in the line and col (cluster) of nodeGroups
			} // end if (found)
		} // end else from if (nodeGroups == null)
		return lineCLuster;
	} // end searchAndReplaceNodeInCluster(SimpleNode newNode)

	/**
	 * Insere o objeto "SimpleNode" passado em todos os "clusters" de newCluster (ArrayList2d)
	 * PS:. Cada linha "newCluster" (ArrayList2d de objetos SimpleNode) representa um cluster de sensores, <p>
	 * classificada pela Medida de Similaridade a partir de dados sensoriados, armazenada em SimpleNode.dataRecordItens <p>
	 * [Eng] Inserts the SimpleNode object in all "clusters" from newCluster (ArrayList2d) 
	 * PS.: Each line in "newCluster" (ArrayList2d of SimpleNode objects) represents a cluster of sensors, 
	 * classified by Dissimilarity Measure from yours data sensed, stored on SimpleNode.dataRecordItens
	 *  
	 * @param newNode Nó sensor a ser inserido <p>[Eng] Message to be used for classify the sensor node 
	 */
	private void insertNewNodeInClusters(ArrayList2d<Double, SimpleNode> newCluster, SimpleNode newNode)
	{
		if (newCluster == null) { // If there isn't a valid node cluster
			Utils.printForDebug("ERROR in insertNewNodeInClusters method: There isn't newCluster object instanciated yet!");
		} // end if (newCluster == null)
		else {
			for (int line = 0; line < newCluster.getNumRows(); line++) {
				newCluster.add(line, newNode);
			}
//				newNode.myCluster = currentNode.myCluster;
		} // end else from if (newCluster == null)
	} // end searchAndReplaceNodeInClusterByMessage(WsnMsgResponse newWsnMsgResp)

	/**
	 * Testa se a distância entre os nós sensores que enviaram tais mensagens (currentWsnMsg e newWsnMsg) é menor ou igual a máxima distância possível (maxDistance) para os dois nós estarem no mesmo cluster <p>
	 * [Eng] Tests if the distance between the sensor nodes send those messages (currentWsnMsg e newWsnMsg) is less than or equal to the maximum possible distance (maxDistance) to the two nodes are in the same cluster
	 * @param currentWsnMsg Mensagem atual já classificada no cluster <p>[Eng] Current message already classified in cluster 
	 * @param newNode Nova mensagem a ser classificada no cluster <p>[Eng] New message to be classified in the cluster
	 * @return Retorna "true" caso a distância entre os nós sensores que enviaram as mensagens não ultrapassa o limite máximo, "false" caso contrário <p>[Eng] Returns "true" if the distance between the sensor nodes that have sent messages does not exceed the maximum limit, "false" otherwise
	 */
	private boolean testDistanceBetweenSensorPositions(SimpleNode currentNode, SimpleNode newNode)
	{
		boolean distanceOK = false;
		if (maxDistance > 0.0)
		{
			if (currentNode.getPosition() != null && newNode.getPosition() != null && currentNode.getPosition().distanceTo(newNode.getPosition()) <= maxDistance)
			{
				distanceOK = true;
			}
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
	
	private static boolean isEqualNode(SimpleNode currentNode, SimpleNode newNode)
	{
		return (currentNode.ID == newNode.ID);
	} // end isEqualNode(WsnMsgResponse currentWsnMsg, WsnMsgResponse newWsnMsg)
	
	/**
	 * Ele testa se há divergência (falta de similaridade) entre os dois conjunto de medida de 2 Sensores trazidos por 2 mensagens <p>
	 * [Eng] It tests if there is dissimilarity (lack of similarity) between the 2 set of measure from 2 sensor brought by the 2 messages
	 * @param currentWsnMsg Representa a mensagem atual do grupo de mensagens (messageGroups) em um "<WsnMsgResponse> ArrayList2d" estrutura <p>[Eng] Represents the current message from the group of messages (messageGroups) in a "ArrayList2d<WsnMsgResponse>" structure 
	 * @param newNode Representa a mensagem chegou recentemente no nó sorvedouro, enviado a partir do nó sensor fonte <p>[Eng] Represents the recently arrived message in the sink node, sent from the source sensor node 
	 * @return Verdadeiro caso as duas mensagens são diferentes, ou seja, de clusters diferentes (ou "grupos"); falsos, caso contrário <p>[Eng] True case the two messages are DISsimilar, i.e., from different clusters (or "groups"); False, otherwise
	 */
	
	
	private boolean testSimilarityMeasureWithPairRounds(SimpleNode currentNode, SimpleNode newNode)
	{
//		boolean sameSize = true;
		boolean mSimilarityMagnitude = false;
		boolean tSimilarityTrend = false;
		
		int currentSize;
		double[][] currentValues;
		if (currentNode.dataRecordItens != null) {
			currentSize = currentNode.dataRecordItens.size();
			currentValues = new double[currentSize][];
		}
		else {
			return (mSimilarityMagnitude && tSimilarityTrend);
		}

		int[] currentRound = new int[currentSize];
		
		//Data read from current sensor (from ArrayList2d)
		//TODO: Analizar a corretude!!!
		for (int cont = 0; cont < currentSize; cont++) {
			currentValues[cont] = currentNode.getDataRecordValues(cont);
		}

		currentRound = currentNode.getDataRecordRounds();

		int newSize;
		double[][] newValues;
		if (newNode.dataRecordItens != null) {
			newSize = newNode.dataRecordItens.size();
			newValues = new double[newSize][];
		}
		else {
			return (mSimilarityMagnitude && tSimilarityTrend);
		}

		int[] newRound = new int[newSize];
		
		//Data read from new sensor (from message received)
		//TODO: Analizar a corretude2!!!
		for (int cont = 0; cont < newSize; cont++) {
			newValues[cont] = newNode.getDataRecordValues(cont);
		}

		newRound = newNode.getDataRecordRounds();

		HashMap<Integer, double[]> hashCurrentMsg, hashNewMsg;
		
		hashCurrentMsg = new HashMap<Integer, double[]>();
		hashNewMsg = new HashMap<Integer, double[]>();
		
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



		
		int numEqualKeys = 0;
		
		double[] sumDifs = new double[dataSensedTypes.length];
		
		Set<Integer> keys = hashCurrentMsg.keySet();
		for (Integer key : keys)
		{
			if (hashNewMsg.containsKey(key))
			{
				numEqualKeys++;
				double[] curValue = hashCurrentMsg.get(key);
				double[] newValue = hashNewMsg.get(key);
				for (int numTypes = 0; numTypes < dataSensedTypes.length; numTypes++) {
					sumDifs[numTypes] += Math.abs(curValue[numTypes] - newValue[numTypes]);
				}
			}
		}

		if (numEqualKeys > 0)
		{
			int cont = 0;
			while ((cont < dataSensedTypes.length) && (sumDifs[cont]/numEqualKeys <= spacialThresholdErrors[cont])) { // It tests if all attributes (dimensions) are in mSimilarityMagnitude
				cont++;
			}
			if (cont == dataSensedTypes.length) {
				mSimilarityMagnitude = true;
			}

		}

		double contN1[] = new double[dataSensedTypes.length];
		double contN = currentSize; // = newSize; // Total size of sensed values from node
		for (int i=1,j=1; (i < currentSize && j < newSize); i++, j++)
		{
			double[] difX = new double[dataSensedTypes.length];
			double[] difY = new double[dataSensedTypes.length];
			for (int numTypes = 0; numTypes < dataSensedTypes.length; numTypes++) {
				difX[numTypes] = (currentValues[i][numTypes] - currentValues[i-1][numTypes]);
				difY[numTypes] = (newValues[j][numTypes] - newValues[j-1][numTypes]);
				if ((difX[numTypes] * difY[numTypes]) >= 0)
				{
					contN1[numTypes]++;
				}
			}
		}
		if (contN > 0.0) {
			int cont = 0;
			while ((cont < dataSensedTypes.length) && (contN1[cont]/contN >= thresholdErrors[cont])) { // It tests if all attributes (dimensions) are in tSimilarityTrend
				cont++;
			}
			if (cont == dataSensedTypes.length) {
				tSimilarityTrend = true;
			}	
		}

		return (mSimilarityMagnitude && tSimilarityTrend);
	} // end testSimilarityMeasureWithPairRounds(WsnMsgResponse currentWsnMsg, WsnMsgResponse newWsnMsg)

	/**
	 * Recebe a mensagem passada, lê os parâmetros (itens) no dataRecordItens,
	 * calcula os coeficientes A e B de acordo com estes parâmetros e envia tais
	 * coeficientes para o nó sensor de origem
	 * <p>
	 * [Eng] Receives the message, reads the parameters (items) in
	 * dataRecordItens, calculates the coefficients A and B according to these
	 * parameters and sends these coefficients for the sensor node of origin
	 * 
	 * @param receivedNode
	 *            Mensagem recebida com os parâmetros a serem lidos <p>[Eng] Received message with the parameters to be read
	 * @param clusterHeadNode
	 *            Indica(seta) o ClusterHead daquele cluster <p>[Eng] Indicates (arrow) the cluster that clusterhead
	 */
	private void receiveMessage(SimpleNode receivedNode, Node clusterHeadNode)
	{
		if (receivedNode != null && receivedNode.dataRecordItens != null)
		{
			int size = receivedNode.dataRecordItens.size();
			double[][] valores = new double[size][];
			double[] tempos = new double[size];

			//Dados lidos do sensor correspondente
			for (int cont=0; cont < size; cont++) {
				valores[cont] = receivedNode.getDataRecordValues(cont);
			}
			tempos = receivedNode.getDataRecordTimes();

			//Coeficientes de regressão linear com os vetores acima
			double[] coeficientesA, coeficientesB;
			double mediaTempos, mediaValores[];

			//Médias dos valores de leitura e tempos
			mediaTempos = calculatesAverage(tempos);
			mediaValores = calculatesAverage(valores);

			//Cálculos dos coeficientes de regressão linear com os vetores acima
			coeficientesB = calculatesBs(valores, tempos, mediaValores, mediaTempos);
			coeficientesA = calculatesAs(mediaValores, mediaTempos, coeficientesB);
			
			sendCoefficients(receivedNode, coeficientesA, coeficientesB, clusterHeadNode);
		}
	} // end receiveMessage(WsnMsgResponse wsnMsgResp, Node clusterHeadNode)
	
	/**
	 * Calcula e retorna a média aritmética dos valores reais passados <p>
	 * [Eng] Computes and returns the arithmetic mean of the actual values ??passed
	 * @param values Array de valores reais de entrada <p>[Eng] Array of real values ??input
	 * @return Média dos valores reais de entrada <p>[Eng] Mean of the actual values ??of input
	 */
	private double calculatesAverage(double[] values)
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
	 * Calcula e retorna a média aritmética dos valores reais passados <p>
	 * [Eng] Computes and returns the arithmetic mean of the actual values passed
	 * @param values Array de valores reais de entrada <p>[Eng] Array of real input values
	 * @return Média dos valores reais de entrada <p>[Eng] Mean of the actual input values 
	 */
	private double[] calculatesAverage(double[][] values)
	{
		double means[] = new double[values[0].length];
		for (int secondDim = 0; secondDim < values[0].length; secondDim++)
		{
			double mean = 0, sum = 0;
			for (int firstDim = 0; firstDim < values.length; firstDim++) {
				sum += values[firstDim][secondDim];
			}
			if (values.length > 0)
			{
				mean = sum/values.length;
			}
			means[secondDim] = mean;
		}
		return means;
	} // end calculaMedia(double[] values)

	
	/**
	 * Calcula o coeficiente B da equação de regressão <p>
	 * [Eng] Calculates the coefficient B of the regression equation
	 * @param values Array de valores (grandezas) das medições dos sensores <p>[Eng] Array of values (magnitudes) of the measurements of the sensors
	 * @param times Array de tempos das medições dos sensores <p>[Eng] Array times of the measurements of the sensors
	 * @param averageValues Média dos valores lidos pelos sensores <p>[Eng] Mean of values read by the sensors 
	 * @param avarageTimes Média dos tempos de leitura dos valores pelos sensores <p>[Eng] Mean time reading the values from sensors <b> mediaTempos </b>
	 * @return Valor do coeficiente B da equação de regressão <p>[Eng] Value of the coefficient B of the regression equation
	 */
	private double[] calculatesBs(double[][] values, double[] times, double[] averageValues, double avarageTimes)
	{
		double Bs[] = new double[values[0].length];
//		System.out.println("@ @ @ # # # values.length = "+values.length+" AND times.length = "+times.length);
		for (int secondDim = 0; secondDim < dataSensedTypes.length; secondDim++) {
//			System.out.println("@ @ @ firstDim = "+firstDim);
			double numerador = 0.0, denominador = 0.0, x = 0.0;
//				System.out.println("@ @ @ i = "+i);
//				System.out.println("@ @ @ # # # values[firstDim][i] = "+values[firstDim][i]+" AND averageValues[firstDim] = "+averageValues[firstDim]);
			for (int firstDim = 0; firstDim < values.length; firstDim++) {

				x = times[firstDim] - avarageTimes;
				numerador += x*(values[firstDim][secondDim] - averageValues[secondDim]);
				denominador += x*x;
			}
			if (denominador != 0) {
				Bs[secondDim] = (numerador/denominador);
			}
			else {
				Bs[secondDim] = 0.0;
			}
		}
		return Bs;
	} // end calculatesBs(double[][] values, double[] times, double[] averageValues, double avarageTimes)
	
	/**
	 * Calcula o coeficiente A da equação de regressão <p>
	 * [Eng] Calculate coefficient A of the regression equation
	 * @param averageValues Média dos valores lidos pelos sensores <p>[Eng] Mean of values ??read by the sensors 
	 * @param avarageTimes Média dos tempos de leitura dos valores pelos sensores <p>[Eng] Mean time reading the values ??from sensors 
	 * @param B Valor do coeficiente B da equação de regressão <p>[Eng] Value of the coefficient B of the regression equation 
	 * @return Valor do coeficiente A <p>[Eng] Value of the coefficient A
	 */
	private double[] calculatesAs(double[] averageValues, double avarageTimes, double[] Bs)
	{
		double As[] = new double[averageValues.length];
		for (int secondDim = 0; secondDim < averageValues.length; secondDim++) {
			As[secondDim] = (averageValues[secondDim] - Bs[secondDim]*avarageTimes);
		}
		return As;
	} // end calculaA(double mediaValores, double mediaTempos, double B)
	
	/**
	 * Cria uma nova mensagem (WsnMsg) para envio dos coeficientes recebidos através dos parâmetros, e a envia para o próximo nó no caminho até o nó de origem da mensagem (wsnMsgResp.origem) <p>
	 * [Eng] Creates a new message (WsnMsg) for sending coefficients received through the parameters, and sends it to the next node on the path until the source node of the message (wsnMsgResp.origem)
	 * @param sourceNode Mensagem de resposta enviada do nó de origem para o nó sink, que agora enviará os (novos) coeficientes calculados para o nó de origem <p>[Eng] Response message sent from the source node to the sink node, which now sends the (new) coefficients calculated for the source node 
	 * @param coefficientsA Valor do coeficiente A da equação de regressão <p>[Eng] Value of the coefficient A of the regression equation 
	 * @param coefficientsB Valor do coeficiente B da equação de regressão <p>[Eng] Value of the coefficient B of the regression equation 
	 */

	private void sendCoefficients(SimpleNode sourceNode, double[] coefficientsA, double[] coefficientsB, Node clusterHeadNode)
	{
		WsnMsg wsnMessage = new WsnMsg(1, this, sourceNode, this, 1, sourceNode.myCluster.sizeTimeSlot, dataSensedTypes, thresholdErrors, clusterHeadNode);
		wsnMessage.setCoefs(coefficientsA, coefficientsB);
		wsnMessage.setPathToSenderNode(sourceNode.getPathToSenderNode(), sourceNode.hopsToTarget);
		sendToNextNodeInPath(wsnMessage);
	} // end sendCoefficients(WsnMsgResponse wsnMsgResp, double coeficienteA, double coeficienteB, Node clusterHeadNode)
	
	/**
	 * Retorna todos os nós sensores no mesmo cluster do nó representante <p>
	 * [Eng] Returns all sensor nodes in the same cluster from representative node
	 * @param rn Nó representante <p> [Eng] Representative Node 
	 * @return Todos nós sensores no mesmo cluster <p>[Eng] All sensor nodes in the same cluster
	 */
	public static Node[] getNodesFromThisCluster(Node rn) {
		Node[] nodes = null;
		ArrayList<SimpleNode> nodesArray;
		int numClusterLine = identifyCluster((SimpleNode)rn);
		int cont = 0;
		if (numClusterLine >= 0) {
			nodesArray = nodeGroups.get(numClusterLine);
			nodes = new Node[nodesArray.size()];
			for (SimpleNode node : nodesArray) {
				nodes[cont] = node;
				cont++;
			}
		}
		return nodes;
	}
	
	public ArrayList2d<Double, SimpleNode> ensuresArrayList2d(ArrayList2d<Double, SimpleNode> array2d) {
		if (array2d == null) {// If there isn't a node group yet, then it does create one and adds the node to it
			array2d = new ArrayList2d<Double, SimpleNode>();
	 		//NodesToReceiveDataReading.ensureCapacity(numTotalOfSensors); // Ensure the capacity as the total number of sensors (nodes) in the data set
		}
		return array2d;
	}

	/**
	 * O sizeTimeSlot do nó representativo que irá ser inversamente proporcional ao número de sensores no mesmo aglomerado -> número de "L" em documentationn <p>
	 * [Eng] The sizeTimeSlot of Representative Node will be inversely proportional to the number of sensors in the same cluster -> number 'L' in documentation
	 * @param globalTimeSlot Initial Size of Time Slot from the sink node
	 * @param numSensorsInThisCLuster Number of nodes (sensors) in that cluster (group)
	 * @return The sizeTimeSlot of Representative Node
	 */
	public Integer calculatesTheSizeTimeSlotFromRepresentativeNode(int globalTimeSlot, int numSensorsInThisCLuster)
	{
		Integer newSizeTimeSlot = 0;
		
		if (numSensorsInThisCLuster == 0) {
			numSensorsInThisCLuster = 1;
		}
			
		newSizeTimeSlot = (int)(globalTimeSlot / numSensorsInThisCLuster);
		//this.newSizeTimeSlot = SimpleNode.maxErrorsPerCluster; // O NR realizará os testes de novidades de tal forma que, apenas quando o número máximo de ciclos (rounds) for atingido (maxErrorsPerCluster) um novo NR será calculado;
		
		if (newSizeTimeSlot < 1) { // newSizeTimeSlot shouldn't be equal to 0 (or less than one)
			newSizeTimeSlot = 1;
		}
		return newSizeTimeSlot;
	}

	
	
}
