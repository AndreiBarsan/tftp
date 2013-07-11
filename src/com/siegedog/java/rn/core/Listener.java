package com.siegedog.java.rn.core;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.siegedog.java.rn.tftp.GlobalConfig;
import com.siegedog.java.rn.util.Logger;

/** Abstract class defining basic functionality for listening for incoming 
 * connection, but no way of handling received data. */
public abstract class Listener extends Thread {
		
	protected final int localPort;
	protected final InetAddress localAddress;
	protected final boolean ownsSocket;
	protected final DatagramSocket socket;
	
	private DatagramPacket lastDatagram;
	private Lock runningLock = new ReentrantLock();
	
	protected boolean shuttingDown = false;
	
	public Listener(DatagramSocket socket) {
		this.socket = socket;
		
		localPort = socket.getLocalPort();
		localAddress = socket.getLocalAddress();
		ownsSocket = false;
		
		runningLock.lock();
	}
	
	@Override
	public void run() {
		listen();
	}

	private void listen() {
		try {
			int buffLen = GlobalConfig.MAX_BYTES_IN_PACKAGE;
			byte[] buff = new byte[buffLen];
			
			DatagramPacket received = new DatagramPacket(buff, buffLen);
			
			while(!shuttingDown) {
				socket.receive(received);
				lastDatagram = received; 
				interpretDatagram(received);
			}
			
		} catch (SocketException e) {
			if(Thread.interrupted()) {
				// The socket was closed elegantly
				Logger.log("Server shutting down...");
			}
			else {
				Logger.fatalError("Something went terribly wrong!", e);
			}
		} catch (IOException e) {
			Logger.fatalError("General IO error.", e);
		}
	}
	
	protected abstract void interpretDatagram(DatagramPacket datagram) throws IOException;

	@Override
	public void interrupt() {
		super.interrupt();
		shutdown();
	}
	
	public void shutdown() {
		shuttingDown = true;
		Logger.log("Shutdown was called.");
		if(ownsSocket) {
			Logger.log("Closing socket.");
			socket.close();
		} else {
			 Logger.log("Note: not closing socket!");
		}
		runningLock.unlock();
	}

	public void waitForShutdown() {
		// Block until the server shuts down
		runningLock.lock();
		
		// But don't hold on to the lock
		runningLock.unlock();
	}
	
	public DatagramPacket getLastDatagram() {
		return lastDatagram;
	}

	public DatagramSocket getSocket() {
		return socket;
	}
}