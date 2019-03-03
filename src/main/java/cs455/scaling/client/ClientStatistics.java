package cs455.scaling.client;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
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

	public final void log() {
		LocalDateTime date = LocalDateTime.now();
		DateTimeFormatter dateFormat = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM);
		System.out.println(String.format("[%s] Total Sent Count: %d, Total Received Count: %d",
				date.format(dateFormat), messagesSentSinceLastLog.getAndSet(0), messagesReceivedSinceLastLog.getAndSet(0)));
	}
}
