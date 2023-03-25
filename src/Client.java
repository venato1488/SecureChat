
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.Set;

public class Client {
	
	private Socket socket;
	private ObjectInputStream in; // to read messages from server
	private ObjectOutputStream out;
	private String username;
	private LinkedHashMap<String, Member> memberList;

	public Client(Socket socket, String username) {
		try {
			this.socket = socket;
			this.in = new ObjectInputStream(socket.getInputStream());
			this.out = new ObjectOutputStream(socket.getOutputStream());
			this.username = username;
			
			
			
		
		} catch (IOException e) {
			e.printStackTrace();
			closeEverything(socket, in, out);
		}
	}
	
	public void sendMessage() {
		try {
			//Because at first connection client has to enter username first
			out.writeObject(new Message(username, null));			
			out.flush();
			
			while(socket.isConnected()) {
				Scanner scanner = new Scanner(System.in);
				String messageToSend = scanner.nextLine();// after user press enter it will be captured in messageToSend
				if (messageToSend.length() != 0) {
					if (messageToSend.startsWith("/p ")) {
						sendPrivateMessage(messageToSend);
					} else if (messageToSend.equals("/members")) {
						System.out.println(memberList);
					} else {
						out.writeObject(new Message(username, messageToSend));
						out.flush();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			closeEverything(socket, in, out);
		}
	}
	
	public void sendPrivateMessage(String msgToSend) {
		String[] msgParts = msgToSend.split(" ");
		String recipient = msgParts[1];
		String content = msgToSend.substring(3 + recipient.length()+1);
		if (recipientExist(recipient)) {
			try {
				out.writeObject(new PrivateMessage(username, content, recipient));
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Recipient doesn't exist! Remember, to send a private message type /p <Recipient> <Message>");
		}
		
	}
	
	public boolean recipientExist(String recipient) {
		Set<String> memberSet = memberList.keySet();
		for (String member : memberSet) {
			if(recipient.equals(member)) return true;
		}
		return false;
	}
	
	public void listenForMessage() {
		new Thread(new Runnable() {
			// Anonymous thread
			
			@Override
			public void run() {
				Object msgFromChat;
				
				while (socket.isConnected()) {
					
					try {
						msgFromChat = in.readObject(); // read serialized data from ClientHandler
						if (msgFromChat instanceof LinkedHashMap) {
							
							@SuppressWarnings("unchecked")
							LinkedHashMap<String, Member> memberMap = (LinkedHashMap<String, Member>) msgFromChat;
							updateMemberMap(memberMap);
							
						} else if (msgFromChat instanceof PrivateMessage) {
							PrivateMessage privMessage = (PrivateMessage) msgFromChat;
							printPrivateMessage(privMessage);
						} else if (msgFromChat instanceof Message) {
							Message message = (Message) msgFromChat; // casting object to its original type
							printBroadcastMessage(message);
						}
					} catch (IOException e1) {
						e1.printStackTrace();
						closeEverything(socket, in, out);
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}				
			}			
		}).start();			
	}
	
	
	
	public synchronized void updateMemberMap(LinkedHashMap<String, Member> newMemberList) {
		memberList = newMemberList;
	}
	
	public void printBroadcastMessage(Message msg) {
		System.out.println("["+timeStampFormatter(msg.getTimestamp())+"] "+msg.getSender()+": "+msg.getContent());
	}
	
	public void printPrivateMessage(PrivateMessage pMsg) {
		System.out.println("["+timeStampFormatter(pMsg.getTimestamp())+"] "+"Private message from " + pMsg.getSender() + ": " + pMsg.getContent());
	}	
	
	public String timeStampFormatter(LocalDateTime timestamp) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
		String formattedTimestamp = timestamp.format(formatter);
		return formattedTimestamp;
		
	}
	public void closeEverything(Socket socket, ObjectInputStream in2, ObjectOutputStream out2) {
		try {
			// Checking for null pointer and also only outer wrapper of stream is closed so inner are closing as well same for socket 
			if (socket != null) {
				socket.close();
			}
			if (in2 != null) {
				in2.close();
			}
			if (out2 != null) {
				out2.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isValidUsername(String username) {
		if(username.length()<3) {
			return false;
		}
		for(int i=0; i < username.length(); i++) {
			char c = username.charAt(i);
			if (c == '!' || c == '@' || c == '#' || c == '$' || c == '%' || c == '^' ||
		        c == '&' || c == '*' || c == '(' || c == ')' || c == '+' || c == '-' || c == '=') {
				return false;
			}
		}		
		return true;
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter your username: ");
		// TODO check if username is unique
		String username = scanner.nextLine();
		Socket socket = new Socket("localhost", 9999);
		Client client = new Client(socket, username);
		client.listenForMessage();
		client.sendMessage();
	}
}
