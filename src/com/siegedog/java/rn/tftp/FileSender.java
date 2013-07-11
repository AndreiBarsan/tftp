package com.siegedog.java.rn.tftp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import com.siegedog.java.rn.core.Sender;
import com.siegedog.java.rn.tftp.datagrams.ACK;
import com.siegedog.java.rn.tftp.datagrams.Data;
import com.siegedog.java.rn.util.Logger;
import com.siegedog.java.rn.util.ObjectUtil;

public class FileSender {

	public static void sendFile(byte[] fileBytes, Sender sender) throws IOException {
		int totalLength = fileBytes.length;
		int nparts = totalLength / GlobalConfig.MAX_BYTES_IN_PACKAGE_DATA + 1;
		
		for(int i = 0; i < nparts; ++i) {
			int sendAttempt = 0;
			boolean timedOut = true;
			
			int from = i * GlobalConfig.MAX_BYTES_IN_PACKAGE_DATA;
			int to = Math.min( (i + 1) * GlobalConfig.MAX_BYTES_IN_PACKAGE_DATA, totalLength);
			byte chunk[] = Arrays.copyOfRange(fileBytes, from, to);
			
			// Wikipedia:
			//  In the new version of the protocol, a block would only be
			//  retransmitted on timeout.
			
			// Note:
			// 	In this case, numbered ACKs are not really needed.
			while(timedOut && sendAttempt < GlobalConfig.MAX_RETRY_ATTEMPTS) {
				if(sendAttempt > 0) {
					Logger.warning("Resending chunk #" + i);
				}
				
				timedOut = false;
				sendAttempt++;
				sender.sendObject(new Data(i, chunk));
				
				byte buff[] = new byte[GlobalConfig.MAX_BYTES_IN_PACKAGE_DATA];
				DatagramPacket response = new DatagramPacket(buff, buff.length);
				try {
					boolean gotRightAck = false;
					while( ! gotRightAck) {
						sender.getSocket().setSoTimeout(GlobalConfig.TIMEOUT_MS);
						sender.getSocket().receive(response);
						
						Object result = ObjectUtil.extractFromDatagram(response);
						if(result instanceof ACK) {
							ACK ack = (ACK) result;
							if(ack.block != i) {
								Logger.log("Received an old ack: " + ack.block);
							}
							else {
								gotRightAck = true;
							}
						}
						else {
							throw new IOException("Unknown response received. Aborting. Cause: " + result);
						}
					}
					
				} catch(SocketTimeoutException timeout) {
					timedOut = true;
				} catch (IOException e) {
					throw new IOException("Unexpected error: " + e.getMessage());
				}
				
				// Simulate latency
				try {
					TimeUnit.MILLISECONDS.sleep(GlobalConfig.SIMULATED_LATENCY_MS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			if(timedOut) {
				throw new SocketTimeoutException("Timed out after " + GlobalConfig.MAX_RETRY_ATTEMPTS + " re-send requests.");
			}
		}
	}
}
