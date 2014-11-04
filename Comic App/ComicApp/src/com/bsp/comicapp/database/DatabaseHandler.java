package com.bsp.comicapp.database;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "comic_database";
	// Table name
	private static final String TABLE_COMIC_INFORMATION = "comic_information";
	private static final String TABLE_COMIC_DOWNLOAD_STATUS = "comic_download_status";
	// TABLE_COMIC_INFORMATION PROPERTY
	private static final String KEY_STATUS = "status";
	private static final String KEY_ERROR = "error";
	private static final String KEY_RECORDS = "records";
	private static final String KEY_BASE_URL = "base_url";
	private static final String KEY_ID = "id";
	private static final String KEY_TITLE = "title";
	private static final String KEY_DESCRIPTION = "description";
	private static final String KEY_SERIES = "series";
	private static final String KEY_COVER = "cover";
	private static final String KEY_DATABOOK = "databook";
	private static final String KEY_PRICE = "price";
	private static final String KEY_AUTHOR = "author";
	private static final String KEY_RATING = "rating";
	private static final String KEY_TYPE = "type";
	private static final String KEY_DATE_CREATE = "date_create";
	private static final String KEY_LANGUAGUE = "language";
	private static final String KEY_URL = "url";
	private static final String KEY_TARGET = "target";
	private static final String KEY_IS_DOWNLOAD_FINISHED = "isdownloadfinished";

	private static final String KEY_IS_DOWNLOAD_CANCEL = "isdownloadcancel";

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_TABLE_COMIC_INFORMATION = "CREATE TABLE "
				+ TABLE_COMIC_INFORMATION + "(" + KEY_STATUS + " TEXT,"
				+ KEY_ERROR + " TEXT," + KEY_RECORDS + " TEXT," + KEY_BASE_URL
				+ " TEXT, " + KEY_ID + " TEXT PRIMARY KEY," + KEY_TITLE
				+ " TEXT," + KEY_DESCRIPTION + " TEXT," + KEY_SERIES + " TEXT,"
				+ KEY_COVER + " TEXT," + KEY_DATABOOK + " TEXT," + KEY_PRICE
				+ " TEXT," + KEY_AUTHOR + " TEXT," + KEY_RATING + " TEXT,"
				+ KEY_TYPE + " TEXT," + KEY_DATE_CREATE + " TEXT,"
				+ KEY_LANGUAGUE + " TEXT," + KEY_URL + " TEXT," + KEY_TARGET
				+ " TEXT," + KEY_IS_DOWNLOAD_FINISHED + " TEXT" + ");";

		String CREATE_TABLE_COMIC_DOWNLOAD_STATUS = "CREATE TABLE "
				+ TABLE_COMIC_DOWNLOAD_STATUS + "(" + KEY_ID + " TEXT, "
				+ KEY_IS_DOWNLOAD_CANCEL + " TEXT);";

		db.execSQL(CREATE_TABLE_COMIC_INFORMATION);
		db.execSQL(CREATE_TABLE_COMIC_DOWNLOAD_STATUS);

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMIC_INFORMATION);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMIC_DOWNLOAD_STATUS);
		// Create tables again
		onCreate(db);
	}

	public ArrayList<HashMap<String, String>> getAllBooksInformation() {
		ArrayList<HashMap<String, String>> arrayBooksInformation = new ArrayList<HashMap<String, String>>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_COMIC_INFORMATION;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				HashMap<String, String> bookInformation = new HashMap<String, String>();
				bookInformation.put(KEY_STATUS, cursor.getString(0));
				bookInformation.put(KEY_ERROR, cursor.getString(1));
				bookInformation.put(KEY_RECORDS, cursor.getString(2));
				bookInformation.put(KEY_BASE_URL, cursor.getString(3));
				bookInformation.put(KEY_ID, cursor.getString(4));
				bookInformation.put(KEY_TITLE, cursor.getString(5));
				bookInformation.put(KEY_DESCRIPTION, cursor.getString(6));
				bookInformation.put(KEY_SERIES, cursor.getString(7));
				bookInformation.put(KEY_COVER, cursor.getString(8));
				bookInformation.put(KEY_DATABOOK, cursor.getString(9));
				bookInformation.put(KEY_PRICE, cursor.getString(10));
				bookInformation.put(KEY_AUTHOR, cursor.getString(11));
				bookInformation.put(KEY_RATING, cursor.getString(12));
				bookInformation.put(KEY_TYPE, cursor.getString(13));
				bookInformation.put(KEY_DATE_CREATE, cursor.getString(14));
				bookInformation.put(KEY_LANGUAGUE, cursor.getString(15));
				bookInformation.put(KEY_URL, cursor.getString(16));
				bookInformation.put(KEY_TARGET, cursor.getString(17));
				bookInformation.put(KEY_IS_DOWNLOAD_FINISHED,
						cursor.getString(18));

				arrayBooksInformation.add(bookInformation);
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		return arrayBooksInformation;
	}

	public HashMap<String, String> getBookInformationById(String id) {
		HashMap<String, String> bookInformation = new HashMap<String, String>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_COMIC_INFORMATION
				+ " WHERE " + KEY_ID + " = " + "'" + id + "'";
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				bookInformation.put(KEY_STATUS, cursor.getString(0));
				bookInformation.put(KEY_ERROR, cursor.getString(1));
				bookInformation.put(KEY_RECORDS, cursor.getString(2));
				bookInformation.put(KEY_BASE_URL, cursor.getString(3));
				bookInformation.put(KEY_ID, cursor.getString(4));
				bookInformation.put(KEY_TITLE, cursor.getString(5));
				bookInformation.put(KEY_DESCRIPTION, cursor.getString(6));
				bookInformation.put(KEY_SERIES, cursor.getString(7));
				bookInformation.put(KEY_COVER, cursor.getString(8));
				bookInformation.put(KEY_DATABOOK, cursor.getString(9));
				bookInformation.put(KEY_PRICE, cursor.getString(10));
				bookInformation.put(KEY_AUTHOR, cursor.getString(11));
				bookInformation.put(KEY_RATING, cursor.getString(12));
				bookInformation.put(KEY_TYPE, cursor.getString(13));
				bookInformation.put(KEY_DATE_CREATE, cursor.getString(14));
				bookInformation.put(KEY_LANGUAGUE, cursor.getString(15));
				bookInformation.put(KEY_URL, cursor.getString(16));
				bookInformation.put(KEY_TARGET, cursor.getString(17));
				bookInformation.put(KEY_IS_DOWNLOAD_FINISHED,
						cursor.getString(18));

			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();

		return bookInformation;
	}

	public void addBookInformation(String status, String error, String records,
			String base_url, String id, String title, String description,
			String series, String cover, String databook, String price,
			String author, String rating, String type, String date_create,
			String language, String url, String target,
			String isdownloadfinished) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		if (status == null) {
			status = "null";
		}
		if (error == null) {
			error = "null";
		}
		if (records == null) {
			records = "null";
		}
		if (base_url == null) {
			base_url = "null";
		}
		if (id == null) {
			id = "null";
		}
		if (title == null) {
			title = "null";
		}
		if (description == null) {
			description = "null";
		}
		if (series == null) {
			series = "null";
		}
		if (cover == null) {
			cover = "null";
		}
		if (databook == null) {
			databook = "null";
		}
		if (price == null) {
			price = "null";
		}
		if (author == null) {
			author = "null";
		}
		if (rating == null) {
			rating = "null";
		}
		if (type == null) {
			type = "null";
		}
		if (date_create == null) {
			date_create = "null";
		}
		if (language == null) {
			language = "null";
		}
		if (url == null) {
			url = "null";
		}
		if (target == null) {
			target = "null";
		}
		if (isdownloadfinished == null) {
			isdownloadfinished = "null";
		}

		values.put(KEY_STATUS, status);
		values.put(KEY_ERROR, error);
		values.put(KEY_RECORDS, records);
		values.put(KEY_BASE_URL, base_url);
		values.put(KEY_ID, id);
		values.put(KEY_TITLE, title);
		values.put(KEY_DESCRIPTION, description);
		values.put(KEY_SERIES, series);
		values.put(KEY_COVER, cover);
		values.put(KEY_DATABOOK, databook);
		values.put(KEY_PRICE, price);
		values.put(KEY_AUTHOR, author);
		values.put(KEY_RATING, rating);
		values.put(KEY_TYPE, type);
		values.put(KEY_DATE_CREATE, date_create);
		values.put(KEY_LANGUAGUE, language);
		values.put(KEY_URL, url);
		values.put(KEY_TARGET, target);
		values.put(KEY_IS_DOWNLOAD_FINISHED, isdownloadfinished);

		// Inserting Row
		db.insert(TABLE_COMIC_INFORMATION, null, values);
		db.close(); // Closing database connection
	}

	public void deleteBook(String id) {
		// "http://vietnamprogrammer.com/sharing/wdp/services/tritueviet/images/"
		SQLiteDatabase db = getWritableDatabase();
		// Delete Row
		String[] whereArgs = { id };
		db.delete(TABLE_COMIC_INFORMATION, KEY_ID + " = ?", whereArgs);
		db.close(); // Closing database connection
	}

	public String getComicDownloadStatus() {
		String download_status = "";
		String selectQuery = "SELECT  * FROM " + TABLE_COMIC_INFORMATION;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			download_status = cursor.getString(1);
		}
		return download_status;
	}

	public void updateIsDownloadFinished(String id_url,
			String isdownloadfinished) {

		SQLiteDatabase db = getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_IS_DOWNLOAD_FINISHED, isdownloadfinished);// is download
		String[] whereArgs = { id_url };
		// Updating Row
		db.update(TABLE_COMIC_INFORMATION, values, KEY_ID + " = ?", whereArgs);
		db.close(); // Closing database connection
	}

	public void updateIsComicDownloadStatus(String id, String is_download_cancel) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_IS_DOWNLOAD_CANCEL, is_download_cancel);
		String whereClause = KEY_ID + " = ?";
		String[] whereArgs = { id };
		db.update(TABLE_COMIC_DOWNLOAD_STATUS, values, whereClause, whereArgs);
		db.close(); // Closing database connection
	}

	public void addComicDownloadStatus(String id, String is_download_cancel) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_ID, id);
		values.put(KEY_IS_DOWNLOAD_CANCEL, is_download_cancel);
		db.insert(TABLE_COMIC_DOWNLOAD_STATUS, null, values);
		db.close(); // Closing database connection
	}
}
