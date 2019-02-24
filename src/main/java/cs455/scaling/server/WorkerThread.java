package cs455.scaling.server;

public class WorkerThread implements Runnable{

	private volatile Task task = null;
    private volatile boolean taskComplete = false;

	public WorkerThread() {
	}



	@Override
	public void run() {
		while(true) {
			if(taskComplete) {
				try {
					wait();
				}catch(InterruptedException e) {}
			}else {
                task.work();
                taskComplete = true;
                ThreadPoolManager.getInstance().returnToThreadPool(this);
            }

		}
	}

	public boolean notifyAndStart(Task task) {
	    if(taskComplete) {
            this.task = task;
            notifyAll();
            return true;
        }return false;
	}
}
