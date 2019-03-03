package cs455.scaling.server;

public class WorkerThread extends Thread{

	private volatile Task task = null;
    private volatile boolean taskComplete = false;

	public WorkerThread() {

	}



	@Override
	public void run() {
		while(true) {
			if(taskComplete || task == null) {
				synchronized (this) {
					try {
						wait();
					}catch (InterruptedException ie) {

					}
				}
			}else {
				taskComplete = false;
				task.work();
				taskComplete = true;
				ThreadPoolManager.getInstance().returnThreadToPool(this);
			}
		}
	}


	 final boolean notifyAndStart(Task task) {
		System.out.println("Notifying Thread");
	    if(taskComplete) {
            this.task = task;
            notifyAll();
            return true;
        }return false;
	}
}
