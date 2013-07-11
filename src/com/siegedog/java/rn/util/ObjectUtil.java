package com.siegedog.java.rn.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;

public class ObjectUtil {

	public static byte[] bytesOf(Object obj) {
		try {
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(byteOut);
			oos.writeObject(obj);
			
			return byteOut.toByteArray();
		}
		catch(IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Object extractFromDatagram(DatagramPacket datagram) throws IOException {
		try(ByteArrayInputStream byteIn = new ByteArrayInputStream(datagram.getData())) {
			try(ObjectInputStream ois = new ObjectInputStream(byteIn)) {
				try {
					return ois.readObject();			
				} catch(ClassCastException e) {
					Logger.fatalError("Received the wrong thing on the network!", e);
				} catch (ClassNotFoundException e) {
					Logger.fatalError("Received the wrong thing on the network, and " +
							"I don't even have the class file for that.", e);
				}
			}
		}
		
		return null;
	}

}
