This is the project of tree controller node under hierarchical control plane.

How to build from scratch:
1. javac *.java
2. java start (<configFileName>)

How to build with eclipse:
import into eclipse and right click "start.java" and choose "run as java application"

Files introduction:
1. start.java
	main entrance of the project
2. ControllerNode.java
	main class for controller
3. Graph.java
	class for topology
4. GSwitch.java
	class for virtual gswitch
5. Host.java
	class for host
6. Client.java
	terminal interface to directly contact controller server
