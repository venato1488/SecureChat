import java.io.Serializable;
import java.time.LocalDateTime;

public class Message implements Serializable, MessageInterface{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L; //This will help maintain compatibility between serialized and deserialized objects.
	private String sender;
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
		
	public String getContent() {
		return content;
	}
	
	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	@Override
	public void setContent(String newContent) {
		this.content = newContent;
	}

	@Override
	public void setSender(String sender) {
		this.sender = sender;
	}
}
