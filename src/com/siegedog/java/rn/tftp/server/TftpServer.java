package com.siegedog.java.rn.tftp.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.siegedog.java.rn.core.Listener;
import com.siegedog.java.rn.tftp.GlobalConfig;
import com.siegedog.java.rn.tftp.datagrams.RRQ;
import com.siegedog.java.rn.tftp.datagrams.WRQ;
import com.siegedog.java.rn.util.Logger;
import com.siegedog.java.rn.util.ObjectUtil;

/**
 * Simple TFTP (Trivial File Transfer Protocol) server implementation that listens
 * for incoming requests on the control port 69, and spawns worker threads to handle
 * those requests and reply with error messages or data.
 * 
 * @author Andrei Barsan
 */
public class TftpServer extends Listener {

	public static final String SERVER_ROOT = "tftp_server_root/";
	private ExecutorService workers;
	private int wid = 0;
	
	public TftpServer() throws SocketException {
		super(new DatagramSocket(GlobalConfig.TFTP_CONTROL_PORT));
		setName("TFTP Server");
		workers = Executors.newCachedThreadPool(new ThreadFactory() {
			@Override
			public Thread newThread(Runnable task) {
				Thread out = new Thread(task);
				out.setName("server-worker-" + wid++);
				return out;
			}
		});
	}
	
	@Override
	protected void interpretDatagram(DatagramPacket datagram) throws IOException {
		Object controlDatagram = ObjectUtil.extractFromDatagram(datagram);
		
		if(controlDatagram instanceof RRQ) {
			Logger.log("Incoming read request! Spawning worker...");
			workers.submit(new ServerReadWorker((RRQ) controlDatagram, 
					datagram.getAddress(), 
					datagram.getPort()));
		}
		else if(controlDatagram instanceof WRQ) {
			Logger.log("Incoming write request! Spawning worker...");
			workers.submit(new ServerWriteWorker((WRQ) controlDatagram,
					datagram.getAddress(),
					datagram.getPort()));
		}
		else {
			Logger.log("Unknown request received on control port. Discarding.");
		}
	}
	
	public void shutdown() {
		super.shutdown();
		try {
			workers.awaitTermination(500, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			// NOP
		} finally {
			workers.shutdownNow();
		}
	}
}
