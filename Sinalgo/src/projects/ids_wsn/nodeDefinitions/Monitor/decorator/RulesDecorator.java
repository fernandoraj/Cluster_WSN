package projects.ids_wsn.nodeDefinitions.Monitor.decorator;

import java.util.List;

import projects.ids_wsn.nodeDefinitions.Monitor.DataMessage;
import projects.ids_wsn.nodeDefinitions.Monitor.IMonitor;
import projects.ids_wsn.nodeDefinitions.Monitor.Rules;
import sinalgo.nodes.Node;

public class RulesDecorator implements IMonitor {
	
	private IMonitor monitor;

	public void doInference() {
		monitor.doInference();

	}
	
	public RulesDecorator(IMonitor monitor) {
		this.monitor = monitor;		
	}

	public List<DataMessage> getDataMessage() {
		return monitor.getDataMessage();
	}

	public void setLocalMaliciousList(Rules rule, List<Node> lista) {
		monitor.setLocalMaliciousList(rule, lista);		
	}

	public Integer getMonitorID() {
		return monitor.getMonitorID();
	}
	
	

}
