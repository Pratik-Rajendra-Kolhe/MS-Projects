package edu.buffalo.cse.cse486586.simpledynamo;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
/**
 *  
 * @author pratik
 */
  
public class DynamoDB {
  
 public static final String KEY_ID = "key";
 public static final String KEY_VALUE = "value";
  
 public static final String LOG_TAG = "DynamoDb";
 public static final String SQLITE_TABLE = "Dynamo";
  
 public static final String DATABASE_CREATE =
  "CREATE TABLE " + SQLITE_TABLE + " (" +
   KEY_ID + " TEXT , " +
   KEY_VALUE + " TEXT" +
   ");";
  
 public static void onCreate(SQLiteDatabase db) {
  Log.w(LOG_TAG, DATABASE_CREATE);
  db.execSQL(DATABASE_CREATE);
 }
  
 public static void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
  Log.w(LOG_TAG, "Upgrading database from version " + oldVersion + " to "
    + newVersion + ", which will destroy all old data");
  db.execSQL("DROP TABLE IF EXISTS " + SQLITE_TABLE);
  onCreate(db);
 }
  
}