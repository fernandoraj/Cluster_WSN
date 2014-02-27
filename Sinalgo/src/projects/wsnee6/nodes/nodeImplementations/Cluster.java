package projects.wsnee6.nodes.nodeImplementations;

import java.util.ArrayList;

import projects.wsnee6.nodes.nodeImplementations.SimpleNode;


public class Cluster //extends ArrayList<SimpleNode>
{
	/**
	 * Maximum distance acceptable to the formation of clusters. If it is equal to zero (0.0), ignoring <p>
	 * [Eng] A distância máxima aceitável para a formação de clusters. Se for igual a zero (0,0), ignorando
	 */
	private double maxDistance = 0.0;

	/**
	 * Numero de dados sensoreados por time slot (Tamanho do time slot) <p>
	 * [Eng] Number of sensed data per time slot (time slot Size)
	 */
	public Integer sizeTimeSlot = 0;
	
//	static final long serialVersionUID;

	public Cluster(SimpleNode clusterHead)
	{
//		this.setColor(Color.RED);
//		serialVersionUID = serial;
		this.head = clusterHead;
		members = new ArrayList<SimpleNode>();
	}
	
	public SimpleNode head;
	public ArrayList<SimpleNode> members;
	
	public void addMember(SimpleNode newMember) {
		members.add(newMember);
	}

	public void setMembers(ArrayList<SimpleNode> membersList) {
		if (membersList != null) {
			members.clear();
			members.addAll(membersList);
		}
	}

}
