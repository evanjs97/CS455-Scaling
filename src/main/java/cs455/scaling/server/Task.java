package cs455.scaling.server;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.LinkedList;

public class Task {
	//private volatile boolean taskComplete = false;
	private LinkedList<Job> jobs;
	//private byte[][] bytes;

	Task(LinkedList<Job> jobs)
	{
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
		}catch (IOException ioe) {
			System.out.println("Exception while writing to client: " + ioe);
		}
	}

	private void registerClient(Job job) {
		try {
			SocketChannel client = job.getServerSocketChannel().accept();
			client.configureBlocking(false);
			ThreadPoolManager.getInstance().registerClient(client);
		}catch (IOException ioe) {
			System.out.println("ERROR: IOException while registering client");
		}
	}

	private static String pad(String hash, int length) {
		while(hash.length() < length) hash = "0" + hash;
		return hash;
	}

	void work() {
		//taskComplete = true;
		for(Job job : jobs) {
			if(job.getType() == SelectionKey.OP_ACCEPT) registerClient(job);
			else {

				byte[] dataRead = read(job);
				String hash = SHA1FromBytes(dataRead);
				hash = pad(hash, 40);
				write(hash, job);
				ThreadPoolManager.getInstance().incrementMessageCount(job.getSocketChannel().toString());
			}
			synchronized (job.getKey()) {
				job.getKey().attach(null);
			}
		}
	}

//	public int numJobs() {
//		return this.jobs.size();
//	}


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

//	public boolean isComplete() { return this.taskComplete; }
}
