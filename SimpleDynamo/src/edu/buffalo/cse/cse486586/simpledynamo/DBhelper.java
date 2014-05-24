package edu.buffalo.cse.cse486586.simpledynamo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
  
public class DBhelper extends SQLiteOpenHelper {
  
 private static final String DATABASE_NAME = "Dynamo.db";
 private static final int DATABASE_VERSION = 1;
  
 DBhelper(Context context) {
  super(context, DATABASE_NAME, null, DATABASE_VERSION);
 }
  
 @Override
 public void onCreate(SQLiteDatabase db) {
  DynamoDB.onCreate(db);
 }
  
 @Override
 public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
  DynamoDB.onUpgrade(db, oldVersion, newVersion);
 }
  
  
}