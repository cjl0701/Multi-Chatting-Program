public class Message {
	private String id;
	private String msg;
	private String type; // 메시지 유형(longin, logout, msg, secret)
	private String rcvId; // 귓속말 수신자 아이디
	
	public Message(String id, String msg, String type, String rcvId) {
		super();
		this.id = id;
		this.msg = msg;
		this.type = type;
		this.rcvId = rcvId;
	}
	
	public Message() {
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getRcvId() {
		return rcvId;
	}
	public void setRcvId(String rcvId) {
		this.rcvId = rcvId;
	}
}
