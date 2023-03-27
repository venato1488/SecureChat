import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{
	// Each object of this class is responsible for communicating with the clients
	
	public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>(); // for keeping track of clients
	private Socket socket; // used to establish client-server connection
	private BufferedReader bufferedReader; // to read messages from client
	private BufferedWriter bufferedWriter; // to send messages to client
	private String clientUsername;
	
	
	public ClientHandler(Socket socket) {
		try {
			this.socket = socket;
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); // buffer with byte stream wrapped inside char string for increased efficiency 
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.clientUsername = bufferedReader.readLine();
			clientHandlers.add(this);
			broadcastMessage("SERVER: " + clientUsername + " has entered the chat!");
		} catch (IOException e) {
			closeEverything(socket, bufferedReader, bufferedWriter);
		}
	}
	
	
	
	@Override
	public void run() {
		// Everything that runs here runs on a separate thread
		String msgFromClient;
		
		
		while (socket.isConnected()) {
			try {
				msgFromClient = bufferedReader.readLine(); // program will halt here until receives a message. Thats why I run it on separate thread so the rest of app isn't stopped here
				broadcastMessage(msgFromClient);
			} catch (IOException e) {
				closeEverything(socket, bufferedReader, bufferedWriter);
				break;
			}
		}
<<<<<<< Updated upstream
=======
	}
	
	public synchronized void addMember() {
		// TODO reasignment of coordinator in case he leaves
		if(clientCount==0) {
			serverMessage(new Message("SERVER", clientUsername + " is Coordinator!"));
			Member member = new Member(clientUsername,socket.getInetAddress(),socket.getPort(),true);
			memberList.put(clientUsername, member);
		} else {
			Member member = new Member(clientUsername,socket.getInetAddress(),socket.getPort(), false);
			memberList.put(clientUsername, member);
		}
		updateClientsMemberList();
		clientCount++;
	}
	
	public void serverMessage(Message serverMessage) {
		// Might be useful when saying that ID is not unique
		for(ClientHandler clientHandler : clientHandlers) {
			try {
				if(clientHandler.clientUsername.equals(clientUsername)) {
					clientHandler.out.writeObject(serverMessage);
					clientHandler.out.flush();
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
		broadcastMessage(new Message("SERVER",username + " has entered the chat."));					
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
				} catch (IOException e) {
					e.printStackTrace();
					closeEverything(socket, in, out);
				}
			}
		}
	}
	
	
	
	public void broadcastMessage(Message messageToSend) {
		for (ClientHandler clientHandler : clientHandlers) {
			try {
				if (!clientHandler.clientUsername.equals(clientUsername)) {
					clientHandler.out.writeObject(messageToSend);
					clientHandler.out.flush();//Buffer needs flushing because message probably won't be big enough to fill the buffer
				}
			} catch (IOException e) {
				closeEverything(socket, in, out);
			}
		}
	}
	
	public boolean isCoordinator(String username){
		if(memberList.get(username).getCoordinator()) {
			return true;
		} else {
			return false;
		}
	}

	public void assignCoordinator() {
		try {
			if(memberList.size()>0){
				String username = memberList.keySet().iterator().next();
				memberList.get(username).setCoordinator(true);
				broadcastMessage(new Message("SERVER", username + " is now the coordinator!"));
			}			
		} catch (Exception e) {
			System.out.println("Error assigning coordinator!");
		}
	}



	
	public synchronized void removeClientHandler() {
		clientCount--;
		clientHandlers.remove(this);
		if (clientUsername != null) broadcastMessage(new Message("SERVER", clientUsername + " has left the chat!"));
>>>>>>> Stashed changes
		
		if (isCoordinator(clientUsername)) {
			memberList.remove(clientUsername);
			assignCoordinator();
		}
		updateClientsMemberList();
		
		
	}
	
	public void broadcastMessage(String messageToSend) {
		for (ClientHandler clientHandler : clientHandlers) {
			try {
				if (!clientHandler.clientUsername.equals(clientUsername)) {
					clientHandler.bufferedWriter.write(messageToSend);
					clientHandler.bufferedWriter.newLine();//Because client will wait for new line I explicitly put it so readLine works
					clientHandler.bufferedWriter.flush();//Buffer needs flushing because message probably won't be big enough to fill the buffer
				}
			} catch (IOException e) {
				closeEverything(socket, bufferedReader, bufferedWriter);
			}
		}
	}
	
	
	public void removeClientHandler() {
		clientHandlers.remove(this);
		broadcastMessage("SERVER: " + clientUsername + " has left the chat!");
	}
	
	
	public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
		removeClientHandler();
		try {
			// Checking for null pointer and also only outer wrapper of stream is closed so inner are closing as well same for socket 
			if (socket != null) {
				socket.close();
			}
			if (bufferedReader != null) {
				bufferedReader.close();
			}
			if (bufferedWriter != null) {
				bufferedWriter.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
