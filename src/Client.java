
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
<<<<<<< Updated upstream
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
=======
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
>>>>>>> Stashed changes
import java.net.Socket;
import java.net.UnknownHostException;
<<<<<<< Updated upstream
=======
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.NoSuchElementException;
>>>>>>> Stashed changes
import java.util.Scanner;

public class Client {
	
	private Socket socket;
	private BufferedReader bufferedReader;
	private BufferedWriter bufferedWriter;
	private String username;

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
<<<<<<< Updated upstream
			closeEverything(socket, bufferedReader, bufferedWriter);
		}
	}
	
	public void listerForMessage() {
=======
			e.printStackTrace();
			closeEverything(socket, in, out);
		} catch (NoSuchElementException e) {
			closeEverything(socket, in, out);
		}
	}
	
	

	public void showMembers() {
    	System.out.println("                   [LIST OF ONLINE MEMBERS]");

    	String format = "| %-20s | %-15s | %-6s | %-12s |%n";
    	// Print table headers
    	System.out.format("+----------------------+-----------------+--------+--------------+%n");
    	System.out.format("| Username             | IP Address      | Port   | Coordinator  |%n");
    	System.out.format("+----------------------+-----------------+--------+--------------+%n");

    	for (String key : memberList.keySet()) {
        	Member member = memberList.get(key);
        	String username = member.getUsername();
       		String ipAddress = member.getIpAddress().toString();
        	int port = member.getPort();
        	String coordinator = member.getCoordinator() ? "Yes" : "No";

        	System.out.format(format, username, ipAddress, port, coordinator);
    	}

    // Print table footer
    System.out.format("+----------------------+-----------------+--------+--------------+%n");
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
		for (String member : memberList.keySet()) {
			if(recipient.equals(member)) return true;
		}
		return false;
	}
	
	public void listenForMessage() {
>>>>>>> Stashed changes
		new Thread(new Runnable() {
			// Anonimous thread
			@Override
			public void run() {
				String msgFromChat;
				
				while (socket.isConnected()) {
					
					try {
						msgFromChat = bufferedReader.readLine();
						System.out.println(msgFromChat);
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
<<<<<<< Updated upstream
	
	
	public static void main(String[] args) throws UnknownHostException, IOException {
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter your username: ");
		String username = scanner.nextLine();
		Socket socket = new Socket("localhost", 9999);
		Client client = new Client(socket, username);
		client.listerForMessage();
		client.sendMessage();
=======

	public void printGreeting(){
		System.out.println("Welcome to the chatroom!");
		System.out.println("To send a private message type /p <Recipient> <Message>");
		System.out.println("To see the list of online members type /members");
		System.out.println("To exit the chatroom press Ctrl+C");
	}

	public static boolean isValidUsername(String username) {
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
		} catch (NumberFormatException e) {
			System.out.println("Invalid port number!");
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Invalid IP address or port number!");
		} catch (ConnectException e) {
			System.out.println("Connection refused! Server is not running or wrong IP address and port number.");
		} catch (IOException e) {
			System.out.println("Oops, something went wrong. :(");
		}
>>>>>>> Stashed changes
	}
}
