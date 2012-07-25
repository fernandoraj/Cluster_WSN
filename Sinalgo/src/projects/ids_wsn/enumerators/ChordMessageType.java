package projects.ids_wsn.enumerators;

public enum ChordMessageType {
	// a partir do numero 101
	ANSWER_MONITOR_ID(101),//informa à baseStation que o no é monitor
	SEND_TO_SUPERVISOR(102),//informa que as mensagens estao sendo enviadas ao supervisor(signature)
	NOTIFY_NEIGHBORS(103),//notifica os vizinhos do monitor que ele é supervisor
	MONITOR_DOWN(104),//notifica à baseStation que um supervisor morreu ou está fora da rede
	FINGER_TABLE_RECEIVED(105);//quando os nós monitores recebem suas finger table eles devem confirmar à estação base que receberam
	
	private Integer value;
	
	private ChordMessageType(Integer value) {
		this.value = value;
	}
	
	public Integer getValue() {
		return value;
	}
	
	public static Integer getMaxValue(){
		return ChordMessageType.values()[0].getValue();
	}
	
	public static Integer getMinValue(){
		return ChordMessageType.values()[ChordMessageType.values().length - 1].getValue();
	}
}
