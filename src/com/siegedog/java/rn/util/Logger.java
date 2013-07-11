package com.siegedog.java.rn.util;


public class Logger {

	public static synchronized void log(String msg) {
		Logger.log(Thread.currentThread().getName(), msg);
	}

	public static synchronized void log(String nm, String msg) {
		System.out.println(nm + ": " + msg);
	}

	public static synchronized void warning(String msg) {
		System.err.println(msg);
	}
	
	/** Reports the fatal error and shuts down the application. */
	public static synchronized void fatalError(String msg, Throwable cause) {
		System.err.println(msg);
		cause.printStackTrace(System.err);
		System.exit(-1);
	}
}
