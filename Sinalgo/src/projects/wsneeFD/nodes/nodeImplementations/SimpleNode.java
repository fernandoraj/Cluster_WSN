package projects.wsneeFD.nodes.nodeImplementations;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import projects.defaultProject.nodes.timers.DirectMessageTimer;
import projects.wsneeFD.nodes.messages.WsnMsg;
import projects.wsneeFD.nodes.messages.WsnMsgResponse;
import projects.wsneeFD.nodes.messages.WsnMsgResponse.DataRecord;
import projects.wsneeFD.nodes.messages.WsnMsgResponse.MessageItens;
import projects.wsneeFD.nodes.timers.PredictionTimer;
import projects.wsneeFD.nodes.timers.WsnMessageResponseTimer;
import projects.wsneeFD.nodes.timers.WsnMessageTimer;
import projects.wsneeFD.utils.FileHandler;
import projects.wsneeFD.utils.Utils;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.nodes.Node;
import sinalgo.nodes.TimerCollection;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.Global;
import sinalgo.tools.Tools;

/**
 * Class that represents an ordinary sensor node that is able to sense natural phenomena
 * (e.g. temperature, pressure, humidity) and send to sink nodes.
 * In our simulation, this node is also able to do in-networking data prediction using 
 * regression models.
 * @author Fernando Rodrigues / Alex Lacerda
 * For RMSE calculation, search for: Global.predictionsCount and Global.squaredError
 */
public class SimpleNode extends Node 
{
	/**
	 * Indica o tamanho da janela deslizante das leituras do sensor que serão enviadas ao sink node quando houver uma "novidade"<p>
	 * [Eng] Indicates the size of Sliding Window from sensor readings to be send to sink node when there is a "novelty".
	 */
	protected static int slidingWindowSize = 10; // According ADAGA-P* = 7 	// This value would be, at least, 10 ^ N, N = quantity of dimensions from data, 
																			// i.e., number of dimensions from "dataSensedTypes", e.g., if dataSensedTypes
																			// = {4,5} => N = 2 => sizeTimeSlot >= 10^2 = 100.

	/**
	 * Número máximo(limite) de predições dos erros de qualquer nó sensor - Isso também pode ser expressado em percentual(double) do total de timeslot.<p> 
	 * [Eng] Maximum (limit) Number of prediction errors of any sensor node - It also could be expressed in percentage (i.e., double) from total timeSlot
	 */
	protected static final double sensorDelay = 2; //1; //5; // SensorDelay = (was) limitPredictionError
	
	/**
	 *  Número máximo(limite) de erro dos nós dos sensores por cluster -  Sobre o limite, o cluster head comunica-se com o sink.<p>
	 * [Eng] Maximum (limit) Number of sensor node's error messages per cluster - above this limit, the cluster head communicates to sink.
	 */
	public static final int clusterDelay = 5; //1; //5; //ClusterDelay = (was) maxErrorsPerCluster
	
	/**
	 * Indica o tamanho da janela deslizante das leituras do sensor que serão enviadas ao sink node quando houver uma "novidade"<p>
	 * [Eng] Indicates the size of Sliding Window from sensor readings to be send to sink node 
	 * when there is a "novelty".
	 */
	protected int windowSize;

	/**
	 * Slot de tempo próprio para cada sensor(representativo).<p>
	 * [Eng] Own slot time from each (representative) sensor - cluster head.
	 */
	protected Integer ownTimeSlot;
	
	/**
	 * Salva/Armazena o nó que será usado para alcançar a Estação-Base.<p>
	 * [Eng] Save/storage the node that will be used for range the Base Station.
	 */
	protected Node nextNodeToBaseStation;
	
	/**
	 * Armazena o número de sequencia da última mensagem recebida<p> 
	 * [Eng] Store the sequency number of the last mensage receveid.
	 */
	protected Integer sequenceNumber = 0;
	
	/**
	 * Valor do último round em que houve leitura do sensor (que teve valor lido do arquivo).<p>
	 * [Eng] Value of the last round which the sensor was read( that has the value read by the file).
	 */
	protected int lastRoundRead;

	/**
	 * Número de predições feito pelo nó do sensor nesse timeslot.<p>
	 * [Eng] Number of predictions made by the sensor node in this timeslot.
	 */
	protected int numTotalSensorReadings = 0;
	
	/**
	 * Valor (grandeza/magnitude) da última leitura do sensor.<p>
	 * [Eng] Value(grandeur/magnitude) of the last sensor read.
	 */
	protected double[] lastValuesRead;
	
	/**
	 * Tempo (data/hora em milisegundos) da última leitura do sensor.<p> 
	 * [Eng] Time(date/ time in milliseconds) of the last sensor read.
	 */
	protected double lastTimeRead;
	
	/**
	 * Último nível de voltagem da bateria do sensor.<p>
	 * [Eng] Last voltage level of the battery from sensor.
	 */
	protected double lastBatLevel;
	
	/**
	 * Número de predições feito pelo nó do sensor nesse timeslot.<p>
	 * [Eng] Number of predictions made by the sensor node in this timeslot.
	 */
	protected int numTotalPredictions;
	
	/**
	 * Número de predições de erros do nó do sensor nesse timeslot.<p>
	 * [Eng] Number of prediction errors of the sensor node in this timeslot.
	 */
	protected int numPredictionErrors;
	
	/**
	 * Número de predições de erros do nó do sensor por tipo de dados.<p>
	 * [Eng] Number of prediction errors of the sensor node per data type sensed.
	 */
	protected int[] numPredictionErrorsPerType;
	
	/**
	 * Cluster que esse nó pertence.<p>
	 * [Eng] Cluster to which this node belongs.
	 */
	public Cluster myCluster;
	
	/**
	 * Nó do sensor que gerencia/representa o cluster para o qual esse nó pertence.<p>
	 * [Eng] Sensor node that manages/represents the cluster to which this node belongs.
	 */
	private Node clusterHead;

	/** 
	 * Número de sensores no cluster desse nó.<p>
	 * [Eng] Number of sensors in cluster of this node.
	 */
//	private int numSensorsInCluster;	
	
	/**
	 * Contador de mensagens de erro recebido pelo ClusterHead nesse cluster.<p>
	 * [Eng] Counter of message errors received by ClusterHead in this cluster.
	 */
	private int errorsInThisCluster;
	
	/**
	 * Nível mínimo(limite) da bateria dos cluster head's - Abaixo do limite, o cluster head comunica-se com o sink.<p>
	 * [Eng] Minimum (limit) level of cluster head's battery level - below this limit, the cluster head communicates to sink.
	 */
	private static final double minBatLevelInClusterHead = 2.1; // According KAMAL, A. R. M.; HAMID, M. A. Reliable data approximation in wireless sensor network. Ad Hoc Networks, n. July, jul. 2013. (l. 540)
	
	private double predictionsCount = 0.0;
	
	private double squaredError = 0.0;
	
	/**
	 * Caminho (formado pelos ID's dos nós) até o sink node, em forma de pilha <p>
	 * [Eng] Path (consisting of ID's of nodes) to the sink node in stack form
	 */
	private Stack<Integer> pathToSenderNode;
	
	/**
	 * Número de saltos até o destino <p>
	 * [Eng] Number of hops to the destination
	 */
	public Integer hopsToTarget = 0;
	
	/**
	 * Armazena as leituras dos sensores deste nó carregados do arquivo de leitura do sensor.
	 * Uma lista de link está começando a ser usado aqui por que como as leituras estão sendo realizadas
	 * (sendo lidas desta lista) por um nó de um sensor eles são discartados.<p>
	 * [Eng] Stores sensor readings of this node loaded from the sensor readings file.
	 * A linked list is being used here because as the readings are being performed 
	 * (being read from this list) by this sensor node they are discarded.
	 */
	private LinkedList<String> sensorReadingsQueue = new LinkedList<String>();
	
	/**
	 * Armazena o valor referenciando à última linha carregada do arquivo de leituras do sensor em ordem que 
	 * quando o <code>sensorReadingsQueue</code> está vazio e o novo carregamento do arquivo tem de ser 
	 * executado para preencher a lista <code>SensorReadingsLoadedFromFile</code>, o carregamento começa da última
	 * linha lida do arquivo.<p>
	 * [Eng] Stores the value referring to the last line loaded from the sensor readings file in order that
	 * when the <code>sensorReadingsQueue</code> is empty and a new load from the file has to be
	 * executed to fill the list <code>SensorReadingsLoadedFromFile</code>, the loading starts from the last
	 * line read from the file.
	 */
	private int lastLineLoadedFromSensorReadingsFile;

	/**
	 * Indica se as leituras do sensor devem ser carregados para o arquivo ou para a memória.
	 * Se o atributo é verdadeiro, o nó deve carregar as leituras do sensor diretamente do
	 * arquivo de leituras do sensor.Caso contrário, isso deve carregar as leituras do sensor da memória,
	 * que é, da lista de leitura dos sensores de todos os nós que são carregados na memória 
	 * antecipadamente pela classe {@link FileHandler}. Esse procedimento é necessário por que o arquivo 
	 * de leituras do sensor é muito largo e deve demorar bastante para ser carregado dependendo da configuração
	 * do computador. Para estes casos que carregam todas as leituras do sensor para a memória
	 * (no <code>FileHandler</code>) não é possível, as leituras do sensor são carregados pelo
	 * arquivo na demanda por cada nó.<p>
	 * 
	 * [Eng] Indicates whether the sensor readings must be loaded from the file or from the memory.
	 * If this attribute is true, the node must load the sensor readings direct from the
	 * sensor readings file. Otherwise, it must load the sensor readings from the memory, 
	 * that is, from the list of the sensor readings from all nodes that is loaded in memory 
	 * beforehand by the {@link FileHandler} class. This procedure is necessary because the sensor 
	 * readings file is very large and may take too long to be loaded depending on the computer 
	 * configuration. For the cases that loading all the sensor readings to the memory 
	 * (on the <code>FileHandler</code>) is not possible, the sensor readings are loaded from the
	 * file on demand by each node.
	 */
	private boolean loadSensorReadingsFromFile = true;

	/**
	 * Indicates whether all the sensor readings have already been loaded from the file
	 */
	private boolean loadAllSensorReadingsFromFile = false;
	
	/**
	 * Salva o round(ciclo) em que o primeiro CluterError ocorreu.<p>
	 * [Eng]Saves the round in which the first ClusterError occurred.
	 */
	private double initialErrorRound;
	
	/**
	 * Salva o round(ciclo) em que o último TriggerPrediction ocorreu.<p>
	 * [Eng]Saves the round in which the last TriggerPrediction occurred.
	 */
	private double lastRoundTriggered;
	
	private boolean validPredictions = true;
	
	private boolean canMakePredictions = false; // variavel para saber se o node pode fazer predições;
	
	private boolean minusOne = false; // Flag to set that the ClusterHead from this node will send "news" 
	//to sink in the next error (miss), so hereafter the nodes must don't compute the RMSE to error case

	public SimpleNode() {
		super();
		numPredictionErrorsPerType = new int[SinkNode.dataSensedTypes.length];
	}
	
	@Override
	public void preStep() {}

	@Override
	public void init() {}

	@Override
	public void neighborhoodChange() {}

	@Override
	public void postStep() {}

	@Override
	public void checkRequirements() throws WrongConfigurationException {}
	
	@Override
	public void handleMessages(Inbox inbox) {
		while (inbox.hasNext()){
			Message message = inbox.next();
			if (message instanceof WsnMsg) //Mensagem que vai do sink para os nós sensores 
			{
				Boolean forward = Boolean.TRUE;
				WsnMsg wsnMessage = (WsnMsg) message;
//				boolean typeIs2 = false;
				
//				Utils.printForDebug("* Entrou em if (message instanceof WsnMsg) * NoID = "+this.ID);
/*			
				if (wsnMessage.typeMsg == 2) {
					nextNodeToBaseStation = null;
					typeIs2 = true;
					wsnMessage.typeMsg = 0;
					TimerCollection tc = this.getTimers();
					tc.clear();
				}
*/
				if (wsnMessage.forwardingHop.equals(this)) // A mensagem voltou. O nó deve descarta-la
				{ 
					forward = Boolean.FALSE;
					
//					Utils.printForDebug("** Entrou em if (wsnMessage.forwardingHop.equals(this)) ** NoID = "+this.ID);
				}
				else if (wsnMessage.typeMsg == 0)// Mensagem que vai do sink para os nós sensores e é um flood. Devemos atualizar a rota
				{ 
					this.setColor(Color.BLUE);
					
					//canMakePredictions = Boolean.FALSE;
					//System.out.println("0 0 0  SensorID = "+this.ID+" Round = "+Global.currentTime+" canMakePredictions = "+canMakePredictions);
					//nextNodeToBaseStation = null;
					
					if (nextNodeToBaseStation == null)
					{
						nextNodeToBaseStation = inbox.getSender();
						sequenceNumber = wsnMessage.sequenceID;
					}
					else if (sequenceNumber < wsnMessage.sequenceID)
					{ 
					//Recurso simples para evitar loop.
					//Exemplo: Nó A transmite em brodcast. Nó B recebe a msg e retransmite em broadcast.
					//Consequentemente, nó A irá receber a msg. Sem esse condicional, nó A iria retransmitir novamente, gerando um loop.
						sequenceNumber = wsnMessage.sequenceID;
					}
					else
					{
						forward = Boolean.FALSE;
					}
				} //if (wsnMessage.tipoMsg == 0)
				// Mensagem que vai do sink para os nós sensores e é um pacote transmissor de dados (coeficientes). Devemos atualizar a rota
				else if (wsnMessage.typeMsg == 1)
				{ 
//					this.setColor(Color.YELLOW);
//					Integer nextNodeId = wsnMessage.popFromPath();
					//TODO: Descobrir quando deve ser executada a linha abaixo:
					canMakePredictions = Boolean.TRUE;
					forward = Boolean.FALSE;

//					System.out.println("NodeID = "+this.ID+" Round = "+Global.currentTime+" hopsToTarget = "+this.hopsToTarget);
					//Definir roteamento de mensagem
					if (wsnMessage.target != this)
					{
						sendToNextNodeInPath(wsnMessage);
					}
					else if (wsnMessage.target == this) //Se este for o nó de destino da mensagem...
					{ 
//						sequenceNumber = wsnMessage.sequenceID;
						//this.setColor(Color.RED);
												
						if (wsnMessage.hasCoefs()) // If this message contains / has coefficients (A and B), then 
						{
							//...então o nó deve receber os coeficientes enviados pelo sink e...
							receiveCoefficients(wsnMessage);
							//...não deve mais encaminhar esta mensagem
						}
						else // Else this message request (new) node sense (reading)
						{
// NÃO DEVERÁ MAIS EXISTIR !!!
/*
//		CASO O CLUSTER PRECISE SOFRER UM SPLIT, CADA UM DOS NÓS DO CLUSTER DEVE RECEBER UMA MENS. SOLICITANDO UM NOVO ENVIO DE DADOS PARA O SINK
							
							
							WsnMsgResponse wsnMsgResp = new WsnMsgResponse(1, this, null, this, 0, 1, "");
							
							if (wsnMessage != null)
							{
								wsnMsgResp = new WsnMsgResponse(1, this, null, this, 1, wsnMessage.sizeTimeSlot, wsnMessage.dataSensedTypes); // wsnMsgResp = new WsnMsgResponse(1, this, null, this, 0, wsnMessage.sizeTimeSlot, wsnMessage.dataSensedTypes); 
								prepararMensagem(wsnMsgResp, wsnMessage.sizeTimeSlot, wsnMessage.dataSensedTypes);
							}
							addThisNodeToPath(wsnMsgResp);
							
							WsnMessageResponseTimer timer = new WsnMessageResponseTimer(wsnMsgResp, nextNodeToBaseStation);
							
							timer.startRelative(wsnMessage.sizeTimeSlot, this); // Espera por "wsnMessage.sizeTimeSlot" rounds e envia a mensagem para o nó sink (próximo nó no caminho do sink)
*/
						}
					}
				} //if (wsnMessage.tipoMsg == 0)
				
				if (forward && wsnMessage.typeMsg == 1)
				{
					wsnMessage.forwardingHop = this; 
					broadcast(wsnMessage);
				}
				else if (forward) //Nó sensor recebe uma mensagem de flooding (com wsnMessage) e deve responder ao sink com uma WsnMsgResponse... (continua em "...além de") 
				{
					
					
					WsnMsgResponse wsnMsgResp = new WsnMsgResponse(1, this, null, this, 0, 1, null);
					
					if (wsnMessage != null)
					{
//	CASO O CLUSTER PRECISE SOFRER UM SPLIT, CADA UM DOS NÓS DO CLUSTER DEVE RECEBER UMA MENS. SOLICITANDO UM NOVO ENVIO DE DADOS PARA O SINK
						
						wsnMsgResp = new WsnMsgResponse(1, this, null, this, 0, wsnMessage.sizeTimeSlot, wsnMessage.dataSensedTypes); 

						windowSize = wsnMessage.sizeTimeSlot;

						prepararMensagem(wsnMsgResp, wsnMessage.sizeTimeSlot, wsnMessage.dataSensedTypes);
					
					}
					addThisNodeToPath(wsnMsgResp);
					
					if (nextNodeToBaseStation != null) {
						WsnMessageResponseTimer timer = new WsnMessageResponseTimer(wsnMsgResp, nextNodeToBaseStation);
						timer.startRelative(wsnMessage.sizeTimeSlot, this); // Espera por "wsnMessage.sizeTimeSlot" rounds e envia a mensagem para o nó sink (próximo nó no caminho do sink)
					}
					
					//Devemos alterar o campo forwardingHop(da mensagem) para armazenar o noh que vai encaminhar a mensagem.
					wsnMessage.forwardingHop = this; 
/*
					if (typeIs2) {
						wsnMessage.typeMsg = 2;
					}
*/
					//...além de repassar a wsnMessage para os próximos nós
					broadcast(wsnMessage);
					
				} //if (encaminhar)
			} //if (message instanceof WsnMsg)

			
			else if (message instanceof WsnMsgResponse) // Mensagem de resposta dos nós sensores, ou para o sink, que deve ser repassada para o "proximoNoAteEstacaoBase", ou para o cluster head, que deve ser recebida/retida pelo mesmo
			{
				
				WsnMsgResponse wsnMsgResp = (WsnMsgResponse) message;
				
				// TRATAR AQUI DO CASO EM QUE OS CLUSTER HEADS DEVEM ASSUMIR O CONTROLE DA SITUAÇÃO!!!
/*
				if (wsnMsgResp.source.ID == 39 || this.ID == 39 || wsnMsgResp.source.ID == 15 || this.ID == 15)
				{
					System.out.println("* * * ID = 39 OR ID = 15 ! ! !");
				}
*/				
				//
				if (wsnMsgResp.typeMsg == 5) {
					//System.out.println("DirectMessageTimer(); received by the node "+this.ID+" from node "+wsnMsgResp.source.ID+" in Round = "+Global.currentTime);
					// validPredictions = false; // Desabilitar "validPredictions" para que sensores continuem sensoriando após o CH deste cluster haver detectado extrapolação do número de erros (errorsInThisRound > clusterDelay)
					//this.minusOne = false;
				}
				else if (wsnMsgResp.typeMsg == 6) {
					this.minusOne = true;
				}
				else if (wsnMsgResp.target != null && wsnMsgResp.target.ID == this.ID) // ou (wsnMsgResp.target == this) ou (this.clusterHead == this) // This is the cluster head sensor which is receiving a message from another sensor of this same clsuter
				{ 

/*
 * Neste caso, algum nó sensor pertencente ao mesmo cluster em que este nó (this) é o Cluster Head, está enviando uma mensagem para ele (CH)
 * informando que houve erro de predição.
 * O CH irá verificar, a cada 2 ou mais mensagens de erro de predição e verificará se os sensores que enviaram tais mensagens estão dentro dos limiares de similaridade.
 */					
					countErrorMessages(wsnMsgResp);
										
					
				} // end if (wsnMsgResp.target != null && wsnMsgResp.target.ID == this.ID)
				
				else
				{
//					this.setColor(Color.YELLOW);
					
					addThisNodeToPath(wsnMsgResp);

					if (nextNodeToBaseStation != null) {
						WsnMessageResponseTimer timer = new WsnMessageResponseTimer(wsnMsgResp, nextNodeToBaseStation);
						timer.startRelative(1, this); // Envia a mensagem para o próximo nó no caminho do sink no próximo round (0) : Antes -> timer.startRelative(1, this);
					}
				} // end else if (wsnMsgResp.target != null && wsnMsgResp.target.ID == this.ID)
				
			} // end else if (message instanceof WsnMsgResponse)
			
		} // end while (inbox.hasNext())
		
	} // end handleMessages(Inbox inbox)
	
	/**
	 * Contabiliza o número de mensagens de erro recebidas por cada Cluster Head (sensor representativo de um cluster / agrupamento) de acordo com o tipo de erro<p>[Eng] Counts the number of error messages received by each Cluster Head(representative sensor of a cluster / grouping) according to the type of error
	 * @param wsnMsgResp Mensagem contendo o código de tipo do erro detectado (pode ser erro de predição, número limite de predições ultrapassado ou baixo nível de energia no CH).<p>[Eng] Message containing the code of type error detected( could be prediction error, number limit exceeded predictions or lowest level of energy in the CH).
	 */
	private void countErrorMessages(WsnMsgResponse wsnMsgResp)
	{
		Integer type = wsnMsgResp.typeMsg;
		
		if (errorsInThisCluster == 0) { //Se é o primeiro erro (notificação de novidade) deste cluster
			initialErrorRound = Global.currentTime; // Salva o número do round (ticket/ciclo) atual
		}
		
		switch (type){
			case 0:
				break;
			case 1:
				break;
			case 2: errorsInThisCluster++;
				break;
			case 3:
				break;
			case 4: // Nível mínimo de bateria atingido (pelo ClusterHead) - Minimum battery level reached (by ClusterHead)
				break;
			case 5: // Aviso de parada de sensoriamento
				break;				
		}
		
		if (errorsInThisCluster > 0) {
			System.out.println("MessageErrorReceivedByCH: SensorIDSource = "+wsnMsgResp.source.ID+" ChID = "+this.ID+" NumErrors = "+errorsInThisCluster);
		}
		
		if (messageItensPackage == null) {
			messageItensPackage = new Vector<MessageItens>();
		}
		
		
		
		
		
		//TODO: PAREI AQUI! Precisa alterar as inner classes para que se tornem classe "reais" (externas).
//		messageItensPackage.add((this.messageItensPackage)wsnMsgResp.messageItens);
		
	
		
		
		
/*		
		if (Global.currentTime > (initialErrorRound + maxErrorsPerCluster)) { // Se o tempo (round/ciclo/ticket) atual for maior do que o inicial mais o número máximo de erros por cluster
			errorsInThisCluster = 0; // Então zera a quantidade de erros deste cluster
		}
*/		
		if (errorsInThisCluster == clusterDelay) // Send "toPorUmaMessage" (minusOne) to all sensors in this cluster
		{
			this.minusOne = true; // Set the flag "minusOne" from this sensor (ClusterHead) to true, 
			// indicates that the next prediction error should not consider the RMSE.
			// Envia uma mensagem para cada um dos nós do cluster atual para que os mesmos saibam que falta um erro (miss) 
			// para atingir o limite de erros deste cluster (sensores devem descartar acúmulo de RMSE)
			for (int i = 0; i < this.myCluster.members.size(); i++) {
				
				SimpleNode targetNode = this.myCluster.members.get(i);
				
				WsnMsgResponse wsnMsgRespStopAddRMSE = new WsnMsgResponse(1, this, targetNode, this, 6);
												
				DirectMessageTimer timer = new DirectMessageTimer(wsnMsgRespStopAddRMSE, targetNode); // Envia uma mensagem diretamente para o 
																										  // nó "targetNode" deste cluster
				timer.startRelative(1, this);
			}			
		
		}
		
		if (errorsInThisCluster > clusterDelay)
		{
			// Deve informar ao Sink tal problema, para que o mesmo providencie o tratamento correto (Qual seja!???)
			
			addThisNodeToPath(wsnMsgResp);

			Utils.printForDebug("@ @ The number of prediction errors in this Cluster ("+errorsInThisCluster+") EXCEEDED the maximum limit of the prediction errors per Cluster ("+clusterDelay+")! NoID = "+this.ID+"\n");

			wsnMsgResp.target = null; // wsnMsgResp.target = SinkNode; || wsnMsgResp.target = null; || wsnMsgResp.target = Tools.getNodeByID(55);
			
			// Disparar uma mensagem para todos os nós do mesmo cluster para que o atributo "validPredictions" torne-se falso até que o sink envie nova
			// mensagem com novos coeficientes e torne o atr. "validPredictions" igual a true.
			// validPredictions = false; // Desabilitar "validPredictions" para que sensores continuem sensoriando após o CH deste cluster haver detectado extrapolação do número de erros (errorsInThisRound > clusterDelay)
			
			//Node.timers.clear(); // Limpar todos os timers deste nó (ClusterHead) e dos outros nós do mesmo cluster
			
			// Envia uma mensagem para cada um dos nós do cluster atual para que os mesmos entrem em modo de "Pre-WaitingMode"
			for (int i = 0; i < this.myCluster.members.size(); i++) {
				
				SimpleNode targetNode = this.myCluster.members.get(i);
				
				WsnMsgResponse wsnMsgRespStopPredictions = new WsnMsgResponse(1, this, targetNode, this, 5);
												
				DirectMessageTimer timer = new DirectMessageTimer(wsnMsgRespStopPredictions, targetNode); // Envia uma mensagem diretamente para o 
																										  // nó "targetNode" deste cluster
				timer.startRelative(1, this);
			}			
			
			if (nextNodeToBaseStation != null) {
				WsnMessageResponseTimer timer = new WsnMessageResponseTimer(wsnMsgResp, nextNodeToBaseStation);
				timer.startRelative(1, this); // Envia a mensagem para o próximo nó no caminho do sink no próximo round (1)
			}
			
			errorsInThisCluster = 0; // Depois de enviar a mensagem para o Sink, reseta (reinicia) o contador de erros deste cluster
			
		} // end if (errorsInThisCluster > maxErrorsPerCluster)
		//errorsInThisCluster = 0;
	} // end private void countErrorMessages(WsnMsgResponse wsnMsgResp)
	
	/**
	 * Adiciona o nó atual no caminho do sink até o nó de origem (source) da mensagem / Adiciona o nó atual para o caminho de retorno da mensagem de volta do sink para este nó<p>
	 * [Eng] Adds the current node to the return path of the message back from the sink node to this node
	 * @param wsnMsgResp Mensagem de resposta a ter o nó atual adicionado (empilhado) em seu caminho do sink para o nó de origem<p>[Eng] Message Response which current node to be add (pushed) in the path from the sink to the source node 
	 */
	private void addThisNodeToPath(WsnMsgResponse wsnMsgResp)
	{
		wsnMsgResp.pushToPath(this.ID);
		//hopsToTarget++;
//		wsnMsgResp.saltosAteDestino++; // Transferido para o método pushToPath() da classe WsnMsgResponse
	} // end addThisNodeToPath(WsnMsgResponse wsnMsgResp)
	
	/**
	 * Inicia o processo de merge no SimpleNode <p>
	 * [Eng] Starts the merge process in SimpleNode
	 */
	public void startMerge()
	{
		TimerCollection tc = this.getTimers();
		tc.clear();
		canMakePredictions = false;
	} // end startMerge()
	
	/**
	 * 
	 */
	public void freeNextNode()
	{
		nextNodeToBaseStation = null;
	} // end freeNextNode()
	
	/**
	 * Prepara a mensagem "wsnMsgResp" para ser enviada para o sink acrescentando os dados lidos pelo nó atual<p>[Eng] Prepare the message "wsnMsgResp" to be sended for the sink increasing the data read by the actual node.
	 * @param wsnMsgResp Mensagem a ser preparada para envio<p>[Eng] Message to be prepared for sending.
	 * @param sizeTimeSlot Tamanho do slot de tempo (intervalo) a ser lido pelo nó sensor, ou tamanho da quantidade de dados a ser enviado para o sink<p>[Eng] Size of time slot(interval) to be read by sensor node, or size of data quantity to be sended to the sink.
	 * @param dataSensedTypes Tipos de dados (temperatura, umidade, luminosidade, etc) a serem sensoreados (lidos) pelo nó sensor.<p>[Eng] Types of data( temperature, humidity, luminosity, etc) to ber sensored(read) by the sensor node.
	 */
	private void prepararMensagem(WsnMsgResponse wsnMsgResp, Integer sizeTimeSlot, int[] dataSensedTypes)
	{
		int numSequenceVoltageData = 7; //Position of voltage data according the data structure in "data*.txt" file
		int numSequenceRound = 2;
/*
 * Exemple:
 * 				2004-03-21 19:02:26.792489 65528 4 87.383 45.4402 5.52 2.31097
 * Position         [0]         [1]         [2] [3]  [4]    [5]    [6]   [7]
 * Data type       Data         Hora       Round ID  Temp   Hum    Lum   Volt
 */

		String dataLine = performSensorReading();
		int i=0; //cont = 0 
		
		while (i<sizeTimeSlot && dataLine != null)
		{
			i++;
			if (dataLine != null && dataSensedTypes != null && dataSensedTypes.length > 0)
			{
				String lines[] = dataLine.split(" ");
				
				double[] values = new double[dataSensedTypes.length];
				double quantTime;
				double batLevel;
				int round;
//				Utils.printForDebug("(ultimoRoundLido + sizeTimeSlot) = "+(ultimoRoundLido + sizeTimeSlot));
//				Utils.printForDebug("cont = "+cont);
				if (lines.length > 4)
				{
//					cont++;
					
					round = Integer.parseInt(lines[numSequenceRound]); //Número do round
					
					for (int nTypes = 0; nTypes < dataSensedTypes.length; nTypes++) {
						if (lines[dataSensedTypes[nTypes]] == null || lines[dataSensedTypes[nTypes]].equals("")) {
							values[nTypes] = 0.0;
						}  // end if (lines[dataSensedTypes[nTypes]] == null || lines[dataSensedTypes[nTypes]].equals(""))
						else {
							try {
								values[nTypes] = Double.parseDouble(lines[dataSensedTypes[nTypes]]);
							}//try
							catch (NumberFormatException e) {
								values[nTypes] = 0.0;
							}//catch
						} // end else if (lines[dataSensedTypes[nTypes]] == null || lines[dataSensedTypes[nTypes]].equals(""))
					} // end for (int nTypes = 0; nTypes < dataSensedTypes.length; nTypes++)
					
					
					if (lines[numSequenceVoltageData] == null || lines[numSequenceVoltageData].equals("")) {
						batLevel = 0.0;
					}
					else {
						try {
							batLevel = Double.parseDouble(lines[numSequenceVoltageData]);
						}//try
						catch (NumberFormatException e) {
							batLevel = 0.0;
						}//catch
					}//else
					
					quantTime = parseCalendarHours(lines[0], lines[1]);
					
					lastValuesRead = values;
					lastTimeRead = quantTime;
					lastBatLevel = batLevel;
					lastRoundRead = round;

					addDataRecordItens(dataSensedTypes, values, quantTime, batLevel, round);

					wsnMsgResp.addDataRecordItens(dataSensedTypes, values, quantTime, batLevel, round);
					
				}//if (linhas.length > 4)
			}//if (dataLine != null && dataSensedType != null && medida != 0)
			if (i<sizeTimeSlot) //Impede que seja perdida uma leitura do sensor
			{
				dataLine = performSensorReading();
			}//if (i<sizeTimeSlot)
		}//while (i<sizeTimeSlot && dataLine != null)
		wsnMsgResp.batLevel = lastBatLevel; // Level of battery from last reading of sensor node
		wsnMsgResp.spatialPos = wsnMsgResp.source.getPosition(); // Spacial position from the source node from message response
	} // end prepararMensagem(WsnMsgResponse wsnMsgResp, Integer sizeTimeSlot, String dataSensedType)
	
	/** 
	 * Verifica se o ID do sensor passado como parâmetro é igual ao ID deste nó.<p>[Eng] Verifies whether the sensor ID passed as parameter is equal to the ID of this node.
	 * @param sensorID ID do sensor para ser comparado ao ID deste nó.<p>[Eng] Sensor ID to be compared to this node's ID
	 * @return Retorna <code>true</code> se os ID's são os mesmos. Retorna <code>false</code> caso contrário.<p>[Eng] Returns <code>true</code> if the IDs are the same. Returns <code>false</code> otherwise.
	 */
	private boolean isMyID(String sensorID) {
		if(!sensorID.equals("")){
			int intSensorID = Integer.parseInt(sensorID);
			if (this.ID == intSensorID && intSensorID <= FileHandler.NUMBER_OF_SENSOR_NODES){
				return true;
			}
		}
		return false;
	} // end isMyID(String sensorID)
	
	/** 
	 * <p>Simula a leitura dos dados do sensor físico realizado para todos os dispositivos de detecção disponíveis neste nó.</p>
	 * <p>De fato, a real leitura dos dados do sensor não foram feitos por esse nó. Em vez disso, as leituras do sensor são
	 * coletados do seu <code>sensorReadingsQueue</code> atributo</p>
	 * <p>Nesse caso que a lista está vazia, ele é preenchido com as leituras do sensor carregadas do 
	 * arquivo de leituras do sensor.</p>
	 * <p>[Eng] Simulates a physical sensor data reading performed for all the sensing devices available in this
	 * node (e.g. temperature, pressure, humidity).</p>
	 * <p>In fact, a real sensor data reading is not done by this node. Instead, a sensor reading is
	 * collected from its <code>sensorReadingsQueue</code> attribute. </p>
	 * <p>In the case that the list is empty, it is filled with sensor readings loaded from the 
	 * sensor readings file.</p>
	 */
	public String performSensorReading()
	{
		Global.sensorReadingsCount++; // Increments the global count of sensor readings
		numTotalSensorReadings++; // Increments the local count of sensor readings
/*
		if (Global.currentTime > 100 && numTotalSensorReadings > Global.currentTime + 1) {
			System.out.println(" * * * Sensor ID = "+this.ID+": numTotalSensorReadings (performSensorReading) = "+numTotalSensorReadings+" and Global.currentTime = "+Global.currentTime);
		}
*/
//		System.out.println("NoID: "+this.ID+" making reading in Round = "+Global.currentTime);
		if (sensorReadingsQueue != null && sensorReadingsQueue.isEmpty())
		{
			loadSensorReadings();
		}
		if (sensorReadingsQueue != null && !sensorReadingsQueue.isEmpty())
		{
			String data = sensorReadingsQueue.remove();
			return data;
		}
		return null;
	} // end performSensorReading()

	/**
	 * Carrega as leituras do sensor para o <code>sensorReadingsQueue</code>
	 * Se o atributo<code>loadSensorReadingsFromFile</code> é verdadeiro, o nó deve carregar as leituras do sensor diretamente para o arquivo de leituras do sensor.
	 * Caso contrário, isso deve carregar as leituras do sensor da memória, que é, da lista de leituras do sensor de todos
	 * os nós que são carregados na memória antecipadamente pela classe {@link FileHandler}.
	 * Esse procedimento é necessário por que o arquivo de leituras do sensor é muito grande e deve tomar
	 * muito tempo para serem carregados dependendo da configuração do computador. Para os casos em que o carregamento
	 * de todas as leituras do sensor para a memória( no <code>FileHandler</code>) não é possível,
	 * as leituras do sensor estão carregados no arquivo na demanda de cada nó.<p>
	 * [Eng] Loads the sensor readings to the <code>sensorReadingsQueue</code>
	 * If the attribute <code>loadSensorReadingsFromFile</code> is true, the node must load the sensor readings direct from the sensor readings file.
	 * Otherwise, it must load the sensor readings from the memory, that is, from the list of the sensor readings from
	 * all nodes that is loaded in memory beforehand by the {@link FileHandler} class.
	 * This procedure is necessary because the sensor readings file is very large and may take
	 * too long to be loaded depending on the computer configuration. For the cases that loading
	 * all the sensor readings to the memory (on the <code>FileHandler</code>) is not possible,
	 * the sensor readings are loaded from the file on demand by each node.
	 */
	private void loadSensorReadings() {
		if (loadSensorReadingsFromFile) {
			loadSensorReadingsFromFile();
		} else {
			loadSensorReadingsFromMemory();
		}
	} // end loadSensorReadings()
	
	/**
	 * Preenche o <code>sensorReadingsQueue</code> com as leituras do sensor do arquivo.
	 * A quantidade de leituras(linhas do arquivo) para serem carregadas são informadas dentro do <code>Config.xml</code> arquivo(<code>SensorReadingsLoadBlockSize</code>)  marcador.<p>
	 * [Eng]Fills the <code>sensorReadingsQueue</code> with sensor readings from the file.
	 * The amount of readings (file lines) to be loaded is informed in the <code>Config.xml</code> file (<code>SensorReadingsLoadBlockSize</code>) tag.
	 */
	private void loadSensorReadingsFromFile(){
		try {
			long initTime = System.currentTimeMillis();
			int sensorReadingsLoadBlockSize = Configuration.getIntegerParameter("SensorReadingsLoadBlockSize"); //amout of readings to be loaded
			String sensorReadingsFilePath = Configuration.getStringParameter("ExternalFilesPath/SensorReadingsFilePath");
			BufferedReader bufferedReader = FileHandler.getBufferedReader(sensorReadingsFilePath);
			int lineCounter = 0;
			
			bufferedReader = putOnLinePosition(bufferedReader, lastLineLoadedFromSensorReadingsFile); //puts the bufferedReader in the last line read
			
			String line = bufferedReader.readLine();
			while(line != null) {
				String lineValues[] = line.split(" ");
				if (lineValues.length > 4 && this.isMyID(lineValues[3])) { //if the line corresponds to data from this node it enters, otherwise it passes to the next line
					for (int loadedLinesCount = 0; ((line != null) && (loadedLinesCount < sensorReadingsLoadBlockSize)); loadedLinesCount++) { //if the specified number of lines to be loaded was not yet satisfied or if there are no more lines, it enters
						lineValues = line.split(" ");
						if (lineValues.length > 4 && this.isMyID(lineValues[3])) {
							sensorReadingsQueue.add(line); //loads the line to the memory
							line = bufferedReader.readLine();
							lineCounter++;
						} else break;
					}
					break;
				}
				lineCounter++;
				line = bufferedReader.readLine();
			}			
			bufferedReader.close();
		if (sensorReadingsQueue.size() < sensorReadingsLoadBlockSize && !(loadAllSensorReadingsFromFile)) {
			loadAllSensorReadingsFromFile = true;
//			System.err.println("NodeID: " + this.ID + " has already read all the sensor readings of the file. " +
//					"\n It has only " + sensorReadingsQueue.size() + " readings in its memory (sensorReadingsLoadedFromFile list)");
		}

		lastLineLoadedFromSensorReadingsFile = lastLineLoadedFromSensorReadingsFile + lineCounter; //updates the last line read from the file
		long finishTime = System.currentTimeMillis();
		Utils.printForDebug("Node ID " + this.ID + " successfully loaded " + sensorReadingsQueue.size() + " sensor readings from the file in " + Utils.getTimeIntervalMessage(initTime, finishTime));
		
		} catch (CorruptConfigurationEntryException e) {
			System.out.println("Problems while loading variable sensorReadingsFilePath at simpleNode.loadSensorReadingFromFile()");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("Problems while reading lines (fileReader.readLine()) from  sensorReadingsFilePath at simpleNode.loadSensorReadingFromFile()");
			e.printStackTrace();
		}
	} // end loadSensorReadingsFromFile()
	
	/**
	 * Preencher a <code>sensorReadingsQueue</code> lista com as leituras do sensor do {@link FileHandler}.<p> 
	 * [Eng]Fills the <code>sensorReadingsQueue</code> list with sensor readings from the {@link FileHandler}.
	 */
	private void loadSensorReadingsFromMemory() {
		long initTime = System.currentTimeMillis();
		List<String> nodesSensorReadingsQueue = FileHandler.getNodesSensorReadingsQueue();
		for (String sensorReading : nodesSensorReadingsQueue) {
			String sensorReadingsValues[] = sensorReading.split(" ");
			if (sensorReadingsValues.length > 4 && this.isMyID(sensorReadingsValues[3])) {
//				nodesSensorReadingsQueue.remove(sensorReading);
				sensorReadingsQueue.add(sensorReading);
			}
		}
		long finishTime = System.currentTimeMillis();
		Utils.printForDebug("Node ID " + this.ID + " successfully loaded " + sensorReadingsQueue.size() + " sensor readings from the memory in " + Utils.getTimeIntervalMessage(initTime, finishTime));
		Utils.printForDebug("");
	} // end loadSensorReadingsFromMemory()
	
	/** 
	 * Coloca o bufferedReader na posição da linha especificada.<p>[Eng] Places the bufferedReader on the line position specified.
	 * @param bufferedReader Buffered Reader sendo usado.<p>[Eng] Buffered Reader to be used
	 * @param linePosition posição no arquivo que o Buffered reader deve estar<p>[Eng] position in the file that the buffered reader must be
	 * @return o buffered reader na linha correspondente para a posição especificada. Que é,
	 * quando bufferedReader.readline() é chamado, a linha retornada corresponderá a posição especificada.
	 * Por exemplo, se LinePosition = 3 então bufferedReader.readline() irá pegar a linha adiante.<p>
	 * [Eng] the buffered reader in the line corresponding to the position specified. That is,
	 * when bufferedReader.readline() is called, the line returned will correspond position specified.
	 * For example, if linePosition = 3 then bufferedReader.readline() will get the forth line. 
	 */
	private BufferedReader putOnLinePosition(BufferedReader bufferedReader, Integer linePosition) {
		try {
			for (int lineCount = 0; (lineCount < linePosition); lineCount++) {
				String line = bufferedReader.readLine();
				if (line == null) {
					return bufferedReader;
				}
			}
		} catch (IOException e) {
			System.out.println("Problems while reading lines (fileReader.readLine()) from  sensorReadingsFilePath at simpleNode.putOnLastLineRead()");
			e.printStackTrace();
		}
			
		return bufferedReader;
	} // end putOnLinePosition(BufferedReader bufferedReader, Integer linePosition)

	/**
	 * Identifica o tipo de dados a ser lido (posição na linha) de acordo com a string passada, conforme o exemplo abaixo: <br>
	 *  			2004-03-21 19:02:26.792489 65528 4 87.383 45.4402 5.52 2.31097 <br>
	 * Posição          [0]         [1]         [2] [3]  [4]    [5]    [6]   [7] <br>
	 * Tipo de dado    Data         Hora       Round ID  Temp   Hum    Lum   Volt <p>
	 * 
	 * Onde: Data e Hora são a data e o horário em que ocorreu a leitura dos valores sensoreados <br>
	 *       Round é o número da rodada (execução) da leitura, que ocorre a cada 31 segundos <br>
	 *       ID é o número identificador do sensor <br>
	 *       Temp é a medida (grandeza) da temperatura aferida <br>
	 *       Hum é a medida (grandeza) da umidade aferida <br>
	 *       Lum é a medida (grandeza) da luminosidade aferida <br>
	 *       Volt é a medida (grandeza) do nível de bateria do sensor (voltagem aferida)
	 * 
	 * 
	 * 
	 * [Eng] Identifies the type of data to be read(position line) according with the string passed, as the example below: <br>
	 * 	             2004-03-21 19:02:26.792489 65528 4 87.383 45.4402 5.52 2.31097 <br>
	 * Position          [0]         [1]         [2] [3]  [4]    [5]    [6]   [7] <br>
	 * Type of data      Date        Hour       Round ID  Temp   Hum    Lum   Volt <p>
	 * 
	 * Where:  Date and Hour are the date and the hour when occurred the reading of sensored values.<br>
	 *         Round is the number of the cycle(execution) of reading, that occurs each 31 seconds <br>
	 *         ID is the identify number of the sensor <br>
	 *         Temp is the measuring(grandeur) of temperature given <br>
	 *         Hum is the measuring (grandeur) of humidity given <br>
	 *         Lum is the measuring (grandeur) of luminity given <br>
	 *         Volt is the measuring (grandeur) of level by sensor battery (voltage given)
	 *         
	 *     
	 * 
	 * @param type Pode ser t: temperatura, h: umidade, l: luminosidade ou v: voltagem<p>[Eng] can be t: temperature, h: humidity, l: luminosity or v: voltage
	 * @return Posição correspondente do tipo de dado a ser aferido na string lida do arquivo de dados (data.txt)<p>        
	 * Position corresponding from data type to be measured in the string read by data file (data.txt)
	 */

	
/*	THIS IS NO LONGER NECESSARY!!!

	private int identifyNumberSequenceByType(String type) 
	{
		if (type.equals("t"))
			return 4;
		else if (type.equals("h"))
			return 5;
		else if (type.equals("l"))
			return 6;
		else if (type.equals("v"))
			return 7;	
		return 0;
	} // end identificarTipo(String tipo)
*/
	
	/**
	 * Transforma os valores de data (AnoMesDia) e hora (hora) passados em uma grandeza inteira com a quantidade de milisegundos total<p>[Eng] Turns the values of the date(YearMonthDay) and hour(hour) passed in a entire grandeur with a total milliseconds quantity
	 * @param yearMonthDay String no formato AAAA-MM-DD representando a data da leitura do valor pelo sensor (A-Ano, M-Mes, D-Dia)<p>[Eng] String in YYYY-MM-DD format representing the date of reading the value from the sensor (Y-Year, M-month, D-Day)
	 * @param hour String no formato HH:MM:SS.LLLLL representando a hora da leitura do valor pelo sensor (H-Hora, M-Minuto, S-Segundo, L-Milisegundo)<p>[Eng] String to format HH:MM:SS.LLLLL representing a hour by reading of the value from sensor(H-Hour, M-Minutes, S-Seconds, L-Milliseconds)
	 * @return Quantidade de milisegundos total representando aquele instante de tempo (Data + Hora) segundo o padrão do Java<p>[Eng] Total quantity of milliseconds representing that instant of time( date + Hour) according to the Java. 
	 */
	private long parseCalendarHours(String yearMonthDay, String hour)
	{
		String[] dates = yearMonthDay.split("-");
		String[] hours = hour.split(":");
		String certain = "";
		String millesegundos = "";
		for (String mille : hours){
			if(mille.contains(".")){
				String correct = mille.substring(0,mille.indexOf("."));
				millesegundos = mille.substring(mille.indexOf(".")+1, mille.length());
				certain = correct;
			}
		}
		hours[2] = certain;
		long quantTime = 0;

		try {
			GregorianCalendar gc = new GregorianCalendar(Integer.parseInt(dates[0]), Integer.parseInt(dates[1]) -1, Integer.parseInt(dates[2]),Integer.parseInt(hours[0]),Integer.parseInt(hours[1]), Integer.parseInt(hours[2]));
			quantTime = (gc.getTimeInMillis() + Long.parseLong(millesegundos)/1000);
		}
		catch (NumberFormatException ex) {
			System.out.println("Erro! SensorID = "+this.ID+" Mensagem: "+ex.getMessage()+" StackTrace: "+ex.getStackTrace());
		}
		
		return quantTime;
	} // end parseCalendarHoras(String AnoMesDia, String hora)
	
	/**
	 * Faz o calculo das predições dos valores sensoreados de acordo com os coeficientes (A e B) informados e o parâmetro de tempo; incrementa o contador de predições (numTotalPredictions) <p>
	 * [Eng]It calculates the prediction sensed value according to coefficients (A and B) informed and time parameter; it increments the prediction count (numTotalPredictions)
	 * @param A Coeficientes A (interceptor) da equação de regressão, dada por S(t) = A + B.t <p>[Eng] Coefficient A(interceptor) of regression equation, given by S(t) = A + B.t
	 * @param B Coeficientes B (slope, inclinação) da equação de regressão, dada por S(t) = A + B.t<p>[Eng] Coefficient B (slope, inclination) of regression equation, given by S(t) = A + B.t
	 * @param time Parâmetro de tempo a ter o valor da grandeza predito<p>[Eng]Parameter of time to has the value by predicted grandeur.
	 * @return Valor predito para o parâmetro sensoreado no tempo dado<p>[Eng]Predicted value to sensored parameter in a determined time.
	 */
	private double[] makePredictions(double[] A, double[] B, double time) // Cálculo da predição / predições dos valores
	{
		double[] predictions = new double[A.length];
		for (int numTypes = 0; numTypes < A.length; numTypes++) {
			predictions[numTypes] = A[numTypes] + B[numTypes]*time;
			this.numTotalPredictions++;
		}
		return predictions;
	} // end makePrediction(double[] A, double[] B, double time)
	
	/**
	 * Inicia um temporizador para enviar a mensagem passada para o próximo nó no caminho até o nó destino.<p>
	 * [Eng] It starts a timer to send the message passed to the next node in path to destination node. 
	 * @param wsnMessage Mensagem a ser enviado para o nó destino. <p>[Eng] Message to be sended to destination node
	 */
	protected void sendToNextNodeInPath(WsnMsg wsnMessage)
	{
		Integer nextNodeId = wsnMessage.popFromPath();
		WsnMessageTimer timer;
		Node nextNode;
		if (nextNodeId != null)
		{
			nextNode = Tools.getNodeByID(nextNodeId);
			timer = new WsnMessageTimer(wsnMessage, nextNode);
			timer.startRelative(1, this); // timer.startRelative(1, this);
		}
		else {
			Utils.printForDebug("@ @ The nextNodeId == null in sendToNextNodeInPath(WsnMsg wsnMessage) method for this.ID = ("+this.ID+")!"+"\n");
		}
	} // end sendToNextNodeInPath(WsnMsg wsnMessage)
		
	/**
	 * Pegar os coeficientes da equação de regressão e o erro threshold
	 * da mensagem passada e acionar as predições para este nó.<p>
	 * [Eng] Get the coefficients from the Regression Equation and the threshold error
	 * from the message passed by and trigger the predictions for this node
	 * @param wsnMessage Mensagem que possui os coeficientes lidos.<p> [Eng] Message which have the coefficients read
	 */
	protected void receiveCoefficients(WsnMsg wsnMessage)
	{
		this.clusterHead = wsnMessage.getClusterHead();
		if (wsnMessage.hasCoefs())
		{
			double[] coefsA = wsnMessage.getCoefsA();
			double[] coefsB = wsnMessage.getCoefsB();
			double[] maxErrors = wsnMessage.getThresholdErrors();
	
			this.numTotalPredictions = 0;
			this.numPredictionErrors = 0;
			this.minusOne = false;
			this.ownTimeSlot = wsnMessage.sizeTimeSlot;
			
			windowSize = slidingWindowSize;
			// validPredictions = true;
			//System.out.println("nodeID = "+this.ID+": validPredictions TRUE in Round = "+Global.currentTime);
			triggerPredictions(wsnMessage.dataSensedTypes, coefsA, coefsB, maxErrors);
		}
	} // end receiveCoefficients(WsnMsg wsnMessage)
	
	/**
	 * Adiciona os últimos valores lidos anteriormente a mensagem que vai para o sink.<p>[Eng] Adds all itens in dataRecordItens vector for the (WsnMsgResponse) wsnMsgResp / Adds the last values previously read to the message that goes to the sink
	 * @param wsnMsgResp Mensagem resposta que recebe os itens dataRecordItens <p>[Eng] Message Response that receives the dataRecordItens itens
	 */
	protected void addDataRecordItensInWsnMsgResponse(WsnMsgResponse wsnMsgResp)
	{
		if (dataRecordItens != null)
		{
			for (int cont=0; cont < dataRecordItens.size(); cont++) //for(int cont=0; cont<slidingWindowSize; cont++)
			{
				wsnMsgResp.addDataRecordItens(dataRecordItens.get(cont).typs, dataRecordItens.get(cont).values, dataRecordItens.get(cont).time, dataRecordItens.get(cont).batLevel, dataRecordItens.get(cont).round); 
			}
			wsnMsgResp.messageItens.sourceNode = this;
		}
	} // end addDataRecordItensInWsnMsgResponse(WsnMsgResponse wsnMsgResp)
	
	/**
	 * Retorna os dados(no DataRecord) do sensor(currentNode) de acordo com o tipo de dados(dataType) indicado<p>
	 * [Eng]Returns the sensor (currentNode) data (in DataRecord) according to the data type (dataType) indicated
	 * @param currentNode Nó sensor que deve ler dados<p>
	 * [Eng] currentNode sensor node that must read data.
	 * @param dataSensedTypes Tipos de dados a serem lidos<p>
	 * [Eng] dataTypes Types of data to be read.
	 * @return Dado do sensor (currentNode)<p>
	 * [Eng] Data from sensor (currentNode)
	 */
	private DataRecord getData(SimpleNode currentNode, int[] dataSensedTypes) {
		DataRecord temp = new DataRecord();
		int numSequenceVoltageData = 7; //According the data structure in "data*.txt" file
		int numSequenceRound = 2; //According the data structure in "data*.txt" file
/*
 * Example:
 * 				2004-03-21 19:02:26.792489 65528 4 87.383 45.4402 5.52 2.31097
 * Position         [0]         [1]         [2] [3]  [4]    [5]    [6]   [7]
 * Data type       Data         Hora       Round ID  Temp   Hum    Lum   Volt
 */
		
		String sensorReading = currentNode.performSensorReading(); 
		
		if (sensorReading != null && dataSensedTypes != null && dataSensedTypes.length > 0)
		{
			double[] values = new double[dataSensedTypes.length];
			double quantTime;
			double batLevel;

			String lines[] = sensorReading.split(" ");

			if (lines.length > 4)
			{
				for (int nTypes = 0; nTypes < dataSensedTypes.length; nTypes++) {
					if (lines[dataSensedTypes[nTypes]] == null || lines[dataSensedTypes[nTypes]].equals("")) {
						values[nTypes] = 0.0;
					}  // end if (lines[dataSensedTypes[nTypes]] == null || lines[dataSensedTypes[nTypes]].equals(""))
					else {
						try {
							values[nTypes] = Double.parseDouble(lines[dataSensedTypes[nTypes]]);
						}//try
						catch (NumberFormatException e) {
							values[nTypes] = 0.0;
						}//catch
					} // end else if (lines[dataSensedTypes[nTypes]] == null || lines[dataSensedTypes[nTypes]].equals(""))
				} // end for (int nTypes = 0; nTypes < dataSensedTypes.length; nTypes++)
				
				if (lines[numSequenceVoltageData] == null || lines[numSequenceVoltageData].equals("")) {
					batLevel = 0.0;
				}
				else {
					try {
						batLevel = Double.parseDouble(lines[numSequenceVoltageData]);
					}//try
					catch (NumberFormatException e) {
						batLevel = 0.0;
					}//catch
				}//else

				int round = Integer.parseInt(lines[numSequenceRound]); // Número do round
				
				quantTime = parseCalendarHours(lines[0], lines[1]);
				
//				temp = new DataRecord();
				temp.typs = dataSensedTypes;
				temp.values = values;
				temp.time = quantTime;
				temp.batLevel = batLevel;
				temp.round = round;
				
			} // end if (linhas.length > 4)
			
		} // end if (sensorReading != null && numSequenceValueData != 0)
		else {
			temp = null;
		}
		return temp;
	} // end getData(SimpleNode currentNode, int[] dataSensedTypes)
	
	/**
	 * Lê os próximos valores (para cada tipo sensoriado) do sensor atual, executa a predição e, de acordo com a predição (acerto ou erro), dispara a próxima ação<p>
	 * [Eng] Read next values (from each sensed type) from present sensor, make the prediction and, according with the predition (hit or miss), trigges the next action 
	 * @param dataSensedTypes Tipos de dados para serem lidos pelo sensor: "t"=temperatura, "h"=umidade, "l"=luminosidade ou "v"=voltagem<p>
	 * [Eng] Types of data to be read from sensor: "t"=temperature, "h"=humidity, "l"=luminosity or "v"=voltage
	 * @param coefsA Coeficientes A da equação de regressão para este sensor<p>
	 * [Eng] Coefficients A from the Regression Equation for this sensor
	 * @param coefsB Coeficientes B da equação de regressão para este sensor<p>
	 * [Eng] Coefficients B from the Regression Equation for this sensor
	 * @param maxErrors Limiares de erro para o cálculo de predições para este sensor<p>
	 * [Eng] Threshold errors to the calculation of predictions for this sensor
	 */
	protected void triggerPredictions(int[] dataSensedTypes, double[] coefsA, double[] coefsB, double[] maxErrors)
	{
		// Alterar número do round desejado
/*
		if (Global.currentTime >= 140.0) { // Round to initiate the Debug Mode
//			System.out.println(" * Debug On * ");
		}
*/
		DataRecord dataRecord = getData(this, dataSensedTypes);
		
		if (dataRecord != null) {
			
				double[] values = dataRecord.values;
				double quantTime = dataRecord.time;
				double batLevel = dataRecord.batLevel;
				int round = dataRecord.round;
				
				double[] predictionValues = makePredictions(coefsA, coefsB, quantTime); // Incrementa o contador numTotalPredictions (numTotalPredictions++)
				
				lastRoundRead = dataRecord.round;

				//TriggerPredictions -> isValuePredictInValueReading
				//for
				boolean[] hitsInThisReading = arePredictValuesInReadingValues(values, predictionValues, maxErrors); // RMSE Calculation in this method
				for (int cont = 0; cont < hitsInThisReading.length; cont++) {
					if (!hitsInThisReading[cont]) {
						numPredictionErrorsPerType[cont]++;
						numPredictionErrors++; // Contador do número de erros de predição
//						System.out.println(" * * Prediction Error: SensorID = "+this.ID+" numPredictionErrors = "+numPredictionErrors+" numPredictionErrorsPerType[cont="+cont+"] = "+numPredictionErrorsPerType[cont]);
					}
				} // end if (!isValuePredictInValueReading(value, predictionValue, maxError))

				addDataRecordItens(dataSensedTypes, values, quantTime, batLevel, round); // Removido (comentado) da linha 987
				
				if (numPredictionErrors > 0) { // Se há erros de predição, então exibe uma mensagem
					Utils.printForDebug("* * * * The number of prediction errors is "+numPredictionErrors+"! NoID = "+this.ID+"\n");
				} // end if (numPredictionErrors > 0)
				
				
				if (this.clusterHead == null) { // Se é um Nó Representativo, ler os valores de todos os outros nós naquele mesmo cluster naquele momento e calcular 
					// o RMSE de cada valor em relação ao predictionValue do Nó Representativo
					Node[] nodes = SinkNode.getNodesFromThisCluster(this);

					// DEVE PEGAR OS VALORES LIDOS DE/POR CADA NÓ E CALCULAR O RMSE EM RELAÇÃO AO VALOR PREDITO PARA O NÓ REPRESENTATIVO !!! 
					if (nodes != null && nodes.length > 1) {
						for (int i=0; i < nodes.length ;i++) { // Para cada um dos nós no mesmo cluste deste Nó Representativo
							if (nodes[i].ID != this.ID) { // Caso não seja o próprio Nó Representativo
							
								DataRecord nodeData = getData((SimpleNode)nodes[i], dataSensedTypes); // Calcular o RMSE

								((SimpleNode)nodes[i]).addDataRecordItens(nodeData);
								
								if (nodeData != null && nodeData.values != null) {
									double[] sensorValues = nodeData.values;
									
									for (int cont = 0; cont < sensorValues.length; cont++) {
										// Code inserted in else block according to Prof. Everardo request in 25/09/2013
										Global.predictionsCount++;
										((SimpleNode)nodes[i]).predictionsCount++;
			
										Global.squaredError += Math.pow((predictionValues[cont] - sensorValues[cont]), 2); // Used for RMSE calculation
										((SimpleNode)nodes[i]).squaredError += Math.pow((predictionValues[cont] - sensorValues[cont]), 2);
										// End of Code inserted		
									}
								}
/*
								System.out.print("\t");
								((SimpleNode)nodes[i]).printNodeRMSE();
*/
							}

						} // end for (int i=0; i < nodes.length ;i++)
						
						
					} // end if (nodes != null)
					
					else { // if (nodes == null)
//						System.out.println("Node with NULL CLuster = "+this.ID);
					}
					if (numPredictionErrors == (sensorDelay)) // Send "toPorUmaMessage" (minusOne) to all sensors in this cluster
					{
						this.minusOne = true;
					}
					
				} // end if (this.clusterHead == null)
				
				
/*
 * Neste ponto deve-se verificar se o parâmetro "clusterHead" é diferente de "null", pois indica que existe um ClusterHead (e não um nó representativo)
 * e que, portanto, a predição deverá ficar em laço contínuo, até atingir o limite de erros de predição.
 */
				if (this.clusterHead != null) // Se existe um CH, ou seja, se o modo de sensoriamento é contínuo (SinkNode.allSensorsMustContinuoslySense = true)
				{
					Boolean exit = false;
					if (this.clusterHead == this) // Se ESTE (this) nó é o/um Cluster Head de seu cluster
					{
						if ((batLevel != 0.0) && (batLevel <= minBatLevelInClusterHead)) // Se o nível da bateria está abaixo do mínimo possível (permitido)
						{
							WsnMsgResponse wsnMsgResp;
					
							Integer messageType = 4; // CÓDIGO PARA INFORMAR AO SINK QUE ESTE CLUSTER HEAD ATINGIU O MIN. DE BATERIA

							wsnMsgResp = new WsnMsgResponse(1, this, null, this, messageType, 0, dataSensedTypes);
							
							addDataRecordItensInWsnMsgResponse(wsnMsgResp);

							addThisNodeToPath(wsnMsgResp);
													
							wsnMsgResp.batLevel = batLevel; // Update the level of battery from last reading of sensor node message

							if (nextNodeToBaseStation != null) {
								WsnMessageResponseTimer timer = new WsnMessageResponseTimer(wsnMsgResp, nextNodeToBaseStation);
								timer.startRelative(1, this); // Envia a mensagem para o nó sink (próximo nó no caminho do sink)
							}
							
							exit = true; // Caso este seja o ClusterHead e esteja com a bateria fraca, então não deve continuar sensoreando 
						} // end if ((batLevel != 0.0) && (batLevel <= minBatLevelInClusterHead))
						
					} // end if (this.clusterHead == this)

					if (!exit) {

						if (numPredictionErrors > sensorDelay) { // Se o número máximo de erros de predição por sensor foi atingido para este 
							// sensor e o mesmo tem um ClusterHead (!=null), então este último deve ser reportado (avisado)

							WsnMsgResponse wsnMsgResp;
							
							wsnMsgResp = new WsnMsgResponse(1, this, clusterHead, this, 2, this.ownTimeSlot, dataSensedTypes);
							//wsnMsgResp = new WsnMsgResponse(1, this, clusterHead, this, 5, this.ownTimeSlot, dataSensedType);
							
							Utils.printForDebug("* The number of prediction errors ("+numPredictionErrors+") REACHED the maximum limit of the prediction errors ("+sensorDelay+")! NoID = "+this.ID+"\n");
							Utils.printForDebug("* * * * The predicted value NO is within the margin of error of the value read! NoID = "+this.ID);
							Utils.printForDebug("Round = "+lastRoundRead+": Vpredito[0] = "+predictionValues[0]+", Vlido[0] = "+values[0]+", Limiar[0] = "+maxErrors[0]+"\n");
							
							addDataRecordItensInWsnMsgResponse(wsnMsgResp);
							
							addThisNodeToPath(wsnMsgResp);
							
							wsnMsgResp.batLevel = batLevel; // Update the level of battery from last reading of sensor node message

							DirectMessageTimer timer = new DirectMessageTimer(wsnMsgResp, clusterHead); // Envia uma mensagem diretamente para o 
																										// ClusterHead deste nó sensor
							timer.startRelative(1, this);
							//System.out.println("new DirectMessageTimer(wsnMsgResp, clusterHead); from node "+this.ID+" to node "+clusterHead.ID+" in Round = "+Global.currentTime);
							
							numPredictionErrors = 0; // Reinicia a contagem dos erros de predição, depois de ter enviado uma mensagem inicial para o 
													// ClusterHead
						
						} // end if (numPredictionErrors >= limitPredictionError)
//						else {
						
						// Se o modo de sensoriamento é contínuo, continua fazendo predição
						PredictionTimer newPredictionTimer = new PredictionTimer(dataSensedTypes, coefsA, coefsB, maxErrors); // Então dispara uma nova predição - laço de predições
						//newPredictionTimer.startRelative(1, this); 
						newPredictionTimer.startRelative(SinkNode.sensorTimeSlot, this);
//						}
					} // end (!exit)
					
				} // end if (this.clusterHead != null)
				
				else // if (this.clusterHead == null) - Se não existe ClusterHead, mas sim apenas Nó Representativo
				{
				
					if ((numPredictionErrors <= sensorDelay) && (numTotalPredictions < this.myCluster.sizeTimeSlot)) // Se o número de erros de predição é menor do que o limite aceitável de erros (limitPredictionError) e o número de predições executadas é menor do que o máximo de predições para o cluster deste nó sensor
					{
						PredictionTimer newPredictionTimer = new PredictionTimer(dataSensedTypes, coefsA, coefsB, maxErrors); // Então dispara uma nova predição - laço de predições
						newPredictionTimer.startRelative(1, this); 
					} // end if ((numPredictionErrors < limitPredictionError) && (numTotalPredictions < this.myCluster.sizeTimeSlot))
					
					else
					{
						WsnMsgResponse wsnMsgResp;
						
						if (!(numPredictionErrors <= sensorDelay) && (numTotalPredictions < this.myCluster.sizeTimeSlot)) // Caso tenha saído do laço de predição por ter excedido o número máximo de erros de predição e não pelo limite do seu time slot (número máximo de predições a serem feitas por este Nó Representativo - ou Cluster Head - deste cluster)
						{
							wsnMsgResp = new WsnMsgResponse(1, this, clusterHead, this, 2, (this.myCluster.sizeTimeSlot - numTotalPredictions), dataSensedTypes);
							
							Utils.printForDebug("* The number of prediction errors ("+numPredictionErrors+") REACHED the maximum limit of the prediction errors ("+sensorDelay+")! NoID = "+this.ID+"\n");
							Utils.printForDebug("* * * * The predicted value NO is within the margin of error of the value read! NoID = "+this.ID);
							Utils.printForDebug("Round = "+lastRoundRead+": Vpredito[0] = "+predictionValues[0]+", Vlido[0] = "+values[0]+", Limiar[0] = "+maxErrors[0]+"\n");
						}
						else // if (numTotalPredictions >= this.ownTimeSlot) // Caso tenha saído do laço de predições por ter excedido o limite do seu time slot próprio(número máximo de predições a serem feitas por este Nó Representativo)
						{
							wsnMsgResp = new WsnMsgResponse(1, this, clusterHead, this, 3, 0, dataSensedTypes);
							
							Utils.printForDebug("* * The total loops predictions ("+numTotalPredictions+") REACHED the maximum of loops predictions (TimeSlot proprio = "+this.ownTimeSlot+") of this representative node / cluster! NoID = "+this.ID+"\n");						
						}					
						
						addDataRecordItensInWsnMsgResponse(wsnMsgResp);

						addThisNodeToPath(wsnMsgResp);
						
						wsnMsgResp.batLevel = batLevel; // Update the level of battery from last reading of sensor node message

						if (this.clusterHead == null) // It means that there isn't a cluster head, so the response message must be send to sink node (base station)
						{
							if (nextNodeToBaseStation != null) {
								WsnMessageResponseTimer timer = new WsnMessageResponseTimer(wsnMsgResp, nextNodeToBaseStation);
								timer.startRelative(1, this); // Espera por 1 round e envia a mensagem para o nó sink (próximo nó no caminho do sink) : Antes -> timer.startRelative(1, this);
							}
							// ERA: Espera por "wsnMessage.sizeTimeSlot" rounds e envia a mensagem para o nó sink (próximo nó no caminho do sink)
						} // end if (this.clusterHead == null)

/* Dispensável devido a restrição anterior, ou seja, por se tratar de else do if (this.clusterHead != null), i.e., dentro de um bloco igual a if (this.clusterHead == null)
						else
						{
							DirectMessageTimer timer = new DirectMessageTimer(wsnMsgResp, clusterHead); // Envia uma mensagem diretamente para o ClusterHead deste nó sensor
							timer.startRelative(1, this);
						} // end else from if (this.clusterHead == null)
*/						

					} // end else from if ((numPredictionErrors < limitPredictionError) && (numTotalPredictions < this.ownTimeSlot))
					
				} // end else from if (this.clusterHead != null)
				
			}// end if (dataRecord != null)
			
	}// end triggerPredictions(String dataSensedType, double coefA, double coefB, double maxError)
	
	/**
	 * Isso chama o método triggerPredictions
	 * [Eng]It calls the method triggerPredictions
	 * @param dataSensedTypes Tipo de dado a ser lido pelo sensor: "t"=temperatura, "h"=umidade, "l"=luminosidade ou "v"=voltagem<p>[Eng] Type of data to be read from sensor: "t"=temperature, "h"=humidity, "l"=luminosity or "v"=voltage
	 * @param coefsA Coeficiente A da equação de regressão para esse sensor<p>[Eng] Coefficient A from the Regression Equation for this sensor
	 * @param coefsB Coeficiente B da equação de regressão para esse sensor<p>[Eng] Coefficient B from the Regression Equation for this sensor
	 * @param maxErrors Erro limiar para calculação da predição para esse sensor.<p>[Eng] Threshold error to the calculation of prediction for this sensor
	 */
	public final void triggerPrediction(int[] dataSensedTypes, double[] coefsA, double[] coefsB, double[] maxErrors)
	{
		if (canMakePredictions) {
			//System.out.println("    SensorID = "+this.ID+" Round = "+Global.currentTime+" canMakePredictions ="+canMakePredictions);
			//if (validPredictions) { // Desabilitar "validPredictions" para que sensores continuem sensoriando após o CH deste cluster haver detectado extrapolação do número de erros (errorsInThisRound > clusterDelay)
				if (Global.currentTime > lastRoundTriggered) { // Ensures that the same prediction will not be executed more than once in the same round for the same sensor
					lastRoundTriggered = Global.currentTime;
					triggerPredictions(dataSensedTypes, coefsA, coefsB, maxErrors);
				}
			//}
		}
/*
		else {
			System.out.println("nodeID = "+this.ID+": validPredictions FALSE in Round = "+Global.currentTime);
		}
*/
	} // end triggerPrediction(String dataSensedType, double coefA, double coefB, double maxError)
	
	/**
	 * It prints the RMSE (Root Mean Square Error) for this sensor <p>
	 * Isso imprime o RMSE ( Root Mean Square Error) para este sensor
	 */
	public void printNodeRMSE(){
		double RMSE = 0.0;
		if(this.predictionsCount > 0)
		{
			RMSE = Math.sqrt(this.squaredError / this.predictionsCount);
		}
		System.out.println(this.ID+"\t"+NumberFormat.getNumberInstance().format(RMSE));
	}
	
	/**
	 * Ele compara o valor de leitura ('value') para o valor predito ('predictionValue') usando 'maxError' como erro limiar<p>[Eng] It compares the read value('value') to the predict value('predictionValue') using 'maxError' as threshold error
	 * @param values Valores lidos pelo sensor<p>[Eng] Values read from the sensor
	 * @param predictionValues Valores preditos para serem comparados<p>[Eng] Values predict to be compared
	 * @param maxErrors Limiar de erro para o calculo da predição para o sensor<p>[Eng] Threshold error to the calculation of prediction for this sensor
	 * @return Verdadeiro se o valor sensoriado(lido) está dentro do valor de predição (mais ou menos no limiar de erro) ou falso, caso contrário<p>[Eng] True if the sensed (read) value is in the predicted value (more or less the threshold error) ou False, otherwise
	 */
	protected boolean[] arePredictValuesInReadingValues(double[] values, double[] predictionValues, double[] maxErrors) // Comparação dos valores resultantes do cálculo da predição com os valores reais (lidos)
	{
		// Code moved to else block below - according to Prof. Everardo request in 25/09/2013: RMSE should only be computed when data are not sent to the sink
/*
		Global.predictionsCount++;
		this.predictionsCount++;

		Global.squaredError += Math.pow((predictionValue - value), 2); // RMSE
		this.squaredError += Math.pow((predictionValue - value), 2);
*/
		boolean[] hits = new boolean[values.length];
		
		for (int numTypes = 0; numTypes < values.length; numTypes++) {
			if (values[numTypes] >= (predictionValues[numTypes] - values[numTypes] * maxErrors[numTypes]) && values[numTypes] <= (predictionValues[numTypes] + values[numTypes] * maxErrors[numTypes]))
			{
				Global.numberOfHitsInThisRound++;

				// Code inserted in else block according to Prof. Everardo request in 25/09/2013

				Global.predictionsCount++; // RMSE
				this.predictionsCount++;

				Global.squaredError += Math.pow((predictionValues[numTypes] - values[numTypes]), 2); // RMSE
				this.squaredError += Math.pow((predictionValues[numTypes] - values[numTypes]), 2);

				// End of Code inserted
				
				//printNodeRMSE();
				
				hits[numTypes] = true;
			}
			else
			{
				Global.numberOfMissesInThisRound++;
				hits[numTypes] = false;
				// isValuePredictInValueReading
				if (!minusOne) {
		
					Global.predictionsCount++;
					this.predictionsCount++;
		
					Global.squaredError += Math.pow((predictionValues[numTypes] - values[numTypes]), 2); // RMSE
					this.squaredError += Math.pow((predictionValues[numTypes] - values[numTypes]), 2);
				}
			}
			
		}
/*
		System.out.print("\t");
		printNodeRMSE();
*/
		return hits;
	} // end arePredictValuesInReadingValues(double[] values, double[] predictionValues, double maxError)
	
	/**
	 * Desempilha um nó do caminho de nós <p>
	 * [Eng] Unstack a node of the path we
	 * @return Nó desempilhado <p>[Eng] unstacked node
	 */
	public Integer popFromPath()
	{
		if (pathToSenderNode == null || pathToSenderNode.isEmpty())
		{
			hopsToTarget = 0;
			return null;
		}
		if (hopsToTarget != null && hopsToTarget > 0) {
			hopsToTarget--;
		}
		return pathToSenderNode.pop(); // Remove/Desempilha o próximo nó (noID) do caminho (path) para o nó de origem e o retorna
	}
	
	
	@SuppressWarnings("unchecked")
	public void setPathToSenderNode(Stack<Integer> pathToSenderNode, Integer hopsToTarget)
	{
		this.pathToSenderNode = (Stack<Integer>)pathToSenderNode.clone();
		this.hopsToTarget = hopsToTarget;
	}
	
	public Stack<Integer> getPathToSenderNode() {
		return this.pathToSenderNode;
	}
	
	/**
	 * Atualiza o "dataRecordItens" estrutura do "currentNode" pela leitura de "windowSize" quantidade de dados do tipo "dataType" <p>
	 * [Eng] Updates the "dataRecordItens" structure from the "currentNode" by the reading of "windowSize" quantity of data from type "dataType"
	 * @param currentNode Sensor node to have the dataRecordItens updated
	 * @param dataTypes Types of data to be read by sensor
	 */
	private void updateDataRecordItens(SimpleNode currentNode, int[] dataTypes) {
		for (int i=0; i < windowSize; i++) { // TODO: check and change windowSize to slidingWindowSize
			currentNode.dataRecordItens.add(getData(currentNode, dataTypes));
		}
		while (currentNode.dataRecordItens.size() > windowSize) { // TODO: change windowSize to slidingWindowSize
			currentNode.dataRecordItens.removeElementAt(0);
		}
	}
	
	/**
	 * Interior da classe(estrutura) para armazenar os dados importante dos sensores lidos, como:<p>
	 * tipo char(Tipo do dado do sensor, e.g.: t=temp.,h=Hum., l=Lum. or v=Volt.), <br>
	 * valor double(Valor absoluto), <br>
	 * Tempo double (Data/tempo do valor lido),<br>
	 * batLevel double(Nível de potencia da bateria no sensor) e <br>
	 * Round int (Número do round)<p>
	 * [Eng]Inner class (structure) to store important data from sersors readings, like: <p>
	 * char type (Type of sensor data, e.g.: t=Temp., h=Hum., l=Lum. or v=Volt.), <br>
	 * double value (Absolute value), <br>
	 * double time (Date/time from value reading), <br> 
	 * double batLevel (Battery power level sensor) and <br> 
	 * int round (Round number) 
	 * @author Fernando Rodrigues
	 */
	public class DataRecord
	{
		public int[] typs;
		public double[] values;
		public double time;
		public double batLevel;
		public int round;
	} // end DataRecord
	
	public Vector<DataRecord> dataRecordItens;
	
	public class MessageItens {
		public Node sourceNode;
		Vector<DataRecord> dataRecordItens;
		
		public MessageItens() {
			dataRecordItens = new Vector<DataRecord>();
		}
	}
	
	public Vector<MessageItens> messageItensPackage;
	
	/**
	 * Adiciona os respectivos valores para o atributo dataRecordItens do sensor (SimpleNode)<p>[Eng] Adds the respective values to dataRecordItens attribute from this sensor (SimpleNode)
	 * @param typ Tipo de dado do sensor, como t=Temp., h=Hum., l=Lum. or v=Volt.<p>[Eng] Type of sensor data, like t=Temp., h=Hum., l=Lum. or v=Volt.
	 * @param val Valor Absoluto<p>[Eng] Absolute value
	 * @param tim Data/Tempo do valor lido no formato double<p> [Eng] Date/time from value reading in double format
	 * @param bat Nível de Potência da bateria no sensor<p>[Eng] Battery power level sensor
	 * @param rnd Número do round<p>[Eng] Round number
	 */
	public void addDataRecordItens(int[] typs, double[] vals, double tim, double bat, int rnd)
	{
		if (this.dataRecordItens == null)
		{
			this.dataRecordItens = new Vector<DataRecord>();
		}
		DataRecord dr = new DataRecord();
		
		dr.typs = typs;
		dr.values = vals;
		dr.time = tim;
		dr.batLevel = bat;
		dr.round = rnd;
		
		dataRecordItens.add(dr);
		//Implements a FIFO structure with the vector 'dataRecordItens' with at most 'slidingWindowSize' elements
		while (dataRecordItens.size() > windowSize)
		{
			dataRecordItens.removeElementAt(0);
		}
		nonRead = true;
	} // end addDataRecordItens(char typ, double val, double tim, double bat, int rnd)

	/**
	 * Adiciona os respectivos valores para o atributo dataRecordItens do sensor (SimpleNode)<p>[Eng] Adds the respective values to dataRecordItens attribute from this sensor (SimpleNode)
	 * @param dataRecord O registo de dados com os dados a serem adicionar ao "dataRecordItens" vector a partir do sensor de corrente <p> [Eng] Data record with the data to be add to "dataRecordItens" vector from the current sensor
	 */
	
	public void addDataRecordItens(DataRecord dataRecord)
	{
		if (dataRecord == null) {
			return;
		}

		if (this.dataRecordItens == null)
		{
			this.dataRecordItens = new Vector<DataRecord>();
		}
		dataRecordItens.add(dataRecord);
		//Implements a FIFO structure with the vector 'dataRecordItens' with at most 'slidingWindowSize' elements
		while (dataRecordItens.size() > slidingWindowSize)
		{
			dataRecordItens.removeElementAt(0);
		}
		nonRead = true;
	} // end addDataRecordItens(char typ, double val, double tim, double bat, int rnd)
	
	
	private boolean nonRead = true;
	
	private int[][] types2;
	
	private double[][] values2;
	
	private double[] times;
	
	private double[] batLevels;
	
	private int[] rounds;
	
	private void readData()
	{
		if (nonRead)
		{
			int tam = 0;
			if (dataRecordItens != null)
			{
				tam = dataRecordItens.size();
			}
			types2 = new int[tam][];
			values2 = new double[tam][];
			times = new double[tam];
			batLevels = new double[tam];
			rounds = new int[tam];
			
			for (int i=0; i<tam; i++)
			{
				if (dataRecordItens.get(i) != null)
				{
					types2[i] = ((DataRecord)dataRecordItens.get(i)).typs;
					values2[i] = ((DataRecord)dataRecordItens.get(i)).values;
					times[i] = ((DataRecord)dataRecordItens.get(i)).time;
					batLevels[i] = ((DataRecord)dataRecordItens.get(i)).batLevel;
					rounds[i] = ((DataRecord)dataRecordItens.get(i)).round;
				}
				else
				{
					types2[i] = null;
					values2[i] = null;
					times[i] = 0.0;
					batLevels[i] = 0.0;
					rounds[i] = 0;
				}
			}
			
			nonRead = false;
		}
	}
	
	public int[] getDataRecordTypes(int ind)
	{
		readData();
		return types2[ind];
	}
	
	public double[] getDataRecordValues(int ind)
	{
		readData();
		return values2[ind];
	}

	public double[] getDataRecordTimes()
	{
		readData();
		return times;
	}

	public double[] getDataRecordBatLevels()
	{
		readData();
		return batLevels;
	}
	
	public int[] getDataRecordRounds()
	{
		readData();
		return rounds;
	}
	
}