package projects.ids_wsn.nodeDefinitions.Monitor.decorator;

import java.util.ArrayList;
import java.util.List;
import projects.ids_wsn.comparators.IntervalComparator;
import projects.ids_wsn.nodeDefinitions.Monitor.DataMessage;
import projects.ids_wsn.nodeDefinitions.Monitor.IMonitor;
import projects.ids_wsn.nodeDefinitions.Monitor.Rules;
import sinalgo.configuration.Configuration;
import sinalgo.configuration.CorruptConfigurationEntryException;
import sinalgo.nodes.Node;
import sinalgo.tools.Tools;

public class IntervalRule extends RulesDecorator {
	
	private Integer timerMin = 0;
	private Integer timerMax = 0;

	public IntervalRule(IMonitor monitor) {
		super(monitor);
		
		try {
			String min = Configuration.getStringParameter("Monitor/Rules/Interval/Min");
			timerMin = Integer.valueOf(min);
			
			String max = Configuration.getStringParameter("Monitor/Rules/Interval/Max");
			timerMax = Integer.valueOf(max);
			
		} catch (CorruptConfigurationEntryException e) {
			Tools.appendToOutput("Min or Max Keys not found");
			e.printStackTrace();
		}
	}
	
	@Override
	public void doInference() {
		applyIntervalRule();
		super.doInference();
	}

	private void applyIntervalRule() {
		Integer tamBuffer = getDataMessage().size();
		List<Node> listTempNodes = new ArrayList<Node>();
		List<DataMessage> listTempDataMessage = new ArrayList<DataMessage>();
		DataMessage data1;
		DataMessage data2;
		IntervalComparator comp = new IntervalComparator(timerMin, timerMax);
	
		for (int x=0; x<tamBuffer-1;x++){
			data1 = getDataMessage().get(x);
			
			//We have to analyze just the messages that were created by a neighboring node
			if (!data1.getSource().equals(data1.getImediateSrc())){
				continue;
			}
			
			if (listTempDataMessage.contains(data1)){
				continue;
			}
			
			for (int y=x+1; y<tamBuffer;y++){
				data2 = getDataMessage().get(y);
				
				//We have to analyze just the messages that were created by a neighboring node
				if (!data2.getSource().equals(data2.getImediateSrc())){
					continue;
				}
				
				if (comp.compare(data1, data2) != 0){
					listTempDataMessage.add(data1);
					break;
				}
			}	
		}
		
		listTempNodes = getNodes(listTempDataMessage);
		setLocalMaliciousList(Rules.INTERVAL, listTempNodes);		
	}
	
	private List<Node> getNodes(List<DataMessage> listDataMessages){
		Node n;
		ArrayList<Node> listNodes = new ArrayList<Node>();
		
		for (DataMessage data : listDataMessages){
			n = Tools.getNodeByID(data.getSource());
			
			if (!listNodes.contains(n)){
				listNodes.add(n);
			}
		}
		return listNodes;
	}

}
