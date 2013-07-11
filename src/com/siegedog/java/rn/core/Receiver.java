package com.siegedog.java.rn.core;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

import com.siegedog.java.rn.util.ObjectUtil;

/** Listens for incoming data, unserializes the bytes into T instances, and 
 *  executes a callback for the received data. */
public class Receiver<T> extends Listener {
	
	private ServerCallback<T> callback;

	public interface ServerCallback<T> {
		public void received(T receivedData);
	}
	
	public Receiver(DatagramSocket socket) {
		super(socket);
	}
	
	public Receiver(DatagramSocket socket, ServerCallback<T> callback) {
		this(socket);
		this.callback = callback;
	}
	
	// We handle unchecked casts in an exception handler, don't worry!
	@Override
	@SuppressWarnings("unchecked")
	protected void interpretDatagram(DatagramPacket datagram) throws IOException {
		Object result = ObjectUtil.extractFromDatagram(datagram);
		callback.received((T) result);
	}

	public ServerCallback<T> getCallback() {
		return callback;
	}

	public void setCallback(ServerCallback<T> callback) {
		this.callback = callback;
	}
}
