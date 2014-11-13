package reca;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.opendaylight.controller.sal.core.Edge;
import org.opendaylight.controller.sal.flowprogrammer.IFlowProgrammerService;
import org.opendaylight.controller.sal.packet.IDataPacketService;
import org.opendaylight.controller.sal.packet.IListenDataPacket;
import org.opendaylight.controller.sal.packet.PacketResult;
import org.opendaylight.controller.sal.packet.RawPacket;
import org.opendaylight.controller.sal.routing.IRouting;
import org.opendaylight.controller.sal.topology.IListenTopoUpdates;
import org.opendaylight.controller.sal.topology.TopoEdgeUpdate;
import org.opendaylight.controller.switchmanager.ISwitchManager;
import org.opendaylight.controller.topologymanager.ITopologyManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class reca extends Observable implements IListenTopoUpdates, Observer {
    private static final Logger logger = LoggerFactory
            .getLogger(reca.class);
    private ISwitchManager switchManager = null;
    private IFlowProgrammerService programmer = null;
    private IDataPacketService dataPacketService = null;
    private ITopologyManager topoManager=null;
    private IRouting routing=null;

    

    void setDataPacketService(IDataPacketService s) {
        this.dataPacketService = s;
    }

    void unsetDataPacketService(IDataPacketService s) {
        if (this.dataPacketService == s) {
            this.dataPacketService = null;
        }
    }

    public void setFlowProgrammerService(IFlowProgrammerService s)
    {
        this.programmer = s;
    }

    public void unsetFlowProgrammerService(IFlowProgrammerService s) {
        if (this.programmer == s) {
            this.programmer = null;
        }
    }

    void setSwitchManager(ISwitchManager s) {
        logger.debug("SwitchManager set");
        this.switchManager = s;
    }

    void unsetSwitchManager(ISwitchManager s) {
        if (this.switchManager == s) {
            logger.debug("SwitchManager removed!");
            this.switchManager = null;
        }
    }


    void setTopologyManager(ITopologyManager t) {
        logger.debug("SwitchManager set");
        this.topoManager = t;
    }

    void unsetTopologyManager(ITopologyManager t) {
        if (this.topoManager == t) {
            logger.debug("SwitchManager removed!");
            this.topoManager = null;
        }
    }
    
    void setRouting(IRouting r) {
        logger.debug("SwitchManager set");
        this.routing = r;
    }

    void unsetRouting(IRouting r) {
        if (this.routing == r) {
            logger.debug("SwitchManager removed!");
            this.routing = null;
        }
    }
    /**
     * Function called by the dependency manager when all the required
     * dependencies are satisfied
     *
     */
    void init() {
        logger.info("Initialized");
        // Disabling the SimpleForwarding and ARPHandler bundle to not conflict with this one
        BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        for(Bundle bundle : bundleContext.getBundles()) {
            if (bundle.getSymbolicName().contains("simpleforwarding")) {
                try {
                    bundle.uninstall();
                } catch (BundleException e) {
                    logger.error("Exception in Bundle uninstall "+bundle.getSymbolicName(), e); 
                }   
            }   
        }   
        initReca();
    }

    /**
     * Function called by the dependency manager when at least one
     * dependency become unsatisfied or when the component is shutting
     * down because for example bundle is being stopped.
     *
     */
    void destroy() {
    }

    /**
     * Function called by dependency manager after "init ()" is called
     * and after the services provided by the class are registered in
     * the service registry
     *
     */
    void start() {
        logger.info("Started");
    }

    /**
     * Function called by the dependency manager before the services
     * exported by the component are unregistered, this will be
     * followed by a "destroy ()" calls
     *
     */
    void stop() {
        logger.info("Stopped");
    }
    
    /**********************************************************************
     * implement this by your self
     *******************************************************************/    
    private void operatorThread(){
    	byte []msg = null;  
    	// read msg from socket
    	setChanged();
    	notifyObservers(new Msg(TYPE.APP_MSG, msg));
    }

    /**********************************************************************
     * implement this by your self
     *******************************************************************/
    private void agentThread(){
    	byte []msg = null;
    	// read msg from socket
    	setChanged();
    	notifyObservers(new Msg(TYPE.PARENT_MSG, msg));
    }

    /**********************************************************************
     * implement this by your self
     *******************************************************************/
    private void initReca(){
    	System.out.println("Init reca.");
    	this.addObserver(this);   	
    	abstraction();
    	
    	// create a thread to listen to mobility application, operatorThread()
    	
    	// create a thread to act as agent. agentThread()
    	
    	setChanged();
    	notifyObservers(new Msg(TYPE.TOPO_CHANGE, null));
    }

    /**********************************************************************
     * implement this by your self
     *******************************************************************/
    private void abstraction(){
    	System.out.println("Computing Abstraction...");
    	// compute G-switch by topology
    	// use ITopologyManager topoManager
    	// https://developer.cisco.com/media/XNCJavaDocs/org/opendaylight/controller/topologymanager/ITopologyManager.html
    	
    }
    
  
    
    
	@Override
	public void edgeOverUtilized(Edge arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void edgeUpdate(List<TopoEdgeUpdate> arg0) {
		// TODO Auto-generated method stub
		abstraction();
		setChanged();
		notifyObservers(new Msg(TYPE.TOPO_CHANGE, null));
	}

	@Override
	public void edgeUtilBackToNormal(Edge arg0) {
		// TODO Auto-generated method stub
		abstraction();
		setChanged();
		notifyObservers(new Msg(TYPE.TOPO_CHANGE, null));
	}

    /**********************************************************************
     * implement this by your self
     *******************************************************************/
	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		Msg msg = (Msg) arg1;
		switch(msg.type){
			case TOPO_CHANGE:
				System.out.println("Topology changed, notify the parent");
				// use the agent socket to send message to parent
				break;
			case APP_MSG:
				System.out.println("Get msg from operator app, send it to the parent");
				// send the msg as packet in message to the parent
				break;
			case PARENT_MSG:
				System.out.println("Get path setup msg from parent, translate it and install rule.");
				// translate the message from G-switch to topology, compute a path and set it up
				// Use the IRouting interface : https://developer.cisco.com/media/XNCJavaDocs/org/opendaylight/controller/sal/routing/IRouting.html
				
		}
	}

	private enum TYPE{
		TOPO_CHANGE, APP_MSG, PARENT_MSG
	}
	private class Msg{
		public TYPE type;
		public Object obj;	
		public Msg(TYPE theType, Object theObj){
			type=theType;
			obj=theObj;
		}
	}
}
