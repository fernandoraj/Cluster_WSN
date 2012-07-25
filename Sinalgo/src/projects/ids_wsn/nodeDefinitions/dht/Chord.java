package projects.ids_wsn.nodeDefinitions.dht;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import projects.ids_wsn.nodeDefinitions.Monitor.Rules;
import projects.ids_wsn.nodeDefinitions.chord.UtilsChord;
import projects.ids_wsn.nodes.nodeImplementations.FingerEntry;
import projects.ids_wsn.nodes.nodeImplementations.MonitorNode;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.tools.Tools;

public class Chord implements IDHT {
	
	/**
	 * When this buffer is full, the signatures must be analyzed 
	 * and the supervisor must subscribe the malicious nodes to the other monitors
	 */
	public static Integer SIGNATURES_BUFFER;
	
	static {
		try {
			SIGNATURES_BUFFER = Configuration.getIntegerParameter("Monitor/Inference/SignaturesBuffer");
		} catch (CorruptConfigurationEntryException e) {
			Tools.appendToOutput("Key Monitor/Inference/SignaturesBuffer not found");
			e.printStackTrace();
		}
	}

	/**
	 * Monitor node which this class references
	 */
	private MonitorNode monitor;

	/**
	 * Stores the SHA-1 Hash ID of the node monitor node
	 */
	private Integer hashID;
	
	/**
	 * Rules which the monitor(supervisor) is responsible for.
	 */
	private Set<Rules> supervisedRules;

	/**
	 * Stores the distributed hash table containing the keys(ID's of violated
	 * rules), the datum(ID's of the monitor responsible for the respective
	 * rule) and some other information or interest according to the <b>Chord
	 * Protocol</b>.
	 */
	private List<FingerEntry> fingerTable;

	/**
	 * Refers to the following node in the formation of the Chord Protocol Ring
	 */
	private MonitorNode nextNodeInChordRing;

	/**
	 * Refers to the previous node in the formation of the Chord Protocol Ring
	 */
	private MonitorNode previousNodeInChordRing;
	
	/**
	 * When a monitor node becomes a supervisor node, this map is used to store
	 * the lists of malicious nodes received from the
	 * <code>mapLocalMaliciousNodes</code> of other monitor nodes.
	 */
	private Map<Rules, Set<Signature>> mapExternalSignatures;

	public Chord(MonitorNode monitor) {
		this.monitor = monitor;
		this.hashID = getHash();
		this.mapExternalSignatures = new HashMap<Rules, Set<Signature>>();
		supervisedRules = new HashSet<Rules>();
		fingerTable = new ArrayList<FingerEntry>();
	}
	
	private Integer getHash() {
		int count = 0;
		Integer hash = null;
		boolean temHashRepetido = true;
		
		while (temHashRepetido) {
			hash = UtilsChord.generateSHA1(this.monitor.ID + count++);
			temHashRepetido = UtilsChord.hasNodeWithTheSameHashID(hash);
		}
		
		return hash;
	}

	public void createFingerTable(){
		for (int index = 0; index < UtilsChord.CHORD_RING_SIZE; index++) {
			System.out.println("\tcriando finger indice: "+index);
			Integer startHash = getStart(index);
			System.out.println("\tstart hash: " + startHash);
			
			MonitorNode sucessorNode = findSucessor(startHash);
			System.out.println("\tsucessor hash: " + sucessorNode.getHashID());
			System.out.println("\t----------------------------------------------");
			FingerEntry fingerEntry = new FingerEntry(this.monitor.ID, index, startHash, sucessorNode);
			
			fingerTable.add(fingerEntry);
		}
	}
	
	public Integer getStart(int index){
	/*----------(hashID + 2.pow(index)) mod 2.pow(chord_ring_size)----------*/
		Integer amount = new Double(Math.pow(2, index)).intValue();
		Integer mod = new Double(Math.pow(2, UtilsChord.CHORD_RING_SIZE)).intValue();
		
		return (this.monitor.getHashID() + amount) % mod;
	}
		

	public MonitorNode findSucessor(Integer hashKey) {
		if(isBetween(hashKey, this.monitor.getHashID(), nextNodeInChordRing.getHashID()) || hashKey == nextNodeInChordRing.getHashID()){
			return nextNodeInChordRing;
		}else{
			MonitorNode fromMyFingerTable = findInMyFingerTable(hashKey);
			if(fromMyFingerTable != null){
				return fromMyFingerTable;
			}
			System.out.println("\t" + this.monitor.getHashID() + " is gonna call FIND_CLOSEST_PREDECESSOR for key " + hashKey);
			MonitorNode closestPredecessor = findClosestPredecessor(hashKey);
			System.out.println("\tclosest: " + closestPredecessor.getHashID());
			return closestPredecessor.getDht().findSucessor(hashKey);
		}
	}

	private MonitorNode findInMyFingerTable(Integer hashKey) {
		//TODO corrigir para comparar o fingerEntry.starthash e retornar fingerEntry.sucessorNode, 
		//pois a comparação está sendo feita pelo fingerEntry.sucessorNode
		for (int i = fingerTable.size()-1; i >= 0; i--) {
			FingerEntry fingerEntry = fingerTable.get(i);
			MonitorNode sucessorFinger = fingerEntry.getSucessorNode(); 
			if(sucessorFinger.getHashID() == hashKey){
				return sucessorFinger;
			}
		}
		
		return null;
	}

	public boolean isBetween(Integer hashKey, Integer before, Integer after) {
		if(before == after){
			return (hashKey != before);
		}
		
		if(before < after){
			return hashKey.compareTo(before) > 0 && hashKey.compareTo(after) < 0;
			
		}
		
		Integer minID = 0;
		Integer maxID = new Double(Math.pow(2, UtilsChord.CHORD_RING_SIZE)).intValue();
		
		return (((before != maxID) && (hashKey > before) && hashKey<=maxID) || 
				(minID != after) && (hashKey >= minID) && (hashKey < after));

	}

	public MonitorNode findClosestPredecessor(Integer hashKey) {
		for (int i = fingerTable.size()-1; i >= 0; i--) {
			FingerEntry fingerEntry = fingerTable.get(i);
			MonitorNode sucessorFinger = fingerEntry.getSucessorNode(); 
			if(sucessorFinger.getHashID().compareTo(this.monitor.getHashID()) > 0 && sucessorFinger.getHashID().compareTo(hashKey) < 0){
				return sucessorFinger;
			}
		}
		
		return this.nextNodeInChordRing;
	}
	
	public Set<Rules> getSupervisedRules() {
		return supervisedRules;
	}

	public void setSupervisedRules(Set<Rules> supervisedRules) {
		this.supervisedRules = supervisedRules;
	}

	public List<FingerEntry> getFingerTable() {
		return fingerTable;
	}

	public void setFingerTable(List<FingerEntry> fingerTable) {
		this.fingerTable = fingerTable;
	}

	public void setMapExternalSignatures(Map<Rules, Set<Signature>> mapExternalMaliciousNode) {
		this.mapExternalSignatures = mapExternalMaliciousNode;
	}

	public Map<Rules, Set<Signature>> getMapExternalSignatures() {
		return mapExternalSignatures;
	}

	public void addExternalSignature(Signature signature) {
		Rules rule = signature.getRule();
		Set<Signature> signatures; 
		if (!mapExternalSignatures.containsKey(rule)) { 
			signatures = new HashSet<Signature>();
		}else{
			signatures = mapExternalSignatures.get(rule);
		}
		
		signatures.add(signature);
		mapExternalSignatures.put(rule, signatures);
		
		//check whether the buffer is full 
		if (signatures.size() >= SIGNATURES_BUFFER) {
			
			this.monitor.correlate(signatures);
			
			//clear the buffer for the rule which is being correlated
			mapExternalSignatures.remove(rule);
		}
	}
	
	public Set<Signature> getSignatures(Rules rule){
		if(mapExternalSignatures.containsKey(rule)){
			return mapExternalSignatures.get(rule);
		}
		
		return null;
	}

	public void addSupervisedRules(Rules rule) {
		this.supervisedRules.add(rule);
	}

	public void removeSupervisedRule(Rules rule) {
		this.supervisedRules.remove(rule);
	}

	public MonitorNode getMonitor() {
		return monitor;
	}

	public void setMonitor(MonitorNode monitor) {
		this.monitor = monitor;
	}

	public MonitorNode getNextNodeInChordRing() {
		return nextNodeInChordRing;
	}

	public void setNextNodeInChordRing(MonitorNode nextNodeInChordRing) {
		this.nextNodeInChordRing = nextNodeInChordRing;
	}

	public MonitorNode getPreviousNodeInChordRing() {
		return previousNodeInChordRing;
	}

	public void setPreviousNodeInChordRing(MonitorNode previousNodeInChordRing) {
		this.previousNodeInChordRing = previousNodeInChordRing;
	}

	public Integer getHashID() {
		return hashID;
	}

	public void setHashID(Integer hashID) {
		this.hashID = hashID;
	}
}
