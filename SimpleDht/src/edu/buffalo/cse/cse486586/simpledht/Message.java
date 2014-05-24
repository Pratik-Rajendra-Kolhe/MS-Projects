package edu.buffalo.cse.cse486586.simpledht;

import java.io.Serializable;
import java.util.TreeMap;

public class Message implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String key;
	public String hashKey;
	public String value;
	public String type;
	public Node sender;
	public int sendToPort;
	public Node modifiedNode;
	public int waittime;
	public int delrows;
	public TreeMap<String,String> result;
	
}
