package cs455.scaling.client;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.LinkedList;

public class Client {
	private final LinkedList<String> hashcodes = new LinkedList<>();
	private final String serverHost;
	private final int serverPort;
	private final int messageRate;
	private final SocketChannel socketChannel;
	private final Selector selector;

	public Client(String serverHost, int serverPort, int messageRate) throws IOException{
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.messageRate = messageRate;
		selector = Selector.open();

		socketChannel = SocketChannel.open();
		socketChannel.configureBlocking(false);
		socketChannel.connect(new InetSocketAddress(serverHost, serverPort));
		int ops  = socketChannel.validOps();
		socketChannel.register(selector, SelectionKey.OP_CONNECT);
	}

//	private void sendData() throws IOException{
//		try {
//			writeData();
//		}catch(InterruptedException ie) {
//
//		}
//	}

	private ByteBuffer readData(SelectionKey key) throws IOException {
		ByteBuffer dataBuffer = ByteBuffer.allocate(20);
		int read = 0;
		while(dataBuffer.hasRemaining() && read != -1) {
			read = socketChannel.read(dataBuffer);
		}
		return dataBuffer;
	}

	private String bufferHash(ByteBuffer buffer) {
		buffer.rewind();
		String hash = SHA1FromBytes(buffer.array());
		return hash;
	}

	private static boolean compareHashes(String hash1, String hash2) {
		return hash1.equals(hash2);
	}

	public final SocketChannel getSocketChannel() {
		return this.socketChannel;
	}


	public void addHash(String hash) {
		synchronized (hashcodes) {
			this.hashcodes.addLast(hash);
		}
	}

	public static String SHA1FromBytes(byte[] data) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA1");
			byte[] hash = digest.digest(data);
			BigInteger hashInt = new BigInteger(1, hash);
			return hashInt.toString(16);
		}catch (NoSuchAlgorithmException nsae) {
			System.out.println("Issue finding SHA1 algorithm: " + nsae);
		}
		return "";
	}

	private void openSender(int messageRate) {

	}

	public void runClient() {
		while(true) {
			try {
				Iterator keys = selector.selectedKeys().iterator();
				while(keys.hasNext()) {
					SelectionKey key = (SelectionKey) keys.next();
					if(key.isConnectable()) {
						socketChannel.finishConnect();
						socketChannel.register(selector, SelectionKey.OP_READ);
					}else if(key.isReadable()) {
						readData(key);
					}
				}
			}catch(IOException ioe) {
				System.out.println("IOException: " + ioe);
			}
		}
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
				client.runClient();
			}catch(NumberFormatException nfe) {
				System.out.println("Error while parsing arguments: " + nfe);
			}catch(IOException ioe) {
				System.out.println("Error while opening socket channel: " + ioe);
			}
		}
	}
}
