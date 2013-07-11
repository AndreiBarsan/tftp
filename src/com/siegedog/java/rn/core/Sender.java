package com.siegedog.java.rn.core;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.siegedog.java.rn.util.ObjectUtil;

/** Sends direct bytes or objects over a given socket. */
public class Sender {
	private DatagramSocket socket;
	private int remotePort;
	private InetAddress remoteAddress;
	
	public Sender(DatagramSocket socket, int remotePort, InetAddress remoteAddress) {
		this.remotePort = remotePort;
		this.remoteAddress = remoteAddress;
		this.socket = socket;
	}
	
	public void sendObject(Object object) {
		send(ObjectUtil.bytesOf(object));
	}
	
	public void send(byte[] bytes) {
		DatagramPacket message;
		try {
			message = new DatagramPacket(
					bytes,
					bytes.length, 
					remoteAddress,
					remotePort);
			socket.send(message);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getLocalPort() {
		return socket.getLocalPort();
	}
	
	/** Simply returns the underlying socket. Use with care. */
	public DatagramSocket getSocket() {
		return socket;
	}
}

