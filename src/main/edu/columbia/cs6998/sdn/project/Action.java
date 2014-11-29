/**
 * 
 */
package edu.columbia.cs6998.sdn.project;

/**
 * @author ubuntu
 *
 */
public interface Action {
	public void addHost(ControllerNode parent, String hostname, String mac, String ip, int port);
	public void addHost(ControllerNode parent, String hostname, Long mac, String ip, int port);
	public void addGswitch(ControllerNode parent, String gswitchId, int port);
	public void removeHost(ControllerNode parent, String hostname);
	public void removeGswitch(ControllerNode parent, String gswitchId);
	public int getPort(String hostname);
	public int getPort(String mac);
	public int getPort(Long mac);
	public int getPort(String ip);
	public boolean discoverGswitchLink(String controllerId, String gswitchId, int port);
}
