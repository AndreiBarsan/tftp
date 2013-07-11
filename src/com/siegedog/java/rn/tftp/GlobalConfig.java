package com.siegedog.java.rn.tftp;

public class GlobalConfig {
	
	public static final int MAX_BYTES_IN_PACKAGE = 512;
	/** Accounts for the large Java serialized object overhead. **/
	public static final int MAX_BYTES_IN_PACKAGE_HEADER = 128;
	public static final int MAX_BYTES_IN_PACKAGE_DATA = MAX_BYTES_IN_PACKAGE - MAX_BYTES_IN_PACKAGE_HEADER;
	public static final int TFTP_CONTROL_PORT = 69;
	protected static final boolean debugMode = true;
	
	public static final int TIMEOUT_MS = 1500;
	public static final int MAX_RETRY_ATTEMPTS = 5;
	public static final long SIMULATED_LATENCY_MS = 200;


}
