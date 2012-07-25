package projects.ids_wsn.nodeDefinitions.routing;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import projects.ids_wsn.Utils;
import projects.ids_wsn.nodeDefinitions.BasicNode;
import projects.ids_wsn.nodes.messages.EventMessage;
import projects.ids_wsn.nodes.messages.FloodFindDsdv;
import projects.ids_wsn.nodes.messages.PayloadMsg;
import projects.ids_wsn.nodes.timers.SimpleMessageTimer;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Message;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;

public class DSDV implements IRouting {
	
	private final static Integer NUM_MAX_ENTRIES_PER_ROUTE = 1; 
	
	//Node who is using the protocol
	private BasicNode node;
	
	public void setNode(BasicNode node) {
		this.node = node;
		
	}
	
	public class MultiRoutingEntry {
		public List<Integer> sequenceNumber = new ArrayList<Integer>();
		public List<Integer> numHops = new ArrayList<Integer>();
		public List<Node> nextHops = new ArrayList<Node>();
		public List<Boolean> active = new ArrayList<Boolean>();
		
		public MultiRoutingEntry(Integer seqNumb, Integer numHops, Node node){
			this.sequenceNumber.add(seqNumb);
			this.numHops.add(numHops);
			this.nextHops.add(node);
			this.active.add(Boolean.TRUE);
		}
		
		public Node getFirstActiveRoute(){
			for (int i = 0; i < active.size(); i++){
				if (active.get(i) == Boolean.TRUE){
					return nextHops.get(i);
				}
			}
			return nextHops.get(0);
		}
		
		public void addElements(Integer seqNumb, Integer numHops, Node node){
			this.sequenceNumber.add(seqNumb);
			this.numHops.add(numHops);
			this.nextHops.add(node);
			this.active.add(Boolean.TRUE);
		}
	}
	
	
	
	public int seqID = 0;
	
	//Routing Table with multiple routes
	protected Hashtable<Node, MultiRoutingEntry> multiRoutingTable = new Hashtable<Node, MultiRoutingEntry>();

	public void receiveMessage(Message message) {
		
		if (message instanceof FloodFindDsdv){ 
			receiveFloodFindMsg(message);
			
		}else if (message instanceof PayloadMsg){ //It's a payload message
			PayloadMsg payloadMessage = (PayloadMsg) message;
			receivePayloadMessage(payloadMessage);
		}else if (message instanceof EventMessage){ //It's an event
			EventMessage eventMessage = (EventMessage) message;
			treatEvent(eventMessage);
		}
	}
	
	private void receiveFloodFindMsg(Message message) {
		FloodFindDsdv floodMsg = (FloodFindDsdv) message;
		Boolean forward = Boolean.TRUE;
		
		if (floodMsg.forwardingNode.equals(node)){ // The message bounced back. The node must discard the msg.
			forward = Boolean.FALSE;
		}else{
			MultiRoutingEntry re = multiRoutingTable.get(floodMsg.baseStation);
			if (re == null){
				multiRoutingTable.put(floodMsg.baseStation, new MultiRoutingEntry(floodMsg.sequenceID, floodMsg.hopsToBaseStation, floodMsg.forwardingNode));
				node.setFirstRoutingTtlRcv(floodMsg.ttl);
//				System.out.println("puting a route to BS in Routing Table: "+floodMsg.baseStation.ID);
			}else if (!re.nextHops.contains(floodMsg.forwardingNode)){
				if (re.nextHops.size() < NUM_MAX_ENTRIES_PER_ROUTE) { 
						re.addElements(floodMsg.sequenceID, floodMsg.hopsToBaseStation, floodMsg.forwardingNode);
				}
				forward = Boolean.FALSE;
			}else {
				Integer ind = re.nextHops.indexOf(floodMsg.forwardingNode);
				if (re.sequenceNumber.get(ind) < floodMsg.sequenceID) { //Update an existing entry
					re.numHops.set(ind, floodMsg.hopsToBaseStation);
					re.sequenceNumber.set(ind,floodMsg.sequenceID);
					re.nextHops.set(ind, floodMsg.forwardingNode);							
				}else{
					forward = Boolean.FALSE;
				}
			}
		}
		
		if (forward && floodMsg.ttl > 1){ //Forward the flooding message
			
			FloodFindDsdv copy = (FloodFindDsdv) floodMsg.clone();
			copy.forwardingNode = node;
			copy.ttl--;
			copy.hopsToBaseStation++;
			sendBroadcast(copy);
		}
	}
	
	private void receivePayloadMessage(PayloadMsg payloadMessage) {
		MultiRoutingEntry re = null;
		
		if (payloadMessage.nextHop == null ){ //It's a broadcast
			if (payloadMessage.ttl > 1) {
				Boolean forward = Boolean.TRUE;
				if (payloadMessage.imediateSender.equals(this)){ // The message bounced back
					forward = Boolean.FALSE;
				}else{
					payloadMessage.ttl--;
					payloadMessage.imediateSender = node;
				}
				if (forward){
					sendBroadcast(payloadMessage);
				}
			}
		}else if (payloadMessage.nextHop.equals(node)){
			
			//this.setColor(Color.YELLOW);
			re = multiRoutingTable.get(payloadMessage.baseStation);
			payloadMessage.nextHop = re.getFirstActiveRoute();
			payloadMessage.imediateSender = node;
			
			checkEnergyLevel();
			sendMessage(payloadMessage);
		}
	}
	
	private void checkEnergyLevel() {
		Logging deadLog = Utils.getDeadNodesLog();
		Float energy = node.getBateria().getEnergy();
//		Boolean sendBeacon = Boolean.FALSE;
		
		if (energy.intValue() > node.energy60.intValue() && energy.intValue() < node.energy70.intValue()){
			node.setMyColor(Color.MAGENTA);
			node.setColor(Color.MAGENTA);
			if (!node.send70){
				//sendBeacon = Boolean.TRUE;
				node.send70 = Boolean.TRUE;
			}
		}
		
		if (energy.intValue() > node.energy50.intValue() && energy.intValue() < node.energy60.intValue()){
			node.setMyColor(Color.LIGHT_GRAY);
			node.setColor(Color.LIGHT_GRAY);
			if (!node.send60){
//				sendBeacon = Boolean.TRUE;
				node.send60 = Boolean.TRUE;
			}
		}
		
		if (energy.intValue() > node.energy40.intValue() && energy.intValue() < node.energy50.intValue()){
			node.setMyColor(Color.GRAY);
			node.setColor(Color.GRAY);
			if (!node.send50){
//				sendBeacon = Boolean.TRUE;
				node.send50 = Boolean.TRUE;
			}
		}
		
		if (energy.intValue() <= 0){
			node.setMyColor(Color.BLACK);
			node.setColor(Color.BLACK);
			node.setIsDead(Boolean.TRUE);
			deadLog.logln(Tools.getGlobalTime()+":"+node.ID);
		}
	}
	
	private void controlColor(){
		node.setColor(Color.YELLOW);
		Utils.restoreColorNodeTimer(node, 3);
	}

	public void sendMessage(Message message) {
		
		if (node.getIsDead()){
			return;
		}
		SimpleMessageTimer messageTimer = new SimpleMessageTimer(message);
		messageTimer.startRelative(2, node);
		controlColor();
	}

	public void sendBroadcast(Message message) {
		sendMessage(message);		
	}

	public Node getBestRoute(Node destino) {
		//TODO: Make this method more generic. In this way, the method only return the route to the base station
		//Node nodeDst = multiRoutingTable.keys().nextElement();
		Node nextHopToDst = multiRoutingTable.elements().nextElement().getFirstActiveRoute();
		
		return nextHopToDst;
	}

	public Boolean isNodeNextHop(Node destination) {
		MultiRoutingEntry re = multiRoutingTable.get(Tools.getNodeByID(1));
		Node nextHop = re.getFirstActiveRoute();
		
		return (nextHop.equals(destination)) ? Boolean.TRUE : Boolean.FALSE;
	}

	public Node getSinkNode() {
		Enumeration<Node> nodes = multiRoutingTable.keys();
		return nodes.nextElement();
	}

	public void sendMessage(Integer value) {
		
		if (node.getIsDead()){
			return;
		}
		
		node.seqID++;
		Node destino = getSinkNode();
		Node nextHopToDestino = getBestRoute(destino);
		
		PayloadMsg msg = new PayloadMsg(destino, node, nextHopToDestino, node);
		msg.value = value;
		msg.immediateSource = node;
		msg.sequenceNumber = ++this.seqID;
		
		SimpleMessageTimer messageTimer = new SimpleMessageTimer(msg);
		messageTimer.startRelative(2, node);
		controlColor();
		
	}
	
	public void sendChordMessage(PayloadMsg message){
		
		if (node.getIsDead()){
			return;
		}
		
		node.seqID++;
		Node destino = getSinkNode();
		Node nextHopToDestino = getBestRoute(destino);
		
		message.baseStation = destino;
		message.sender = node;
		message.nextHop = nextHopToDestino;
		message.imediateSender = node;
		message.immediateSource = node;
		message.sequenceNumber = ++this.seqID;
		
		SimpleMessageTimer messageTimer = new SimpleMessageTimer(message);
		messageTimer.startRelative(2, node);
		controlColor();
		
		
	}

	public void printRoutingTable() {
		
	}
	
	public void treatEvent(EventMessage message){
		if (!multiRoutingTable.isEmpty()){						
			sendMessage(message.value);
		}
	}

	public void sendMessageWithTimer(Message message, Integer timer) {
		if (node.getIsDead()){
			return;
		}
		
		SimpleMessageTimer messageTimer = new SimpleMessageTimer(message);
		messageTimer.startRelative(timer, node);
		controlColor();
		
	}

	public void sendMessageWithTimer(Integer value, Integer timer) {
		if (node.getIsDead()){
			return;
		}
		
		if (multiRoutingTable.isEmpty()){
			return;
		}
		
		node.seqID++;
		Node destino = getSinkNode();
		Node nextHopToDestino = getBestRoute(destino);
		
		PayloadMsg msg = new PayloadMsg(destino, node, nextHopToDestino, node);
		msg.value = value;
		msg.immediateSource = node;
		msg.sequenceNumber = ++this.seqID;
		
		SimpleMessageTimer messageTimer = new SimpleMessageTimer(msg);
		messageTimer.startRelative(timer, node);
		controlColor();
		
	}
}
