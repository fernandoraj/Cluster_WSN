package projects.wsn2.nodes.timers;

import projects.wsn2.nodes.messages.wsnMsg;
import sinalgo.nodes.timers.Timer;
import projects.wsn2.nodes.nodeImplementations.simpleNode;



public class Wsn2MessageTimer extends Timer{
	private wsnMsg message = null;
	public Wsn2MessageTimer(wsnMsg message){
	this.message = message; }
	
	
	@Override
	public void fire() {
	((simpleNode)node).broadcast(message); }
	}
	

