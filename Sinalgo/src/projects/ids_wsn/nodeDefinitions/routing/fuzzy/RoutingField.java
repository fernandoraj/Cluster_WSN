package projects.ids_wsn.nodeDefinitions.routing.fuzzy;

import sinalgo.nodes.Node;

public class RoutingField {
	private Integer sequenceNumber;
	private Integer numHops;
	private Node nextHop;
	private Boolean active;
	private Double fsl;
	private Integer index;
	
	public RoutingField(Integer seq, Integer numHops, Node nextHop, Boolean active, Double fsl, Integer index){
		this.sequenceNumber = seq;
		this.numHops = numHops;
		this.nextHop = nextHop;
		this.active = active;
		this.fsl = fsl;
		this.index = index;
	}

	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public void setNumHops(Integer numHops) {
		this.numHops = numHops;
	}

	public void setNextHop(Node nextHop) {
		this.nextHop = nextHop;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public void setFsl(Double fsl) {
		this.fsl = fsl;
	}

	public Integer getNumHops() {
		return numHops;
	}

	public Node getNextHop() {
		return nextHop;
	}

	public Boolean getActive() {
		return active;
	}

	public Double getFsl() {
		return fsl;
	}

	public Integer getSequenceNumber() {
		return sequenceNumber;
	}

	public Integer getIndex() {
		return index;
	}

}
