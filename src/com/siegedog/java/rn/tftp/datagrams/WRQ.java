package com.siegedog.java.rn.tftp.datagrams;

import java.io.Serializable;

public class WRQ implements Serializable {
	private static final long serialVersionUID = -7049923051003925001L;
	
	public final String fileName;
	public WRQ(String fileName) {
		this.fileName = fileName;
	}
}