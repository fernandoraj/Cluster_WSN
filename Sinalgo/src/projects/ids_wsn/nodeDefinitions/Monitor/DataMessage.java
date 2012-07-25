package projects.ids_wsn.nodeDefinitions.Monitor;



public class DataMessage {
	
	private Integer clock;
	private Integer source;
	private Integer finalDst;
	private Integer idMsg;
	private Integer imediateSrc;
	private Integer imediateDst;
	private Integer data;
	
	
	public Integer getClock() {
		return clock;
	}
	public void setClock(Integer clock) {
		this.clock = clock;
	}
	public Integer getSource() {
		return source;
	}
	public void setSource(Integer source) {
		this.source = source;
	}
	public Integer getFinalDst() {
		return finalDst;
	}
	public void setFinalDst(Integer finalDst) {
		this.finalDst = finalDst;
	}
	public Integer getIdMsg() {
		return idMsg;
	}
	public void setIdMsg(Integer idMsg) {
		this.idMsg = idMsg;
	}
	public Integer getImediateSrc() {
		return imediateSrc;
	}
	public void setImediateSrc(Integer imediateSrc) {
		this.imediateSrc = imediateSrc;
	}
	public Integer getImediateDst() {
		return imediateDst;
	}
	public void setImediateDst(Integer imediateDst) {
		this.imediateDst = imediateDst;
	}
	public Integer getData() {
		return data;
	}
	public void setData(Integer data) {
		this.data = data;
	}
	
	@Override
	public boolean equals(Object obj) {
		DataMessage dataMsg = (DataMessage) obj;
		
		//TODO: devemos colocar o Clock para diferenciar as mensagens?
		if (dataMsg.idMsg.equals(this.idMsg) && dataMsg.imediateSrc.equals(this.imediateSrc) &&
				dataMsg.imediateDst.equals(this.imediateDst) && dataMsg.source.equals(this.source)){
			return true;
		}else{
			return false;
		}
	}
	
	@Override
	public String toString() {
		return "idMsg: "+idMsg+" - imediateSrc: "+imediateSrc+" - imediateDst: "+imediateDst;
	}

}
