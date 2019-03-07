package cs455.scaling.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

public class Server {
	private final int port;
	private final String hostname;
	private final ServerSocketChannel serverChannel;
	private final Selector selector;

	public Server(int port, int threadCount, int batchSize, double batchTime) throws IOException{
		this.port = port;
		this.serverChannel = ServerSocketChannel.open();
		System.out.println("opened server socket channel");
		this.hostname = InetAddress.getLocalHost().getHostName();
		serverChannel.bind(new InetSocketAddress(hostname, port));
		System.out.println("Bound server to " + hostname + " on port " + port);
		serverChannel.configureBlocking(false);

		selector = Selector.open();
		SelectionKey key = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("Registered server with OP_ACCEPT key");



		ThreadPoolManager.open(threadCount, batchSize, batchTime, selector);
		System.out.println("Opened ThreadPoolManager");
		Thread thread = new Thread(ThreadPoolManager.getInstance());
		thread.start();
	}

	private void log() {

		System.out.println(ThreadPoolManager.getInstance().logAndReset());
	}


	private void listen() {
		long time = System.nanoTime();
		while(true) {

			try {
				int numKeys = selector.selectNow();
//				System.out.println("Running: "  +System.nanoTime());
				if(numKeys == 0) {

					long endTime = System.nanoTime();
					long timeDiff = (endTime - time) / 1000000000;
					if(timeDiff >= 20) {
//						for(SelectionKey key : selector.selectedKeys()) {
//							System.out.println("Key: " + key.interestOps());
//						}
					}
					continue;
				}
				//System.out.println(numKeys);
				Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
				//System.out.println("Started While Loop");
				while (keyIter.hasNext()) {
					SelectionKey key = keyIter.next();
					synchronized (key) {
						//if (!key.isValid()) continue;
						//System.out.println(key.attachment());
						if (!key.isValid() || key.attachment() != null) continue;
						if (key.isAcceptable()) {
							key.attach(new Boolean(true));
							serverChannel.register(selector, key.interestOps() & ~SelectionKey.OP_ACCEPT);
							ThreadPoolManager.getInstance().addJob(SelectionKey.OP_ACCEPT, serverChannel, key);
						} else if (key.isReadable()) {
							key.attach(new Boolean(true));
							//SocketChannel channel = (SocketChannel) key.channel();
							ThreadPoolManager.getInstance().addJob(SelectionKey.OP_READ, key.channel(), key);
						}
					}
					keyIter.remove();
				}
				selector.selectedKeys().clear();
				//System.out.println("Finished While Loop");
			}catch (IOException ioe) {
				System.out.println(ioe);
			}
			long endTime = System.nanoTime();
			long timeDiff = (endTime - time) / 1000000000;
			//System.out.println("TIME DIFFERENCE: " + timeDiff);
			if(timeDiff >= 20) {
				log();
				time = endTime;
			}
		}
	}


	public static void main(String[] args) {
		if(args.length < 4) {
			System.out.println("Error: Server requires four arguments.");
			System.exit(1);
		}else {
			try {
				int port = Integer.parseInt(args[0]);
				int threadCount = Integer.parseInt(args[1]);
				int batchSize = Integer.parseInt(args[2]);
				double batchTime = Double.parseDouble(args[3]);
				System.out.println("PORT: " + port + " Threads: " + threadCount + " Batch Size: " + batchSize + " Batch Time: " + batchTime);
				Server server = new Server(port, threadCount, batchSize, batchTime);
				System.out.println("Starting up server on " + server.hostname + " using port " + server.port);
				server.listen();
			}catch(NumberFormatException e) {
				System.out.println("Error parsing arguments: " + e);
			}catch(IOException e) {
				System.out.println("Error opening server: " + e);
			}
		}
	}
}
