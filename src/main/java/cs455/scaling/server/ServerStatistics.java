package cs455.scaling.server;

import java.nio.channels.SocketChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;


public class ServerStatistics {

	private final HashMap<String, AtomicInteger> clientThroughput = new HashMap<>();
	private final LinkedList<String> clients = new LinkedList<>();
	private AtomicInteger messagesSinceLastLog = new AtomicInteger();

	final void incrementMessageCount(String client) {
		synchronized (clientThroughput) {
			clientThroughput.get(client).incrementAndGet();
		}
		messagesSinceLastLog.incrementAndGet();
	}

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



	final void registerClient(SocketChannel client) {
		clients.add(client.toString());
		clientThroughput.put(client.toString(), new AtomicInteger());
	}
}
