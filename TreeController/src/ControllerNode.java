import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


public class ControllerNode {
	public String parentAddress;
	public List<String> childrenAddresses;
	public List<GSwitch> switches;
	public List<Host> hosts;
	public Graph topology;
	public int port;
	
	public ControllerNode(String parent, List<String> childrenA, int p){
		parentAddress = parent;
		childrenAddresses = childrenA;
		switches = new ArrayList<GSwitch>();
		hosts = new ArrayList<Host>();
		topology = new Graph();
		port = p;
	}
	
	void run() throws IOException{
		ServerSocket serverSocket = new ServerSocket(port);
		Socket clientSocket;
		while((clientSocket=serverSocket.accept())!=null){
			
		}
	}
}
