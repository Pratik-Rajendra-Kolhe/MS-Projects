package edu.buffalo.cse.cse486586.simpledynamo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class MessageProcessor {

	public static final String AUTHORITY = "edu.buffalo.cse.cse486586.simpledynamo.provider"; 
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	private static final String KEY_FIELD = "key";
	private static final String VALUE_FIELD = "value";
	Context c;
	DBhelper dbHelper;
	public MessageProcessor(Context c){
		this.c=c;
		dbHelper=new DBhelper(c);
	}

	void processMessage(Message msg){

		if(msg.type.contains("insert")){
			processInsertRequestMsg(msg);
		}else if(msg.type.contains("query")){
			processQueryRequestMsg(msg);
		}
		else if(msg.type.contains("del")){
			processDeleteRequestMsg(msg);
		}else if(msg.type.contains("recovery")){
			processRecoveryMsg(msg);
		}


	}

	void processInsertRequestMsg(Message msg){

		try{			

			if(msg.type.equalsIgnoreCase("insert")){
				if(msg.replica<3){
					msg.replica++;	
					// Forward msg to successor for replication
					msg.sendto=SimpleDynamoProvider.myNode.successor;
					msg.sendToPort=SimpleDynamoProvider.myNode.successor.port;
					Sender s=new Sender(msg);
					s.sendMsg();

					ContentValues cv=new ContentValues();
					cv.put(KEY_FIELD,msg.key);
					cv.put(VALUE_FIELD,msg.value);

					SQLiteDatabase database = dbHelper.getWritableDatabase();
					database.insertWithOnConflict(DynamoDB.SQLITE_TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE );
					database.close();



				}
			}

		}catch(Exception e){
			System.out.println("insert issue");
		}

	}


	synchronized void processQueryRequestMsg(Message msg){
		try{	

			if(msg.type.equalsIgnoreCase("query-*")){

				// IF STAR QUERY
				if(!msg.sender.name.equalsIgnoreCase(SimpleDynamoProvider.myNode.name)){
					// IF MSG SENDER NOT CURRENT NODE
					String selectQuery = "SELECT * FROM "+DynamoDB.SQLITE_TABLE;	
					SQLiteDatabase database = dbHelper.getWritableDatabase();
					Cursor cursor = database.rawQuery(selectQuery, null);
					int keyIndex=cursor.getColumnIndex(KEY_FIELD);
					int valIndex=cursor.getColumnIndex(VALUE_FIELD);

					while (cursor.moveToNext()) {
						msg.result.put(cursor.getString(keyIndex),cursor.getString(valIndex));
					}
					cursor.close();
					database.close();
					msg.sendto=SimpleDynamoProvider.myNode.successor;
					msg.sendToPort=SimpleDynamoProvider.myNode.successor.port;
					Sender s=new Sender(msg);
					s.sendMsg();

				}else{

					//IF MSG SENDER CURRENT NODE
					for(String key : msg.result.keySet()){
						String value=msg.result.get(key);
						String row[]=new String[2];
						row[0]=key;
						row[1]=value;
						SimpleDynamoProvider.resultCursor.addRow(row);

					}
					SimpleDynamoProvider.isQueried=true;

				}


			}else if(msg.type.equalsIgnoreCase("query-k")){


				String selectQuery = "SELECT * FROM "+DynamoDB.SQLITE_TABLE+" WHERE "+DynamoDB.KEY_ID+" = '"+msg.key+"'";
				SQLiteDatabase database = dbHelper.getWritableDatabase();
				Cursor cursor = database.rawQuery(selectQuery, null);

				if(cursor.getCount()>0){	
					int keyIndex=cursor.getColumnIndex(KEY_FIELD);
					int valIndex=cursor.getColumnIndex(VALUE_FIELD);
					while (cursor.moveToNext()) {
						msg.result.put(cursor.getString(keyIndex),cursor.getString(valIndex));
					}
					msg.sendto=msg.sender;
					msg.sendToPort=msg.sender.port;
					msg.type="query-result";
					Sender s=new Sender(msg);
					s.sendMsg();
				}
				database.close();

			}else if(msg.type.equalsIgnoreCase("query-result")){

				for(String key : msg.result.keySet()){
					String value=msg.result.get(key);
					String row[]=new String[2];
					row[0]=key;
					row[1]=value;
					SimpleDynamoProvider.resultCursor.addRow(row);
				}
				SimpleDynamoProvider.isQueried=true;


			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	void processDeleteRequestMsg(Message msg){

		try{	

			if(msg.type.equalsIgnoreCase("del-*")){

				// IF STAR QUERY
				if(!msg.sender.name.equalsIgnoreCase(SimpleDynamoProvider.myNode.name)){
					// IF MSG SENDER NOT CURRENT NODE
					SQLiteDatabase database = dbHelper.getWritableDatabase();
					int delrows = database.delete(DynamoDB.SQLITE_TABLE, null, null);
					database.close();


					msg.sendto=SimpleDynamoProvider.myNode.successor;
					msg.sendToPort=SimpleDynamoProvider.myNode.successor.port;
					msg.delrows=msg.delrows+delrows;

					Sender s=new Sender(msg);
					s.sendMsg();


				}else{
					SimpleDynamoProvider.delrows=msg.delrows;

				}

			}else if(msg.type.equalsIgnoreCase("del-k")){

				if(msg.replica>0){

					msg.replica--;	
					String where=KEY_FIELD+"='"+msg.key+"'";
					SQLiteDatabase database = dbHelper.getWritableDatabase();
					database.delete(DynamoDB.SQLITE_TABLE, where, null);

					msg.sendto=SimpleDynamoProvider.myNode.successor;
					msg.sendToPort=SimpleDynamoProvider.myNode.successor.port;
					Sender s=new Sender(msg);
					s.sendMsg();			
					database.close();	
				}

			}

		}catch(Exception e){
			e.printStackTrace();
		}
	}

	synchronized void processRecoveryMsg(Message msg){
		try{
			if(msg.type.equalsIgnoreCase("@-recovery")){

				String selectQuery = "SELECT * FROM "+DynamoDB.SQLITE_TABLE;	
				SQLiteDatabase database = dbHelper.getWritableDatabase();
				Cursor cursor = database.rawQuery(selectQuery, null);
				int keyIndex=cursor.getColumnIndex(KEY_FIELD);
				int valIndex=cursor.getColumnIndex(VALUE_FIELD);

				while (cursor.moveToNext()) {
					msg.result.put(cursor.getString(keyIndex),cursor.getString(valIndex));
				}
				cursor.close();
				database.close();

				msg.type="recovery-result";
				msg.sendto=msg.sender;
				msg.sendToPort=msg.sender.port;
				Sender s=new Sender(msg);
				s.sendMsg();

			}
		else if(msg.type.equalsIgnoreCase("recovery")){

				String selectQuery = "SELECT * FROM "+DynamoDB.SQLITE_TABLE;	
				SQLiteDatabase database = dbHelper.getWritableDatabase();
				Cursor cursor = database.rawQuery(selectQuery, null);
				int keyIndex=cursor.getColumnIndex(KEY_FIELD);
				int valIndex=cursor.getColumnIndex(VALUE_FIELD);


				while (cursor.moveToNext()) {
					String key=cursor.getString(keyIndex);
					String value=cursor.getString(valIndex);

					if(isMyPartiton(genHash(key))){
						msg.result.put(key,value);
					}

				}
				cursor.close();
				database.close();
				msg.type="recovery-result";
				msg.sendto=msg.sender;
				msg.sendToPort=msg.sender.port;
				Sender s=new Sender(msg);
				s.sendMsg();

			}
		else if(msg.type.equalsIgnoreCase("recovery-result")){
			for(String key : msg.result.keySet()){
				String value=msg.result.get(key);
				String row[]=new String[2];
				row[0]=key;
				row[1]=value;
				SimpleDynamoProvider.recoveryCursor.addRow(row);
			}
			SimpleDynamoProvider.recovery=true;
		}

	}catch(Exception e){
		e.printStackTrace();
	}
}



private String genHash(String input) throws NoSuchAlgorithmException {
	MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
	byte[] sha1Hash = sha1.digest(input.getBytes());
	Formatter formatter = new Formatter();
	for (byte b : sha1Hash) {
		formatter.format("%02x", b);
	}
	return formatter.toString();
}


public boolean isMyPartiton(String hashKey){

	if((hashKey.compareTo(SimpleDynamoProvider.myNode.hashKey))<=0 && (hashKey.compareTo(SimpleDynamoProvider.myNode.predessor.hashKey))>0){
		return true;			
	}else if(((hashKey.compareTo(SimpleDynamoProvider.myNode.hashKey))>0) && ((hashKey.compareTo(SimpleDynamoProvider.myNode.predessor.hashKey))>0)  && ((SimpleDynamoProvider.myNode.hashKey.compareTo(SimpleDynamoProvider.myNode.predessor.hashKey))<0)){

		return true;

	}else if(((hashKey.compareTo(SimpleDynamoProvider.myNode.hashKey))<0) && ((hashKey.compareTo(SimpleDynamoProvider.myNode.predessor.hashKey))<0)  && ((SimpleDynamoProvider.myNode.hashKey.compareTo(SimpleDynamoProvider.myNode.predessor.hashKey))<0)){

		return true;

	}else{
		return false;
	}
}

}
