/**
 * 
 * This class implements the remote methods defined on the parent controller
 * and will be invoked by the child controller by remote calls
 */
package edu.columbia.cs6998.sdn.project;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ubuntu
 *
 */
public class ActionImpl implements Action {
	/*
	 *  hostMap holds the mapping for hosts
	 *  Long - is the mac
	 *  List[0] - hostname
	 *  List[1] - ip
	 *  List[2] - port host is connected to
	 * 
	 */
	private Map<Long, List<Object>> hostMap;
	private Map<String, Integer> gswitchMap;
	protected static Logger log = LoggerFactory.getLogger(ActionImpl.class);
	
	public ActionImpl() {
		this.hostMap = new ConcurrentHashMap<Long, List<Object>>();
		this.gswitchMap = new ConcurrentHashMap<String, Integer>();
	}
	
	@Override
	public void addHost(String hostname, String mac, int ip, int port) {
		// TODO Auto-generated method stub
		log.info("This method has not been implemented");
	}

	@Override
	public void addHost(String hostname, Long mac, int ip, int port) {
		// TODO Auto-generated method stub
		List<Object> hostList = new ArrayList<Object>();
		hostList.add(hostname);
		hostList.add(ip);
		hostList.add(port);
		this.hostMap.put(mac, hostList);		
	}

	@Override
	public void addGswitch(String gswitchId, int port) {
		// TODO Auto-generated method stub
		this.gswitchMap.put(gswitchId, port);
	}

	@Override
	public void removeHost(String hostname) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeGswitch(String gswitchId) {
		// TODO Auto-generated method stub
		if (gswitchId != null) this.gswitchMap.remove(gswitchId);
		else log.info("The Key of the GSwitch to be removed is null");
	}


	@Override
	public boolean discoverGswitchLink(String controllerId, String gswitchId,
			int port) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeHost(Long mac) throws RemoteException {
		// TODO Auto-generated method stub
		if (this.hostMap.containsKey(mac)) {
			this.hostMap.remove(mac);
			log.info("Host with {} has been successfully removed", mac);
		}
	}

	@Override
	public int getHostPort(String mac) throws RemoteException {
		// TODO Auto-generated method stub
		log.info("This method is yet to be implemented");
		return 0;
	}

	@Override
	public int getHostPort(Long mac) throws RemoteException {
		/*
		 *  call a get method to retrieve the value indexed at the port position
		 */
		List<Object> hostList = this.hostMap.get(mac);
		return getPortFromList(hostList);
	}

	@Override
	public int getHostPort(int ip) throws RemoteException {
		/* Iterate through all the values stored in the map
		 * 
		 * Return the port number in the list that has a value for ip
		 * index 1 is for Ip, while List.get(2) is the port number
		 */		
		Collection<List<Object>> valueList = this.hostMap.values();
		Iterator<List<Object>> iterateValues = valueList.iterator();
		if(iterateValues.hasNext()) {
			List<Object> hostParamList = (List<Object>) iterateValues.next();
			if((int)hostParamList.get(1) == ip) {
				return (int)hostParamList.get(2);
			}
		}
		log.info("The host with ip address {} has not been learned. 0 is returned by default", ip);
		return 0;
	}

	private int getPortFromList(List<Object> hostParamList) {
		return (int) hostParamList.get(2);
	}
	

}
