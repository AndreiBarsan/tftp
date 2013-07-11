package com.siegedog.java.rn.tftp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.FileAlreadyExistsException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.siegedog.java.rn.tftp.server.TftpServer;


/**
 * Ghetto test-suite instead of proper JUnit stuff.
 * @author Andrei Barsan */
public class UseCase {

	public static void main(String[] args) {
		TftpServer server;
		try {
			server = new TftpServer();
			server.setDaemon(true);
			server.start();
		} catch (SocketException e2) {
			System.err.println("Failed to create server.");
			e2.printStackTrace();
			return;
		}
		
		System.out.println("\n\tTesting out parallel operations on the server:\n");
		
		ExecutorService exec = Executors.newCachedThreadPool();
		exec.submit(new Runnable() {
			@Override
			public void run() {
				try {
					new TftpClient("tftp_client_root").fetchFile("another_test_file.txt");
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}
		});
		exec.submit(new Runnable() {
			@Override
			public void run() {
				try {
					new TftpClient("tftp_client_root").fetchFile("test_file.txt");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		exec.shutdown();
		
		// Make sure all the tasks are done
		try {
			exec.awaitTermination(3000, TimeUnit.MILLISECONDS);
		} catch (Exception e) { }
		
		try {
			TftpClient client = new TftpClient("tftp_client_root", InetAddress.getLocalHost(), InetAddress.getLocalHost());
			System.out.println("\n\tRead file successfully: \n");
			client.fetchFile("test_file.txt");
			
			System.out.println("\n\tWrite file successfully: \n");
			client.sendFile("thing_to_send.txt", "thing_to_send.txt.backup");
			
			System.out.println("\n\tTry to read non-existent file:\n");
			try {
				client.fetchFile("i_dont_exist.txt");
			} catch(FileNotFoundException e) {
				System.out.println("File not found error raised correctly: " + e.getMessage());
			}
			
			// Note: the TFTP-RFC actually states the opposite - we are only
			// allowed to write to existing files, but for testing purposes
			// this detail isn't that important
			System.out.println("\n\tTry to write to an existing file:\n");
			try {
				client.sendFile("thing_to_send.txt", "test_file.txt");
			} catch(FileAlreadyExistsException e) {
				System.out.println("Error raised correctly: " + e.getMessage());
			}
			
			// Client-side error handling
			System.out.println("\n\tTry to send a file that doesn't exist:\n");
			try {
				client.sendFile("nothing_to_send.fake", "test_file.txt");
			} catch(FileNotFoundException e) {
				System.out.println("Error raised correctly: " + e.getMessage());
			}
						
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// ...before shutting down the server (and his ExecutorService).
		server.shutdown();	
	}
}
