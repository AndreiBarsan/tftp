package com.siegedog.java.rn.tftp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.siegedog.java.rn.core.Sender;
import com.siegedog.java.rn.core.Receiver;
import com.siegedog.java.rn.core.Receiver.ServerCallback;
import com.siegedog.java.rn.tftp.datagrams.ACK;
import com.siegedog.java.rn.tftp.datagrams.Data;
import com.siegedog.java.rn.tftp.datagrams.ERR;
import com.siegedog.java.rn.util.Logger;

public class FileReceiver {
	
	public static byte[] receiveFile(final DatagramSocket socket, final InetAddress clientAddress) throws IOException {
		final ArrayList<byte[]> chunks = new ArrayList<>();
		final Set<Integer> receivedBlocks = new HashSet<>();
		final TransferStatus transferStatus = new TransferStatus();
		
		socket.setSoTimeout(GlobalConfig.TIMEOUT_MS);
		
		final Receiver<Object> receiver = new Receiver<>(socket);
		receiver.setCallback(new ServerCallback<Object>() {
			@Override
			public void received(Object object) {
				if(object instanceof Data) {
					Data receivedData = (Data) object;
					
					if(receivedBlocks.contains(receivedData.block)) {
						Logger.log("Discarding duplicate block #" + receivedData.block);
						return;
					}
					
					receivedBlocks.add(receivedData.block);
					chunks.add(receivedData.data);
					
					int ackPort = receiver.getLastDatagram().getPort();
					
					Sender signalSender = new Sender(receiver.getSocket(), ackPort, clientAddress);
					signalSender.sendObject(new ACK(receivedData.block));
					
					if(receivedData.data.length < GlobalConfig.MAX_BYTES_IN_PACKAGE_DATA) {
						// We are done!
						Logger.log("Transfer complete.");
						transferStatus.success = true;
						receiver.shutdown();
					}
				}
				else if(object instanceof ERR) {
					ERR err = (ERR) object;
					
					transferStatus.success = false;
					transferStatus.message = "Received ERR from the file sender:";
					
					switch(err.errorCode) {
					
					case ERR.FILE_NOT_FOUND: 
						transferStatus.message += "Remote file does not exist. Aborting.";
						receiver.shutdown();
						return;
						
					default:
						transferStatus.message += "Error code " + err.errorCode + ". Aborting transfer.";
						receiver.shutdown();
						return;
					}
				}
				else {
					transferStatus.success = false;
					transferStatus.message = "Received unexpected data: " + object + "\nAborting.";
					receiver.shutdown();
					return;
				}
			}
		});
		
		receiver.setName("TFTP Client receiver");
		new Thread(receiver).run();
		
		Logger.log("Wating to receive");
		receiver.waitForShutdown();
		
		
		if( ! transferStatus.success) {
			throw new IOException(transferStatus.message);
		}
		
		int totalSize = (chunks.size() - 1) * GlobalConfig.MAX_BYTES_IN_PACKAGE_DATA
				+ chunks.get(chunks.size() - 1).length;
		
		byte result[] = new byte[totalSize];
		for(int i = 0; i < chunks.size(); ++i) {
			System.arraycopy(chunks.get(i),
					0,
					result,
					i * GlobalConfig.MAX_BYTES_IN_PACKAGE_DATA,
					chunks.get(i).length);
		}
	
		return result;
	}
}
