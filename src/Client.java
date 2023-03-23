
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Set;

public class Client {
	
	private Socket socket;
	private ObjectInputStream in; // to read messages from server
	private ObjectOutputStream out;
	private String username;
	private Set<String> memberList;

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
			System.out.println("username sent to server");
			
			Scanner scanner = new Scanner(System.in);
			while(socket.isConnected()) {
				String messageToSend = scanner.nextLine();// after user press enter it will be captured in messageToSend
				out.writeObject(new Message(username, messageToSend));
				out.flush();
				System.out.println("Message sent");
			}
		} catch (IOException e) {
			e.printStackTrace();
			closeEverything(socket, in, out);
		}
	}
	
	public void listenForMessage() {
		new Thread(new Runnable() {
			// Anonymous thread
			@Override
			public void run() {
				Object msgFromChat;
				
				while (socket.isConnected()) {
					
					try {
						msgFromChat = in.readObject(); // read serialized data
						Message message = (Message) msgFromChat; // casting object to its original type
						
						if (message != null) {
							System.out.println(message.getSender()+": "+message.getContent());
						}
						
					} catch (IOException e) {
						e.printStackTrace();
						closeEverything(socket, in, out);
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
			
		}).start();
			
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
		if(username.length()>3 && !username.contains("!@#$%^&*()_+-=")) {
			return true;
		} else return false;
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
