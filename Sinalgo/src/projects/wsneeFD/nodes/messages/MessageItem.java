package projects.wsneeFD.nodes.messages;

import sinalgo.nodes.Node;

/**
 * Classe que representa itens de uma mensagem enviada a um (ou mais) sensor(es) <p>
 * [Eng] Class that represents items of a message sent to one (or more) sensor(s)
 * 
 * @author Fernando Rodrigues
 * 
 */

public class MessageItem {
	public Node sourceNode;
	DataRecordItens dataRecordItens;
	
	public MessageItem(Node source, DataRecordItens dRItens) {
		sourceNode = source;
		dataRecordItens = dRItens;
	}
	
	public MessageItem() {
		super();
	}

	public void add(DataRecord dr, int sWindSize) {
		dataRecordItens.add(dr, sWindSize);
	}
	public DataRecordItens getDataRecordItens() {
		return dataRecordItens;
	}
	
} // end class MessageItem
