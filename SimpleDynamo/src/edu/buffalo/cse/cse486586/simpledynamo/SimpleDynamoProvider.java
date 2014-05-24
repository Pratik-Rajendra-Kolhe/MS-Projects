package edu.buffalo.cse.cse486586.simpledynamo;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import edu.buffalo.cse.cse486586.simpledynamo.DynamoDB;
import edu.buffalo.cse.cse486586.simpledynamo.DBhelper;
import edu.buffalo.cse.cse486586.simpledynamo.Message;
import edu.buffalo.cse.cse486586.simpledynamo.Node;
import edu.buffalo.cse.cse486586.simpledynamo.Receiver;
import edu.buffalo.cse.cse486586.simpledynamo.Sender;
import android.annotation.SuppressLint;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.telephony.TelephonyManager;
/**
 *  
 * @author pratik
 */
public class SimpleDynamoProvider extends ContentProvider {

	DBhelper dbHelper ;
	public static final String AUTHORITY = "edu.buffalo.cse.cse486586.simpledynamo.provider"; 
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	static final String KEY_FIELD = "key";
	static final String VALUE_FIELD = "value";


	public static Node myNode;
	public static HashMap<String,Node> nodeMap;
	public static boolean lastNode;
	public static MatrixCursor resultCursor;
	public static MatrixCursor recoveryCursor;
	public static int delrows;
	public static boolean isresult;
	public static boolean isQueried;
	public static boolean isInserted;
	public static boolean isRecovered;
	public static boolean recovery;
	public static Context c;
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		try{

			if(selection.equalsIgnoreCase("@")){
				SQLiteDatabase database = dbHelper.getWritableDatabase();
				int delRows = database.delete(DynamoDB.SQLITE_TABLE, null, null);
				database.close();
				return delRows;

			}

			else if(selection.equalsIgnoreCase("*")){

				SQLiteDatabase database = dbHelper.getWritableDatabase();
				int delRows = database.delete(DynamoDB.SQLITE_TABLE, null, null);

				Message msg=new Message();
				msg.key=null;
				msg.hashKey=null;
				msg.type="del-*";
				msg.sendto=myNode.successor;
				msg.sendToPort=myNode.successor.port;
				msg.sender=myNode;
				msg.modifiedNode=null;
				msg.result=new HashMap<String,String>();
				msg.delrows=delRows;

				Sender s=new Sender(msg);
				s.sendMsg();

				database.close();
				return delrows;

			}else {

				String key=selection;
				String hashKey= genHash(key);

				Node sendNode=getNode(hashKey);

				Message msg=new Message();
				msg.key=key;
				msg.hashKey=hashKey;
				msg.type="del-k";
				msg.sendto=sendNode;
				msg.sendToPort=sendNode.port;
				msg.sender=myNode;
				msg.modifiedNode=null;
				msg.replica=3;

				Sender s=new Sender(msg);
				s.sendMsg();

			}

		}catch(Exception e){
			e.printStackTrace();
		}

		return 0;

	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@SuppressLint("InlinedApi")
	@Override
	public Uri insert(Uri uri, ContentValues values) {


		try {
			if(myNode.successor.name.equalsIgnoreCase(myNode.name)){

				SQLiteDatabase database = dbHelper.getWritableDatabase();

				long val=database.insert(DynamoDB.SQLITE_TABLE, null, values);

				uri=Uri.withAppendedPath(CONTENT_URI, String.valueOf(val));

				database.close();
				return uri;

			}

			String key=(String)values.get(KEY_FIELD);
			String value=(String)values.get(VALUE_FIELD);
			String hashKey= genHash(key);

			Node sendNode=getNode(hashKey);

			if(sendNode.name.equalsIgnoreCase(myNode.name)){

				Message msg=new Message();
				msg.key=key;
				msg.value=value;
				msg.hashKey=hashKey;
				msg.type="insert";
				msg.sendto=sendNode.successor;
				msg.sendToPort=sendNode.successor.port;
				msg.sender=myNode;
				msg.modifiedNode=null;
				msg.replica=1;

				Sender s=new Sender(msg);
				s.sendMsg();



				SQLiteDatabase database = dbHelper.getWritableDatabase();

				long val=database.insert(DynamoDB.SQLITE_TABLE, null, values);

				uri=Uri.withAppendedPath(CONTENT_URI, String.valueOf(val));

				database.close();

			}else{
				Message msg=new Message();
				msg.key=key;
				msg.value=value;
				msg.hashKey=hashKey;
				msg.type="insert";
				msg.sendto=sendNode;
				msg.sendToPort=sendNode.port;
				msg.sender=myNode;
				msg.modifiedNode=null;
				msg.replica=0;

				Sender s=new Sender(msg);
				s.sendMsg();
			}


		} catch (Exception e) {

			SQLiteDatabase database = dbHelper.getWritableDatabase();

			long val=database.insert(DynamoDB.SQLITE_TABLE, null, values);

			uri=Uri.withAppendedPath(CONTENT_URI, String.valueOf(val));

			database.close();
			return uri;


		}
		return uri;

	}

	@Override
	public boolean onCreate() {

		String portStr=null;
		try {
			dbHelper = new DBhelper(getContext());
			myNode=new Node();
			lastNode=false;
			resultCursor=new MatrixCursor(new String[] {KEY_FIELD, VALUE_FIELD});
			recoveryCursor=new MatrixCursor(new String[] {KEY_FIELD, VALUE_FIELD});

			c=getContext();

			TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
			portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);

			isRecovered=false;
			recovery=false;
			delrows=0;


			Receiver r=new Receiver(c);
			r.start();

			isresult=false;
			isQueried=false;
			isInserted=false;

			nodeMap=new HashMap<String,Node> ();

			join(portStr);
		} catch (Exception e) {
			e.printStackTrace();
		}




		return true;
	}

	@Override
	synchronized public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {


		resultCursor=new MatrixCursor(new String[] {KEY_FIELD, VALUE_FIELD});


		try{


			if(myNode.successor.name.equalsIgnoreCase(myNode.name)){
				String selectQuery;
				if(selection.equalsIgnoreCase("*") || selection.equalsIgnoreCase("@")){
					selectQuery = "SELECT * FROM "+DynamoDB.SQLITE_TABLE;
				}
				else
					selectQuery = "SELECT * FROM "+DynamoDB.SQLITE_TABLE+" WHERE "+DynamoDB.KEY_ID+" = '"+selection+"'";
				SQLiteDatabase database = dbHelper.getWritableDatabase();
				Cursor cursor = database.rawQuery(selectQuery, null);
				cursor.moveToFirst();
				return cursor;
			}


			String selectQuery;
			if(selection.equalsIgnoreCase("@")){

				selectQuery = "SELECT * FROM "+DynamoDB.SQLITE_TABLE;	
				SQLiteDatabase database = dbHelper.getWritableDatabase();
				Cursor cursor = database.rawQuery(selectQuery, null);
				return cursor;

			}

			else if(selection.equalsIgnoreCase("*")){


				selectQuery = "SELECT * FROM "+DynamoDB.SQLITE_TABLE;	
				SQLiteDatabase database = dbHelper.getWritableDatabase();
				Cursor cursor = database.rawQuery(selectQuery, null);
				Message msg=new Message();
				msg.key=null;
				msg.hashKey=null;
				msg.type="query-*";
				msg.sendto=myNode.successor;
				msg.sendToPort=myNode.successor.port;
				msg.sender=myNode;
				msg.modifiedNode=null;
				msg.result=new HashMap<String,String>();

				int keyIndex=cursor.getColumnIndex(KEY_FIELD);
				int valIndex=cursor.getColumnIndex(VALUE_FIELD);

				while (cursor.moveToNext()) {
					msg.result.put(cursor.getString(valIndex),cursor.getString(keyIndex));
				}
				cursor.close();
				database.close();
				Sender s=new Sender(msg);
				s.sendMsg();

				while(!isQueried){
				}
				isQueried=false;
				return resultCursor;

			}else {

				String key=selection;
				String hashKey= genHash(key);


				Node n=getNode(hashKey);
				if(n.name.equalsIgnoreCase(myNode.name)){
					selectQuery = "SELECT * FROM "+DynamoDB.SQLITE_TABLE+" WHERE "+DynamoDB.KEY_ID+" = '"+selection+"'";
					SQLiteDatabase database = dbHelper.getWritableDatabase();
					Cursor cursor = database.rawQuery(selectQuery, null);
					cursor.moveToFirst();
					return cursor;

				}else{
					Message msg=new Message();
					msg.key=key;
					msg.hashKey=hashKey;
					msg.type="query-k";
					msg.sendto=n;
					msg.sendToPort=n.port;
					msg.sender=myNode;
					msg.modifiedNode=null;
					msg.result=new HashMap<String,String>();
					Sender s=new Sender(msg);
					s.sendMsg();
					while(!isQueried){
					}
					isQueried=false;

					return resultCursor;
				}

			}

		}catch(Exception e){
			String selectQuery;
			if(selection.equalsIgnoreCase("*") || selection.equalsIgnoreCase("@")){
				selectQuery = "SELECT * FROM "+DynamoDB.SQLITE_TABLE;
			}
			else
				selectQuery = "SELECT * FROM "+DynamoDB.SQLITE_TABLE+" WHERE "+DynamoDB.KEY_ID+" = '"+selection+"'";
			SQLiteDatabase database = dbHelper.getWritableDatabase();
			Cursor cursor = database.rawQuery(selectQuery, null);
			cursor.moveToFirst();
			return cursor;
		}

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
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


	public Node getNode(String hashKey){
		Node myNode=null;
		for(String node : SimpleDynamoProvider.nodeMap.keySet()){
			myNode=SimpleDynamoProvider.nodeMap.get(node);

			if((hashKey.compareTo(myNode.hashKey))<=0 && (hashKey.compareTo(myNode.predessor.hashKey))>0){
				return myNode;			
			}else if(((hashKey.compareTo(myNode.hashKey))>0) && ((hashKey.compareTo(myNode.predessor.hashKey))>0)  && ((myNode.hashKey.compareTo(myNode.predessor.hashKey))<0)){

				return myNode;

			}else if(((hashKey.compareTo(myNode.hashKey))<0) && ((hashKey.compareTo(myNode.predessor.hashKey))<0)  && ((myNode.hashKey.compareTo(myNode.predessor.hashKey))<0)){

				return myNode;

			}

		}

		return myNode;
	}

	public void join(String myName){

		try{

			Node n1=new Node();
			Node n2=new Node();
			Node n3=new Node();
			Node n4=new Node();
			Node n5=new Node();

			n1.name="5554";
			n1.hashKey=genHash("5554");
			n1.port=11108;


			n2.name="5556";
			n2.hashKey=genHash("5556");
			n2.port=11112;

			n3.name="5558";
			n3.hashKey=genHash("5558");
			n3.port=11116;

			n4.name="5560";
			n4.hashKey=genHash("5560");
			n4.port=11120;

			n5.name="5562";
			n5.hashKey=genHash("5562");
			n5.port=11124;

			n1.successor=n3;
			n1.predessor=n2;

			n2.successor=n1;
			n2.predessor=n5;

			n3.successor=n4;
			n3.predessor=n1;

			n4.successor=n5;
			n4.predessor=n3;

			n5.successor=n2;
			n5.predessor=n4;

			nodeMap.put("5554", n1);
			nodeMap.put("5556", n2);
			nodeMap.put("5558", n3);
			nodeMap.put("5560", n4);
			nodeMap.put("5562", n5);

			myNode=nodeMap.get(myName);


			SharedPreferences sharedpref=c.getSharedPreferences("Recovery", 0);

			boolean isRecovery=sharedpref.getBoolean("recovery",false);


			if(isRecovery){
				System.out.println("recovering...");
				recover();
				System.out.println("recovered!!");

			}else{

				Editor editor = sharedpref.edit();
				editor.putBoolean("recovery", true);
				editor.commit();
			}


			isRecovered=true;
		}catch(Exception e){
			System.out.println(" Exception in Join");
			e.printStackTrace();
		}


	}



	void recover(){

		SQLiteDatabase database = dbHelper.getWritableDatabase();
		database.delete(DynamoDB.SQLITE_TABLE, null, null);
		database.close();

		recoveryCursor=new MatrixCursor(new String[] {KEY_FIELD,VALUE_FIELD});

		// Getting data from predessors
		Message msg1=new Message();
		msg1.type="recovery";
		msg1.sendto=myNode.predessor;
		msg1.sendToPort=myNode.predessor.port;
		msg1.sender=myNode;
		msg1.modifiedNode=null;
		msg1.replica=0;
		msg1.result=new HashMap<String,String>();

		Sender s1=new Sender(msg1);
		s1.sendMsg();

		while(!recovery){

		}
		recovery=false;

		insertReplications(recoveryCursor);
		recoveryCursor.close();

		recoveryCursor=new MatrixCursor(new String[] {KEY_FIELD,VALUE_FIELD});

		//Getting data from predessor's predessor

		Message msg2=new Message();
		msg2.type="recovery";
		msg2.sendto=myNode.predessor.predessor;
		msg2.sendToPort=myNode.predessor.predessor.port;
		msg2.sender=myNode;
		msg2.modifiedNode=null;
		msg2.replica=0;
		msg2.result=new HashMap<String,String>();

		Sender s2=new Sender(msg2);
		s2.sendMsg();

		while(!recovery){

		}
		recovery=false;

		insertReplications(recoveryCursor);
		recoveryCursor.close();

		recoveryCursor=new MatrixCursor(new String[] {KEY_FIELD,VALUE_FIELD});



		//Getting data from Successor

		Message msg3=new Message();
		msg3.type="@-recovery";
		msg3.sendto=myNode.successor;
		msg3.sendToPort=myNode.successor.port;
		msg3.sender=myNode;
		msg3.modifiedNode=null;
		msg3.replica=0;
		msg3.result=new HashMap<String,String>();

		Sender s3=new Sender(msg3);
		s3.sendMsg();

		while(!recovery){

		}
		recovery=false;

		insertMissedKeys(recoveryCursor);

		recoveryCursor.close();
		recoveryCursor=new MatrixCursor(new String[] {KEY_FIELD,VALUE_FIELD});
	}

	private  void insertMissedKeys(MatrixCursor cursor) {

		try{
			int keyIndex=cursor.getColumnIndex(KEY_FIELD);
			int valIndex=cursor.getColumnIndex(VALUE_FIELD);

			DBhelper dbHelper=new DBhelper(c);

			SQLiteDatabase database = dbHelper.getWritableDatabase();


			while (cursor.moveToNext()) {
				String key=cursor.getString(keyIndex);
				String value=cursor.getString(valIndex);
				if(isMyPartiton(genHash(key))){
					ContentValues cv=new ContentValues();
					cv.put(KEY_FIELD,key);
					cv.put(VALUE_FIELD,value);
					database.insertWithOnConflict(DynamoDB.SQLITE_TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE );
				}
			}
			database.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}





	private  void insertReplications(MatrixCursor cursor) {
		int keyIndex=cursor.getColumnIndex(KEY_FIELD);
		int valIndex=cursor.getColumnIndex(VALUE_FIELD);

		DBhelper dbHelper=new DBhelper(c);

		SQLiteDatabase database = dbHelper.getWritableDatabase();


		while (cursor.moveToNext()) {

			ContentValues cv=new ContentValues();
			cv.put(KEY_FIELD,cursor.getString(keyIndex));
			cv.put(VALUE_FIELD,cursor.getString(valIndex));
			database.insertWithOnConflict(DynamoDB.SQLITE_TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE );
		}
		database.close();
	}


	public  boolean isMyPartiton(String hashKey){

		if((hashKey.compareTo(myNode.hashKey))<=0 && (hashKey.compareTo(myNode.predessor.hashKey))>0){
			return true;			
		}else if(((hashKey.compareTo(myNode.hashKey))>0) && ((hashKey.compareTo(myNode.predessor.hashKey))>0)  && ((myNode.hashKey.compareTo(myNode.predessor.hashKey))<0)){

			return true;

		}else if(((hashKey.compareTo(myNode.hashKey))<0) && ((hashKey.compareTo(myNode.predessor.hashKey))<0)  && ((myNode.hashKey.compareTo(myNode.predessor.hashKey))<0)){

			return true;

		}else{

			return false;
		}
	}

}
