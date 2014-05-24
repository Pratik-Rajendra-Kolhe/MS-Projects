package edu.buffalo.cse.cse486586.simpledht;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *  
 * @author pratik
 */
  
public class DBhelper extends SQLiteOpenHelper {
  
 private static final String DATABASE_NAME = "Chord.db";
 private static final int DATABASE_VERSION = 1;
  
 DBhelper(Context context) {
  super(context, DATABASE_NAME, null, DATABASE_VERSION);
 }
  
 @Override
 public void onCreate(SQLiteDatabase db) {
  ChordDB.onCreate(db);
 }
  
 @Override
 public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
  ChordDB.onUpgrade(db, oldVersion, newVersion);
 }
  
  
}