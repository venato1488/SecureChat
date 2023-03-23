
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Set;

public class Client {
	
	private Socket socket;
	private BufferedReader bufferedReader; // to read messages from server
	private BufferedWriter bufferedWriter;
	private String username;
	private Set<String> memberList;

	public Client(Socket socket, String username) {
		try {
			this.socket = socket;
			this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			this.username = username;
		
		} catch (IOException e) {
			closeEverything(socket, bufferedReader, bufferedWriter);
		}
	}
	
	public void sendMessage() {
		try {
			//Because at first connection client has to enter username first
			bufferedWriter.write(username);
			bufferedWriter.newLine();
			bufferedWriter.flush();
			
			Scanner scanner = new Scanner(System.in);
			while(socket.isConnected()) {
				String messageToSend = scanner.nextLine();// after user press enter it will be captured in messageToSend
				bufferedWriter.write(username + ": " + messageToSend);
				bufferedWriter.newLine();
				bufferedWriter.flush();
			}
		} catch (IOException e) {
			closeEverything(socket, bufferedReader, bufferedWriter);
		}
	}
	
	public void listenForMessage() {
		new Thread(new Runnable() {
			// Anonimous thread
			@Override
			public void run() {
				String msgFromChat;
				
				while (socket.isConnected()) {
					
					try {
						msgFromChat = bufferedReader.readLine();
						if (msgFromChat != null) {
							System.out.println(msgFromChat);
						}
						
					} catch (IOException e) {
						closeEverything(socket, bufferedReader, bufferedWriter);
					}
				}
				
			}
			
		}).start();
			
	}
	
	
	public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
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
	
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter your username: ");
		String username = scanner.nextLine();
		Socket socket = new Socket("localhost", 9999);
		Client client = new Client(socket, username);
		client.listenForMessage();
		client.sendMessage();
	}
}
