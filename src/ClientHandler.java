import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;


public class ClientHandler implements Runnable{
	// Each object of this class is responsible for communicating with the clients
	
	public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();// for keeping track of clients
	private static LinkedHashMap<String, Member> memberList = new LinkedHashMap<>();// for keeping track of members
	private Socket socket; // used to establish client-server connection
	private ObjectInputStream in; // to read messages from client
	private ObjectOutputStream out; // to send messages to client
	private String clientUsername;
	
	
	public ClientHandler(Socket socket) throws ClassNotFoundException {
		try {
			this.socket = socket;
			this.out = new ObjectOutputStream(socket.getOutputStream()); // buffer with byte stream wrapped inside char string for increased efficiency 
			this.in = new ObjectInputStream(socket.getInputStream());
			Object temporaryUsernameMessage = in.readObject();
			String temporaryUsername = ((Message) temporaryUsernameMessage).getSender();
			if (isUniqueID(temporaryUsername)){
				approveUser(temporaryUsername);
			} else {
				denyUser();
			}
			
		} catch (IOException e) {
			closeEverything(socket, in, out);
		}
	}
	
	
	
	private void denyUser() {
		try {
			out.writeObject(MessageFactory.createMessage(MessageFactory.MessageType.SERVER, null, "Username is not unique! Reconnect with different username!", null));
			out.flush();
			closeEverything(socket, in, out);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	@Override
	public void run() {
		// Everything that runs here runs on a separate thread
		Object msgFromClientObject;
		
		
		while (socket.isConnected()) {
			try {
				msgFromClientObject = in.readObject(); // program will halt here until receives a message. Thats why I run it on separate thread so the rest of app isn't stopped here
				if (msgFromClientObject != null){
					if (msgFromClientObject instanceof PrivateMessage) {
						PrivateMessage msgFromClient = (PrivateMessage) msgFromClientObject;
						if (msgFromClient.getContent().equals("/kick")){
							kickMember(msgFromClient);
						} else {
							privateMessage(msgFromClient);
						}
						
					} else if (msgFromClientObject instanceof Message) {
						Message msgFromClient = (Message) msgFromClientObject;
						broadcastMessage(msgFromClient);
					}		
				}
				
			} catch (IOException e) {
				System.out.println("Client " + clientUsername + " has disconnected.");
				closeEverything(socket, in, out);
				break;
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}		
	}
	
	public synchronized void kickMember(PrivateMessage message){
		if (isCoordinator(message.getSender())) {
			for (ClientHandler clientHandler : clientHandlers) {
				if (clientHandler.clientUsername.equals(message.getRecipient())){
					serverMessage("You have kicked " + message.getRecipient() + " from the chat.");
					broadcastMessage(MessageFactory.createMessage(MessageFactory.MessageType.SERVER, null, message.getRecipient() + " has been kicked from the chat.", null));
					try {
						if (clientHandler.socket != null){
							clientHandler.socket.close();
						}
						if (clientHandler.in != null){
							clientHandler.in.close();
						}
						if (clientHandler.out != null){
							clientHandler.out.close();
						}
					} catch (IOException e) {
						System.out.println("Something went wrong while kicking user!");
					}
					clientHandlers.remove(clientHandler);
					memberList.remove(clientHandler.clientUsername);
					updateClientsMemberList();
					break;
				}
			}
		} else {
			serverMessage("Permission denied! You are not the coordinator!");
		}
	}

	public void updateClientsMemberList() {
		if (clientHandlers.size() != 0){
			for(ClientHandler ch : clientHandlers) {
				try {
					ch.out.reset(); /* resets previous state of out, without it local version of 
									   memberList wasn't updating*/
					ch.out.writeObject(memberList);
					ch.out.flush();
					
				} catch (IOException e) {
					System.out.println("Something went wrong while updating clients member list!");
					e.printStackTrace();
				}
			}
		}		
	}
	
	public synchronized void addMember() {
		if(memberList.size() == 0) {
			serverMessage(clientUsername + " you are Coordinator because you are the first one to join the chat!\nType /kick username to kick a user from the chat.");
			Member member = new Member(clientUsername,socket.getInetAddress(),socket.getPort(),true);
			memberList.put(clientUsername, member);
		} else {
			Member member = new Member(clientUsername,socket.getInetAddress(),socket.getPort(), false);
			memberList.put(clientUsername, member);
		}
		updateClientsMemberList();
	}
	
	public void serverMessage(String message) {
		MessageInterface serverMessage = MessageFactory.createMessage(MessageFactory.MessageType.SERVER, null, message, null);
		for(ClientHandler clientHandler : clientHandlers) {
			try {
				if(clientHandler.clientUsername.equals(clientUsername)) {
					clientHandler.out.writeObject(serverMessage);
					clientHandler.out.flush();
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				closeEverything(socket, in, out);
			}
		}
	}

	
	
	public void approveUser(String username) {		
		this.clientUsername = username;
		clientHandlers.add(this);
		addMember();
		broadcastMessage(MessageFactory.createMessage(MessageFactory.MessageType.SERVER, null, username + " has entered the chat.", null));				
	}
	
	public boolean isUniqueID(String username) {
		for(ClientHandler clientHandler : clientHandlers) {
			if(clientHandler.clientUsername.equals(username)) {
				return false;
			}
		}
		return true;		
	}
	
	public void privateMessage(PrivateMessage message) {
		for (ClientHandler ch : clientHandlers) {
			if(ch.clientUsername.equals(message.getRecipient())) {
				try {
					ch.out.reset();
					ch.out.writeObject(message);
					ch.out.flush();
					break;
				} catch (IOException e) {
					e.printStackTrace();
					closeEverything(socket, in, out);
				}
			}
		}
	}
	
	
	
	public void broadcastMessage(MessageInterface message) {
		for (ClientHandler clientHandler : clientHandlers) {
			try {
				if (!clientHandler.clientUsername.equals(clientUsername)) {
					clientHandler.out.reset();
					clientHandler.out.writeObject(message);
					clientHandler.out.flush();//Buffer needs flushing because message probably won't be big enough to fill the buffer
					
				}
			} catch (IOException e) {
				e.printStackTrace();
				closeEverything(socket, in, out);
			}
		}
	}
	
	public boolean isCoordinator(String username){
		if (memberList.get(username) != null) {
			if (memberList.get(username).getCoordinator()) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	public void assignCoordinator() {
		try {
			if(memberList.size()>0){
				String username = memberList.keySet().iterator().next();
				memberList.get(username).setCoordinator(true);
				broadcastMessage(MessageFactory.createMessage(MessageFactory.MessageType.SERVER, null, username + " is now the coordinator!", null));
			}			
		} catch (Exception e) {
			System.out.println("Error assigning coordinator!");
		}
	}



	
	public synchronized void removeClientHandler() {
		// Remove client from memberList and update clients memberList if coordinator is removed assign new coordinator
		if (isCoordinator(clientUsername)) {
			memberList.remove(clientUsername);
			assignCoordinator();
		} else {
			memberList.remove(clientUsername);
		}
		clientHandlers.remove(this);
		updateClientsMemberList();
	}
	
	
	public void closeEverything(Socket socket, ObjectInputStream in3, ObjectOutputStream out3) {
		removeClientHandler();
		if (clientUsername != null) broadcastMessage(MessageFactory.createMessage(MessageFactory.MessageType.SERVER, null, clientUsername + " has left the chat!", null));
		try {
			// Checking for null pointer and also only outer wrapper of stream is closed so inner are closing as well same for socket 
			if (socket != null) {
				socket.close();
			}
			if (in3 != null) {
				in3.close();
			}
			if (out3 != null) {
				out3.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}