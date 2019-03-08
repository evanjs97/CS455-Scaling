## CS455-Distributed Systems: Scalable Server
### Author: Evan Steiner
### Purpose: Building a scalable server using thread pools to manage and load balance active network connections

#### Instructions, Compilation/Running:
* Compile on command line: "gradle build"
* run start_scaling.sh script
* start_scaling.sh will default to starting registry on santa-fe on port 3007, but you can modify this if you like.
* Server will open in a large window and all the clients will open as tabs in smaller window.
* The clients will begin sending messages to the server once connected
* You may also specify which machines will be used as clients in the machine_list file, currently it contains a list of 10 machines
* You may also change other values in the start_scaling.sh script such as:
	* Num_Threads which is currently set at 50
	* Batch_Size which is currently set at 20
	* Batch_Time which is currently set at 2 (seconds)
	* Message_Rate which is currently set at 10 (messages per second)
	* Clients_Per_Machine is the number of clients to start on each machine in the machine_list file
	
#### Notes regarding runtime:
* Sometimes some nodes will send fewer messages then messageRate, this seems like it mostly occurs when a particular machine is in high use. A machine in high use by other students will have trouble maintaining a high messages sent per second rate. Only an issue if machines are in high use.
* Sometimes not all the clients will start up and connect to server, once again likely due to usage (i.e if you start 100 clients, only 99 or 98 might start and connect)
* While client messages will be processed once (BatchSize or Time is reached), client connection requests will be processed as soon as possible, regardless of batchTime or batchSize. This is so that starting up all the clients doesn't take too long (i.e) having to wait till batchTime to register the first few clients.

#### Classes/Project Structure:
* client
	* Client.java: A client which will start a connection with the server takes arguments [0] = serverHost, [1] = serverPort, [2] = messageRate
	* ClientStatistics.java: Keeps track of statistics for the client (messages sent and received)
	* SenderThread.java: The thread that continuously sends messages to the server as specified by messageRate
* server
	* Job.java: A job that will be completed by a thread, a Task consists of many jobs (usually Batch_Size)
	* Server.java: The Server which opens up other threads to handle all connections
	* ServerStatistics.java: The statistics for the server
	* Task.java: A task is a list of jobs to complete. Once all jobs have been ran, the task will finish.
	* ThreadPoolManager.java: Is its own thread and will assign jobs to the threads it holds when able
	* WorkerThread.java: Is a thread that will complete any given task it is assigned, then return itself to the threadpool