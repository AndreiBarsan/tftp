package com.siegedog.java.rn.tftp.server;

import java.net.InetAddress;

public abstract class ServerWorker implements Runnable {

	protected InetAddress remoteAddress;
	protected int remotePort;
	
	public ServerWorker(InetAddress remoteAddress, int remotePort) {
		this.remoteAddress = remoteAddress;
		this.remotePort = remotePort;
	}
}
