## CS455-Distributed Systems: Scalable Server
### Author: Evan Steiner
### Purpose: Building a scalable server using thread pools to manage and load balance active network connections

#### Instructions, Compilation/Running:
* Compile on command line: "gradle build"
* run start_scaling.sh script
* start_scaling.sh will default to starting registry on washington-dc on port 3007, but you can modify this if you like.
* Server will open in a large window and all the clients will open as tabs in smaller window.
* The clients will begin sending messages to server once connected

#### Classes/Project Structure:
* client
	* Client.java: 
	* ClientStatistics.java: 
	* SenderThread.java: 
* server
	*