package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.content.Context;

/**
 *  
 * @author pratik
 */
public class Receiver extends Thread {
	int port;
	ServerSocket server;
	ObjectInputStream instream;
	Socket connection;
	Context c;

	public Receiver(Context c){
		try{
			this.port=10000;
			this.server=new ServerSocket(port);
			this.c=c;
		}catch(Exception e){
			SimpleDynamoProvider.isRecovered=true;
			System.out.println("Bind :X");	        
		}
	}


	public void beginReceivingMsg()
	{
		this.start();
	}

	public void run(){
		Message msg=null;
		try {
			while(true)
			{
				connection=server.accept();
				instream=new ObjectInputStream(connection.getInputStream());
				msg=(Message)instream.readObject();
				instream.close();
				if(msg!=null){
					new MessageProcessor(c).processMessage(msg);
				}

			}
		} 
		catch (EOFException e) {
			System.out.println("EOF exception");
			try {
				server.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			Receiver r=new Receiver(c);
			r.beginReceivingMsg();

		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}catch(Exception e){
			System.out.println("Null pointer :/");
		}


	}



}