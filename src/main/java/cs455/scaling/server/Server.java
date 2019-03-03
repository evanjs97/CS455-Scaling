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

	public Server(int port, int threadCount, int batchSize, int batchTime) throws IOException{
		this.port = port;
		this.serverChannel = ServerSocketChannel.open();
		System.out.println("opened server socket channel");
		this.hostname = InetAddress.getLocalHost().getHostName();
		serverChannel.bind(new InetSocketAddress(hostname, port));
		System.out.println("Bound server to " + hostname + " on port " + port);
		serverChannel.configureBlocking(false);

		selector = Selector.open();
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		System.out.println("Registered server with OP_ACCEPT key");



		ThreadPoolManager.open(threadCount, batchSize, batchTime, selector);
		System.out.println("Opened ThreadPoolManager");
		Thread thread = new Thread(ThreadPoolManager.getInstance());
		thread.start();
	}


	private void listen() {
		while(true) {
			try {
				int numKeys = selector.selectNow();
				if(numKeys == 0)continue;
				Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();
				while (keyIter.hasNext()) {
					SelectionKey key = keyIter.next();
					if (!key.isValid()) continue;
					if (key.isAcceptable()) {
						ThreadPoolManager.getInstance().addJob(SelectionKey.OP_ACCEPT, serverChannel, key);
					}else if (key.isReadable()) {
						SocketChannel channel = (SocketChannel) key.channel();
						ThreadPoolManager.getInstance().addJob(SelectionKey.OP_READ, channel, key);
					}
					keyIter.remove();
				}
			}catch (IOException ioe) {

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
				int batchTime = Integer.parseInt(args[3]);
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
