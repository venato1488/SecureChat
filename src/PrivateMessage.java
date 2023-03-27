
public class PrivateMessage extends Message{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String recipient;
	
	public PrivateMessage(String sender, String content, String recipient) {
		super(sender, content);
		this.recipient = recipient;
	}
	
	public String getRecipient() {
		return recipient;
	}
}
