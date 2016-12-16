package com.waylau.netty.demo.cs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class FormerIoClient {

	public static void main(String[] args) throws UnknownHostException, IOException {
		Socket client = new Socket("127.0.0.1",8980);
		
		PrintStream ps = new PrintStream(client.getOutputStream());
		ps.println("hello");
		
		//ps.close();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
		
		String line = br.readLine();
		
		System.out.println(line);
		
		client.close();
	}

}
