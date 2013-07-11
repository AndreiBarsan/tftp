package com.siegedog.java.rn.tftp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.siegedog.java.rn.core.Sender;
import com.siegedog.java.rn.tftp.datagrams.ACK;
import com.siegedog.java.rn.tftp.datagrams.ERR;
import com.siegedog.java.rn.tftp.datagrams.RRQ;
import com.siegedog.java.rn.tftp.datagrams.WRQ;
import com.siegedog.java.rn.util.Logger;
import com.siegedog.java.rn.util.ObjectUtil;


public class TftpClient {

	private File rootFolder;
	
	InetAddress serverAddress;
	InetAddress clientAddress;
	
	public TftpClient(String rootName) throws IOException {
		this(rootName, InetAddress.getLocalHost());
	}
	
	public TftpClient(String rootName, InetAddress serverAddress) throws IOException  {
		this(rootName, serverAddress, InetAddress.getLocalHost());
	}
	
	public TftpClient(String rootName, InetAddress serverAddress, InetAddress clientAddress) throws IOException {
		this.rootFolder = new File(rootName);
		this.serverAddress = serverAddress;
		this.clientAddress = clientAddress;
		
		if( ! rootFolder.exists()) {
			throw new FileNotFoundException("Root folder does not exist.");
		}
		
		if( ! rootFolder.isDirectory()) {
			throw new IOException("Given client root folder is actually a file.");
		}
		
	}
	
	/**
	 * Synchronously fetch a file from the server. This method blocks until the
	 * transfer is complete.
	 * 
	 * @throws IOException When the requested file already exists on the client,
	 * or when the server responds with an error (such as a timeout or a file not
	 * found exception).
	 */
	public void fetchFile(String fileName) throws IOException {
		if(Arrays.asList(rootFolder.list()).contains(fileName)) {
			throw new FileAlreadyExistsException(fileName);
		}
		
		DatagramSocket clientSocket = new DatagramSocket();
		
		Sender sender = new Sender(clientSocket, GlobalConfig.TFTP_CONTROL_PORT, serverAddress);
		sender.sendObject(new RRQ(fileName));
			
		Logger.log("TFTP client waiting for the requested file: " + fileName);
		
		byte[] result = null;
		try {
			Logger.log("Receiving file. Listening for data on port: " + clientSocket.getLocalPort());
			result = FileReceiver.receiveFile(clientSocket, clientAddress);
			clientSocket.close();
		} catch(IOException e) {
			throw new FileNotFoundException("Failed to receive file from server: " + e.getMessage());
		}
		
		Logger.log("Received the file successfully: " + fileName);
		String fileContents = new String(result);
		
		Logger.log("Downloaded file contents: ");
		Logger.log(fileContents);
		
		// Not writing at the moment for testing purposes
		/*
		File newFile = new File(rootFolder, fileName);
		
		try(PrintWriter pw = new PrintWriter(newFile)) {
			pw.write(fileContents);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}*/
		
		Logger.log("Finished task READ file " + fileName);
	}
	
	/**
	 * Synchronously sends a file to the server. This method blocks until the 
	 * transfer is complete.
	 * 
	 * @param localFileName The name of the local file being sent.
	 * @param fileOnServer The name that file should have on the server.
	 * @throws IOException If the local file doesn't exist or is a folder, or if
	 * there's a server problem - a timeout, or a file with that same name already
	 * exists on the server.
	 */
	public void sendFile(String localFileName, String fileOnServer) throws IOException {
		
		File file = new File(rootFolder, localFileName);
		if(! file.exists()) {
			throw new FileNotFoundException("File " + localFileName + " not found in the root folder " + rootFolder.getAbsolutePath());
		}
		
		if(file.isDirectory()) {
			throw new IOException("Writing whole folders isn't supported. Please send each file individually.");
		}
		
		try(DatagramSocket clientSocket = new DatagramSocket()) {
			Sender sender = new Sender(clientSocket, GlobalConfig.TFTP_CONTROL_PORT, serverAddress);
			sender.sendObject(new WRQ(fileOnServer));
			
			byte[] buff = new byte[256];
			DatagramPacket datagram = new DatagramPacket(buff, buff.length);
			clientSocket.setSoTimeout(GlobalConfig.TIMEOUT_MS);
			try {
				clientSocket.receive(datagram);
			}
			catch(SocketTimeoutException e) {
				throw new SocketTimeoutException("Server didn't respond to our request to write "
						+ localFileName + " as " + fileOnServer + " in time.");
			}
			
			Object response = ObjectUtil.extractFromDatagram(datagram);
			if(response instanceof ERR) {
				ERR err = (ERR)response;
				if(err.errorCode == ERR.FILE_ALREADY_EXISTS) {
					throw new FileAlreadyExistsException("Cannot write to server: " + err.message);
				}
				else {
					throw new IOException("Cannot write to server: " + err.message);
				}
			}
			else if(response instanceof ACK) {
				Logger.log("Got the green light to write to the server!");
				try(Scanner s = new Scanner(file)) {
					s.useDelimiter("\\Z");
					String textContents = s.next();
					byte[] fileBytes = textContents.getBytes();
					
					try {
						TimeUnit.MILLISECONDS.sleep(100);
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					int dataLinkPort = datagram.getPort();			
					Logger.log("Sending file to server @ port " + dataLinkPort);
					Sender fileSender = new Sender(clientSocket, dataLinkPort, serverAddress);
					
					try {
						FileSender.sendFile(fileBytes, fileSender);
					} catch(IOException e) {
						throw new IOException("Failed to send file to server: " + e.getMessage());
					}
				}
				Logger.log("Done sending.");
			}
		}
	}	
}
