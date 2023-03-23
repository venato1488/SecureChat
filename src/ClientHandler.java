import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable{
	// Each object of this class is responsible for communicating with the clients
	
	public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
	private static ArrayList<Member> memberList = new ArrayList<>();// for keeping track of clients
	public static int clientCount = 0;
	private Socket socket; // used to establish client-server connection
	private ObjectInputStream in; // to read messages from client
	private ObjectOutputStream out; // to send messages to client
	private String clientUsername;
	private Member member;
	
	
	public ClientHandler(Socket socket) throws ClassNotFoundException {
		try {
			this.socket = socket;
			this.out = new ObjectOutputStream(socket.getOutputStream()); // buffer with byte stream wrapped inside char string for increased efficiency 
			this.in = new ObjectInputStream(socket.getInputStream());
			Object temporaryUsernameMessage = in.readObject();
			Message temporaryUsername = (Message) temporaryUsernameMessage;
			isValidID(temporaryUsername.getSender());
			//this.clientUsername = bufferedReader.readLine();
			
			//clientHandlers.add(this);
			//broadcastMessage("SERVER: " + clientUsername + " has entered the chat!");
		} catch (IOException e) {
			closeEverything(socket, in, out);
		}
	}
	
	
	
	@Override
	public void run() {
		// Everything that runs here runs on a separate thread
		Object msgFromClientObject;
		
		
		while (socket.isConnected()) {
			try {
				msgFromClientObject = in.readObject(); // program will halt here until receives a message. Thats why I run it on separate thread so the rest of app isn't stopped here
				Message msgFromClient = (Message) msgFromClientObject;
				
				broadcastMessage(msgFromClient);
			} catch (IOException e) {
				closeEverything(socket, in, out);
				break;
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	public void addMember() {
		if(clientCount==0) {
			serverMessage(new Message("SERVER",clientUsername + " is Coordinator!"));
			this.member = new Member(clientUsername,socket.getInetAddress(),socket.getPort(),true);
			memberList.add(member);
		} else {
			this.member = new Member(clientUsername,socket.getInetAddress(),socket.getPort(), false);
			memberList.add(member);
		}
		clientCount++;
	}
	
	public void serverMessage(Message serverMessage) {
		for(ClientHandler clientHandler : clientHandlers) {
			try {
				if(clientHandler.clientUsername.equals(clientUsername)) {
					clientHandler.out.writeObject(serverMessage);
					clientHandler.out.flush();
					System.out.println("server msg");
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		}
	}
	
	public void isValidID(String username) {
		if(isUniqueID(username)) {
			this.clientUsername = username;
			clientHandlers.add(this);
			broadcastMessage(new Message("SERVER",username + " has entered the chat."));
			
		} else {
			closeEverything(socket, in, out);
		}
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
					ch.out.writeObject(message);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
	
	
	public void removeClientHandler() {
		clientCount--;
		clientHandlers.remove(this);
		memberList.remove(member);
		if (clientUsername != null) broadcastMessage(new Message("SERVER", clientUsername + " has left the chat!"));
		
	}
	
	
	public void closeEverything(Socket socket, ObjectInputStream in3, ObjectOutputStream out3) {
		removeClientHandler();
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
