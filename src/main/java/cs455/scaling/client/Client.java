package cs455.scaling.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class Client {

	private String serverHost;
	private int serverPort;
	private int messageRate;
	private SocketChannel socketChannel;
	private Selector selector;

	public Client(String serverHost, int serverPort, int messageRate) throws IOException{
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		selector = Selector.open();

		socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);
		socketChannel.connect(new InetSocketAddress(serverHost, serverPort));
		socketChannel.register(selector, SelectionKey.OP_CONNECT);
	}

	private void sendData() {
		while(true) {
			try {
				writeData();
				Thread.sleep(1000 / messageRate);
			}catch(InterruptedException ie) {

			}
		}
	}

	private void writeData() {
		ByteBuffer data = ByteBuffer.wrap(new byte[8000]);
		data.put()
	}

	public static void main(String[] args) {
		if(args.length < 3) {
			System.out.println("Error: Client requires three arguments");
			System.exit(1);
		}else {
			try {
				String serverHost = args[0];
				int serverPort = Integer.parseInt(args[1]);
				int messageRate = Integer.parseInt(args[2]);

				Client client = new Client(serverHost, serverPort, messageRate);
				client.sendData();
			}catch(NumberFormatException nfe) {
				System.out.println("Error while parsing arguments: " + nfe);
			}catch(IOException ioe) {
				System.out.println("Error while opening socket channel: " + ioe);
			}
		}
	}
}
