package projects.ids_wsn.nodeDefinitions.dht;

import java.util.List;
import java.util.Map;
import java.util.Set;

import projects.ids_wsn.nodeDefinitions.Monitor.Rules;
import projects.ids_wsn.nodes.nodeImplementations.FingerEntry;
import projects.ids_wsn.nodes.nodeImplementations.MonitorNode;

public interface IDHT {
	
	public MonitorNode getNextNodeInChordRing();
	
	public void setNextNodeInChordRing(MonitorNode nextNodeInChordRing);

	public MonitorNode getPreviousNodeInChordRing();
	
	public void setPreviousNodeInChordRing(MonitorNode previoiusNodeInChordRing);
	
	public void createFingerTable();
	
	public Integer getStart(int index);

	public MonitorNode findSucessor(Integer hashKey);
	
	public MonitorNode findClosestPredecessor(Integer hashKey);

	public List<FingerEntry> getFingerTable();

	public void setFingerTable(List<FingerEntry> fingerTable);

	public void setMapExternalSignatures(Map<Rules, Set<Signature>> mapExternalSignatures);

	public Map<Rules, Set<Signature>> getMapExternalSignatures();

	public void addExternalSignature(Signature signature);
	
	public Set<Signature> getSignatures(Rules rule);
	
	public Set<Rules> getSupervisedRules();

	public void setSupervisedRules(Set<Rules> supervisedRules);

	public void addSupervisedRules(Rules rule);

	public void removeSupervisedRule(Rules rule);

	public MonitorNode getMonitor();

	public void setMonitor(MonitorNode monitor);
	
	public Integer getHashID();

	public void setHashID(Integer hashID);

}
