package edu.buffalo.cse.cse486586.groupmessenger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;



import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;

import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.TreeMap;

/**
 * GroupMessengerActivity 
 * @author pratik
 */
public class GroupMessengerActivity extends Activity {

	static final String TAG = GroupMessengerActivity.class.getSimpleName();

	static final String REMOTE_PORT0 = "11108";
	static final String REMOTE_PORT1 = "11112";
	static final String REMOTE_PORT2 = "11116";
	static final String REMOTE_PORT3 = "11120";
	static final String REMOTE_PORT4 = "11124";


	static final int SERVER_PORT = 10000;

	static	TreeMap<Timestamp,String> orderedMsgs = new TreeMap<Timestamp,String>(new Comparator<Timestamp>() {  
		@Override public int compare(Timestamp o1, Timestamp o2) {  
			if(o1.compareTo(o2)!=0)
				return o1.compareTo(o2);
			else return 1;
		}});


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_messenger);

		//Calculating the port number on which this AVD listens
		TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
		final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
		
		//Creating server socket
		try {

			ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
			new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
		} catch (IOException e) {
			Log.e(TAG, "Can't create a ServerSocket");
			return;
		}

		
		TextView tv = (TextView) findViewById(R.id.textView1);
		tv.setMovementMethod(new ScrollingMovementMethod());

		final EditText editText = (EditText) findViewById(R.id.editText1);

		
		findViewById(R.id.button1).setOnClickListener(
				new OnPTestClickListener(tv, getContentResolver()));

		
		
		findViewById(R.id.button4).setOnClickListener(
				new OnSendClickListener(editText,myPort));

		

	}
	private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

		@Override
		protected Void doInBackground(ServerSocket... sockets) {
			ServerSocket serverSocket = sockets[0];
			Socket soc=new Socket();
			/*
			 * Server code that receives messages and passes them
			 * to onProgressUpdate().
			 */
			String msg=new String();

			try {
				while (true)// loop for server socket to listen continuously
				{   
					soc=serverSocket.accept(); //opening the socket
					InputStream in=soc.getInputStream();  // input stream to read.
					BufferedReader br=new BufferedReader(new InputStreamReader(in));
					msg=br.readLine();// reading msg from the Buffer
					publishProgress(msg); //publishing the msg from client
					br.close();  //closing the bufferred reader
				}



			} catch (IOException e) {
				Log.e(TAG,"Reading or Publishing failed ");
			}
			finally
			{
				try {
					soc.close();               // closing the socket
					serverSocket.close();      //closing the server socket 
				} catch (IOException e) {
					Log.e(TAG,"Socket closing failed");
				}

			}

			return null;
		}

		protected void onProgressUpdate(String...strings) {
			/*
			 * The following code displays what is received in doInBackground().
			 */


			String strReceived = strings[0].trim();
			try {
				order(strReceived);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			return;
		}

	}

	public void order(String msg) throws ParseException{

		Log.e(TAG,"Server--"+msg);

		String t=msg.split("\\|")[0];
		String value=msg.split("\\|")[1];
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
		Date parsedDate = dateFormat.parse(t);
		Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
	    ContentResolver cr=getContentResolver();
		Uri uri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger.provider");
	     
		

		orderedMsgs.put(timestamp, value);
		int k=0;
		String key;
		
		for(String val : orderedMsgs.values()){
			key=String.valueOf(k);
			k++;
			ContentValues cv=new ContentValues();
			cv.put("key",key);
			cv.put("value",val);
			cr.insert(uri,cv);
		}
		

	}

	private Uri buildUri(String scheme, String authority) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.scheme(scheme);
		return uriBuilder.build();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
		return true;
	}
	
	
}
