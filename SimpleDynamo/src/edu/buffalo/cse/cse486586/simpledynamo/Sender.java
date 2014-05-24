package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

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
			
			SocketAddress sockaddr = new InetSocketAddress("10.0.2.2",msg.sendToPort);
			connection = new Socket();
			connection.connect(sockaddr, 500);
			outstream=new ObjectOutputStream(connection.getOutputStream());
			outstream.writeObject(msg);
			connection.close();

		} catch (Exception e) {
			
			if(msg.type.equalsIgnoreCase("insert")){
				if(msg.replica<3){
				msg.replica++;
				msg.sendto=msg.sendto.successor;
				msg.sendToPort=msg.sendto.port;
				Sender s=new Sender(msg);
				s.sendMsg();
				}
				
			}else if(msg.type.equalsIgnoreCase("query-k") || msg.type.equalsIgnoreCase("query-@-recovery") || msg.type.equalsIgnoreCase("query-*")){
				if(msg.replica<2){
				msg.sendto=msg.sendto.successor;
				msg.sendToPort=msg.sendto.port;
				Sender s=new Sender(msg);
				s.sendMsg();
				}else{
					msg.type="query-result";
					msg.sendto=msg.sender;
					msg.sendToPort=msg.sender.port;
					Sender s=new Sender(msg);
					s.sendMsg();
				}
			
			}else if(msg.type.equalsIgnoreCase("query-recovery")){
				if(msg.replica<2){
				msg.replica++;
				msg.sendto=msg.sendto.predessor;
				msg.sendToPort=msg.sendto.port;
				Sender s=new Sender(msg);
				s.sendMsg();
				}else{
					msg.type="query-result";
					msg.sendto=msg.sender;
					msg.sendToPort=msg.sender.port;
					Sender s=new Sender(msg);
					s.sendMsg();
				}
			}
			
		} 

	}


}

