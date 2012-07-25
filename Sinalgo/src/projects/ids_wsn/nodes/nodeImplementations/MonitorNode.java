package projects.ids_wsn.nodes.nodeImplementations;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import projects.ids_wsn.enumerators.ChordMessageType;
import projects.ids_wsn.nodeDefinitions.BasicNode;
import projects.ids_wsn.nodeDefinitions.Monitor.DataMessage;
import projects.ids_wsn.nodeDefinitions.Monitor.IMonitor;
import projects.ids_wsn.nodeDefinitions.Monitor.Rules;
import projects.ids_wsn.nodeDefinitions.Monitor.decorator.IntervalRule;
import projects.ids_wsn.nodeDefinitions.Monitor.decorator.RepetitionRule;
import projects.ids_wsn.nodeDefinitions.Monitor.decorator.RetransmissionRule;
import projects.ids_wsn.nodeDefinitions.chord.UtilsChord;
import projects.ids_wsn.nodeDefinitions.dht.Chord;
import projects.ids_wsn.nodeDefinitions.dht.IDHT;
import projects.ids_wsn.nodeDefinitions.dht.Signature;
import projects.ids_wsn.nodes.messages.ChordMessage;
import projects.ids_wsn.nodes.messages.FloodFindDsdv;
import projects.ids_wsn.nodes.messages.FloodFindFuzzy;
import projects.ids_wsn.nodes.messages.PayloadMsg;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.nodes.Connections;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;
import sinalgo.tools.storage.ReusableListIterator;

/**
 * Node responsible for monitoring your neighbors through the promiscuous
 * listening mode. Stores informations of interest, processing them due to
 * specified rules. It can act like a <b>Supervisor Node</b>. In this case, it
 * becomes able to correlate evidences discovered by others monitor nodes. Each
 * supervisor node is responsible for correlating a specific sub-set of rules.
 * 
 * @author Marcus Vinícius Lemos<br/>
 * @changes Alex Lacerda Ramos
 */
public class MonitorNode extends BasicNode implements IMonitor {

	/**
	 * It stores the internal messages buffer size (<code>dataMessages</code>).
	 */
	public static Integer INTERNAL_BUFFER;

	/**
	 * Messages intercepted by the monitor node. When the messages buffer is
	 * full, the rules must be applied to the messages in order to find
	 * malicious nodes.
	 */
	private List<DataMessage> dataMessages;

	/**
	 * Map that contains a list of possible malicious nodes according to each
	 * rule. This is a local map, hence it contains only the nodes watched by
	 * this monitor node.
	 */
	private Map<Rules, List<Node>> mapLocalMaliciousNodes;

	private Integer hashLength = 1024;
	
	private Integer hashChain[] = new Integer[hashLength];
	
	private IDHT dht;
	
	static {
		try {
			INTERNAL_BUFFER = Configuration
					.getIntegerParameter("Monitor/Inference/InternalBuffer");
		} catch (CorruptConfigurationEntryException e) {
			Tools
					.appendToOutput("Key Monitor/Inference/InternalBuffer not found");
			e.printStackTrace();
		}
	}

	@Override
	public void init() {
		setMyColor(Color.RED);
		super.init();
		this.dht = new Chord(this);
		mapLocalMaliciousNodes = new HashMap<Rules, List<Node>>();
		dataMessages = new ArrayList<DataMessage>();
		initHashChain();
	}

	@Override
	protected void preProcessingMessage(Message message) {
		if (message instanceof PayloadMsg) {
			PayloadMsg msg = (PayloadMsg) message;
			if (!msg.isChordMessage()) {
				addMessageToList(msg);
			}
		}
	}

	@Override
	public Boolean beforeSendingMessage(Message message) {
		if (message instanceof PayloadMsg) {
			PayloadMsg msg = (PayloadMsg) message;
			if (!msg.isChordMessage()) {
				addMessageToList(msg);
			}
		}
		return Boolean.TRUE;
	}
	
	private void addMessageToList(PayloadMsg msg) {
		DataMessage dataMessage = new DataMessage();
		dataMessage.setClock((int) Tools.getGlobalTime());
		dataMessage.setData(msg.value);
		dataMessage.setFinalDst(msg.baseStation.ID);
		dataMessage.setIdMsg(msg.sequenceNumber);
		dataMessage.setImediateDst(msg.nextHop.ID);
		dataMessage.setImediateSrc(msg.imediateSender.ID);
		dataMessage.setSource(msg.sender.ID);

		dataMessages.add(dataMessage);

		if (dataMessages.size() == INTERNAL_BUFFER) {
			applyRules();
			dataMessages.clear();
		}
	}

	private void applyRules() {
		IMonitor rule1 = new RepetitionRule(this);
		IMonitor rule2 = new RetransmissionRule(rule1);
		IMonitor rule3 = new IntervalRule(rule2);
		rule3.doInference();

		
		if(BaseStation.IS_FINGER_TABLE_CREATED){
			this.sendMaliciousNodesToSupervisor();
		}
	}

	private void sendMaliciousNodesToSupervisor() {
		
		for (Rules rule : mapLocalMaliciousNodes.keySet()) {

			Integer hashKey = UtilsChord.generateSHA1(rule.name());
			MonitorNode sucessorNode = this.getDht().findSucessor(hashKey);

			System.out.println("node" + this.ID + "(hash: " + this.getHashID() + ")" + " is sending malicious for rule: " + rule.name() + 
					" hash(" +hashKey + ") for supervisor " + sucessorNode.ID + " hash(" + sucessorNode.getHashID() + ")" );

			List<Node> maliciousNodes = mapLocalMaliciousNodes.get(rule);
			
			PayloadMsg payloadMsg = new PayloadMsg();
			payloadMsg.value = ChordMessageType.SEND_TO_SUPERVISOR.getValue();

			Signature signature = new Signature(this, maliciousNodes, rule, sucessorNode);
			payloadMsg.setSignature(signature);
			
			
			this.getRouting().sendChordMessage(payloadMsg);
		}
		//all the malicious nodes were sent to supervisor, they don't need to be kept here anymore
		this.mapLocalMaliciousNodes.clear();
	}
	
	public void doInference() {
		
	}
	
	public void correlate(Set<Signature> signatures) {
		// TODO Pegar método de correlacionar no projeto antigo do marvin
		System.out.println("##########correlating...###########");
	}
	
	public void addLocalMaliciousList(Rules rule, List<Node> lista) {
		mapLocalMaliciousNodes.put(rule, lista);
	}
	
	@Override
	protected void postProcessingMessage(Message message) {
		super.postProcessingMessage(message);
		
		//depois que os monitores receberem uma mensagem de roteamento, eles estao certos de que podem enviar uma
		//mensagem para a baseStation notificando-a de sua existência
		if(message instanceof FloodFindDsdv || message instanceof FloodFindFuzzy){
			this.sendMessageToBaseStation(ChordMessageType.ANSWER_MONITOR_ID.getValue());
			this.notifyNeighbors();// notify neighbors that this node is a monitor
		}
		
		if (message instanceof PayloadMsg) {
			PayloadMsg payloadMsg = (PayloadMsg) message;
			
			//recebendo assinaturas dos outros monitores
			if (payloadMsg.value == ChordMessageType.SEND_TO_SUPERVISOR.getValue().intValue()) {
				
				Signature signature = payloadMsg.getSignature();

				if (signature.getSupervisor().equals(this)) {//this node is a supervisor (target) responsible for the signature rule
					this.getDht().addExternalSignature(signature);
				}
			}
		}
	}

	private void notifyNeighbors() {
		ChordMessage chordMessage = new ChordMessage();
		chordMessage.setSender(this);
		chordMessage.setChordMessageType(ChordMessageType.NOTIFY_NEIGHBORS);
		
		Edge edge;
		Node neighbour;
		Connections conn = this.outgoingConnections;
		ReusableListIterator<Edge> listConn = conn.iterator();
		
		while (listConn.hasNext()){
			edge = listConn.next();
			neighbour = edge.endNode;
			neighbour.sendDirect(chordMessage, neighbour);
		}
		
	}

	@NodePopupMethod(menuText="Print Finger Table")
	public void printFingerTable(){
		Tools.appendToOutput("\n\nnode: " + this.ID + " ( "+ this.getHashID() +" )");
		
		List<FingerEntry> fingerTable = this.getDht().getFingerTable();
		for (FingerEntry fingerEntry : fingerTable) {
			Tools.appendToOutput("\n"+fingerEntry.getIndex()+" | "+fingerEntry.getStartHash()+" --> "+fingerEntry.getSucessorNode().getHashID());
		}
	}
	
	@NodePopupMethod(menuText="Print Ring Information")
	public void printRingInfomation(){
		Tools.appendToOutput("\nnode: " + this.ID + " ( "+ this.getHashID() +" )");
		
		Tools.appendToOutput("\nnext: "+this.getDht().getNextNodeInChordRing().ID+" (hash: " + this.getDht().getNextNodeInChordRing().getHashID() + ")");
		Tools.appendToOutput("\nprevious: "+this.getDht().getPreviousNodeInChordRing().ID+" (hash: " + this.getDht().getPreviousNodeInChordRing().getHashID() + ")");
		Tools.appendToOutput("\n-----------------");
	}

	@NodePopupMethod(menuText="Kill node")
	public void killMyself(){
		this.autokill();
		Tools.appendToOutput("Nó " + this.ID + " HASH(" + this.dht.getHashID() + ") is dead");
		for (Node node : Tools.getNodeList()) {
			if (node instanceof BasicNode) {
				BasicNode basicNode = (BasicNode) node;
				basicNode.getBateria().restarEnergySpentPerRound();
			}
		}
	}

	@NodePopupMethod(menuText="set alive")
	public void comeBackToLife(){
		this.revive();
		Tools.appendToOutput("Nó " + this.ID + " HASH(" + this.dht.getHashID() + ") is back again");
		for (Node node : Tools.getNodeList()) {
			if (node instanceof BasicNode) {
				BasicNode basicNode = (BasicNode) node;
				basicNode.getBateria().restarEnergySpentPerRound();
			}
		}
	}
	/*----------------------------------------------------
	---------------- GETTTERS AND SETTERS ----------------
	----------------------------------------------------*/

	public List<DataMessage> getDataMessages() {
		return dataMessages;
	}

	public void setDataMessages(List<DataMessage> dataMessages) {
		this.dataMessages = dataMessages;
	}

	public Map<Rules, List<Node>> getMapLocalMaliciousNodes() {
		return mapLocalMaliciousNodes;
	}

	public void setMapLocalMaliciousNodes(
			Map<Rules, List<Node>> mapLocalMaliciousNodes) {
		this.mapLocalMaliciousNodes = mapLocalMaliciousNodes;
	}

	public Integer getMonitorID() {
		return this.ID;
	}
	
	private void initHashChain(){
		Random rnd = Tools.getRandomNumberGenerator();
		for (int i = 0; i<hashLength; i++){
			hashChain[i] = rnd.nextInt();
		}
	}

	@Override
	public List<DataMessage> getDataMessage() {
		return this.dataMessages;
	}

	@Override
	public void setLocalMaliciousList(Rules rule, List<Node> lista) {
		
	}
	
	public Integer getHashID() {
		return this.dht.getHashID();
	}
	
	public void setHashID(Integer hashID) {
		this.getDht().setHashID(hashID);
	}
	
	public IDHT getDht() {
		return dht;
	}
	
	public void setDht(IDHT dht) {
		this.dht = dht;
	}
}
