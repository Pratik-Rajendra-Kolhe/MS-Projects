package edu.buffalo.cse.cse486586.simpledht;

import java.io.Serializable;

public class Node implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String name;
	public Node predessor;
	public Node successor;
	public String hashKey;
	public int port;
	
}
