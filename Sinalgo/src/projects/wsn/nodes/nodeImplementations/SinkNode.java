package projects.wsn.nodes.nodeImplementations;

import java.awt.Color;
import java.awt.Graphics;

import projects.wsn.nodes.messages.WsnMsg;
import projects.wsn.nodes.timers.WsnMessageTimer;
import sinalgo.gui.transformation.PositionTransformation;


public class SinkNode extends SimpleNode 
{

	public SinkNode()
	{
		super();
		this.setColor(Color.RED);
	}

	@Override
	public void draw(Graphics g, PositionTransformation pt, boolean highlight) {
		String text = "S";
		super.drawNodeAsSquareWithText(g, pt, highlight, text, 1, Color.WHITE);
	}
	
	@NodePopupMethod(menuText="Definir Sink como Raiz de Roteamento")
	public void construirRoteamento(){
		this.proximoNoAteEstacaoBase = this;
		//WsnMsg wsnMessage = new WsnMsg(1, this, null, this, 0); //Integer seqID, Node origem, Node destino, Node forwardingHop, Integer tipo
		WsnMsg wsnMessage = new WsnMsg(1, this, null, this, 0, 100, "t"); 
		WsnMessageTimer timer = new WsnMessageTimer(wsnMessage);
		timer.startRelative(1, this);
	}
	
}
