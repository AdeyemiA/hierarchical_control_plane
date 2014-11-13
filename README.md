=====================================================================
The softmow controller is built on top of Opendaylight. When running
this, we assume the Opendaylight is pre-installed by maven. (Maven
manage all installed in ~/.m2/repository). 

=====================================================================
There are two applications in softmow controller: RecA and Mobility.
RecA reads the topology information from the Opendaylight platform and
compute the abstract G-switch, and it speaks Openflow to its parent.

Mobility app listens to the packet-in message. If it has a local path it
sets up the path; otherwise, it sends packets to RecA.

=====================================================================
The framework is just implement the communication between Opendaylight
and the two apps. You need to implement the internal logic of mobility
app and RecA; and you also need to implement the communication between
the two apps and between RecA and parent controller's Opendaylight
platform.

=====================================================================
Structure of the code:
common: This is some common variables in the project build
distribution: This includes the original Opendaylight controller, as we
    just implement 2 plugins on the platform, you do not need to change
    it.
reca: This is the reca plugin. It is compiled and put into the plugin 
    folder in distribution. You need to implement its logic. 
mobility: This is the mobility plugin, and you need to implement it.

=====================================================================
To run:
    cd <softmow root>  
        // the following command compile the two plugins, and put them  into distribution's plugins folder 
    mvn install     
        // the runnable java package is in distribution folder and the run.sh runs the java package.
    ./distribution/opendaylight-osgi/target/distribution-osgi-1.0.0-osgipackage/opendaylight/run.sh 

Start mininet and configure the switch to connect to softmow controller,
you will observe the packet-in messages in mobility app; you need to
process these messages (either set path for them or report to RecA). 
As for RecA, I create the topology and routing object in reca.java, you 
can use them for path and G-switch computation. 

=====================================================================
I may expect several difficulties in this project:
1. The reca topo module is not tested. So if it does not work, you need
to fix that.
2. The reca agent needs to speack Openflow to parent controller's
Opendaylight platform. So you need to implement an agent which acts as a
switch with Openflow 1.3.
3. This framework is made in a hurry. I build this by modifying the
L2_forwarding tutorial. I am not sure whether it has any other bugs. 

If you have questions about SoftMoW, Opendaylight and Mininet, I am glad
to help you.

=====================================================================
Good luck!
=====================================================================
