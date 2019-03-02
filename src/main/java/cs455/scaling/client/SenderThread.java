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
		client.addHash(Client.SHA1FromBytes(data));

		ByteBuffer dataBuffer = ByteBuffer.wrap(data);
		while(dataBuffer.hasRemaining()) {
			synchronized (client.getSocketChannel()) {
				client.getSocketChannel().write(dataBuffer);
			}
		}
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
