package edu.buffalo.cse.cse486586.simpledht;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class MessageProcessor {

	public static final String AUTHORITY = "edu.buffalo.cse.cse486586.simpledht.provider"; 
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	private static final String KEY_FIELD = "key";
	private static final String VALUE_FIELD = "value";
	Context c;
	public MessageProcessor(Context c){
		this.c=c;
	}

	void processMessage(Message msg){

		if(msg.type.equalsIgnoreCase("join")){
			processJoinRequestMsg(msg);
		}else if(msg.type.equalsIgnoreCase("modified-node")){
			processModifyNodeMsg(msg);

		}else if(msg.type.equalsIgnoreCase("insert")){
			processInsertRequestMsg(msg);
		}else if(msg.type.contains("query")){
			processQueryRequestMsg(msg);
		}
		else if(msg.type.contains("del")){
			processDeleteRequestMsg(msg);
		}


	}

	void processModifyNodeMsg(Message msg){

		SimpleDhtProvider.myNode=msg.modifiedNode;

	}

	void processInsertRequestMsg(Message msg){
		try{			
			if((msg.hashKey.compareTo(SimpleDhtProvider.myNode.hashKey))<=0 && (msg.hashKey.compareTo(SimpleDhtProvider.myNode.predessor.hashKey))>0){
				// IF KEY LIES BETWEEN PREDESSOR AND CURRENT NODE THEN INSERT

				ContentResolver cr=c.getContentResolver();
				Uri uri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
				ContentValues cv=new ContentValues();
				cv.put(KEY_FIELD,msg.key);
				cv.put(VALUE_FIELD,msg.value);
				cr.insert(uri,cv);

			}
			else if(((msg.hashKey.compareTo(SimpleDhtProvider.myNode.hashKey))>0) && ((msg.hashKey.compareTo(SimpleDhtProvider.myNode.predessor.hashKey))>0)  && ((SimpleDhtProvider.myNode.hashKey.compareTo(SimpleDhtProvider.myNode.predessor.hashKey))<0))
			{
				ContentResolver cr=c.getContentResolver();
				Uri uri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
				ContentValues cv=new ContentValues();
				cv.put(KEY_FIELD,msg.key);
				cv.put(VALUE_FIELD,msg.value);
				cr.insert(uri,cv);

			}else if(((msg.hashKey.compareTo(SimpleDhtProvider.myNode.hashKey))<0) && ((msg.hashKey.compareTo(SimpleDhtProvider.myNode.predessor.hashKey))<0)  && ((SimpleDhtProvider.myNode.hashKey.compareTo(SimpleDhtProvider.myNode.predessor.hashKey))<0))
			{
				ContentResolver cr=c.getContentResolver();
				Uri uri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
				ContentValues cv=new ContentValues();
				cv.put(KEY_FIELD,msg.key);
				cv.put(VALUE_FIELD,msg.value);
				cr.insert(uri,cv);

			}


		}catch(Exception e){
			e.printStackTrace();
		}

	}


	void processQueryRequestMsg(Message msg){
		try{	

			if(msg.type.equalsIgnoreCase("query-*")){

				// IF STAR QUERY
				if(!msg.sender.name.equalsIgnoreCase(SimpleDhtProvider.myNode.name)){
					// IF MSG SENDER NOT CURRENT NODE
					System.out.println("in query *- not sender");
					ContentResolver cr=c.getContentResolver();
					Uri uri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
					Cursor cursor = cr.query(uri, null,"@", null, null);	
					int keyIndex=cursor.getColumnIndex(KEY_FIELD);
					int valIndex=cursor.getColumnIndex(VALUE_FIELD);

					while (cursor.moveToNext()) {
						msg.result.put(cursor.getString(valIndex),cursor.getString(keyIndex));
					}
					cursor.close();
					msg.sendToPort=SimpleDhtProvider.myNode.successor.port;
					Sender s=new Sender(msg);
					s.sendMsg();

				}else{
					//IF MSG SENDER CURRENT NODE
					for(String value : msg.result.keySet()){
						String key=msg.result.get(value);
						String row[]=new String[2];
						row[0]=key;
						row[1]=value;
						SimpleDhtProvider.resultCursor.addRow(row);

					}
						SimpleDhtProvider.isresult=true;

				}


			}else if(msg.type.equalsIgnoreCase("query-k")){

				ContentResolver cr=c.getContentResolver();
				Uri uri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
				Cursor cursor = cr.query(uri, null,msg.key, null, "asc");
				if(cursor.getCount()>0){	
					int keyIndex=cursor.getColumnIndex(KEY_FIELD);
					int valIndex=cursor.getColumnIndex(VALUE_FIELD);
					while (cursor.moveToNext()) {
						msg.result.put(cursor.getString(valIndex),cursor.getString(keyIndex));
					}
					msg.sendToPort=msg.sender.port;
					msg.type="query-result";
					Sender s=new Sender(msg);
					s.sendMsg();
					cursor.close();
				}

			}else if(msg.type.equalsIgnoreCase("query-result")){
				for(String value : msg.result.keySet()){
					String key=msg.result.get(value);
					String row[]=new String[2];
					row[0]=key;
					row[1]=value;
					SimpleDhtProvider.resultCursor.addRow(row);
				}
				SimpleDhtProvider.isresult=true;

			}

		}catch(Exception e){
			e.printStackTrace();
		}




	}

	void processDeleteRequestMsg(Message msg){

		try{	

			if(msg.type.equalsIgnoreCase("del-*")){

				// IF STAR QUERY
				if(!msg.sender.name.equalsIgnoreCase(SimpleDhtProvider.myNode.name)){
					// IF MSG SENDER NOT CURRENT NODE
					ContentResolver cr=c.getContentResolver();
					Uri uri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
					int delrows=cr.delete(uri, "@", null);

					msg.sendToPort=SimpleDhtProvider.myNode.successor.port;
					msg.delrows=msg.delrows+delrows;

					Sender s=new Sender(msg);
					s.sendMsg();

				}else{
					SimpleDhtProvider.delrows=msg.delrows;

				}

			}else if(msg.type.equalsIgnoreCase("del-k")){

				if((msg.hashKey.compareTo(SimpleDhtProvider.myNode.hashKey))<=0 && (msg.hashKey.compareTo(SimpleDhtProvider.myNode.predessor.hashKey))>0){
					// IF KEY LIES BETWEEN PREDESSOR AND CURRENT NODE 
					ContentResolver cr=c.getContentResolver();
					Uri uri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
					int delrows=cr.delete(uri,msg.key, null);

					msg.sendToPort=msg.sender.port;
					msg.delrows=msg.delrows+delrows;
					msg.type="del-result";

					Sender s=new Sender(msg);
					s.sendMsg();


				}else if(msg.sender.name.equalsIgnoreCase(SimpleDhtProvider.myNode.name)){
					SimpleDhtProvider.lastNode=true;
					ContentResolver cr=c.getContentResolver();
					Uri uri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
					cr.delete(uri,msg.key, null);
				}
				else{

					//FORWARD THE MESSAGE
					msg.sendToPort=SimpleDhtProvider.myNode.successor.port;
					Sender s=new Sender(msg);
					s.sendMsg();
				}


			}else if(msg.type.equalsIgnoreCase("del-result")){
				SimpleDhtProvider.delrows=msg.delrows;
			}

		}catch(Exception e){
			e.printStackTrace();
		}





	}

	private Uri buildUri(String scheme, String authority) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.scheme(scheme);
		return uriBuilder.build();
	}
	
	void processJoinRequestMsg(Message msg){
		Node joiner=msg.sender;

		if((joiner.hashKey.compareTo(SimpleDhtProvider.myNode.hashKey))>0 && (joiner.hashKey.compareTo(SimpleDhtProvider.myNode.successor.hashKey))<0){
			// IF JOINER'S HASH KEY LIES BETWEEN CURRENT NODE AND ITS SUCCESSOR

			// SET JOINER'S SUCCESSOR'S AND PREDESSORS
			joiner.successor=SimpleDhtProvider.myNode.successor;
			joiner.predessor=SimpleDhtProvider.myNode;

			//NOTIFY JOINER
			Message newMsg=new Message();
			newMsg.type="modified-node";
			newMsg.sender=SimpleDhtProvider.myNode;
			newMsg.sendToPort=joiner.port;
			newMsg.modifiedNode=joiner;

			Sender s1=new Sender(newMsg);
			s1.sendMsg();

			// MODIFY CURRENT NODE'S SUCCESSOR
			SimpleDhtProvider.myNode.successor=joiner;

			// MODIFY SUCCESSOR
			Node modifiedSuccessor=new Node();
			modifiedSuccessor=joiner.successor;
			modifiedSuccessor.predessor=joiner;

			Message newMsg2=new Message();
			newMsg2.type="modified-node";
			newMsg2.sender=SimpleDhtProvider.myNode;
			newMsg2.sendToPort=modifiedSuccessor.port;
			newMsg2.modifiedNode=modifiedSuccessor;

			Sender s2=new Sender(newMsg2);
			s2.sendMsg();
		}

		else if(SimpleDhtProvider.myNode.successor.name.equalsIgnoreCase(SimpleDhtProvider.myNode.name)){
			// FIRST NODE TO JOIN

			joiner.predessor=SimpleDhtProvider.myNode;
			joiner.successor=SimpleDhtProvider.myNode;

			//MODIFY CURRENT NODE
			SimpleDhtProvider.myNode.successor=joiner;
			SimpleDhtProvider.myNode.predessor=joiner;

			//NOTIFY JOINER
			Message newMsg=new Message();
			newMsg.type="modified-node";
			newMsg.sender=SimpleDhtProvider.myNode;
			newMsg.sendToPort=joiner.port;
			newMsg.modifiedNode=joiner;

			Sender s=new Sender(newMsg);
			s.sendMsg();

		}else if((SimpleDhtProvider.myNode.successor.hashKey.compareTo(SimpleDhtProvider.myNode.hashKey))<0){
			
			Node suc=SimpleDhtProvider.myNode.successor;
			
			if((joiner.hashKey.compareTo(suc.hashKey))>0 && (joiner.hashKey.compareTo(SimpleDhtProvider.myNode.hashKey))>0){
          		
			//FINAL PART OF THE CIRCLE
			joiner.successor=SimpleDhtProvider.myNode.successor;
			joiner.predessor=SimpleDhtProvider.myNode;


			//NOTIFY JOINER
			Message newMsg=new Message();
			newMsg.type="modified-node";
			newMsg.sender=SimpleDhtProvider.myNode;
			newMsg.sendToPort=joiner.port;
			newMsg.modifiedNode=joiner;

			Sender s1=new Sender(newMsg);
			s1.sendMsg();


			SimpleDhtProvider.myNode.successor=joiner;

			// MODIFY SUCCESSOR

			Node modifiedSuccessor=new Node();
			modifiedSuccessor=joiner.successor;
			modifiedSuccessor.predessor=joiner;

			Message newMsg2=new Message();
			newMsg2.type="modified-node";
			newMsg2.sender=SimpleDhtProvider.myNode;
			newMsg2.sendToPort=modifiedSuccessor.port;
			newMsg2.modifiedNode=modifiedSuccessor;

			Sender s2=new Sender(newMsg2);
			s2.sendMsg();
			
			}
			else if((joiner.hashKey.compareTo(SimpleDhtProvider.myNode.hashKey))<0){
				
				if((joiner.hashKey.compareTo(suc.hashKey))<0){
					
					//FINAL PART OF THE CIRCLE
					joiner.successor=SimpleDhtProvider.myNode.successor;
					joiner.predessor=SimpleDhtProvider.myNode;


					//NOTIFY JOINER
					Message newMsg=new Message();
					newMsg.type="modified-node";
					newMsg.sender=SimpleDhtProvider.myNode;
					newMsg.sendToPort=joiner.port;
					newMsg.modifiedNode=joiner;

					Sender s1=new Sender(newMsg);
					s1.sendMsg();


					SimpleDhtProvider.myNode.successor=joiner;

					// MODIFY SUCCESSOR

					Node modifiedSuccessor=new Node();
					modifiedSuccessor=joiner.successor;
					modifiedSuccessor.predessor=joiner;

					Message newMsg2=new Message();
					newMsg2.type="modified-node";
					newMsg2.sender=SimpleDhtProvider.myNode;
					newMsg2.sendToPort=modifiedSuccessor.port;
					newMsg2.modifiedNode=modifiedSuccessor;

					Sender s2=new Sender(newMsg2);
					s2.sendMsg();

				}else if((joiner.hashKey.compareTo(suc.hashKey))>0){

					//FINAL PART OF THE CIRCLE
					joiner.predessor=suc;
					joiner.successor=suc.successor;


					//NOTIFY JOINER
					Message newMsg=new Message();
					newMsg.type="modified-node";
					newMsg.sender=SimpleDhtProvider.myNode;
					newMsg.sendToPort=joiner.port;
					newMsg.modifiedNode=joiner;

					Sender s1=new Sender(newMsg);
					s1.sendMsg();

					// MODIFY SUCCESSOR

					Node modifiedSuccessor=new Node();
					modifiedSuccessor=suc;
					modifiedSuccessor.successor=joiner;

					Message newMsg2=new Message();
					newMsg2.type="modified-node";
					newMsg2.sender=SimpleDhtProvider.myNode;
					newMsg2.sendToPort=suc.port;
					newMsg2.modifiedNode=modifiedSuccessor;

					Sender s2=new Sender(newMsg2);
					s2.sendMsg();
	
					
					// MODIFY  SUCCESSOR SUCCESSOR

					Node sucSuccessor=new Node();
					sucSuccessor=suc.successor;
					sucSuccessor.predessor=joiner;

					if(!SimpleDhtProvider.myNode.name.equalsIgnoreCase(sucSuccessor.name)){
					Message newMsg3=new Message();
					newMsg3.type="modified-node";
					newMsg3.sender=SimpleDhtProvider.myNode;
					newMsg3.sendToPort=sucSuccessor.port;
					newMsg3.modifiedNode=sucSuccessor;

					Sender s3=new Sender(newMsg3);
					s3.sendMsg();

					}
				}
			
			}

		}
		else{
			// FORWARD TO SUCCESSOR
			msg.sendToPort=SimpleDhtProvider.myNode.successor.port;
			Sender s=new Sender(msg);
			s.sendMsg();

		}
	}

}
