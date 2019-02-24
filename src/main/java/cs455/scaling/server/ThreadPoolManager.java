package cs455.scaling.server;

import java.util.LinkedList;
import java.util.Queue;

public class ThreadPoolManager {
	private final Queue<Thread> threadPool = new LinkedList<>();
	int maxThreads;

	public ThreadPoolManager(int maxThreads) {
		for(int i = 0; i < maxThreads; i++) {
			threadPool.add(new Thread());
		}
		this.maxThreads = maxThreads;
	}

	public Thread getThreadIfAvailable() {
		synchronized (threadPool) {
			if(!threadPool.isEmpty()) {
				return threadPool.poll();
			}else return null;
		}
	}

	public void returnThread(Thread thread) {
		synchronized (threadPool) {
			threadPool.add(thread);
		}
	}
}
