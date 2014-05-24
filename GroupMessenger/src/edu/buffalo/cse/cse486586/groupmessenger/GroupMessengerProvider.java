package edu.buffalo.cse.cse486586.groupmessenger;


import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.database.sqlite.*;
/**
 * GroupMessengerProvider is a key-value table.
 */
public class GroupMessengerProvider extends ContentProvider {

	DBhelper dbHelper ;
	public static final String AUTHORITY = "edu.buffalo.cse.cse486586.groupmessenger.provider"; 
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY  + "/Messages");
	
	@Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
    		  SQLiteDatabase database = dbHelper.getWritableDatabase();

    		  long value=database.insertWithOnConflict(MessagesDB.SQLITE_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE );
    		  
    		  uri=Uri.withAppendedPath(CONTENT_URI, String.valueOf(value));
    	        
        return uri;
    }

    @Override
    public boolean onCreate() {

    	dbHelper = new DBhelper(getContext());
    	return true;

    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {

    	if(selection!=null)
    	  selectQuery = "SELECT * FROM "+MessagesDB.SQLITE_TABLE+" WHERE "+MessagesDB.KEY_ID+" = '"+selection+"'"; 
    	else
    		selectQuery = "SELECT * FROM "+MessagesDB.SQLITE_TABLE;	
    	
    	SQLiteDatabase database = dbHelper.getWritableDatabase();
    	Cursor cursor = database.rawQuery(selectQuery, null);
    	return cursor;
       
      
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
    
    
}
