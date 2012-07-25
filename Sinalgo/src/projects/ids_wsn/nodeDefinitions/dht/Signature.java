package projects.ids_wsn.nodeDefinitions.dht;

import java.util.List;

import projects.ids_wsn.nodeDefinitions.Monitor.Rules;
import projects.ids_wsn.nodes.nodeImplementations.MonitorNode;
import sinalgo.nodes.Node;

public class Signature {
	
	/**
	 * Stores the malicious nodes to be sent to the supervisor
	 */
	private List<Node> maliciousNodes;
	
	/**
	 * Rule violated by the malicious list
	 */
	private Rules rule;
	
	/**
	 * Monitor which is sending the malicious list to the supervisor
	 */
	private MonitorNode sender;
	
	/**
	 * Monitor node that must receive the malicious list
	 */
	private MonitorNode supervisor;

	
	public Signature(MonitorNode sender, List<Node> maliciousNodes, Rules rule,  MonitorNode supervisor) {
		this.sender = sender;
		this.maliciousNodes = maliciousNodes;
		this.rule = rule;
		this.supervisor = supervisor;
	}

	public List<Node> getMaliciousNodes() {
		return maliciousNodes;
	}

	public void setMaliciousNodes(List<Node> maliciousNodes) {
		this.maliciousNodes = maliciousNodes;
	}

	public MonitorNode getSender() {
		return sender;
	}

	public void setSender(MonitorNode sender) {
		this.sender = sender;
	}

	public MonitorNode getSupervisor() {
		return supervisor;
	}

	public void setSupervisor(MonitorNode supervisor) {
		this.supervisor = supervisor;
	}

	public Rules getRule() {
		return rule;
	}

	public void setRule(Rules rule) {
		this.rule = rule;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((rule == null) ? 0 : rule.hashCode());
		result = prime * result + ((sender == null) ? 0 : sender.hashCode());
		result = prime * result
				+ ((supervisor == null) ? 0 : supervisor.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Signature other = (Signature) obj;
		if (rule == null) {
			if (other.rule != null)
				return false;
		} else if (!rule.equals(other.rule))
			return false;
		if (sender == null) {
			if (other.sender != null)
				return false;
		} else if (!sender.equals(other.sender))
			return false;
		if (supervisor == null) {
			if (other.supervisor != null)
				return false;
		} else if (!supervisor.equals(other.supervisor))
			return false;
		return true;
	}
	
	
}
