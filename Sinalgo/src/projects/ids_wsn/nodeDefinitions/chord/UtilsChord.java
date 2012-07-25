package projects.ids_wsn.nodeDefinitions.chord;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import projects.ids_wsn.nodeDefinitions.BasicNode;
import projects.ids_wsn.nodes.nodeImplementations.BaseStation;
import projects.ids_wsn.nodes.nodeImplementations.MonitorNode;
import projects.ids_wsn.nodes.timers.ChordDelayTimer;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.nodes.Node;
import sinalgo.nodes.TimerCollection;
import sinalgo.nodes.timers.Timer;
import sinalgo.runtime.nodeCollection.NodeCollectionInterface;
import sinalgo.tools.Tools;

public class UtilsChord {
	
	public static Integer CHORD_RING_SIZE;

	static{
		try {
			CHORD_RING_SIZE = Configuration.getIntegerParameter("Monitor/Chord/Ring/Size");
		} catch (CorruptConfigurationEntryException e) {
			String errorMessage = "Key Monitor/Inference/InternalBuffer not found";
			System.out.println(errorMessage);
			Tools.appendToOutput(errorMessage);
			e.printStackTrace();
		}
	}
	
	/**
	 * Generate the SHA-1 hash number with max size according to the value of the
	 * Chord network size stored in the <code>Config.xml</code> file.
	 * of the Chord Protocol.
	 * @param key (<code>String</code>) to be hashed
	 * @return An <code>Integer</code>  containing the hash of the key passed
	 */
	public static Integer generateSHA1(String key){

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			
			byte[] hashMsg = md.digest(key.toString().getBytes());
			
			BigInteger bi = new BigInteger(hashMsg).abs();
			
			Integer networkSize = new Double(Math.pow(2, CHORD_RING_SIZE)).intValue();
			BigInteger mod = new BigInteger(networkSize.toString());

			return bi.mod(mod).intValue();
			
		} catch (Exception e) {
			String errorMessage = "An error has ocurred when trying to generate a SHA-1 hash number for the key: " + key +
					" with max size of: " + CHORD_RING_SIZE + " bits.";
			
			Tools.appendToOutput(errorMessage);
			System.out.println(errorMessage);
			
			return null;
		}
	}
	
	/**
	 * Generate the SHA-1 hash number with max size according to the value of the
	 * Chord network size stored in the <code>Config.xml</code> file.
	 * of the Chord Protocol.
	 * @param key (<code>Integer</code>) to be hashed
	 * @return An <code>Integer</code>  containing the hash of the key passed
	 */
	public static Integer generateSHA1(Integer key){
		return generateSHA1(key.toString());
	}

	
	/**
	 * According to Chord Protocol, the monitors must reference its following monitor
	 * in the Chord Ring
	 * @return 
	 * 
	 */
	public static List<MonitorNode> createMonitorsRing(List<MonitorNode> monitorNodes){
		if(monitorNodes.isEmpty()){
			String message = "There are no Monitor Nodes in this Network.";
			Tools.appendToOutput(message);
			System.out.println(message);
			return null;
		}

		sortMonitors(monitorNodes);
		
		linkMonitors(monitorNodes);
		
		return monitorNodes;
	}

	public static List<MonitorNode> getAliveMonitorNodes() {
		NodeCollectionInterface nodes = Tools.getNodeList();
		List<MonitorNode> aliveMonitors = new ArrayList<MonitorNode>();
		
		for (Node node : nodes) {
			if (node instanceof MonitorNode) {
				MonitorNode monitor = (MonitorNode) node;
				if (!monitor.getIsDead()) {
					aliveMonitors.add((MonitorNode) node);
				}
			}
		}
		return aliveMonitors;
	}
	
	private static void sortMonitors(List<MonitorNode> monitorNodes) {
		//sort the monitors in ascendent order of ID hash
		Collections.sort(monitorNodes, new Comparator<MonitorNode>() {
			@Override
			public int compare(MonitorNode m1, MonitorNode m2) {
				Integer m1ID = m1.getHashID();
				Integer m2ID = m2.getHashID();
				return m1ID.compareTo(m2ID);
			}
		});
	}

	private static void linkMonitors(List<MonitorNode> monitorNodes) {
		for (int i = 0; i < monitorNodes.size(); i++) {
			MonitorNode monitor = monitorNodes.get(i);

			Integer nextIndex = (i + 1) % monitorNodes.size();
			Integer previousIndex = (monitorNodes.size() - 1 + i) % monitorNodes.size();

			MonitorNode nextMonitor = monitorNodes.get(nextIndex);
			monitor.getDht().setNextNodeInChordRing(nextMonitor);
			
			MonitorNode previousMonitor = monitorNodes.get(previousIndex);
			monitor.getDht().setPreviousNodeInChordRing(previousMonitor);
		}
	}
	
	public static void createFingerTables(List<MonitorNode> monitorNodes){
		BaseStation.IS_FINGER_TABLE_CREATED = true;
		
		if(hasNodeWithTheSameHashID(monitorNodes)){
			throw new RuntimeException("There are two monitor nodes with the same HASH ID." +
						"\ntry to increase the Chord/Ring/Size in the config.xml file.");
		}
		
		UtilsChord.createMonitorsRing(monitorNodes);
		
		System.out.println("\nCREATING FINGER TABLES FOR "+monitorNodes.size() + " NODES");
		for (MonitorNode monitorNode : monitorNodes) {
			monitorNode.getDht().getFingerTable().clear();

			System.out.println("Creating finger tables for node " + monitorNode.ID + " (hash=" + monitorNode.getHashID() + ")");
			
			monitorNode.getDht().createFingerTable();
		}
	}

	public static boolean hasNodeWithTheSameHashID(List<MonitorNode> monitorNodes) {
		for (MonitorNode monitor : monitorNodes) {
			int count = 0;
			for (MonitorNode monitorNode : monitorNodes) {
				if (monitor.getHashID() == monitorNode.getHashID()) {
					count++;
				}
			}
			if (count > 1) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasNodeWithTheSameHashID(Integer hashID) {
		for (MonitorNode monitor : getAliveMonitorNodes()) {
			if (monitor.getHashID().compareTo(hashID) == 0) {
				return true;
			}
		}
		return false;
	}

	public static void removeMonitorFromLists(MonitorNode monitor) {
		NodeCollectionInterface nodes = Tools.getNodeList();
		for (Node node : nodes) {
			if (node instanceof BasicNode) {
				BasicNode basicNode = (BasicNode) node;
					basicNode.monitors.remove(monitor);
			}
		}
	}

	public static void removeTimers() {
		NodeCollectionInterface nodes = Tools.getNodeList();
		for (Node node : nodes) {
			if (node instanceof BasicNode) {
				BasicNode basicNode = (BasicNode) node;
				
				TimerCollection timers = basicNode.getTimers();
				for (Timer timer : timers) {
					if (timer instanceof ChordDelayTimer && basicNode.monitors.size() == 0) {
						timers.remove(timer);
					}
				}
			}
		}
		
	}
	
	public static void gerarLogDeEnergiaPerRound(String fileName) {
		
		Map<Double, Float> mapaRoundEnergySpent = new TreeMap<Double, Float>();
		
		for (Node n : Tools.getNodeList()){
			if (n instanceof BasicNode){
				BasicNode node = (BasicNode)n;
				Hashtable<Double, Float> energyPerRound = node.getBateria().getEnergyPerRound();
				for (Double round : energyPerRound.keySet()) {
					Float newValue = energyPerRound.get(round);
					
					if (mapaRoundEnergySpent.containsKey(round)) {
						Float lastValue = mapaRoundEnergySpent.get(round);
						mapaRoundEnergySpent.put(round, lastValue + newValue);
					}else{
						mapaRoundEnergySpent.put(round, newValue);
					}
				}
			}
		}
		
		StringBuilder text = new StringBuilder("Round;Energia\n");
		Float energiaGastaTotal = new Float("0"); 
		for (Double round : mapaRoundEnergySpent.keySet()) {
			Float energySpent = mapaRoundEnergySpent.get(round);
			
			energiaGastaTotal+=energySpent;
			
			text.append(round.intValue() + ";" + new DecimalFormat(",###.00").format(energySpent) +"\n");
		}
		
		
		String msg = "total gasto: "+energiaGastaTotal;
		Tools.appendToOutput("\n"+msg);
		System.out.println(msg);
		
		File file = new File("D:\\DOCUMENTOS\\ANALISE E DESENVOLVIMENTO DE SISTEMAS\\MOD V\\Analise de sistemas IFPI\\MARVIN\\MONOGRAFIA\\resultados das simulações\\"+fileName);
		FileWriter f = null;
		try {
			f = new FileWriter(file);
		} catch (IOException e) {
			System.out.println("erro ao criar arquivo");
			e.printStackTrace();
		}
		BufferedWriter bw= new BufferedWriter(f);
		
		try {
			bw.write(text.toString());
		} catch (IOException e) {
			System.out.println("erro ao escrever no arquivo");
			e.printStackTrace();
		}
		
		try {
			bw.close();
		} catch (IOException e) {
			System.out.println("erro ao tentar fechar o arquivo");
			e.printStackTrace();
		}
	}
}
