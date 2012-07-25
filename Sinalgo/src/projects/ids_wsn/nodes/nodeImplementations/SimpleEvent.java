package projects.ids_wsn.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Random;

import projects.ids_wsn.nodeDefinitions.IEvent;
import projects.ids_wsn.nodes.messages.EventMessage;
import sinalgo.configuration.WrongConfigurationException;
import sinalgo.gui.transformation.PositionTransformation;
import sinalgo.nodes.Node;
import sinalgo.nodes.messages.Inbox;
import sinalgo.tools.Tools;

public class SimpleEvent extends Node implements IEvent {
	
	private Integer count = 0;

	@Override
	public void checkRequirements() throws WrongConfigurationException {

	}

	@Override
	public void handleMessages(Inbox inbox) {

	}

	@Override
	public void init() {
		this.setColor(Color.CYAN);
	}

	@Override
	public void neighborhoodChange() {

	}

	@Override
	public void postStep() {

	}

	@Override
	public void preStep() {
		if (Tools.getGlobalTime() % 100 == 0){
			Random rnd = Tools.getRandomNumberGenerator();
			Integer intValue = (1 + (rnd.nextInt() % 100));
			if (intValue >= 90){
				generateEvent();
			}
		}

	}

	public void generateEvent() {
		BaseStation base = (BaseStation) Tools.getNodeByID(1);
		
		if (base.getIsRouteBuild()){
			this.setColor(Color.ORANGE);
			Random 	rnd = Tools.getRandomNumberGenerator();
			Integer rndValue = (1 + (rnd.nextInt() % 100));
			EventMessage eventMsg = new EventMessage(rndValue);
			this.broadcast(eventMsg);
			count = count + 1;
			this.setColor(Color.CYAN);
		}		
	}
	
	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		super.drawAsDisk(g, pt, highlight, 8);
	}

}
