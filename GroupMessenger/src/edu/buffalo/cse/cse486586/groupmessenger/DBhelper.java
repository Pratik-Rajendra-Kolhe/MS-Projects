package edu.buffalo.cse.cse486586.groupmessenger;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *  
 * @author pratik
 */
  
public class DBhelper extends SQLiteOpenHelper {
  
 private static final String DATABASE_NAME = "Messenger.db";
 private static final int DATABASE_VERSION = 1;
  
 DBhelper(Context context) {
  super(context, DATABASE_NAME, null, DATABASE_VERSION);
 }
  
 @Override
 public void onCreate(SQLiteDatabase db) {
  MessagesDB.onCreate(db);
 }
  
 @Override
 public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
  MessagesDB.onUpgrade(db, oldVersion, newVersion);
 }
  
  
}