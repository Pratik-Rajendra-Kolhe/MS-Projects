package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.Serializable;
import java.util.HashMap;
import java.util.TreeMap;

/**
 *  
 * @author pratik
 */

public class Message implements Serializable{
	
	private static final long serialVersionUID = 1L;
	public String key;
	public String hashKey;
	public String value;
	public String type;
	public Node sender;
	public Node sendto;
	public int sendToPort;
	public Node modifiedNode;
	public int delrows;
	public int replica;
	public HashMap<String,Node> nodeMap;
	public HashMap<String,String> result;
	
}
