import java.io.IOException;
import java.net.ServerSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;


public class Server {

	private ServerSocket serverSocket;
	
	public Server(SSLServerSocket serverSocket) {
		this.serverSocket = serverSocket;
		
	}
	
	
	public void startServer() {
		try {
			while (!serverSocket.isClosed()) {
				SSLSocket socket = (SSLSocket) serverSocket.accept(); //blocking method, program halted until client connects, when finally connects, a socket object is returned
				printNewConnection(socket.getInetAddress().toString());
				ClientHandler clientHandler = new ClientHandler(socket);
				
				Thread thread = new Thread(clientHandler);
				thread.start();
			}
		} catch (IOException e) {
			System.out.println("Hmmm");
		} catch (ClassNotFoundException e) {
			System.out.println("Hmmm");
			e.printStackTrace();
		}
	}
	
	public void closeServerSocket() {
		try {
			if (serverSocket != null) {
				serverSocket.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void printNewConnection(String ipAddress) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
		String timestamp = LocalDateTime.now().format(formatter);
		System.out.println('[' + timestamp + ']' + ' ' + ipAddress + " has connected");
	}
	
	public static void main(String[] args) throws IOException {
		System.setProperty("javax.net.ssl.keyStore", "E:\\keydir\\server-keystore.p12");
        System.setProperty("javax.net.ssl.keyStorePassword", System.getenv("keystore_pwd"));
        System.setProperty("javax.net.ssl.keyStoreType", "PKCS12");
		
		SSLServerSocketFactory sslssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		SSLServerSocket sslSocket = (SSLServerSocket) sslssf.createServerSocket(9999);

		Server server = new Server(sslSocket);
		System.out.println("Server is up and running.");
		server.startServer();
	}
}