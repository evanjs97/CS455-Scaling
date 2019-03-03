package cs455.scaling.server;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

public class Server {
	private final int port;
	private final String hostname;
	private final ServerSocketChannel serverChannel;
	private final Selector selector;

	public Server(int port, int threadCount, int batchSize, int batchTime) throws IOException{
		this.port = port;
		this.serverChannel = ServerSocketChannel.open();
		this.hostname = serverChannel.socket().getInetAddress().getHostName();
		serverChannel.bind(new InetSocketAddress(hostname, port))
		serverChannel.configureBlocking(false);

		selector = Selector.open();
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);



		ThreadPoolManager.open(threadCount, batchSize, batchTime);
		ThreadPoolManager.getInstance().run();
	}

	private void listen() {
		while(true) {
			selector.select();
			Iterator<SelectionKey> keyIter = selector.selectedKeys().iterator();

			while(keyIter.hasNext()) {
				SelectionKey key = keyIter.next();
				if(key.isValid() == false) continue;

				if(key.isAcceptable()) registerClient();

				if(key.isReadable())
			}
		}
	}

	private void registerClient() {
		SocketChannel client = serverChannel.accept();
		client.configureBlocking(false);
		client.register(selector, SelectionKey.OP_READ);
		System.out.println("Client has registered with server.");
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
				server.listen();
			}catch(NumberFormatException e) {
				System.out.println("Error parsing arguments: " + e);
			}catch(IOException e) {
				System.out.println("Error opening server: " + e);
			}
		}
	}
}
