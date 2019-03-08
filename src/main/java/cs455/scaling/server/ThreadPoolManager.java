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
	//private final ArrayDeque<byte[]> workPool = new ArrayDeque<>();
	//private final HashSet<SelectableChannel> acceptedKeys = new HashSet<>();


	//statistics for server
	private final HashMap<String, AtomicInteger> clientThroughput = new HashMap<>();
	private final LinkedList<String> clients = new LinkedList<>();
	private long lastBatchRemoved = System.nanoTime();
	private AtomicInteger messagesSinceLastLog = new AtomicInteger();


	//info about this thread pool manager
	private final int maxThreads;
	private final int batchSize;
	private final long batchTime;
	private final Selector selector;

	//used to immediately register new clients (avoid waiting batchTime)
	private boolean isNewConnection = false;

	private SocketChannel toRegister = null;
//	private ServerSocketChannel serverRegister = null;

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

	final void reregister(ServerSocketChannel channel) {
		try {
			synchronized(channel) {
				channel.register(selector, SelectionKey.OP_ACCEPT);
			}
		}catch (ClosedChannelException cce) {
			System.out.println(cce);
		}
	}

	static ThreadPoolManager getInstance() {
		return manager;
	}

	final Selector getSelector() { return manager.selector; }

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
		clients.add(client.toString());
		clientThroughput.put(client.toString(), new AtomicInteger());
		toRegister = client;
		selector.wakeup();
	}

	final void incrementMessageCount(String client) {
		synchronized (clientThroughput) {
			clientThroughput.get(client).incrementAndGet();
		}
		messagesSinceLastLog.incrementAndGet();
	}

	final void returnThreadToPool(WorkerThread thread) {
		threadPool.add(thread);

	}


//	public final void removeKey(SelectableChannel channel) {
//		synchronized (acceptedKeys) {
//			this.acceptedKeys.remove(channel);
//		}
//	}

	final String logAndReset() {
		LocalDateTime date = LocalDateTime.now();
		DateTimeFormatter dateFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
		double total = messagesSinceLastLog.getAndSet(0);
		double mean = total / clients.size();
		String logMessage = String.format("[%s] Server Throughput: %.2f messages/s, Active Client Connections: %d, " +
						"Mean Per-Client Throughput: %.2f messages/s ", date.format(dateFormat), total / 20 , clients.size(), mean / 20);
		double differences = 0;
		for(String client : clients) {
			double sent = clientThroughput.get(client).getAndSet(0);
			differences += Math.pow(sent - mean,2);
		}
		logMessage += String.format("Std. Dev. of Per-Client Throughput: %.2f messages/s", Math.sqrt(differences / clients.size()) / 20);
		return logMessage;
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
					//System.out.println(curSize);
					WorkerThread worker = threadPool.poll();
					Task task = new Task(jobKeys.pollFirst());
					worker.notifyAndStart(task);
					lastBatchRemoved = System.nanoTime();
				}

			}
		}
	}
}
