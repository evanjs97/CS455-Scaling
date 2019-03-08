package cs455.scaling.server;

public class WorkerThread extends Thread{

	private volatile Task task = null;
    private volatile boolean taskComplete = true;

	/**
	 * worker thread will wait till it has a task, and is notified of it, then it will run the task
	 * finally it will return itself to the thread pool
	 */
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

	/**
	 * this method will assign the workerThread a task and notify it
	 * @param task the task to assign to the WorkerThread
	 * @return false if WorkerThread hasn't finished its last task, true when successful notification.
	 */
	 final synchronized boolean notifyAndStart(Task task) {
	    if(taskComplete) {
            this.task = task;
            taskComplete = false;
            notifyAll();
            return true;
        }return false;
	}
}
