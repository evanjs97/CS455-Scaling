package cs455.scaling.client;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientStatistics {

	private AtomicInteger messagesSentSinceLastLog = new AtomicInteger();
	private AtomicInteger messagesReceivedSinceLastLog = new AtomicInteger();

	public final void receivedMessage() {
		messagesReceivedSinceLastLog.incrementAndGet();
	}

	public final void sendMessage() {
		messagesSentSinceLastLog.incrementAndGet();
	}

	public final void log(long time) {
		System.out.println(String.format("[%s] Total Sent Count: %d, Total Received Count: %d",
				new Date(time).toString(), messagesSentSinceLastLog.getAndSet(0), messagesReceivedSinceLastLog.getAndSet(0)));
	}
}
