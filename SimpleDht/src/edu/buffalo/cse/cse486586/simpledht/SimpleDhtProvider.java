package edu.buffalo.cse.cse486586.simpledht;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.HashMap;
import java.util.TreeMap;

import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.telephony.TelephonyManager;


public class SimpleDhtProvider extends ContentProvider {

	DBhelper dbHelper ;
	public static final String AUTHORITY = "edu.buffalo.cse.cse486586.simpledht.provider"; 
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	private static final String KEY_FIELD = "key";
	private static final String VALUE_FIELD = "value";


	public static Node myNode;
	public static HashMap<String,Node> nodeMap;
	public static HashMap<String,Integer> portMap;
	public static boolean lastNode;
	public static MatrixCursor resultCursor;
	public static int delrows;
	public static boolean isresult;
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		try{

			if(selection.equalsIgnoreCase("@")){
				SQLiteDatabase database = dbHelper.getWritableDatabase();
				int delRows = database.delete(ChordDB.SQLITE_TABLE, null, null);
				return delRows;

			}

			else if(selection.equalsIgnoreCase("*")){

				SQLiteDatabase database = dbHelper.getWritableDatabase();
				int delRows = database.delete(ChordDB.SQLITE_TABLE, null, null);

				Message msg=new Message();
				msg.key=null;
				msg.hashKey=null;
				msg.type="del-*";
				msg.sendToPort=myNode.successor.port;
				msg.sender=myNode;
				msg.modifiedNode=null;
				msg.result=new TreeMap<String,String>();
				msg.delrows=delRows;

				Sender s=new Sender(msg);
				s.sendMsg();

				return delrows;

			}else {

				String key=selection;
				String hashKey= genHash(key);

				if((hashKey.compareTo(myNode.hashKey))<=0 && (hashKey.compareTo(myNode.predessor.hashKey))>0){
					// IF KEY LIES BETWEEN PREDESSOR AND CURRENT NODE 
					String where=KEY_FIELD+"='"+key+"'";
					SQLiteDatabase database = dbHelper.getWritableDatabase();
					int delRows = database.delete(ChordDB.SQLITE_TABLE, where, null);
					return delRows;


				}else if(lastNode){

					String where=KEY_FIELD+"='"+key+"'";
					SQLiteDatabase database = dbHelper.getWritableDatabase();
					int delRows = database.delete(ChordDB.SQLITE_TABLE, where, null);
					lastNode=false;
					return delRows;


				}
				else{
					// FORWARD THE MESSAGE TO THE SUCCESSOR
					Message msg=new Message();
					msg.key=key;
					msg.hashKey=hashKey;
					msg.type="del-k";
					msg.sendToPort=myNode.successor.port;
					msg.sender=myNode;
					msg.modifiedNode=null;
					Sender s=new Sender(msg);
					s.sendMsg();


					return delrows;
				}

			}

		}catch(Exception e){
			e.printStackTrace();
		}

		return 0;

	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressLint("InlinedApi")
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		try {

			if(myNode.successor.name.equalsIgnoreCase(myNode.name)){

				SQLiteDatabase database = dbHelper.getWritableDatabase();

				long val=database.insert(ChordDB.SQLITE_TABLE, null, values);

				uri=Uri.withAppendedPath(CONTENT_URI, String.valueOf(val));

				return uri;


			}

			String key=(String)values.get(KEY_FIELD);
			String value=(String)values.get(VALUE_FIELD);
			String hashKey= genHash(key);



			if((hashKey.compareTo(myNode.hashKey))<=0 && (hashKey.compareTo(myNode.predessor.hashKey))>0){

				// IF KEY LIES BETWEEN PREDESSOR AND CURRENT NODE THEN INSERT

				SQLiteDatabase database = dbHelper.getWritableDatabase();

				long val=database.insert(ChordDB.SQLITE_TABLE, null, values );

				uri=Uri.withAppendedPath(CONTENT_URI, String.valueOf(val));

				return uri;

			}else if(((hashKey.compareTo(myNode.hashKey))>0) && ((hashKey.compareTo(myNode.predessor.hashKey))>0)  && ((myNode.hashKey.compareTo(myNode.predessor.hashKey))<0)){


				SQLiteDatabase database = dbHelper.getWritableDatabase();

				long val=database.insert(ChordDB.SQLITE_TABLE, null, values);

				uri=Uri.withAppendedPath(CONTENT_URI, String.valueOf(val));


				return uri;

			}else if(((hashKey.compareTo(myNode.hashKey))<0) && ((hashKey.compareTo(myNode.predessor.hashKey))<0)  && ((myNode.hashKey.compareTo(myNode.predessor.hashKey))<0)){

				SQLiteDatabase database = dbHelper.getWritableDatabase();

				long val=database.insert(ChordDB.SQLITE_TABLE, null, values);

				uri=Uri.withAppendedPath(CONTENT_URI, String.valueOf(val));


				return uri;



			}else{

				// FORWARD THE MESSAGE TO THE SUCCESSOR
				for(String s1 : portMap.keySet()){
					int portNo=portMap.get(s1);	
					if(portNo!=myNode.port){
						Message msg=new Message();
						msg.key=key;
						msg.value=value;
						msg.hashKey=hashKey;
						msg.type="insert";
						msg.sendToPort=portNo;
						msg.sender=myNode;
						msg.modifiedNode=null;

						Sender s=new Sender(msg);
						s.sendMsg();
					}
				}			

			}


		} catch (Exception e) {

			SQLiteDatabase database = dbHelper.getWritableDatabase();

			long val=database.insert(ChordDB.SQLITE_TABLE, null, values);

			uri=Uri.withAppendedPath(CONTENT_URI, String.valueOf(val));

			return uri;


		}
		return uri;

	}

	@Override
	public boolean onCreate() {

		try {
			dbHelper = new DBhelper(getContext());
			myNode=new Node();
			lastNode=false;
			resultCursor=new MatrixCursor(new String[] {KEY_FIELD, VALUE_FIELD});
			Context c=getContext();
			TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
			String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
			isresult=false;
			delrows=0;

			portMap= new HashMap<String,Integer> ();
			nodeMap=new HashMap<String,Node> ();

			portMap.put("5554", 11108);
			portMap.put("5556", 11112);
			portMap.put("5558", 11116);
			portMap.put("5560", 11120);
			portMap.put("5562", 11124);

			myNode.name=portStr;
			myNode.hashKey=genHash(portStr);
			myNode.port=portMap.get(myNode.name);
			myNode.predessor=null;
			myNode.successor=null;

			Receiver r=new Receiver(c);
			r.beginReceivingMsg();

			if(!myNode.name.equalsIgnoreCase("5554")){

				Message msg=new Message();
				msg.type="join";
				msg.sender=myNode;
				msg.sendToPort=portMap.get("5554");
				Sender s=new Sender(msg);
				s.sendMsg();

			}
			else{

				myNode.predessor=myNode;
				myNode.successor=myNode;
			}


		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		resultCursor=new MatrixCursor(new String[] {KEY_FIELD, VALUE_FIELD});
		

		try{

			
			if(myNode.successor.name.equalsIgnoreCase(myNode.name)){
				String selectQuery;
				   if(selection.equalsIgnoreCase("*") || selection.equalsIgnoreCase("@")){
					   selectQuery = "SELECT * FROM "+ChordDB.SQLITE_TABLE;
				   }
				   else
					   selectQuery = "SELECT * FROM "+ChordDB.SQLITE_TABLE+" WHERE "+ChordDB.KEY_ID+" = '"+selection+"'";
				SQLiteDatabase database = dbHelper.getWritableDatabase();
				Cursor cursor = database.rawQuery(selectQuery, null);
				cursor.moveToFirst();
				System.out.println(cursor.getCount());
				return cursor;
			}

			
			String selectQuery;
			if(selection.equalsIgnoreCase("@")){

				selectQuery = "SELECT * FROM "+ChordDB.SQLITE_TABLE;	
				SQLiteDatabase database = dbHelper.getWritableDatabase();
				Cursor cursor = database.rawQuery(selectQuery, null);
				return cursor;

			}

			else if(selection.equalsIgnoreCase("*")){

				selectQuery = "SELECT * FROM "+ChordDB.SQLITE_TABLE;	
				SQLiteDatabase database = dbHelper.getWritableDatabase();
				Cursor cursor = database.rawQuery(selectQuery, null);
				Message msg=new Message();
				msg.key=null;
				msg.hashKey=null;
				msg.type="query-*";
				msg.sendToPort=myNode.successor.port;
				msg.sender=myNode;
				msg.modifiedNode=null;
				msg.result=new TreeMap<String,String>();

				int keyIndex=cursor.getColumnIndex(KEY_FIELD);
				int valIndex=cursor.getColumnIndex(VALUE_FIELD);

				while (cursor.moveToNext()) {
					msg.result.put(cursor.getString(valIndex),cursor.getString(keyIndex));
				}
				cursor.close();

				Sender s=new Sender(msg);
				s.sendMsg();

				while(!isresult){
				}
				isresult=false;
				return resultCursor;

			}else {

				String key=selection;

				String hashKey= genHash(key);
				selectQuery = "SELECT * FROM "+ChordDB.SQLITE_TABLE+" WHERE "+ChordDB.KEY_ID+" = '"+key+"'";
				SQLiteDatabase database = dbHelper.getWritableDatabase();
				Cursor cursor = database.rawQuery(selectQuery, null);

				if(cursor.getCount()>0){
					return cursor;	
				}else
					if(sortOrder==null){	
						// FORWARD THE MESSAGE TO THE SUCCESSOR
						for(String s1 : portMap.keySet()){
							int portNo=portMap.get(s1);
							if(portNo!=myNode.port){	
								Message msg=new Message();
								msg.key=key;
								msg.hashKey=hashKey;
								msg.type="query-k";
								msg.sendToPort=portNo;
								msg.sender=myNode;
								msg.modifiedNode=null;
								msg.result=new TreeMap<String,String>();
								Sender s=new Sender(msg);
								s.sendMsg();
							}
						}
						while(!isresult){
						}
						isresult=false;

						return resultCursor;

					}else{
						return cursor;

					}
			}

		}catch(Exception e){
			String selectQuery;
			   if(selection.equalsIgnoreCase("*") || selection.equalsIgnoreCase("@")){
				   selectQuery = "SELECT * FROM "+ChordDB.SQLITE_TABLE;
			   }
			   else
				   selectQuery = "SELECT * FROM "+ChordDB.SQLITE_TABLE+" WHERE "+ChordDB.KEY_ID+" = '"+selection+"'";
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			Cursor cursor = database.rawQuery(selectQuery, null);
			cursor.moveToFirst();
			return cursor;
		}

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
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

}
