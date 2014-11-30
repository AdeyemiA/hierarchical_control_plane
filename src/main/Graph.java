import java.util.HashMap;
import java.util.HashSet;


public class Graph {
	public int V;
	public int E;
	public HashMap<String, HashSet<String>> adjMap; //e.g "s1"->["s2","h1"]
	
	public Graph(){
		V = 0;
		E = 0;
		adjMap = new HashMap<String, HashSet<String>>();
	}
	
	public boolean addNode(String nodeName){
		V++;
		return true;
	}
	
	public boolean deleteNode(String nodeName){
		V--;
		return true;
	}
	
	public HashSet<String> getAdjacentNodes(String nodeName){
		return adjMap.get(nodeName);
	}
	
	public boolean addEdge(String nodeName1, String nodeName2){
		E++;
		return true;
	}
	
	public boolean deleteEdge(String nodeName1, String nodeName2){
		E--;
		return true;
	}
	
	public boolean isConnected(String nodeName1, String nodeName2){
		return adjMap.get(nodeName1).contains(nodeName2);
	}
}
