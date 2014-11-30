/**
 * 
 */
package edu.columbia.cs6998.sdn.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import net.floodlightcontroller.devicemanager.IDevice;
import net.floodlightcontroller.devicemanager.internal.DeviceManagerImpl;
import net.floodlightcontroller.devicemanager.internal.Entity;
import net.floodlightcontroller.devicemanager.web.DeviceEntityResource;
import net.floodlightcontroller.devicemanager.web.DeviceResource;

/**
 * @author ubuntu
 * 
 * This is the child controller
 *
 */
public class QuerySwitch extends Switch {
	private Map<Long, List<Object>> hostMap;
	
	public void addHost(Long mac, String hostname, int ip, int port) {
		List<Object> hostObject = new ArrayList<Object>();
		hostObject.add(hostname);
		hostObject.add(ip);
		hostObject.add(port);
		this.hostMap.put(mac, hostObject);
	}
	
	public QuerySwitch() {
		this.hostMap = new HashMap<Long, List<Object>>();
	}
	
	public Map<Long, List<Object>> getHostMap() {
		return hostMap;
	}
	/**
	 * @param args
	 * @throws IOException 
	 * 
	 */
	public static void main(String[] args) throws IOException {

		/**
		 *  execute remote methods on default port 1099
		 */
        try {
            Registry registry = LocateRegistry.getRegistry();
            Action stub = (Action) registry.lookup("Action");
            stub.addGswitch("GS1", 1);
            stub.addHost("Host1", 167777777L, 167772161, 1);
            System.out.println("Host1 is connected to port " + stub.getHostPort(167777777L));
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
/*		QuerySwitch sw = new QuerySwitch();
		sw.addHost(1234567893L, "hostname", 167777771, 1);
		System.out.println(sw.getHostMap());*/
		
		//Switch newSwitch = new Switch();
		//newSwitch.startUp(context);
/*		try {
		    String httpURL = "http://localhost:8080/wm/core/controller/switches/json";
		    URL myurl = new URL(httpURL);
		    HttpURLConnection connection = (HttpURLConnection)myurl.openConnection();
		    InputStream inputStream = connection.getInputStream();
		    InputStreamReader inputStreamReader= new InputStreamReader(inputStream);
		    BufferedReader bufferedRead = new BufferedReader(inputStreamReader);
		    String inputLine;
		 
		    while ((inputLine = bufferedRead.readLine()) != null)
		    {
		      System.out.println(inputLine);
		    }
		    bufferedRead.close();
		} catch(IOException e) {
			System.out.println("Make sure the controller is running");
		}*/
	 
	    
/*	    InetAddress hostName = Inet4Address.getLoopbackAddress();
	    int portNumber = 6644;
	    System.out.println(hostName);
        try (
            Socket echoSocket = new Socket(hostName, portNumber);
            PrintWriter out =
                new PrintWriter(echoSocket.getOutputStream(), true);
            BufferedReader in =
                new BufferedReader(
                    new InputStreamReader(echoSocket.getInputStream()));
            BufferedReader stdIn =
                new BufferedReader(
                    new InputStreamReader(System.in))
        ) {
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                out.println(userInput);
                System.out.println("echo: " + in.readLine());
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " +
                hostName);
            System.exit(1);
        }*/
	}

}
