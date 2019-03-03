package cs455.scaling.server;

import java.nio.channels.*;

public class Job {
	private final int type;
	private final SocketChannel socketChannel;
	private final ServerSocketChannel serverSocketChannel;
	private final SelectionKey key;

	public Job(int type, Channel channel, SelectionKey key) {
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

	public final SelectionKey getKey() { return this.key; }

	public final int getType() {
		return this.type;
	}

	public final SocketChannel getSocketChannel() {
		return socketChannel;
	}

	public final ServerSocketChannel getServerSocketChannel() {
		return serverSocketChannel;
	}

}
