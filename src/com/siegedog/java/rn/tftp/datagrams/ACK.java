package com.siegedog.java.rn.tftp.datagrams;

import java.io.Serializable;

public class ACK implements Serializable {
	private static final long serialVersionUID = -6636314549588940063L;
	public final int block;

	public ACK(int block) {
		this.block = block;
	}
}