package cs455.scaling.server;

import java.nio.channels.Channel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ThreadPoolManager implements Runnable{
	private static ThreadPoolManager manager;
	private final LinkedBlockingQueue<WorkerThread> threadPool = new LinkedBlockingQueue<>();
	private final ArrayDeque<byte[]> workPool = new ArrayDeque<>();

	private final ArrayDeque<LinkedList<Job>> jobKeys = new ArrayDeque<>();
	private final int maxThreads;
	private final int batchSize;
	private final int batchTime;
	private final Selector selector;
	private long lastBatchRemoved = System.nanoTime();

	private ThreadPoolManager(int maxThreads, int batchSize, int batchTime, Selector selector) {
		for(int i = 0; i < maxThreads; i++) {
			WorkerThread worker = new WorkerThread();
			threadPool.offer(worker);
			worker.start();
		}
		this.selector = selector;
		this.maxThreads = maxThreads;
		this.batchSize = batchSize;
		this.batchTime = batchTime;
	}

	public static void open(int maxThreads, int batchSize, int batchTime, Selector selector) {
		if(manager == null) {
			manager = new ThreadPoolManager(maxThreads, batchSize, batchTime, selector);
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

	public final Selector getSelector() { return manager.selector; }

	public void addJob(int type, Channel channel) {
		//System.out.println("Adding Key: " + key);
		synchronized (jobKeys) {
			if(jobKeys.isEmpty()) jobKeys.push(new LinkedList<Job>());
			Iterator<LinkedList<Job>> iter = jobKeys.iterator();
			boolean didAdd = false;
			while(iter.hasNext()) {
				LinkedList<Job> current = iter.next();
				if(current.size() < batchSize) {
					current.addLast(new Job(type, channel));
					didAdd = true;
				}
			}
			if(!didAdd) {
				LinkedList<Job> temp = new LinkedList<>();
				temp.add(new Job(type, channel));
				jobKeys.push(temp);
			}
		}
	}

	private WorkerThread getThreadIfAvailable() {
		return threadPool.poll();
	}

	public final void returnThreadToPool(WorkerThread thread) {
		threadPool.add(thread);
	}

	@Override
	public void run() {
		while(true) {
			synchronized (jobKeys) {
				if (jobKeys.isEmpty() || threadPool.isEmpty()) continue;
				int curSize = jobKeys.peek().size();


				if (curSize >= batchSize) {
					System.out.println("curSize: " + curSize + " batchSize: " + batchSize);
					WorkerThread worker = threadPool.poll();
					System.out.println("Sending notification to thread: ");
					System.out.println("Size Before: " + jobKeys.size());
					worker.notifyAndStart(new Task(jobKeys.poll()));
					System.out.println("Size After: " + jobKeys.size());
				}

			}
		}
	}
}
