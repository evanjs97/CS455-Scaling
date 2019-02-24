package cs455.scaling.server;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Task {
	private boolean taskComplete = false;
	private byte[][] bytes;

	public Task(byte[][] bytes) {
		this.bytes = bytes;
	}


	public void work() {
		for(byte[] arr : bytes) {
			SHA1FromBytes(arr);
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
