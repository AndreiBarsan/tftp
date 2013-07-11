package com.siegedog.java.rn.tftp.datagrams;

import java.io.Serializable;

public class ERR implements Serializable {
	public static final int UNKNOWN_ERROR = 0;
	public static final int FILE_NOT_FOUND = 1;
	public static final int ACCESS_VIOLATION = 2;
	public static final int DISK_FULL = 3;
	public static final int ILLEGAL_OPERATION = 4;
	public static final int UNKNOWN_TRANSFER_ID = 5;
	public static final int FILE_ALREADY_EXISTS = 6;
	public static final int NO_SUCH_USER = 7;
	
	
	private static final long serialVersionUID = 6196600960899586303L;
	public final int errorCode;
	public final String message;
	
	public ERR(int errorCode, String message) {
		this.errorCode = errorCode;
		this.message = message;
	}
}