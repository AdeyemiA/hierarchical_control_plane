package mobility;

import java.util.Observable;
import java.util.Observer;

import org.opendaylight.controller.sal.flowprogrammer.IFlowProgrammerService;
import org.opendaylight.controller.sal.packet.IDataPacketService;
import org.opendaylight.controller.sal.packet.IListenDataPacket;
import org.opendaylight.controller.sal.packet.PacketResult;
import org.opendaylight.controller.sal.packet.RawPacket;
import org.opendaylight.controller.sal.routing.IRouting;
import org.opendaylight.controller.switchmanager.ISwitchManager;
import org.opendaylight.controller.topologymanager.ITopologyManager;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class mobility extends Observable implements IListenDataPacket, Observer {
    private static final Logger logger = LoggerFactory
            .getLogger(mobility.class);
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
        initMobility();
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

    /***************************************************
     * Implement this by yourself
     ***************************************************/    
    private void recaThread(){
    	byte []msg=null;
    	// read msg from reca
    	setChanged();
    	notifyObservers(new Msg(TYPE.RECA_MSG, msg));
    }
    
    /***************************************************
     * Implement this by yourself
     ***************************************************/    
    private void initMobility(){
    	// build a socket thread to read message from RecA, recaThread()
    	
    	this.addObserver(this);
    }
    
    
	@Override
	public PacketResult receiveDataPacket(RawPacket arg0) {
		setChanged();
		notifyObservers(new Msg(TYPE.PKTIN_MSG, arg0));
		return PacketResult.KEEP_PROCESSING;
	}

    /***************************************************
     * Implement this by yourself
     ***************************************************/    
	@Override
	public void update(Observable arg0, Object arg1) {
		// TODO Auto-generated method stub
		Msg msg=(Msg)arg1;
		switch(msg.type){
		case PKTIN_MSG:
			System.out.println("Mobility get a packet-in msg, need to process it");
			// use routing to get the route,
			// if no path, send to reca
			break;
		case RECA_MSG:
			System.out.println("Mobility get a RecA msg");
			// leav this prcessing blank, I leave the space for future extension
			
		
		}
	}
	private enum TYPE{
		PKTIN_MSG, RECA_MSG
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
