package cs455.scaling.server;

import java.nio.channels.*;

public class Job {
	private final int type;
	private final SocketChannel socketChannel;
	private final ServerSocketChannel serverSocketChannel;
	private final SelectionKey key;

	public Job(int type, SelectableChannel channel, SelectionKey key) {
		this.type = type;
		if(channel instanceof SocketChannel) {
			this.socketChannel = (SocketChannel) channel;
			this.serverSocketChannel = null;
		}else{
			this.serverSocketChannel = (ServerSocketChannel) channel;
			this.socketChannel = null;
		}
		this.key = key;
	}

	public String toString() {
		if(this.socketChannel != null) return this.type + " Socket Channel: ";
		else return this.type + " Server Socket Channel: " ;
	}

	public final SelectableChannel getChannel() {
		if(socketChannel == null) return serverSocketChannel;
		else return socketChannel;
	}

	final SelectionKey getKey() { return this.key; }

	final int getType() {
		return this.type;
	}

	final SocketChannel getSocketChannel() {
		return socketChannel;
	}

	final ServerSocketChannel getServerSocketChannel() {
		return serverSocketChannel;
	}

}
