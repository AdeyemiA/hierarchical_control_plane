/**
 * 
 */
package edu.columbia.cs6998.sdn.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import net.floodlightcontroller.devicemanager.IDevice;
import net.floodlightcontroller.devicemanager.internal.DeviceManagerImpl;
import net.floodlightcontroller.devicemanager.internal.Entity;
import net.floodlightcontroller.devicemanager.web.DeviceEntityResource;
import net.floodlightcontroller.devicemanager.web.DeviceResource;

/**
 * @author ubuntu
 *
 */
public class QuerySwitch {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		// get requests using floodlight REST API
		
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
	}

}
