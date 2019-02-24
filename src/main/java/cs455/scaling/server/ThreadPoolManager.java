package cs455.scaling.server;

import java.util.LinkedList;
import java.util.Queue;

public class ThreadPoolManager implements Runnable{
	private static final ThreadPoolManageer manager;
	private final LinkedBlockingQueue<WorkerThread> threadPool = new LinkedBlockingQueue<>();
	private final Queue<Queue<byte[]>> workPool = new LinkedList<byte[]>();
	private int maxThreads;
	private int batchSize;
	private int batchTime;

	private ThreadPoolManager(int maxThreads, int batchSize, int batchTime) {
		for(int i = 0; i < maxThreads; i++) {
			threadPool.add(new WorkerThread(this));
		}
		this.maxThreads = maxThreads;
		this.batchSize = batchSize;
		this.batchTime = batchTime;
	}

	public static getInstance(int maxThreads, int batchSize, int batchTime) {
		if(manager == null) {
			manager = new ThreadPoolManager(maxThreads);
		}else return maanager;
	}

	private Thread getThreadIfAvailable() {
		return threadPool.poll()
	}

	private void returnThreadToPool(WorkerThread thread) {
		threadPool.add(thread);
	}

	@Override
	public void run() {
		while(true) {
			if(workPool.peek().size() > ) {
				WorkerThread worker = getThreadIfAvailable();
				worker.notifyAndStart(new Task());
			}
		}
	}
}
