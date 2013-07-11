package com.siegedog.java.rn.tftp.datagrams;

import java.io.Serializable;

public class RRQ implements Serializable {
	private static final long serialVersionUID = 3559823155440493733L;
	public final String fileName;

	public RRQ(String fileName) {
		this.fileName = fileName;
	}
}