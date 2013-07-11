package com.siegedog.java.rn.tftp.server;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import com.siegedog.java.rn.core.Sender;
import com.siegedog.java.rn.tftp.FileReceiver;
import com.siegedog.java.rn.tftp.datagrams.ACK;
import com.siegedog.java.rn.tftp.datagrams.ERR;
import com.siegedog.java.rn.tftp.datagrams.WRQ;
import com.siegedog.java.rn.util.Logger;

/** Handles WRITE requests sent to the server. */
public class ServerWriteWorker extends ServerWorker {

	private WRQ request;
	
	public ServerWriteWorker(WRQ request, InetAddress remoteAddress, int remotePort) {
		super(remoteAddress, remotePort);
		this.request = request;
	}

	public boolean isValid() {
		File file = new File(TftpServer.SERVER_ROOT + request.fileName);
		return ! file.exists();
	}
	
	@Override
	public void run() {
		try(DatagramSocket workerSocket = new DatagramSocket()) {
			
			Sender statusSender = new Sender(workerSocket, remotePort, remoteAddress);
			
			if(!isValid()) {
				statusSender.sendObject(new ERR(ERR.FILE_ALREADY_EXISTS, "The requested file" + request.fileName + " aleardy exists on the server."));
				return;
			}
			else {
				statusSender.sendObject(new ACK(0));
			}
			
			Logger.log("Starting file receive");
			try {
				byte[] result = FileReceiver.receiveFile(workerSocket, remoteAddress);
				Logger.log("Got file on server: " + new String(result));
			}
			catch(IOException e) {
				Logger.fatalError("Failed file tranfer: " + e.getMessage(), e);
			}
			
		} catch (SocketException e) {
			Logger.fatalError("Failed to create server worker: ", e);
		}
	}

}
