package edu.buffalo.cse.cse486586.groupmessenger;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import java.sql.Timestamp;
import java.util.Date;


public class OnSendClickListener implements OnClickListener {
	private static final String TAG = OnSendClickListener.class.getName();
	private static String myport;
	private final EditText editText;

	public OnSendClickListener(EditText _et,String port) {
		myport=port;
		editText = _et;
	}


	@Override
	public void onClick(View arg0) {
		// TODO Auto-generated method stub
		String msg = editText.getText().toString() + "\n";
		editText.setText(""); 
		new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myport);
		return;
	}


	private class ClientTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... msgs) {

		String ports[]={"11108","11112","11116","11120","11124"};


		String msgToSend = msgs[0];
		Date date= new java.util.Date();
		Timestamp t=new Timestamp(date.getTime());
		
		String msg=new String(t.toString()+"|"+msgToSend);
		
		Log.e(TAG, "Message= "+msg);

		
		
		for(String remotePort : ports){
				try{
		
					Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
							Integer.parseInt(remotePort));
	
					OutputStream out=socket.getOutputStream();    //outputstream to write
					BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(out));
					bw.write(msg);  // writing the message
					bw.flush();           //flushing
					socket.close();        //closing the socket


				} catch (UnknownHostException e) {
					Log.e(TAG, "ClientTask UnknownHostException");
				} catch (IOException e) {
					Log.e(TAG, "ClientTask socket IOException");
				}

			}

			return null;
		}
	}
}
