package projects.ids_wsn.nodes.nodeImplementations;


public class FingerEntry {
	private Integer nodeID;
	private Integer index;
	private Integer startHash;
	private MonitorNode sucessorNode;
	
	public FingerEntry() {
		
	}

	public FingerEntry(Integer nodeID, Integer index, Integer startHash, MonitorNode sucessorNode) {
		this.nodeID = nodeID;
		this.index = index;
		this.startHash = startHash;
		this.sucessorNode = sucessorNode;
	}

	public Integer getNodeID() {
		return nodeID;
	}

	public void setNodeID(Integer nodeID) {
		this.nodeID = nodeID;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public Integer getStartHash() {
		return startHash;
	}

	public void setStartHash(Integer startHash) {
		this.startHash = startHash;
	}

	public MonitorNode getSucessorNode() {
		return sucessorNode;
	}

	public void setSucessorNode(MonitorNode sucessorNode) {
		this.sucessorNode = sucessorNode;
	}
}
