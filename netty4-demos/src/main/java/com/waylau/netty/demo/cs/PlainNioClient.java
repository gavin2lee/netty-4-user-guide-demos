package com.waylau.netty.demo.cs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Iterator;

public class PlainNioClient {
	
	

	public static void main(String[] args) throws UnknownHostException, IOException {
		long count = 0;
		
		SocketChannel clientSc = SocketChannel.open();
		clientSc.configureBlocking(false);
		
		
		Selector selector = Selector.open();
		clientSc.register(selector, SelectionKey.OP_READ);
		
		clientSc.connect(new InetSocketAddress("localhost", 20180));
		
		while(!clientSc.finishConnect()){
			System.out.println("please check the connection");
		}
		
		String s = (count++)+" hi server,"+ (new Date().toString());
		ByteBuffer bb = ByteBuffer.wrap(s.getBytes("UTF-8"));
		clientSc.write(bb);
		
		while(true){
			
			int n = selector.select();
			if(n < 1){
				continue;
			}
			
			Iterator<SelectionKey> selectorIter = selector.selectedKeys().iterator();
			while(selectorIter.hasNext()){
				SelectionKey key = selectorIter.next();  
				
				if(key.isAcceptable()){
					System.out.println("accept");
				}
				
				if(key.isReadable()){
					SocketChannel serverSc = (SocketChannel) key.channel();
					serverSc.configureBlocking(false);
					ByteBuffer buf = ByteBuffer.allocate(8);
					StringBuilder sb = new StringBuilder();
					
					while(serverSc.read(buf) > 0){
						buf.flip();
						sb.append(buf.array());
						buf.clear();
					}
					
					System.out.println("read: " + sb.toString());
					serverSc.register(selector, SelectionKey.OP_WRITE);
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
				selectorIter.remove();
			}
		}
	}

}
