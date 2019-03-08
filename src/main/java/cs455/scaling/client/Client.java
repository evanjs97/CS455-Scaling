package cs455.scaling.client;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;


public class Client {
	private final HashSet<String> hashcodes = new HashSet<>();
	private final String serverHost;
	private final int serverPort;
	private final int messageRate;
	private final SocketChannel socketChannel;
	private final Selector selector;
	private SenderThread senderThread;
	private long startTime;
	private final ClientStatistics statistics = new ClientStatistics();

	/**
	 * Client constructor creates client socket channel, connects it to server and registers it with selector
	 * @param serverHost the name of the server to connect to
	 * @param serverPort the port to connect over
	 * @param messageRate the number of messages to send per second to the server
	 * @throws IOException if socket channel opening fails
	 */
	private Client(String serverHost, int serverPort, int messageRate) throws IOException{
		this.serverHost = serverHost;
		this.serverPort = serverPort;
		this.messageRate = messageRate;
		selector = Selector.open();
		socketChannel = SocketChannel.open(new InetSocketAddress(serverHost, serverPort));
		socketChannel.configureBlocking(false);
		InetSocketAddress address = (InetSocketAddress) socketChannel.getLocalAddress();
		System.out.println("Client started on " + address.getHostName() + ":" + address.getPort());
		socketChannel.register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
	}


	void sentMessage() { statistics.sendMessage(); }
	private void receivedMessage() { statistics.receivedMessage(); }

	private String readData(SelectionKey key) throws IOException {
		ByteBuffer dataBuffer = ByteBuffer.allocate(40);
		int read = 0;
		while(dataBuffer.hasRemaining() && read != -1) {
			read = socketChannel.read(dataBuffer);
		}
		String data = new String(dataBuffer.array());
		if(data.length() < 40) System.out.println("Failure: Data read is: " + data.length());
		return data;
	}

	final SocketChannel getSocketChannel() {
		return this.socketChannel;
	}


	final void addHash(String hash) {
		synchronized (hashcodes) {
			this.hashcodes.add(hash);
		}
	}

	/**
	 * Takes a byte array of data, applies the SHA1 hashing algorithm to the data and returns a String representation
	 * @param data the byte[] to hash
	 * @return a string representation of hash of data
	 */
	static String SHA1FromBytes(byte[] data) {
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

	private void openSender() {
		senderThread = new SenderThread(messageRate, this);
		Thread thread = new Thread(senderThread);
		thread.start();
	}

	private void runClient() {
		startTime = System.nanoTime();
		while(true) {
			try {
				int numKeys = selector.select();
				if(numKeys == 0) continue;
				Iterator keys = selector.selectedKeys().iterator();
				while(keys.hasNext()) {
					SelectionKey key = (SelectionKey) keys.next();
					if(!key.isValid()) {
						System.out.println("Not Valid Key: ");
						continue;
					}

					if(key.isReadable()) {
						String data = readData(key);
						synchronized (hashcodes) {
							if (hashcodes.remove(data)) {
								receivedMessage();
							}
						}

					}
					if(key.isWritable()) {
						System.out.println("Opening Sender");
						socketChannel.register(selector, SelectionKey.OP_READ);
						openSender();
					}

					keys.remove();

				}
				selector.selectedKeys().clear();
				long currTime = System.nanoTime();
				long timeDiff =  (currTime - startTime) / 1000000000;
				if(timeDiff >= 20) {
					statistics.log();
					startTime = currTime;
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
				System.out.println("Starting up client: Connecting to server " + serverHost + " on port " + serverPort);
				Client client = new Client(serverHost, serverPort, messageRate);
				while(!client.getSocketChannel().isConnected()){}
				try {
					Thread.sleep(1000);
				}catch (InterruptedException ie) {}
				System.out.println("Connected");
				client.runClient();
			}catch(NumberFormatException nfe) {
				System.out.println("Error while parsing arguments: " + nfe);
			}catch(IOException ioe) {
				System.out.println("Error while opening socket channel: " + ioe);
			}
		}
	}
}
