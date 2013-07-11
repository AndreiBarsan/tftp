package com.siegedog.java.rn.tftp.datagrams;

import java.io.Serializable;

public class Data implements Serializable {
	private static final long serialVersionUID = -633233086785912704L;
	public final int block;
	public final byte[] data;
	
	public Data(int block, byte[] data) {
		super();
		this.block = block;
		this.data = data;
	}
}