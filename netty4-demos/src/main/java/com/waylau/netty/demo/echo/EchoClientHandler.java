package com.waylau.netty.demo.echo;

import java.io.UnsupportedEncodingException;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class EchoClientHandler extends ChannelInboundHandlerAdapter {

	private final ByteBuf firstMessage;

	/**
	 * Creates a client-side handler.
	 */
	public EchoClientHandler() {
		firstMessage = Unpooled.buffer(EchoClient.SIZE);
		for (int i = 0; i < firstMessage.capacity(); i++) {
			firstMessage.writeByte((byte) i);
		}
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		try {
			ctx.writeAndFlush(Unpooled.buffer(EchoClient.SIZE).writeBytes("AAAAAAAAAAA".getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		System.out.println("Client read:" + msg);
		ctx.writeAndFlush(msg);
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		System.out.println("client");
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // Close
																				// the
																				// connection
																				// when
																				// an
																				// exception
																				// is
																				// raised.
		cause.printStackTrace();
		ctx.close();
	}
}