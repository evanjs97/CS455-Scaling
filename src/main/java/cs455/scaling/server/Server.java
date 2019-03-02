package cs455.scaling.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class Server {
	private int port;
	private String hostname;

	private Selector selector;

	public Server(int port, int threadCount, int batchSize, int batchTime) throws IOException{
		this.port = port;
		this.serverChannel = ServerSocketChannel.open();
		selector = Selector.open();
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		serverChannel.configureBlocking(false);

		this.hostname = serverChannel.socket().getInetAddress().getHostName();
		ThreadPoolManager.open(threadCount, batchSize, batchTime);
		ThreadPoolManager.getInstance().run();
	}

	private void listen() {
		while(true) {
			if(serverChannel.)
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

				Server server = new Server(port, threadCount, batchSize, batchTime);

			}catch(NumberFormatException e) {
				System.out.println("Error parsing arguments: " + e);
			}catch(IOException e) {
				System.out.println("Error opening server: " + e);
			}
		}
	}
}
