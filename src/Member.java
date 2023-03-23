import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;

public class Member implements Serializable{
	
	private String username;
	private InetAddress ipAddress;
	private int port;
	private boolean isCoordinator;
	private static final long serialVersionUID = 1L;
	
	
	public Member(String username, InetAddress inetAddress, int port, boolean coordinator) {
		this.username = username;
		this.ipAddress = inetAddress;
		this.port = port;
		this.isCoordinator = coordinator;
		
	}
	
	public String getUsername() {
		return username;
	}

	public InetAddress getIpAddress() {
		return ipAddress;
	}
	
	public int getPort() {
		return port;
	}
	
	public boolean getCoordinator() {
		return isCoordinator;
	}
	
}
