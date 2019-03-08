package cs455.scaling.server;

import java.net.Socket;
import java.nio.channels.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolManager implements Runnable{
	//for running the jobs
	private static ThreadPoolManager manager;
	private final LinkedBlockingQueue<WorkerThread> threadPool = new LinkedBlockingQueue<>();
	private final ArrayDeque<LinkedList<Job>> jobKeys = new ArrayDeque<>();

	//server statistics usage
	private long lastBatchRemoved = System.nanoTime();
	ServerStatistics statistics = new ServerStatistics();


	//info about this thread pool manager
	private final int maxThreads;
	private final int batchSize;
	private final long batchTime;
	private final Selector selector;

	//used to immediately register new clients (avoid waiting batchTime)
	private boolean isNewConnection = false;

	private SocketChannel toRegister = null;

	private ThreadPoolManager(int maxThreads, int batchSize, double batchTime, Selector selector) {
		for(int i = 0; i < maxThreads; i++) {
			WorkerThread worker = new WorkerThread();
			threadPool.offer(worker);
			worker.start();
		}
		this.selector = selector;
		this.maxThreads = maxThreads;
		this.batchSize = batchSize;
		this.batchTime = new Double(batchTime * 1000000000).longValue();
	}

	static void open(int maxThreads, int batchSize, double batchTime, Selector selector) {
		if(manager == null) {
			manager = new ThreadPoolManager(maxThreads, batchSize, batchTime, selector);
			System.out.println("Successfully started thread pool manager.");
		}else {
			System.out.println("Failed to start thread pool manager: Thread pool manager has already been created.");
		}
	}

	static ThreadPoolManager getInstance() {
		return manager;
	}

	void addJob(int type, SelectableChannel channel, SelectionKey key) {

		synchronized (jobKeys) {
			if(channel instanceof ServerSocketChannel) {
				LinkedList<Job> connection = new LinkedList<>();
				isNewConnection = true;
				connection.push(new Job(type, channel, key));
				jobKeys.addFirst(connection);
				return;
			}
			if(jobKeys.isEmpty()) jobKeys.push(new LinkedList<Job>());
			Iterator<LinkedList<Job>> iter = jobKeys.iterator();
			boolean didAdd = false;
			while(iter.hasNext()) {
				LinkedList<Job> current = iter.next();
				if(current.size() < batchSize) {
					current.addLast(new Job(type, channel, key));
					didAdd = true;
					break;
				}
			}
			if(!didAdd) {
				LinkedList<Job> temp = new LinkedList<>();
				temp.add(new Job(type, channel, key));
				jobKeys.addLast(temp);
			}
		}
	}

	final SocketChannel getToRegister() {
		return this.toRegister;
	}

	final void resetToRegister() {
		toRegister = null;
	}

	final void registerClient(SocketChannel client) {
		statistics.registerClient(client);
		toRegister = client;
		selector.wakeup();
	}

	final void receivedMessage(String client) {
		statistics.incrementMessageCount(client);
	}


	final void returnThreadToPool(WorkerThread thread) {
		threadPool.add(thread);

	}

	final String logWithStats() {
		return statistics.logAndReset();
	}

	@Override
	public void run() {
		while(true) {
			synchronized (jobKeys) {
				if (jobKeys.isEmpty() || threadPool.isEmpty()) continue;
				int curSize = jobKeys.peekFirst().size();

				long time = System.nanoTime();
				long timeDifferential = time - lastBatchRemoved;
				if (curSize >= batchSize || timeDifferential > batchTime || isNewConnection) {
					isNewConnection = false;
					WorkerThread worker = threadPool.poll();
					Task task = new Task(jobKeys.pollFirst());
					worker.notifyAndStart(task);
					lastBatchRemoved = System.nanoTime();
				}

			}
		}
	}
}
