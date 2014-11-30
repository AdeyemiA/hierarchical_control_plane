/**
 *  This interface describes the methods to be 
 *  remotely executed on the Parent Controller
 *  by the child controller
 */
package edu.columbia.cs6998.sdn.project;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author ubuntu
 *
 */
public interface Action extends Remote {
	public void addHost(String hostname, String mac, int ip, int port) throws RemoteException;
	public void addHost(String hostname, Long mac, int ip, int port) throws RemoteException;
	public void addGswitch(String gswitchId, int port) throws RemoteException;
	public void removeHost(String hostname) throws RemoteException;
	public void removeHost(Long mac) throws RemoteException;
	public void removeGswitch(String gswitchId) throws RemoteException;
	public int getHostPort(String mac) throws RemoteException;
	public int getHostPort(Long mac) throws RemoteException;
	public int getHostPort(int ip) throws RemoteException;
	public boolean discoverGswitchLink(String controllerId, String gswitchId, int port);
}
