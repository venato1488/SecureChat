package v2;

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
