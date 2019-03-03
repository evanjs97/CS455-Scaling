package cs455.scaling.server;

public class WorkerThread extends Thread{

	private volatile Task task = null;
    private volatile boolean taskComplete = true;

	public WorkerThread() {

	}

	public boolean getComplete() { return this.taskComplete; }

	@Override
	public synchronized void run() {
		while(true) {
			if(taskComplete || task == null) {
				try {
					wait();
				}catch (InterruptedException ie) {
					System.out.println("Thread Interrupted");
				}
			}else {
				task.work();
				taskComplete = true;
				ThreadPoolManager.getInstance().returnThreadToPool(this);
			}
		}
	}


	 final synchronized boolean notifyAndStart(Task task) {
		System.out.println("Notifying Thread");
	    if(taskComplete) {
            this.task = task;
            taskComplete = false;
            notifyAll();
            return true;
        }return false;
	}
}
