package projects.ids_wsn.nodeDefinitions.Monitor.decorator;

import java.util.ArrayList;
import java.util.List;

import projects.ids_wsn.nodeDefinitions.Monitor.DataMessage;
import projects.ids_wsn.nodeDefinitions.Monitor.IMonitor;
import projects.ids_wsn.nodeDefinitions.Monitor.Rules;
import sinalgo.nodes.Connections;
import sinalgo.nodes.Node;
import sinalgo.nodes.edges.Edge;
import sinalgo.tools.Tools;
import sinalgo.tools.storage.ReusableListIterator;

public class TransmissionRangeRule extends RulesDecorator {

	public TransmissionRangeRule(IMonitor monitor) {
		super(monitor);
	}
	
	@Override
	public void doInference() {
		applyTransmissionRangeRule();
		super.doInference();
	}

	private void applyTransmissionRangeRule() {
		List<Node> listNodes = new ArrayList<Node>();
		for (DataMessage dataMessage : getDataMessage()){
			Integer nodeImedSrcID = dataMessage.getImediateSrc();
			
			Node monitor = Tools.getNodeByID(getMonitorID());
			
			Connections conn = monitor.outgoingConnections;
			ReusableListIterator<Edge> listConn = conn.iterator();
			listConn.reset();
			
			while(listConn.hasNext()){
				Edge e = listConn.next();
				if (nodeImedSrcID.equals(e.endNode.ID)){
					listNodes.add(e.endNode);
					break;
				}
			}			
		}
		
		setLocalMaliciousList(Rules.TRANSMISSION_RANGE, listNodes);
	}

}
