public class MessageFactory {
    
    public enum MessageType {
        USERNAME, BROADCAST, PRIVATE, SERVER
    }

    public static MessageInterface createMessage(MessageType type, String sender, String content, String recipient) {
        switch (type) {
            case USERNAME:
                return new Message(sender, " ");
            case BROADCAST:
                return new Message(sender, content);
            case PRIVATE:
                if (recipient == null) {
                    throw new IllegalArgumentException("Recipient is required for private messages.");
                }
                return new PrivateMessage(sender, content, recipient);
            case SERVER:
                return new Message("SERVER", content);
            default:
                throw new IllegalArgumentException("Unknown message type: " + type);
        }
    }
}
