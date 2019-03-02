package cs455.scaling.server;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Task {
	private boolean taskComplete = false;
	private LinkedList<SelectionKey> keys;
	private byte[][] bytes;

	public Task(LinkedList<SelectionKey key> keys) {
		this.keys = keys;
	}

	private byte[] read(SelectionKey key) {
		ByteBuffer buffer = ByteBuffer.allocate(8000);
		SocketChannel client = (SocketChannel) key.channel();
		int read = 0;
		while(dataBuffer.hasRemaining() && read != -1) {
			read = client.read(buffer)
		}
		return dataBuffer.array();
	}

	private void send(SelectionKey key, bytes) {
		SocketChannel client = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.wrap(bytes);

		while(buffer.hasRemaining()) {
			synchronized (client) {
				client.write(buffer);
			}
		}
	}

	public void work() {
		for(SelectionKey key : keys) {
			byte[] dataRead = read(key);
			String hash = SHA1FromBytes(dataRead);

		}
	}


	private String SHA1FromBytes(byte[] data) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA1");
			byte[] hash = digest.digest(data);
			BigInteger hashInt = new BigInteger(1, hash);
			return hashInt.toString(16);
		}catch(NoSuchAlgorithmException nsae) {
			System.out.println("NO SUCH ALGORITHM!");
		}finally {
			return "";
		}
	}

	public boolean isComplete() { return this.taskComplete; }
}
