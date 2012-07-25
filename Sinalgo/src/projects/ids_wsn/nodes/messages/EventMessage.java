package projects.ids_wsn.nodes.messages;

import sinalgo.nodes.messages.Message;

public class EventMessage extends Message {
	public Integer value = 0;
	
	public EventMessage(Integer value) {
		this.value = value;
	}

	@Override
	public Message clone() {
		return this;
	}

}
