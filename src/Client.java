import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Client {
	
	private Socket socket;
	private ObjectInputStream in; // to read messages from server
	private ObjectOutputStream out;
	private String username;
	private LinkedHashMap<String, Member> memberList;

	public Client(Socket socket, String username) {
		// 
		try {
			this.socket = socket;
			// BufferedInputStream and BufferedOutputStream are used to increase efficiency
			this.in = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
			this.out = new ObjectOutputStream(socket.getOutputStream());
			this.username = username;
			
			
			
		
		} catch (IOException e) {
			e.printStackTrace();
			closeEverything(socket, in, out);
		}
	}
	
	public void sendMessage() {
		// This method is responsible for sending messages to the server
		try {
			//Because at first connection client has to enter username first
			out.writeObject(MessageFactory.createMessage(MessageFactory.MessageType.USERNAME, username, null, null));			
			out.flush();
			
			while(socket.isConnected() && !socket.isClosed()) {
				Scanner scanner = new Scanner(System.in);
				String messageToSend = scanner.nextLine();// after user press enter it will be captured in messageToSend
				if (messageToSend.length() != 0) {
					if (messageToSend.startsWith("/p ")) {
						sendPrivateMessage(messageToSend);
					} else if(messageToSend.startsWith("/kick ")){
						sendKickRequest(messageToSend);
					}else if (messageToSend.equals("/members")) {
						showMembers();
					} else {
						out.writeObject(new Message(username, messageToSend));
						out.flush();
					}
				}
			}
			/*if (socket.isClosed()) {
				System.out.println("Connection closed");
				closeEverything(socket, in, out);
			}*/
		} catch (IOException e) {
			System.out.println("Server closed the connection");
			closeEverything(socket, in, out);
		} catch (NoSuchElementException e) {
			closeEverything(socket, in, out);
		}
	}
	
	

	public void showMembers() {
		// This method is responsible for showing the list of members
    	System.out.println("  ╔════════════════════════════════════════════════════════════╗");
		System.out.println("  ║               [LIST OF ONLINE MEMBERS]                     ║");
		System.out.println("  ║------------------------------------------------------------║");
		System.out.println("  ║  Username        IP Address           Port    Coordinator  ║");
		System.out.println("  ║------------------------------------------------------------║");

		for (String key : memberList.keySet()) {
			Member member = memberList.get(key);
			String username = member.getUsername();
			String ipAddress = member.getIpAddress().toString();
			int port = member.getPort();
			String coordinator = member.getCoordinator() ? "Yes" : "No";

			System.out.format("  ║  %-15s %-20s %-6s  %-12s ║%n", username, ipAddress, port, coordinator);
		}

		System.out.println("  ╚════════════════════════════════════════════════════════════╝");

	}

	public void sendKickRequest(String msgToSend) {
		// This method is responsible for sending kick request to the server 
		String[] msgParts = msgToSend.split(" ");
		String recipient = msgParts[1];
		if (recipientExist(recipient)) {
			try {
				out.writeObject(MessageFactory.createMessage(MessageFactory.MessageType.PRIVATE, username, "/kick", recipient));
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Recipient doesn't exist! Remember, to kick a client type /kick <Username>");
		}
		
	}
	
	public void sendPrivateMessage(String msgToSend) {
		// This method is responsible for sending private message to the server
		String[] msgParts = msgToSend.split(" ");
		String recipient = msgParts[1];
		String content = msgToSend.substring(3 + recipient.length()+1);
		if (recipientExist(recipient)) {
			try {
				out.writeObject(MessageFactory.createMessage(MessageFactory.MessageType.PRIVATE, username, content, recipient));
				out.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Recipient doesn't exist! Remember, to send a private message type /p <Recipient> <Message>");
		}
		
	}
	
	public boolean recipientExist(String recipient) {
		for (String member : memberList.keySet()) {
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
				
				while (socket.isConnected() && !socket.isClosed()) {
					
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
					} catch (SocketException e) {
						System.out.println("Oops, something went wrong. :(");
						break;
						//e.printStackTrace();
					} catch (EOFException e) {
						System.out.println("Seems like you have been kicked from the chat. :(");
						break;
					} catch (IOException e1) {
						System.out.println("IO Exception while listening to messages. :(");
						e1.printStackTrace();
						closeEverything(socket, in, out);
					} catch (ClassNotFoundException e) {
						System.out.println("Class not found. :(");
						//e.printStackTrace();
					} 
				}	
			}			
		}).start();			
	}
	
	public boolean msgFromCoordinator(String sender) {
		if (memberList != null) {
			for (String key : memberList.keySet()) {
				if (memberList.get(key).getCoordinator()) {
					return memberList.get(key).getUsername().equals(sender);
				}
			}
		}
		
		return false;
	}
	
	public synchronized void updateMemberMap(LinkedHashMap<String, Member> newMemberList) {
		memberList = newMemberList;
	}
	
	public void printBroadcastMessage(Message msg) {
		if (!msgFromCoordinator(msg.getSender())) {
			System.out.println("["+timeStampFormatter(msg.getTimestamp())+"] "+msg.getSender()+": "+msg.getContent());
		} else {
			System.out.println("["+timeStampFormatter(msg.getTimestamp())+"] [COORDINATOR] "+msg.getSender()+": "+msg.getContent());
		}
	}
	
	public void printPrivateMessage(PrivateMessage pMsg) {
		if (!msgFromCoordinator(pMsg.getSender())) {
			System.out.println("["+timeStampFormatter(pMsg.getTimestamp())+"] "+"Private message from " + pMsg.getSender() + ": " + pMsg.getContent());
		} else {
			System.out.println("["+timeStampFormatter(pMsg.getTimestamp())+"] "+"Private message from [COORDINATOR] " + pMsg.getSender() + ": " + pMsg.getContent());
		}
		
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
			// Nothing I can do at this point
		}
		System.exit(0);
	}

	public void printGreeting(){
		System.out.println("  ╔════════════════════════════════════════════════════════════╗");
		System.out.println("  ║                 Welcome to the chatroom!                   ║");
		System.out.println("  ║    To broadcast a message just type it in command line     ║");
		System.out.println("  ║    To send a private message type /p <Recipient> <Message> ║");
		System.out.println("  ║    To see the list of online members type /members         ║");
		System.out.println("  ║    To exit the chatroom press Ctrl+C                       ║");
		System.out.println("  ╚════════════════════════════════════════════════════════════╝");
	}

	public static boolean isValidUsername(String username) {
		if(username.length()<3) {
			return false;
		}
		for(int i=0; i < username.length(); i++) {
			char c = username.charAt(i);
			if (c == '!' || c == '@' || c == '#' || c == '$' || c == '%' || c == '^' || c == ' ' ||
		        c == '&' || c == '*' || c == '(' || c == ')' || c == '+' || c == '-' || c == '=') {
				return false;
			}
		}		
		return true;
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		try (Scanner scanner = new Scanner(System.in)) {
			System.out.println("Enter IP address and port of the server in the format <IP address> <port>: ");
			String ipAndPort = scanner.nextLine();
			String[] ipAndPortArray = ipAndPort.split(" ");
			String ipAddress = ipAndPortArray[0];
			int port = Integer.parseInt(ipAndPortArray[1]);   
			
			Socket socket = new Socket(ipAddress, port);
			
			System.out.println("Enter your username: ");
			String username = scanner.nextLine();
			if (isValidUsername(username) == false) {
				System.out.println("Invalid username! Username must be at least 3 characters long and can't contain special characters.");
				System.exit(0);
			}
			Client client = new Client(socket, username);
			client.printGreeting();
			client.listenForMessage();
			client.sendMessage();
		} catch (IllegalArgumentException e){
			System.out.println("Invalid port number!");
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Invalid IP address or port number!");
		} catch (ConnectException e) {
			System.out.println("Connection refused! Server is not running or wrong IP address and port number.");
		} catch (IOException e) {
			System.out.println("Oops, something went wrong. :(");
		}
	}
}