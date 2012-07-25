package projects.ids_wsn.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import projects.ids_wsn.comparators.EnergyComparator;
import projects.ids_wsn.enumerators.ChordMessageType;
import projects.ids_wsn.enumerators.Order;
import projects.ids_wsn.nodeDefinitions.BasicNode;
import projects.ids_wsn.nodeDefinitions.chord.UtilsChord;
import projects.ids_wsn.nodes.messages.FloodFindDsdv;
import projects.ids_wsn.nodes.messages.FloodFindFuzzy;
import projects.ids_wsn.nodes.messages.PayloadMsg;
import projects.ids_wsn.nodes.timers.BaseStationMessageTimer;
import projects.ids_wsn.nodes.timers.BaseStationRepeatMessageTimer;
import projects.ids_wsn.nodes.timers.RestoreColorBSTime;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Connections;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.nodes.messages.Inbox;
import sinalgo.nodes.messages.Message;
import sinalgo.runtime.nodeCollection.NodeCollectionInterface;
import sinalgo.tools.Tools;
import sinalgo.tools.storage.ReusableListIterator;

public class BaseStation extends Node {
	
	public static boolean IS_FINGER_TABLE_CREATED;

	private Integer sequenceID = 0;

	private Boolean isRouteBuild = Boolean.FALSE;
	
	private Integer countReceivedMessages;
	
	private Integer numberOfRoutes;
	
	private Integer broadcastID = 0;
	
	private Boolean printReceivedMessage =  Boolean.FALSE;
	
	/**
	 * Stores the network monitors that form the Chord Ring
	 */
	private List<MonitorNode> monitorNodes;	

	/**
	 * Stores the network monitors that received their finger tables
	 */
	private List<MonitorNode> monitorsWithFinger;
	
	public Boolean getIsRouteBuild() {
		return isRouteBuild;
	}

	public Integer getNumberOfRoutes() {
		return numberOfRoutes;
	}
	
	@Override
	public void checkRequirements() throws WrongConfigurationException {
		
	}

	@Override
	public void handleMessages(Inbox inbox) {
		while (inbox.hasNext()){
			Message message = inbox.next();
			
			if (message instanceof PayloadMsg){
				PayloadMsg payloadMessage = (PayloadMsg) message;
				if ((payloadMessage.nextHop == null) ||
					(payloadMessage.nextHop.equals(this))){
					countReceivedMessages++;
					
					//mensagens do tipo ANSWER_MONITOR_ID sao enviadas por monitores para notificar a baseStation sobre
					//sua existencia. Entao a baseStation estará apta a criar o Chord Ring.
					if (payloadMessage.value.equals(ChordMessageType.ANSWER_MONITOR_ID.getValue())){
						receiveMonitorAnswer((MonitorNode) payloadMessage.sender);
					}
					
					//se um monitor está fora da rede, a baseStation refaz o Chord Ring somente com os monitores vivos
					if (payloadMessage.value.equals(ChordMessageType.MONITOR_DOWN.getValue())){
						createFingerTables(Boolean.TRUE);
					}
					
					// se um monitor receveu sua finger table ele envia uma msg de confirmação para a estação base
					if (payloadMessage.value.equals(ChordMessageType.FINGER_TABLE_RECEIVED.getValue())){
						receiveMonitorWithFingerAnswer((MonitorNode) payloadMessage.sender);
					}
				
					controlColor();	
					if (printReceivedMessage){
						Tools.appendToOutput("ID: "+payloadMessage.sender.ID+" /Msg: "+payloadMessage.sequenceNumber+" /Timer: "+Tools.getGlobalTime()+"\n");
					}
				}
			}
		}
	}

	private void receiveMonitorAnswer(MonitorNode monitorNode) {
		Boolean adicionouNovoNoh = this.addMonitorNode(monitorNode);
		
		if (adicionouNovoNoh) {
			String msg = "id=" + monitorNode.ID + " - hash=" + monitorNode.getHashID() + " - t=" + Tools.getGlobalTime();
			System.out.println(msg);
			Tools.appendToOutput("\n"+msg);
		}
		
		if(adicionouNovoNoh && this.monitorNodes.size() >= UtilsChord.getAliveMonitorNodes().size()){
			createFingerTables(Boolean.FALSE);
		}
	}
	
	private void receiveMonitorWithFingerAnswer(MonitorNode monitorNode) {
		Boolean adicionouNovoNoh = this.addMonitorWithFinger(monitorNode);
		
		if (adicionouNovoNoh) {
			String msg = "id=" + monitorNode.ID + " - hash=" + monitorNode.getHashID() + " - rec at=" + Tools.getGlobalTime();
			System.out.println(msg);
			Tools.appendToOutput("\n"+msg);
		}
		
		if(adicionouNovoNoh && this.monitorsWithFinger.size() >= UtilsChord.getAliveMonitorNodes().size()){
			String msg = "all fingers rec at=" + Tools.getGlobalTime();
			System.out.println(msg);
			Tools.appendToOutput("\n"+msg);

//			if (teste2) {
				int totalDeNos = getQuantidadeNosNormais() + getQuantidadeNosMonitores();
				UtilsChord.gerarLogDeEnergiaPerRound("energylog_ " + totalDeNos + "nodes " + getQuantidadeNosMonitores() +"monitores " + getQuantidadeNosNormais() + "normais.csv");
				Tools.clearOutput();
				Tools.appendToOutput("log criado");
//			}

			if (!teste2) {
//				((MonitorNode)UtilsChord.getAliveMonitorNodes().get(0)).killMyself();
//				((MonitorNode)UtilsChord.getAliveMonitorNodes().get(1)).killMyself();
//				((MonitorNode)UtilsChord.getAliveMonitorNodes().get(2)).killMyself();
				Tools.clearOutput();
				Tools.appendToOutput("okkkkkkkkkkkkkk");
				teste2 = true;
			}
			
			this.monitorsWithFinger = new ArrayList<MonitorNode>();
		}
	}
	public static boolean teste2 = false;
	public int getQuantidadeNosMonitores(){
		int count = 0;
		
		NodeCollectionInterface allNodes = Tools.getNodeList();
		for (Node node : allNodes) {
			if (node.getClass().equals(MonitorNode.class)) {
				count++;
			}
		}
		
		return count;
	}
	
	public int getQuantidadeNosNormais(){
		int count = 0;
		
		NodeCollectionInterface allNodes = Tools.getNodeList();
		for (Node node : allNodes) {
			if (node.getClass().equals(NormalNode.class)) {
				count++;
			}
		}
		
		return count;
	}
	
	@NodePopupMethod(menuText="Enable/Disable the printing of received messages")
	public void enableDisableImpMsg(){
		Tools.clearOutput();
		if (printReceivedMessage) 
			printReceivedMessage = Boolean.FALSE; 
		else 
			printReceivedMessage = Boolean.TRUE;
	}
	
	@NodePopupMethod(menuText="Print the number of received messages")
	public void printCountReceivedMessages(){
		//Tools.clearOutput();
		Tools.appendToOutput(this+": "+this.countReceivedMessages+" messages\n");
	}

	@Override
	public void init() {
		this.countReceivedMessages = 0;
		this.monitorNodes = new ArrayList<MonitorNode>();
		this.monitorsWithFinger = new ArrayList<MonitorNode>();
		try {
			//Here, we have to get the Number of Routes Value from Config.xml and inject into numberOfRoutes attribute
			String number = Configuration.getStringParameter("NetworkLayer/NumbersOfRoutesFuzzy");
			this.numberOfRoutes = Integer.valueOf(number);
		} catch (CorruptConfigurationEntryException e) {
			Tools.appendToOutput("NumbersOfRoutesFuzzy key not founf in Config.xml");
			e.printStackTrace();
		}
	}

	@Override
	public void neighborhoodChange() {
		sendMessageTo();
//		sendMessageFuzzyTo();
	}

	@Override
	public void postStep() {}

	@Override
	public void preStep() {}
	
	@NodePopupMethod(menuText="Build routing tree - DSDV")
	public void sendMessageTo(){	
		FloodFindDsdv floodMsg = new FloodFindDsdv(++sequenceID, this, this, this, this);
		floodMsg.energy = 500000;
		BaseStationMessageTimer t = new BaseStationMessageTimer(floodMsg);
		t.startRelative(1, this);
		this.isRouteBuild = Boolean.TRUE;
	}
	
	@NodePopupMethod(menuText="Build routing tree - Fuzzy")
	public void sendMessageFuzzyTo(){
		BaseStationRepeatMessageTimer t = new BaseStationRepeatMessageTimer(700);
		t.startRelative(1, this);
		
	}
	
	@NodePopupMethod(menuText="Build routing tree - Only Multi-Path")
	public void sendMessageMultiPathWithoutFuzzyTo(){	
		BaseStationRepeatMessageTimer t = new BaseStationRepeatMessageTimer(0);
		t.startRelative(1, this);
		
	}
	
	public void prepareSendRouteMessage(){
		List<BasicNode> listNodes = getNeighboringNodes();
		broadcastID++;
		
		int x = 0;
		for (Node n : listNodes){
			x = x + 1;
			sendRouteMessage(x, n, broadcastID);
		}
	}
	
	private void sendRouteMessage(Integer index, Node dst, int broadcastID) {
		FloodFindFuzzy floodMsg = new FloodFindFuzzy(broadcastID, this, this, this, this, index, dst, broadcastID);
		//FloodFindDsdv floodMsg = new FloodFindDsdv(++sequenceID, this, this, this, this);
		floodMsg.energy = 500000;
		BaseStationMessageTimer t = new BaseStationMessageTimer(floodMsg);
		t.startRelative((index+1)*2, this);
		this.isRouteBuild = Boolean.TRUE;
		
	}

	@Override
	public String toString() {
		return "Base Station "+this.ID;
	}
	
	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		String text = "BS";
		super.drawNodeAsSquareWithText(g, pt, highlight, text, 11, Color.WHITE);
	}
	
	private void controlColor(){
		this.setColor(Color.YELLOW);
		RestoreColorBSTime restoreColorTime = new RestoreColorBSTime();
		restoreColorTime.startRelative(5, this);
	}
	
	public Integer getSequenceID(){
		return ++sequenceID;
	}
	
	private List<BasicNode> getNeighboringNodes(){
		List<BasicNode> listNodes = new ArrayList<BasicNode>();
		Node n = null;
		Edge e = null;
		Integer index = 0;
		Connections conn = this.outgoingConnections;
		ReusableListIterator<Edge> listConn = conn.iterator();
		
		while (listConn.hasNext()){
			e = listConn.next();
			n = e.endNode;
			if (n instanceof BasicNode)
				listNodes.add((BasicNode)n);
			index++;
		}
		
		Collections.sort(listNodes,new EnergyComparator(Order.DESC));
		return listNodes;
	}
	
	public Boolean addMonitorNode(MonitorNode monitorNode){
		for (MonitorNode monitor : monitorNodes) {
			if (monitor.equals(monitorNode)) {
				return Boolean.FALSE;
			}
		}
		return this.monitorNodes.add(monitorNode);
	}
	
	public Boolean addMonitorWithFinger(MonitorNode monitorNode){
		for (MonitorNode monitor : monitorsWithFinger) {
			if (monitor.equals(monitorNode)) {
				return Boolean.FALSE;
			}
		}
		return this.monitorsWithFinger.add(monitorNode);
	}
	
	public Boolean isMonitorListFull(){
		return monitorNodes.size() >= UtilsChord.getAliveMonitorNodes().size();
	}

	public List<MonitorNode> getMonitorNodes() {
		return monitorNodes;
	}
	
	@NodePopupMethod(menuText="Print qtde. monitors")
	public void printMonitorNodesQuantity(){
		Tools.appendToOutput("Total: " + monitorNodes.size());
		for (MonitorNode monitorNode : monitorNodes) {
			Tools.appendToOutput("\nnó "+ monitorNode.ID + " (hash: " + monitorNode.getHashID() + ")");
		}
	}
	
	@NodePopupMethod(menuText="Create new Finger Tables")
	public void createFingerTableDueToNewNodes(){
		this.createFingerTables(Boolean.TRUE);
	}
	
	public void createFingerTables(boolean loadMonitors){
		if (loadMonitors) {
			this.monitorNodes = UtilsChord.getAliveMonitorNodes();
		}
		
		UtilsChord.createFingerTables(monitorNodes);
		
		comunicateMonitors();
	}

	/**
	 * Informa aos monitores que eles já possuem finger tables criadas
	 */
	private void comunicateMonitors() {
		List<MonitorNode> monitors = UtilsChord.getAliveMonitorNodes();
		for (MonitorNode monitor : monitors) {
			monitor.sendMessageToBaseStation(ChordMessageType.FINGER_TABLE_RECEIVED.getValue());
		}
		
	}
}

