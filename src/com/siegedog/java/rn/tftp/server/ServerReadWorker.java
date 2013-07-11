package com.siegedog.java.rn.tftp.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Scanner;

import com.siegedog.java.rn.core.Sender;
import com.siegedog.java.rn.tftp.FileSender;
import com.siegedog.java.rn.tftp.datagrams.ERR;
import com.siegedog.java.rn.tftp.datagrams.RRQ;
import com.siegedog.java.rn.util.Logger;

/** Handles read requests sent to the server */
class ServerReadWorker extends ServerWorker {

	private RRQ request;
	
	public ServerReadWorker(RRQ request, InetAddress remoteAddress, int remotePort) {
		super(remoteAddress, remotePort);
		this.request = request;
	}
	
	public boolean isValid() {
		File file = new File(TftpServer.SERVER_ROOT + request.fileName);
		return file.exists() && ( ! file.isDirectory());
	}
	
	@Override
	public void run() {
		try(DatagramSocket workerSocket = new DatagramSocket()) {
			Sender sender = new Sender(workerSocket, remotePort, remoteAddress);
			
			Logger.log("Starting worker file transfer from port: " + sender.getLocalPort() + " to " + remotePort);
			Logger.log("Transfering file: " + request.fileName);
			
			if(! isValid()) {
				sender.sendObject(new ERR(ERR.FILE_NOT_FOUND, "Filename: " + request.fileName));
				return;
			}
			
			File file = new File(TftpServer.SERVER_ROOT + request.fileName);
			try(Scanner s = new Scanner(file)) {
				s.useDelimiter("\\Z");
				String contents = s.next();
				Logger.log("Sending to: " + remotePort + " on " + remoteAddress + " from " + sender.getLocalPort() + ".");
				
				try {
					FileSender.sendFile(contents.getBytes(), sender);
				} catch(SocketTimeoutException e) {
					Logger.warning("Timeout sending [" + file.getAbsolutePath() + "]: " + e.getMessage());
					return;
				} catch(IOException e) {
					Logger.warning("Problem sending [" + file.getAbsolutePath() + "]:" + e.getMessage());
				}
				
			} catch (FileNotFoundException e) {
				// Stupid checked exceptions. We already checked this in isValid(). 
			}
		} catch (SocketException e) {
			Logger.warning("Failed to create server worker: " + e.getMessage());
		}
	}
	
}