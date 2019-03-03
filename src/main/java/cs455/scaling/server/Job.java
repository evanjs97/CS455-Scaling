package cs455.scaling.server;

import java.nio.channels.Channel;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class Job {
	private final int type;
	private final SocketChannel socketChannel;
	private final ServerSocketChannel serverSocketChannel;

	public Job(int type, Channel channel) {
		this.type = type;
		if(channel instanceof SocketChannel) {
			this.socketChannel = (SocketChannel) channel;
			this.serverSocketChannel = null;
		}else{
			this.serverSocketChannel = (ServerSocketChannel) channel;
			this.socketChannel = null;
		}
	}

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
