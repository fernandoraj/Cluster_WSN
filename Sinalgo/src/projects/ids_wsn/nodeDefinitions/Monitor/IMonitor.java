package projects.ids_wsn.nodeDefinitions.Monitor;

import java.util.List;

import sinalgo.nodes.Node;

public interface IMonitor {
	public void doInference();
	public List<DataMessage> getDataMessage();
	public void setLocalMaliciousList(Rules rule, List<Node> lista);
	public Integer getMonitorID();
}
