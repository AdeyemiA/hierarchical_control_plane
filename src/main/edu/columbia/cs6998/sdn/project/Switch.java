/**
*    Copyright 2014, Columbia University.
*    Homework 1, COMS E6998-10 Fall 2014
*    Software Defined Networking
*    Originally created by Shangjin Zhang, Columbia University
* 
*    Licensed under the Apache License, Version 2.0 (the "License"); you may
*    not use this file except in compliance with the License. You may obtain
*    a copy of the License at
*
*         http://www.apache.org/licenses/LICENSE-2.0
*
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
*    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
*    License for the specific language governing permissions and limitations
*    under the License.
**/

/**
 * Floodlight
 * A BSD licensed, Java based OpenFlow controller
 *
 * Floodlight is a Java based OpenFlow controller originally written by David Erickson at Stanford
 * University. It is available under the BSD license.
 *
 * For documentation, forums, issue tracking and more visit:
 *
 * http://www.openflowhub.org/display/Floodlight/Floodlight+Home
 **/

package edu.columbia.cs6998.sdn.project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPv4;

import org.openflow.protocol.OFError;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFFlowRemoved;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.util.HexString;
import org.openflow.util.LRULinkedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Switch 
    implements IFloodlightModule, IOFMessageListener {
    protected static Logger log = LoggerFactory.getLogger(Switch.class);
    
    // Module dependencies
    protected IFloodlightProviderService floodlightProvider;
    
// CS6998: data structures for the learning switch feature
    // Stores the learned state for each switch
    protected Map<IOFSwitch, Map<Long, Short>> macToSwitchPortMap;


// CS6998: data structures for the firewall feature
    // Stores the MAC address of hosts to block: <Macaddr, blockedTime>
    // keep track of the number of interacted destinations in hostDestinations
    // keep track of the number of max flows per sources
    protected Map<Long, String> blacklist;
    protected Map<Long, Map<Long, Integer>> hostDestinations;
    protected Map<IOFSwitch, Map<Long, Integer>> swMaxFlows;

    //...more


    // flow-mod - for use in the cookie
    public static final int SWITCH_APP_ID = 10;
    // LOOK! This should probably go in some class that encapsulates
    // the app cookie management
    public static final int APP_ID_BITS = 12;
    public static final int APP_ID_SHIFT = (64 - APP_ID_BITS);
    public static final long SWITCH_COOKIE = (long) (SWITCH_APP_ID & ((1 << APP_ID_BITS) - 1)) << APP_ID_SHIFT;
    
    // more flow-mod defaults 
    protected static final short IDLE_TIMEOUT_DEFAULT = 10;
    protected static final short HARD_TIMEOUT_DEFAULT = 2;
    protected static final short PRIORITY_DEFAULT = 100;
    
    // for managing our map sizes
    protected static final int MAX_MACS_PER_SWITCH  = 1000;    

    // maxinum allowed elephant flow number for one switch
    protected static final int MAX_ELEPHANT_FLOW_NUMBER = 1;

    // maximum allowed destination number for one host
    protected static final int MAX_DESTINATION_NUMBER = 3;

    // maxinum allowed transmission rate
    protected static final int ELEPHANT_FLOW_BAND_WIDTH = 500;

    // time duration the firewall will block each node for
    protected static final int FIREWALL_BLOCK_TIME_DUR = (10 * 1000);
    
    // reason for blocking
    protected static final String BLOCK_FOR_ELEPHANT = "Blocked for Elephant Flow";
    
    // reason for blocking
    protected static final String BLOCK_FOR_MAX_DESTINATIONS = "Blocked for MAX destinations";
    
    // server ip's equal to 10.0.0.1
    protected static final int SERVER_ONE_IP = 167772161;
    
    protected static final int SERVER_TWO_IP = 167772162;  
    
    // equal to 10.0.5.1
    protected static final int COLUMBIA_WEB_SERVER_IP = 167773441;
    // server mac's
    protected static final int SERVER_ONE_MAC = 1; 
    
    protected static final int SERVER_TWO_MAC = 2;
    
    protected static final String BROADCAST = "ff:ff:ff:ff:ff:ff";
    /**
     * @param floodlightProvider the floodlightProvider to set
     */
    public void setFloodlightProvider(IFloodlightProviderService floodlightProvider) {
        this.floodlightProvider = floodlightProvider;
    }
    
    @Override
    public String getName() {
        return "switch";
    }

    /**
     * Adds a host to the MAC->SwitchPort mapping
     * @param sw The switch to add the mapping to
     * @param mac The MAC address of the host to add
     * @param portVal The switchport that the host is on
     */
    //CS6998: fill out the following ????s
    protected void addToPortMap(IOFSwitch sw, long mac, short portVal) {
        Map<Long, Short> swMap = macToSwitchPortMap.get(sw);
        
        if (swMap == null) {
            // May be accessed by REST API so we need to make it thread safe
            swMap = Collections.synchronizedMap(new LRULinkedHashMap<Long, Short>(MAX_MACS_PER_SWITCH));
         
        }
        swMap.put(mac, portVal);
        macToSwitchPortMap.put(sw, swMap);
    }

    
    /**
     * Removes a host from the MAC->SwitchPort mapping
     * @param sw The switch to remove the mapping from
     * @param mac The MAC address of the host to remove
     */
    //CS6998: fill out the following ????s
    protected void removeFromPortMap(IOFSwitch sw, long mac) {
        Map<Long, Short> swMap = macToSwitchPortMap.get(sw);
        if (swMap != null) {
            swMap.remove(mac);
        	macToSwitchPortMap.put(sw, swMap);	
        }else {
        	log.info("The {} has not been learned by switch {}", new Object[] { mac, sw });
        }
    }


    /**
     * Get the port that a MAC is associated with
     * @param sw The switch to get the mapping from
     * @param mac The MAC address to get
     * @return The port the host is on
     */
// CS6998: fill out the following method
    public Short getFromPortMap(IOFSwitch sw, long mac) {
    	
    	// obtain the value (map) mapped to the Switch key
    	// return the port value associated with the dest mac address
    	if(macToSwitchPortMap.containsKey(sw)) {
    		Map<Long, Short> swMap = macToSwitchPortMap.get(sw);
    		return (swMap.containsKey(mac)) ? swMap.get(mac): null;
    	} else {
    		return null;
    	}
               
    }

    
    /**
     * Writes a OFFlowMod to a switch.
     * @param sw The switch tow rite the flowmod to.
     * @param command The FlowMod actions (add, delete, etc).
     * @param bufferId The buffer ID if the switch has buffered the packet.
     * @param match The OFMatch structure to write.
     * @param outPort The switch port to output it to.
     */
    private void writeFlowMod(IOFSwitch sw, short command, int bufferId,
            OFMatch match, short outPort) {
        // from openflow 1.0 spec - need to set these on a struct ofp_flow_mod:
        // struct ofp_flow_mod {
        //    struct ofp_header header;
        //    struct ofp_match match; /* Fields to match */
        //    uint64_t cookie; /* Opaque controller-issued identifier. */
        //
        //    /* Flow actions. */
        //    uint16_t command; /* One of OFPFC_*. */
        //    uint16_t idle_timeout; /* Idle time before discarding (seconds). */
        //    uint16_t hard_timeout; /* Max time before discarding (seconds). */
        //    uint16_t priority; /* Priority level of flow entry. */
        //    uint32_t buffer_id; /* Buffered packet to apply to (or -1).
        //                           Not meaningful for OFPFC_DELETE*. */
        //    uint16_t out_port; /* For OFPFC_DELETE* commands, require
        //                          matching entries to include this as an
        //                          output port. A value of OFPP_NONE
        //                          indicates no restriction. */
        //    uint16_t flags; /* One of OFPFF_*. */
        //    struct ofp_action_header actions[0]; /* The action length is inferred
        //                                            from the length field in the
        //                                            header. */
        //    };
        OFFlowMod flowMod = (OFFlowMod) floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD);
        flowMod.setMatch(match);
        flowMod.setCookie(Switch.SWITCH_COOKIE);
        flowMod.setCommand(command);
        flowMod.setIdleTimeout(Switch.IDLE_TIMEOUT_DEFAULT);
        flowMod.setHardTimeout(Switch.HARD_TIMEOUT_DEFAULT);
        flowMod.setPriority(Switch.PRIORITY_DEFAULT);
        flowMod.setBufferId(bufferId);
        flowMod.setOutPort((command == OFFlowMod.OFPFC_DELETE) ? outPort : OFPort.OFPP_NONE.getValue());
        flowMod.setFlags((command == OFFlowMod.OFPFC_DELETE) ? 0 : (short) (1 << 0)); // OFPFF_SEND_FLOW_REM

        // set the ofp_action_header/out actions:
        // from the openflow 1.0 spec: need to set these on a struct ofp_action_output:
        // uint16_t type; /* OFPAT_OUTPUT. */
        // uint16_t len; /* Length is 8. */
        // uint16_t port; /* Output port. */
        // uint16_t max_len; /* Max length to send to controller. */
        // type/len are set because it is OFActionOutput,
        // and port, max_len are arguments to this constructor
        flowMod.setActions(Arrays.asList((OFAction) new OFActionOutput(outPort, (short) 0xffff)));
        flowMod.setLength((short) (OFFlowMod.MINIMUM_LENGTH + OFActionOutput.MINIMUM_LENGTH));

        if (log.isTraceEnabled()) {
            log.trace("{} {} flow mod {}", 
                      new Object[] { sw, (command == OFFlowMod.OFPFC_DELETE) ? "deleting" : "adding", flowMod });
        }

        // and write it out
        try {
            sw.write(flowMod, null);
        } catch (IOException e) {
            log.error("Failed to write {} to switch {}", new Object[] { flowMod, sw }, e);
        }
    }

    /**
     * Writes an OFPacketOut message to a switch.
     * @param sw The switch to write the PacketOut to.
     * @param packetInMessage The corresponding PacketIn.
     * @param egressPort The switchport to output the PacketOut.
     */
    private void writePacketOutForPacketIn(IOFSwitch sw, 
                                          OFPacketIn packetInMessage, 
                                          short egressPort) {

        // from openflow 1.0 spec - need to set these on a struct ofp_packet_out:
        // uint32_t buffer_id; /* ID assigned by datapath (-1 if none). */
        // uint16_t in_port; /* Packet's input port (OFPP_NONE if none). */
        // uint16_t actions_len; /* Size of action array in bytes. */
        // struct ofp_action_header actions[0]; /* Actions. */
        /* uint8_t data[0]; */ /* Packet data. The length is inferred
                                  from the length field in the header.
                                  (Only meaningful if buffer_id == -1.) */
        
        OFPacketOut packetOutMessage = (OFPacketOut) floodlightProvider.getOFMessageFactory().getMessage(OFType.PACKET_OUT);
        short packetOutLength = (short) OFPacketOut.MINIMUM_LENGTH; // starting length

        // Set buffer_id, in_port, actions_len
        packetOutMessage.setBufferId(packetInMessage.getBufferId());
        packetOutMessage.setInPort(packetInMessage.getInPort());
        packetOutMessage.setActionsLength((short) OFActionOutput.MINIMUM_LENGTH);
        packetOutLength += OFActionOutput.MINIMUM_LENGTH;
        
        // set actions
        List<OFAction> actions = new ArrayList<OFAction>(1);      
        actions.add(new OFActionOutput(egressPort, (short) 0));
        packetOutMessage.setActions(actions);

        // set data - only if buffer_id == -1
        if (packetInMessage.getBufferId() == OFPacketOut.BUFFER_ID_NONE) {
            byte[] packetData = packetInMessage.getPacketData();
            packetOutMessage.setPacketData(packetData); 
            packetOutLength += (short) packetData.length;
        }
        
        // finally, set the total length
        packetOutMessage.setLength(packetOutLength);              
            
        // and write it out
        try {
            sw.write(packetOutMessage, null);
        } catch (IOException e) {
            log.error("Failed to write {} to switch {}: {}", new Object[] { packetOutMessage, sw, e });
        }
    }
    
    /**
     * Processes a OFPacketIn message. If the switch has learned the MAC to port mapping
     * for the pair it will write a FlowMod for. If the mapping has not been learned the 
     * we will flood the packet.
     * @param sw
     * @param pi
     * @param cntx
     * @return
     */
    private Command processPacketInMessage(IOFSwitch sw, OFPacketIn pi, FloodlightContext cntx) {
        // Read in packet data headers by using OFMatch
        OFMatch match = new OFMatch();
        match.loadFromPacket(pi.getPacketData(), pi.getInPort());
        Long sourceMac = Ethernet.toLong(match.getDataLayerSource());
        Long destMac = Ethernet.toLong(match.getDataLayerDestination());
        
/* CS6998: Do works here to learn the port for this MAC
        ....
*/
    	this.addToPortMap(sw, sourceMac, match.getInputPort());
    	
/* CS6998: Do works here to implement super firewall
        Hint: You may check connection limitation here.
        ....
*/
    	//match.
    	// check if this is an ARP packet
    	if(Switch.COLUMBIA_WEB_SERVER_IP == match.getNetworkDestination() && Switch.BROADCAST.equals(HexString.toHexString(match.getDataLayerDestination()))) {
    		match.setNetworkDestination(Switch.SERVER_ONE_IP);
    		match.setDataLayerDestination("00:00:00:00:00:01");
    		destMac = (long) 1;
    		log.info("{} is the destination IP address", match.getNetworkDestination());
    	}
    	

    	// firewall based on max destinations
    	//log.info("{} is the total packet length  and {} the packet length", pi.getTotalLength(), pi.getLength());
    	Map<Long, Integer> tmpMap = (hostDestinations.containsKey(sourceMac)) ?hostDestinations.get(sourceMac) : null;
    	if(tmpMap != null) {
    	// check if the destination is new and the maximum limit is reached
    		if((tmpMap.size() == Switch.MAX_DESTINATION_NUMBER) && (!(tmpMap.keySet()).contains(destMac)) && (destMac < 100)) {
    			//log.info("User Space: The Destinations logged for sourceMac {} are {} but an attempt to reach destMac {}", new Object[] { sourceMac,(tmpMap.keySet()).toString(), destMac});
    			this.blacklist.put(sourceMac, Switch.BLOCK_FOR_MAX_DESTINATIONS);
    		} else if (tmpMap.keySet().size() < Switch.MAX_DESTINATION_NUMBER && (!(tmpMap.keySet()).contains(destMac)) && (destMac < 100)) {
    			tmpMap.put(destMac, 1);
    			hostDestinations.put(sourceMac, tmpMap);
    			//log.info("User Space: Mapping a new destination {}, for source {}", destMac,sourceMac);
    		}
    	} else if(destMac < 100){
    		tmpMap = Collections.synchronizedMap(new LinkedHashMap<Long, Integer>());
    		tmpMap.put(destMac, 1);
    		hostDestinations.put(sourceMac, tmpMap);
    		//log.info("User Space: Creating new map and mapping a new destination {}, for source {}", destMac,sourceMac);
    	}
    	
    	
/* CS6998: Filter-out hosts in blacklist
 *         Also, when the host is in blacklist check if the blockout time is
 *         expired and handle properly
        if (....)
            return Command.CONTINUE;
*/
    	// keep a reference in time to monitor spurious exits from wait
/*    	if(blacklist.containsKey(sourceMac)) {
    		new Thread(new Block(sourceMac, sw)).start();
    		//log.info("User Space: Came back from wait thread");
    		return Command.CONTINUE;
    	}*/

/* CS6998: Ask the switch to flood the packet to all of its ports
 *         Thus, this module currently works as a dummy hub
 */
        //this.writePacketOutForPacketIn(sw, pi, OFPort.OFPP_FLOOD.getValue());

//	 CS6998: Ask the switch to flood the packet to all of its ports
        // Now output flow-mod and/or packet
        // CS6998: Fill out the following ???? to obtain outPort
        Short outPort = (macToSwitchPortMap.containsKey(sw)) ? this.getFromPortMap(sw, destMac): null;
        if (outPort == null) {
            // If we haven't learned the port for the dest MAC, flood it
            // CS6998: Fill out the following ????
            this.writePacketOutForPacketIn(sw, pi, OFPort.OFPP_FLOOD.getValue());
        } else if (outPort == match.getInputPort()) {
            log.trace("ignoring packet that arrived on same port as learned destination:"
                    + " switch {} dest MAC {} port {}",
                    new Object[]{ sw, HexString.toHexString(destMac), outPort });
        } else {
            // Add flow table entry matching source MAC, dest MAC and input port
            // that sends to the port we previously learned for the dest MAC.
            match.setWildcards(((Integer)sw.getAttribute(IOFSwitch.PROP_FASTWILDCARDS)).intValue()
                    & ~OFMatch.OFPFW_IN_PORT
                    & ~OFMatch.OFPFW_DL_SRC & ~OFMatch.OFPFW_DL_DST
                    & ~OFMatch.OFPFW_NW_SRC_MASK & ~OFMatch.OFPFW_NW_DST_MASK);
            // CS6998: Fill out the following ????
            this.writeFlowMod(sw, OFFlowMod.OFPFC_ADD, pi.getBufferId(), match, outPort);
        }

        return Command.CONTINUE;
    }

    /**
     * Processes a flow removed message. 
     * @param sw The switch that sent the flow removed message.
     * @param flowRemovedMessage The flow removed message.
     * @return Whether to continue processing this message or stop.
     */
    private Command processFlowRemovedMessage(IOFSwitch sw, OFFlowRemoved flowRemovedMessage) {
        if (flowRemovedMessage.getCookie() != Switch.SWITCH_COOKIE) {   	
            return Command.CONTINUE;
        }

        Long sourceMac = Ethernet.toLong(flowRemovedMessage.getMatch().getDataLayerSource());
        Long destMac = Ethernet.toLong(flowRemovedMessage.getMatch().getDataLayerDestination());

        if (log.isTraceEnabled()) {
            log.trace("{} flow entry removed {}", sw, flowRemovedMessage);
        }

        // CS6998: Do works here to implement super firewall
        //  Hint: You may detect Elephant Flow here.
        //  ....
        //
        
        long bandwidth = (flowRemovedMessage.getDurationSeconds() != 0) ?flowRemovedMessage.getByteCount()/flowRemovedMessage.getDurationSeconds():0;
        //log.info("User Space: The flowRemovedMessage.getByteCount is {}", flowRemovedMessage.getByteCount());
        //log.info("User Space: The flowRemovedMessage.getDurationSeconds is {}", flowRemovedMessage.getDurationSeconds());
        if(bandwidth > Switch.ELEPHANT_FLOW_BAND_WIDTH) {
    		
        	//log.info("User Space: The bandwidth for flow from {} is {}", sourceMac, bandwidth);
    		// if there has been at least one elephant flow for the switch
    		if(swMaxFlows.containsKey(sw)) {
    			Set<Long> keys = swMaxFlows.get(sw).keySet();
    			int count = 0;
    			for(Long key : keys) {
    				count += swMaxFlows.get(sw).get(key);
    				//log.info("User Space: Switch {} already has {} elephant flows, but an elephant flow from src {} to dst {} is observed",new Object[] {sw, count, sourceMac, destMac});
    			}
    			
    			// and this current flow means the max flow is exceeded
    			if(count >= Switch.MAX_ELEPHANT_FLOW_NUMBER) {
    				//log.info("User Space: Blacklisted source {} of elephantflow for switch {}", sourceMac, sw);
        			blacklist.put(sourceMac, Switch.BLOCK_FOR_ELEPHANT);
    			} else {
    				// we have not exceeded the max flow count but keep tabs on this flow
    				// this is called for use case where the maximum allowed flow count is greater than 1
    				if(keys.contains(sourceMac)) {
    					Map<Long, Integer> tmpMap = swMaxFlows.get(sw);
    					tmpMap.put(sourceMac,tmpMap.get(sourceMac)+1);
    					swMaxFlows.put(sw, tmpMap);
    				}else {
    	    			Map<Long, Integer> tmpMap1 = Collections.synchronizedMap(new LinkedHashMap<Long, Integer>());
    	    			tmpMap1.put(sourceMac, 1);
    	    			swMaxFlows.put(sw, tmpMap1);
    				}
    			}
    		} else {
    			/* no entry for this switch
    			 make the entry and assign value of 1 for number of flows from host
    			 */
    			Map<Long, Integer> tmpMap1 = Collections.synchronizedMap(new LinkedHashMap<Long, Integer>());
    			tmpMap1.put(sourceMac, 1);
    			swMaxFlows.put(sw, tmpMap1);
    			//log.info("User Space: Logging entry on switch {} for elephant flow source {}", new Object[] {sw, sourceMac});
    		}
    	}        
        
        return Command.CONTINUE;
    }

    // IOFMessageListener
    
    @Override
    public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
        switch (msg.getType()) {
            case PACKET_IN:
                return this.processPacketInMessage(sw, (OFPacketIn) msg, cntx);
            case FLOW_REMOVED:
                return this.processFlowRemovedMessage(sw, (OFFlowRemoved) msg);
            case ERROR:
                log.info("received an error {} from switch {}", (OFError) msg, sw);
                return Command.CONTINUE;
            default:
                break;
        }
        log.error("received an unexpected message {} from switch {}", msg, sw);
        return Command.CONTINUE;
    }

    @Override
    public boolean isCallbackOrderingPrereq(OFType type, String name) {
        return false;
    }

    @Override
    public boolean isCallbackOrderingPostreq(OFType type, String name) {
        return false;
    }

    // IFloodlightModule
    
    @Override
    public Collection<Class<? extends IFloodlightService>> getModuleServices() {
        Collection<Class<? extends IFloodlightService>> l = 
                new ArrayList<Class<? extends IFloodlightService>>();
        return l;
    }

    @Override
    public Map<Class<? extends IFloodlightService>, IFloodlightService>
            getServiceImpls() {
        Map<Class<? extends IFloodlightService>,
            IFloodlightService> m = 
                new HashMap<Class<? extends IFloodlightService>,
                    IFloodlightService>();
        return m;
    }

    @Override
    public Collection<Class<? extends IFloodlightService>>
            getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l = 
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        return l;
    }

    @Override
    public void init(FloodlightModuleContext context)
            throws FloodlightModuleException {
        floodlightProvider =
                context.getServiceImpl(IFloodlightProviderService.class);
//	 CS6998: Initialize data structures
        macToSwitchPortMap = 
                new ConcurrentHashMap<IOFSwitch, Map<Long, Short>>();
        blacklist =
                new ConcurrentHashMap<Long, String>();
        hostDestinations = new ConcurrentHashMap<Long, Map<Long, Integer>>();
        swMaxFlows = new ConcurrentHashMap<IOFSwitch, Map<Long, Integer>>();

    }

    @Override
    public void startUp(FloodlightModuleContext context) {
        floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
        floodlightProvider.addOFMessageListener(OFType.FLOW_REMOVED, this);
        floodlightProvider.addOFMessageListener(OFType.ERROR, this);
	    InetAddress hostName = Inet4Address.getLoopbackAddress();
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
        }
    }
    
    class Block implements Runnable {
    	private Long sourceMac;
    	private IOFSwitch sw;
    	public Block(Long sourceMac, IOFSwitch sw) {
    		this.sourceMac = sourceMac;
    		this.sw = sw;
    	}
    	public void run() {
    		long timeNow = new Date().getTime();
			synchronized(this) {
				try{
					while((new Date().getTime() - timeNow) < Switch.FIREWALL_BLOCK_TIME_DUR) 
						this.wait(Switch.FIREWALL_BLOCK_TIME_DUR);
					notify();	
				} catch(InterruptedException e) {
					log.error("User Space: The firewall blocking of {} was interrupted unexpectedly: {}", sourceMac, e);
				}finally{
					//log.info("User Space: Source {}  is {}", sourceMac, blacklist.get(sourceMac));
					if(Switch.BLOCK_FOR_ELEPHANT.equals(blacklist.get(sourceMac))) {
						swMaxFlows.get(sw).remove(sourceMac);
					}
					if(Switch.BLOCK_FOR_MAX_DESTINATIONS.equals(blacklist.get(sourceMac))) {
						hostDestinations.remove(sourceMac);						
					}
					blacklist.remove(sourceMac);

				}
			}
    	}
    }
}