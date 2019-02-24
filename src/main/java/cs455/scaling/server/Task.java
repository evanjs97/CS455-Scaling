package cs455.scaling.server;

public class Task {
	private boolean taskComplete = false;

	public Task() {

	}


	public void work() {

	}


	private String SHA1FromBytes(byte[] data) {
		MessageDigest digest = MessageDigest.getInstance("SHA1");
		byte[] hash = digest.digest(data);
		BigInteger hashInt = new BigInteger(1, hash);
		return hashInt.toString(16);
	}

	public boolean isComplete() { return this.taskComplete; }
}
