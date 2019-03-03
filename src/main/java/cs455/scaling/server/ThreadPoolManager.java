package cs455.scaling.server;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPoolManager implements Runnable{
	private static ThreadPoolManager manager;
	private final LinkedBlockingQueue<WorkerThread> threadPool = new LinkedBlockingQueue<>();
	private final ArrayDeque<byte[]> workPool = new ArrayDeque<>();
	private int maxThreads;
	private int batchSize;
	private int batchTime;
	private long lastBatchRemoved = System.nanoTime();

	private ThreadPoolManager(int maxThreads, int batchSize, int batchTime) {
		for(int i = 0; i < maxThreads; i++) {
			WorkerThread worker = new WorkerThread();
			worker.run();
			threadPool.offer(worker);
		}
		this.maxThreads = maxThreads;
		this.batchSize = batchSize;
		this.batchTime = batchTime;
	}

	public static void open(int maxThreads, int batchSize, int batchTime) {
		if(manager == null) {
			manager = new ThreadPoolManager(maxThreads, batchSize, batchTime);
			System.out.println("Successfully started thread pool manager.");
		}else {
			System.out.println("Failed to start thread pool manager: Thread pool manager has already been created.");
		}
	}

	public static ThreadPoolManager getInstance() {
		return manager;
	}

	public void addWork(byte[] work) {
		synchronized (workPool) {
			workPool.offer(work);
		}
	}

	private WorkerThread getThreadIfAvailable() {
		return threadPool.poll();
	}

	public void returnThreadToPool(WorkerThread thread) {
		threadPool.add(thread);
	}

	@Override
	public void run() {
		while(true) {
			if(workPool.size() >= batchSize) {
				WorkerThread worker = threadPool.poll();
				byte[][] byteRange = new byte[batchSize][];
				synchronized (workPool) {
					for (int i = 0; i < batchSize; i++) {
						byteRange[i] = workPool.poll();
					}
				}
				worker.notifyAndStart(new Task(byteRange));
			}
		}
	}
}
