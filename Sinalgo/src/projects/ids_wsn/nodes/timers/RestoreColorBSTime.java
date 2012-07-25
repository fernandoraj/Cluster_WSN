package projects.ids_wsn.nodes.timers;

import java.awt.Color;
import sinalgo.nodes.timers.Timer;

public class RestoreColorBSTime extends Timer {

	@Override
	public void fire() {
		node.setColor(Color.BLACK);
	}

}
