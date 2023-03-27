import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String sender;
	private String recipient;
	private LocalDateTime timestamp;
	private String content;
	
	public Message(String sender, String content) {
		this.sender = sender;
		this.timestamp = LocalDateTime.now();
		this.content = content;
	}
	
	public String getSender() {
		return sender;
	}
	
	public String getRecipient() {
		return recipient;
	}
	
	public String getContent() {
		return content;
	}
	
	public LocalDateTime getTimestamp() {
		return timestamp;
	}
}
