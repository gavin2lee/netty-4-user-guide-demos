package com.waylau.netty.demo.cs;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

public class HeartBreakNioServer {
	public void server(int port) throws Exception {
		AtomicLong count = new AtomicLong();
		AtomicLong nameCount = new AtomicLong();
		System.out.println("Listening for connections on port " + port);
		// open Selector that handles channels
		Selector selector = Selector.open();
		// open ServerSocketChannel
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		// get ServerSocket
		ServerSocket serverSocket = serverChannel.socket();
		// bind server to port
		serverSocket.bind(new InetSocketAddress(port));
		// set to non-blocking
		serverChannel.configureBlocking(false);
		// register ServerSocket to selector and specify that it is interested
		// in new accepted clients
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		final ByteBuffer msg = ByteBuffer.wrap("Hi!\r\n".getBytes());
		while (true) {
			// Wait for new events that are ready for process. This will block
			// until something happens
			int n = selector.select();
			if (n > 0) {
				// Obtain all SelectionKey instances that received events
				Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
				while (iter.hasNext()) {
					SelectionKey key = iter.next();
					iter.remove();

					if (key.isConnectable()) {
						System.out.println("connectable");
					}

					try {
						if (key.isAcceptable()) {
							ServerSocketChannel server = (ServerSocketChannel) key.channel();
							SocketChannel client = server.accept();
							System.out.println("Accepted connection from " + client);
							client.configureBlocking(false);
							
							String scName = "SC-"+nameCount.incrementAndGet();

							client.register(selector, SelectionKey.OP_READ, scName);
							key.interestOps(SelectionKey.OP_ACCEPT);
						}
						// Check if event was because new client ready to get
						// accepted
						if (key.isReadable()) {
							String scName = (String) key.attachment();
							System.out.println("read");
							SocketChannel client = (SocketChannel) key.channel();
							System.out.println("Read connection from " + client);
							
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							ByteBuffer buf = ByteBuffer.allocate(100);
							while(client.read(buf)>0){
								buf.flip();
								bos.write(buf.array());
								buf.clear();
							}
							
							System.out.println("RECV from " + scName + " - " + new String(bos.toByteArray(),"UTF-8"));
							
							String msgSend = count.getAndIncrement() + "hi client :" + new Date().toString();
							
							client.write(ByteBuffer.wrap(msgSend.getBytes("UTF-8")));

							client.register(selector, SelectionKey.OP_READ,scName);
							key.interestOps(SelectionKey.OP_READ);

							// Accept client and register it to selector
							// client.register(selector, SelectionKey.OP_WRITE,
							// msg.duplicate());
						}
						// Check if event was because socket is ready to write
						// data
						if (key.isWritable()) {
							System.out.println("write");

							SocketChannel client = (SocketChannel) key.channel();

							client.write(ByteBuffer.wrap("hi client".getBytes("UTF-8")));
							// ByteBuffer buff = (ByteBuffer) key.attachment();
							// //write data to connected client
							// while (buff.hasRemaining()) {
							// if (client.write(buff) == 0) {
							// break;
							// }
							// }
							// client.close();//close client
							client.register(selector, SelectionKey.OP_ACCEPT);
							key.interestOps(SelectionKey.OP_WRITE);
						}
					} catch (Exception e) {
						key.cancel();
						key.channel().close();
					}
				}
			}
		}
	}

	public static void main(String... args) {
		HeartBreakNioServer server = new HeartBreakNioServer();
		try {
			server.server(20180);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
