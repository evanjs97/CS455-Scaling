package cs455.scaling.server;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;

public class Task {
	private volatile boolean taskComplete = false;
	private LinkedList<Job> jobs;
	private byte[][] bytes;

	public Task(LinkedList<Job> jobs) {
		this.jobs = jobs;
	}

	private byte[] read(Job job) {
		ByteBuffer buffer = ByteBuffer.allocate(8000);
		SocketChannel client = job.getSocketChannel();
		int read = 0;
		while(buffer.hasRemaining() && read != -1) {
			try {
				read = client.read(buffer);
			}catch (IOException ioe) {

			}
		}
		return buffer.array();
	}

	private void write(String hash, Job job) {
		try {
			ByteBuffer dataBuffer = ByteBuffer.wrap(hash.getBytes());
			job.getSocketChannel().write(dataBuffer);
		}catch (IOException ioe) {
			System.out.println("Exception while writing to client: " + ioe);
		}
	}

	private void send(SelectionKey key, byte[] bytes) {
		SocketChannel client = (SocketChannel) key.channel();
		ByteBuffer buffer = ByteBuffer.wrap(bytes);

		while(buffer.hasRemaining()) {
			synchronized (client) {
				try {
					client.write(buffer);
				}catch(IOException ioe) {

				}
			}
		}
	}

	private void registerClient(Job job) {
		try {
			SocketChannel client = job.getServerSocketChannel().accept();
			client.configureBlocking(false);
			client.register(ThreadPoolManager.getInstance().getSelector(), SelectionKey.OP_READ);
			System.out.println("Client has registered with server.");
		}catch (IOException ioe) {
			System.out.println("ERROR: IOException while registering client");
		}
	}

	public void work() {
		taskComplete = true;
		for(Job job : jobs) {
			if(job.getType() == SelectionKey.OP_ACCEPT) registerClient(job);
			else {
				byte[] dataRead = read(job);
				String hash = SHA1FromBytes(dataRead);
				System.out.println("Received From Channel: " + hash);
				write(hash, job);
			}
			ThreadPoolManager.getInstance().removeKey(job.getKey());
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
		}
		return "";
	}

	public boolean isComplete() { return this.taskComplete; }
}
