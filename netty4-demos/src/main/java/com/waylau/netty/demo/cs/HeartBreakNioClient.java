package com.waylau.netty.demo.cs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;

public class HeartBreakNioClient {
	
	

	public static void main(String[] args) throws UnknownHostException, IOException {
		long count = 0;
		
		SocketChannel clientSc = SocketChannel.open();
		clientSc.configureBlocking(false);
		
		
		Selector selector = Selector.open();
		clientSc.register(selector, SelectionKey.OP_READ);
		
		clientSc.connect(new InetSocketAddress("192.168.0.101", 20180));
		
		while(!clientSc.finishConnect()){
			System.out.println("please check the connection");
		}
		
		clientSc.write(ByteBuffer.wrap("hi server".getBytes("UTF-8")));
		
		while(true){
			
			int n = selector.select();
			if(n < 1){
				continue;
			}
			
			Iterator<SelectionKey> selectorIter = selector.selectedKeys().iterator();
			while(selectorIter.hasNext()){
				SelectionKey key = selectorIter.next();  
				selectorIter.remove();
				
				if(key.isConnectable()){
					SocketChannel serverSc = (SocketChannel) key.channel();
					if(serverSc.isConnectionPending()){
						serverSc.finishConnect();
					}
					serverSc.configureBlocking(false);
					serverSc.register(selector, SelectionKey.OP_READ);  
				}
				
				if(key.isAcceptable()){
					System.out.println("accept");
					SocketChannel serverSc = (SocketChannel) key.channel();
					serverSc.register(selector, SelectionKey.OP_READ);
				}
				
				if(key.isReadable()){
					SocketChannel serverSc = (SocketChannel) key.channel();
					ByteBuffer buf = ByteBuffer.allocate(100);
					
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					while(serverSc.read(buf) > 0){
						buf.flip();
						baos.write(buf.array());
						buf.clear();
					}
					
					String s = new String(baos.toByteArray(),"UTF-8");
					System.out.println("read: " + s);
					
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					String word = (count++)+" hi server,"+ (new Date().toString());
					ByteBuffer wordBuf = ByteBuffer.wrap(word.getBytes("UTF-8"));
					serverSc.write(wordBuf);
					
					serverSc.register(selector, SelectionKey.OP_READ);
					
					
					//serverSc.register(selector, SelectionKey.OP_WRITE);
				}
				
				if(key.isWritable()){
					SocketChannel serverSc = (SocketChannel) key.channel();
					serverSc.configureBlocking(false);
					
					String word = (count++)+" hi server,"+ (new Date().toString());
					ByteBuffer wordBuf = ByteBuffer.wrap(word.getBytes("UTF-8"));
					serverSc.write(wordBuf);
					
					System.out.println("write");
					
					serverSc.register(selector, SelectionKey.OP_ACCEPT);
				}
				
				if(key.isConnectable()){
					System.out.println("connect...");
				}
				
			}
		}
	}

}
