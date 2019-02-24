package cs455.scaling.server;

public class WorkerThread implements Runnable{

	public volatile Task task = null;


	public WorkerThread() {
	}

	@Override
	public void run() {
		while(true) {
			//add wait notify mechanism
			if(task == null || task.isComplete()) {
				try {
					wait();
				}catch(InterruptedException e) {

				}
			}
//			while(task != null && task.isComplete()) {
//
//			}
		}
	}

	public void notifyAndStart(Task task) {
		this.task = task;
		notifyAll()
	}
}
