package edu.buffalo.cse.cse486586.simpledht;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *  
 * @author pratik
 */

public class Sender extends Thread{

	Message msg;
	Node node;
	Socket connection;
	ObjectOutputStream outstream;
	public Sender(Message msg){
		this.msg=msg;
	}

	public void sendMsg()
	{
		this.start();
	}

	public void run(){
		try {

			connection=new Socket("10.0.2.2",msg.sendToPort);
			outstream=new ObjectOutputStream(connection.getOutputStream());
			outstream.writeObject(msg);
			outstream.flush();
			outstream.close();
			connection.close();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}


}

