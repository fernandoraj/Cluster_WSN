package projects.ids_wsn;

//import net.sourceforge.jFuzzyLogic.FIS;
//import net.sourceforge.jFuzzyLogic.FunctionBlock;
import projects.ids_wsn.nodeDefinitions.energy.IEnergy;
import projects.ids_wsn.nodeDefinitions.energy.simple.SimpleEnergy;
import projects.ids_wsn.nodeDefinitions.routing.IRouting;
import projects.ids_wsn.nodes.timers.RestoreColorTime;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.nodes.Connections;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.tools.Tools;
import sinalgo.tools.logging.Logging;
import sinalgo.tools.storage.ReusableListIterator;

public class Utils {
	
	public static IRouting StringToRoutingProtocol(String name){
		IRouting routing = null;
		
		try {
			Class<?> classe = Class.forName(name);
			try {
				routing = (IRouting) classe.newInstance();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return routing;
	}
	
	public static IEnergy StringToEnergyModel(String name){
		IEnergy energy = null;
		if (name.contains("Simple")){
			energy = new SimpleEnergy();
		}
		return energy;
	}
	
	public static void restoreColorNodeTimer(Node node, Integer time){
		RestoreColorTime restoreColorTime = new RestoreColorTime();
		restoreColorTime.startRelative(time, node);
	}
	
	public static Double calculateFsl(Float energy, Integer numHops){
		Double fsl = 0d;
//		
//		String fileName = "fcl/routing.fcl";
//		FIS fis = FIS.load(fileName, true);
//		
//		if (fis == null){
//			System.err.println("Can't load file: '" + fileName + "'");
//			new Exception();
//		}
//		
//		//show rules set
//		FunctionBlock fb = fis.getFunctionBlock(null);
//		
//		//set inputs
//		fb.setVariable("energy", energy);
//		fb.setVariable("path_length", numHops);		
//		
//		fb.evaluate();
//		
//		fsl = fb.getVariable("psl").defuzzify();
		
		return fsl;
	}
	
	public static Logging getGeneralLog(){
		String logFile = "";
		String simulationName = getSimulationName();
		try {
			logFile = Configuration.getStringParameter("LogFiles/General");
			logFile = logFile+"_"+simulationName+".log";
		} catch (CorruptConfigurationEntryException e) {
			Tools.appendToOutput("General log file not found");
			e.printStackTrace();
		}
		return Logging.getLogger(logFile);
	}
	
	public static Logging getDeadNodesLog(){
		String logFile = "";
		String simulationName = getSimulationName();
		try {
			logFile = Configuration.getStringParameter("LogFiles/DeadNodes");
			logFile = logFile+"_"+simulationName+".log";
		} catch (CorruptConfigurationEntryException e) {
			Tools.appendToOutput("Dead Nodes log file not found");
			e.printStackTrace();
		}
		return Logging.getLogger(logFile);
	}
	
	public static String getSimulationName(){
		String name = "";
		try {
			name = Configuration.getStringParameter("SimulationName");
		} catch (CorruptConfigurationEntryException e) {
			Tools.appendToOutput("Simulation Name entry not found");
			e.printStackTrace();
		}
		return name;
		
	}
	
	/**
	 * 
	 * @param ori
	 * @param dst
	 * @return
	 */
	public static Boolean isNeighboringNode(Node ori, Node dst){
		
		Boolean result = Boolean.FALSE;
		
		Node n = null;
		Edge e = null;
		Connections conn = ori.outgoingConnections;
		ReusableListIterator<Edge> listConn = conn.iterator();
		
		while (listConn.hasNext()){
			e = listConn.next();
			n = e.endNode;
			
			if (n.equals(dst)){
				result = Boolean.TRUE;
			}
		}
		
		return result;
	}
	
	public static Boolean isMultiPathBalanced(){
		Boolean balanced = Boolean.FALSE;
		try {
			String multiPathBalanced = Configuration.getStringParameter("NetworkLayer/MultiPathBalanced");
			if (multiPathBalanced.equals("yes")){
				balanced = Boolean.TRUE;				
			}
		} catch (CorruptConfigurationEntryException e) {
			Tools.appendToOutput("Key NetworkLayer/MultiPathBalanced not found");
			e.printStackTrace();
		}
		return balanced;
	}
}
