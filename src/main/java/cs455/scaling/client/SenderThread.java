package cs455.scaling.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Random;

public class SenderThread implements Runnable{
	private int messageRate;
	private Client client;

	public SenderThread(int messageRate, Client client) {
		this.messageRate = messageRate;
		this.client = client;
	}

	private void writeData() throws IOException {
		byte[] data = new byte[8000];
		new Random().nextBytes(data);
		String hash = Client.SHA1FromBytes(data);
		hash = pad(hash, 40);
		client.addHash(hash);
		//System.err.println("Sent: " + hash);
		ByteBuffer dataBuffer = ByteBuffer.wrap(data);
		//System.out.println("Writing to channel: " + hash);
		client.getSocketChannel().write(dataBuffer);
		client.sentMessage();
	}

	private static String pad(String hash, int length) {
		while(hash.length() < length) hash = "0" + hash;
		return hash;
	}

	@Override
	public void run() {
		while(true) {
			try {
				writeData();
				Thread.sleep(1000/ messageRate);
			}catch (IOException ioe) {
				System.out.println("IOEXCEPTION: " + ioe);
			}catch (InterruptedException ie){

			}
		}
	}
}
