import java.util.HashMap;


public class GSwitch {
	public int ports;
	public HashMap<Integer, String> portNameMap;
	public HashMap<String, Integer> namePortMap;
	
	public boolean addLink(int port, String name){
		return true;
	}
	
	public boolean deleteLink(int port){
		return true;
	}
	
	public boolean isConnectedTo(String name){
		return namePortMap.containsKey(name);
	}
	
	public String whatConnectedTo(int port){
		return portNameMap.get(port);
	}
	
	public int getPortConnectedTo(String name){
		return namePortMap.get(name);
	}
}
