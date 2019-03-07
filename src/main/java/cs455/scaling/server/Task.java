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

	public Task(LinkedList<Job> jobs)
	{
//		System.out.print("Task created with: " );
//		for(Job job : jobs) { System.out.print(job + " --> "); }
//		System.out.println();
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
				System.out.println("Error while reading");
			}
		}
		return buffer.array();
	}

	private void write(String hash, Job job) {
		try {
			ByteBuffer dataBuffer = ByteBuffer.wrap(hash.getBytes());
			job.getSocketChannel().write(dataBuffer);
			//System.out.println("Wrote to channel: " + "   " + hash);
		}catch (IOException ioe) {
			System.out.println("Exception while writing to client: " + ioe);
		}
	}
//
//	private void send(SelectionKey key, byte[] bytes) {
//		SocketChannel client = (SocketChannel) key.channel();
//		ByteBuffer buffer = ByteBuffer.wrap(bytes);
//
//		while(buffer.hasRemaining()) {
//			synchronized (client) {
//				try {
//					client.write(buffer);
//				}catch(IOException ioe) {
//
//				}
//			}
//		}
//	}

	private void registerClient(Job job) {
		try {
			SocketChannel client = job.getServerSocketChannel().accept();
			client.configureBlocking(false);
			client.register(ThreadPoolManager.getInstance().getSelector(), SelectionKey.OP_READ);
			ThreadPoolManager.getInstance().registerClient(client.toString());
			System.out.println("Client has registered with server.");
			ThreadPoolManager.getInstance().reregister(job.getServerSocketChannel());
		}catch (IOException ioe) {
			System.out.println("ERROR: IOException while registering client");
		}
	}

	private static String pad(String hash, int length) {
		while(hash.length() < length) hash = "0" + hash;
		return hash;
	}

	public void work() {
		taskComplete = true;
		for(Job job : jobs) {
			if(job.getType() == SelectionKey.OP_ACCEPT) registerClient(job);
			else {

				byte[] dataRead = read(job);
				String hash = SHA1FromBytes(dataRead);
				hash = pad(hash, 40);
				System.out.println("Reading and Writing from client: " + hash);
				write(hash, job);
				ThreadPoolManager.getInstance().incrementMessageCount(job.getSocketChannel().toString());
				System.out.println("Finished Reading and Writing from Client");
			}
//			ThreadPoolManager.getInstance().reregister(job.getChannel());
			//ThreadPoolManager.getInstance().removeKey(job.getChannel());
//			System.out.println("removing key");
			synchronized (job.getKey()) {
				job.getKey().attach(null);
			}
			System.out.println("Removed key");
		}
	}

	public int numJobs() {
		return this.jobs.size();
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
