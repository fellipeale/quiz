package br.ufpr.ees.quiz.repository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBase {
	public static String KEY_ID = "_id";
	public static String KEY_NAME = "name";
	public static String KEY_DATE = "date";
	public static String KEY_VALUE = "value";
	
	String DB_NAME = "quizDB";
	String TABLE_NAME = "score";
	int DB_VERSION = 1;
	String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " "
								+ "(" + KEY_ID + " integer primary key autoincrement, "
								+ KEY_NAME + " text not null, "
								+ KEY_DATE + " text not null, "
								+ KEY_VALUE + " integer not null);";
	
	final Context context;
	MyOpenHelper openHelper;
	SQLiteDatabase db;
	
	public DataBase(Context context) {
		super();
		this.context = context;
		openHelper = new MyOpenHelper(context);
	}
	
	public DataBase open() {
		db = openHelper.getWritableDatabase();
		return this;
	}
	
	public void close() {
		openHelper.close();
	}
	
	public long insertScore(String name, int score) {
		ContentValues fields = new ContentValues();
		fields.put(KEY_NAME, name);
		fields.put(KEY_DATE, getDateTime());
		fields.put(KEY_VALUE, score);
		
		return db.insert(TABLE_NAME, null, fields);
	}
	
	
	public boolean resetScores() {
		return db.delete(TABLE_NAME, null, null) > 0;
	}
	
	public Cursor listScores() {
		return db.query(TABLE_NAME, new String[] {
				KEY_ID, KEY_NAME, KEY_DATE, KEY_VALUE
		}, null, null, null, null, KEY_VALUE + " DESC");
	}
	
	private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "dd/MM/yyyy", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
}
	
	private class MyOpenHelper extends SQLiteOpenHelper {

		public MyOpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			try {
				db.execSQL(SQL_CREATE_TABLE);
			} catch (SQLException ex) {
				Log.e("quiz", "Error creating table.", ex);
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
			onCreate(db);
		}
		
	}
}
