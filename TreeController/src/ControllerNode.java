import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
	
	public String process(String command){
		return "OK";
	}
	
	public class ChildThread implements Runnable{
		private Socket clientSocket;
		
		public ChildThread(Socket clientSoc){
			clientSocket = clientSoc;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				PrintWriter pw = new PrintWriter(clientSocket.getOutputStream());
				BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				String line = "";
				while((line = br.readLine())!=null){
					System.out.println(line);
					pw.println(process(line));
					pw.flush();
				}
				pw.close();
				br.close();
				clientSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
	}
	
	public ControllerNode() throws IOException{
		parentAddress = "";
		childrenAddresses = new ArrayList<String>();
		switches = new ArrayList<GSwitch>();
		hosts = new ArrayList<Host>();
		topology = new Graph();
		port = 12091;
		this.run();
	}
	
	public ControllerNode(String configfilename) throws IOException{
		parentAddress = "";
		childrenAddresses = new ArrayList<String>();
		switches = new ArrayList<GSwitch>();
		hosts = new ArrayList<Host>();
		topology = new Graph();
		port = 12091;
		parseConfigFile(configfilename);
		this.run();
	}
	
	private void parseConfigFile(String filename){
		
	}
	
	void run() throws IOException{
		ServerSocket serverSocket = new ServerSocket(port);
		Socket clientSocket;
		while((clientSocket=serverSocket.accept())!=null){
			ChildThread p = new ChildThread(clientSocket);
			new Thread(p).start();
		}
		serverSocket.close();
	}
}
