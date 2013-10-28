package projects.wsnee.nodes.nodeImplementations;

import java.awt.Color;
import java.util.ArrayList;
import sinalgo.nodes.Node;


public class Cluster //extends ArrayList<SimpleNode>
{
	/**
	 * Maximum distance acceptable to the formation of clusters. If it is equal to zero (0.0), ignoring
	 */
	private double maxDistance = 0.0;

//	static final long serialVersionUID;

	public Cluster(Node clusterHead)
	{
//		this.setColor(Color.RED);
//		serialVersionUID = serial;
		this.head = clusterHead;
		members = new ArrayList<Node>();
	}
	
	public Node head;
	public ArrayList<Node> members;
	
	public void addMember(Node newMember) {
		members.add(newMember);
	}

	public void setMembers(ArrayList<Node> membersList) {
		if (membersList != null) {
			members.clear();
			members.addAll(membersList);
		}
	}

}
