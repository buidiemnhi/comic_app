package com.bsp.comicapp.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;



import com.bsp.comicapp.MainActivity;
import com.bsp.comicapp.MyComicActivity;
import com.bsp.comicapp.R;
import com.bsp.comicapp.database.DatabaseHandler;


import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class DownloadFanComicService extends Service {
	private String downloadUrl;
	private String stringBroadcast;
	private LocalBroadcastManager mLocalBroadcastManager;
	private BroadcastReceiver mReceiver;
	private File dir = null;
	private File file = null;

	private long fileSize = 0;
	private long total;
	public static DownloadAsyncTask downloadAsyncTask;
	public static DownloadContentAsyncTask downloadContentAsyncTask;
	// private String pdfDirectory, audioDirectory, propertyDirectory;

	private HashMap<String, String> bookInformation;
	private String path = "";
	private ArrayList<String> arrayImagesName = new ArrayList<String>();
	private int numberImageDownloaded = 0;

	private String base_url = "";
	private String url = "";
	private String databook = "";
	private static final String KEY_TITLE = "title";
	private static final String KEY_BASE_URL = "base_url";
	private static final String KEY_URL = "url";
	private static final String KEY_DATABOOK = "databook";

	private String DOWNLOAD_CANCEL = "pitdpn.readbook.download.DOWNLOAD_CANCEL";
	private String INTERNET_NOT_CONNECTED = "pitdpn.readbook.download.INTERNET_NOT_CONNECTED";

	private DatabaseHandler databaseHandler;
	private Context c;
	SharedPreferences langPrefeences;
	String lang="";

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public DownloadFanComicService(final Context c, String id, String broadcast) {
		numberImageDownloaded = 0;
		this.c = c;
		langPrefeences = c.getSharedPreferences(
				Config.LANGUAGE_PREFERENCE_NAME, MODE_PRIVATE);
		lang = langPrefeences.getString(Config.ENGLISH_LANGUAGUE, "eng");
		this.stringBroadcast = broadcast;
		databaseHandler = new DatabaseHandler(c.getApplicationContext());
		bookInformation = databaseHandler.getBookInformationById(id);
		databaseHandler.close();
		String nameOfBook = removeAccent(bookInformation.get(KEY_TITLE));
		this.path = "/data/data/" + c.getPackageName() + "/Documents/"
				+ nameOfBook + "-" + bookInformation.get("id");
		this.base_url = bookInformation.get(KEY_BASE_URL);
		this.url = bookInformation.get(KEY_URL);
		this.databook = bookInformation.get(KEY_DATABOOK);

		downloadUrl = "http://" + base_url + "/" + url + "/" + databook;
		
		this.stringBroadcast = broadcast;
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(c);
		dir = new File(path);
		if (!dir.exists()) {
			dir.mkdir();
		}
		path = path + "/data_zip";
		dir = new File(path);
		if (!dir.exists()) {
			dir.mkdir();
		}

		mLocalBroadcastManager = LocalBroadcastManager.getInstance(c);
		IntentFilter filter = new IntentFilter();
		filter.addAction(DOWNLOAD_CANCEL);
		filter.addAction(INTERNET_NOT_CONNECTED);
		
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				
				//boolean isConnectNetWork=intent.getBooleanExtra(Config.EXTRA_ISCONNECTED, false);
				
				if (intent.getAction().equals(DOWNLOAD_CANCEL)) {
					databaseHandler = new DatabaseHandler(context);
					databaseHandler.updateIsComicDownloadStatus("01", "idle");
					databaseHandler.close();
					if (downloadAsyncTask != null) {
						downloadAsyncTask.cancel(true);
					}
					if (downloadContentAsyncTask != null) {
						downloadContentAsyncTask.cancel(true);
					}

				} else if (intent.getAction().equals(INTERNET_NOT_CONNECTED)) {
					Toast.makeText(context, "aaaa", Toast.LENGTH_LONG).show();
					if (downloadAsyncTask != null) {
						downloadAsyncTask.cancel(true);
					}
					if (downloadContentAsyncTask != null) {
						if (downloadContentAsyncTask.getStatus() != Status.RUNNING) {
							if (MyComicActivity.txtLabel != null) {
								if(lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE))
								{
								MyComicActivity.txtLabel
										.setText("");
								}else
								{
									MyComicActivity.txtLabel
									.setText(c.getResources().getString(R.string.ar_waitingdownload));	
								}
								//MyComicActivity.txtLabel
//										.setText("Waiting download...");
							}
						}
					} else {
						if (MyComicActivity.txtLabel != null) {
							if(lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE))
							{
							MyComicActivity.txtLabel
									.setText("");
							}else
							{
								MyComicActivity.txtLabel
								.setText(c.getResources().getString(R.string.ar_waitingdownload));	
							}
							//MyComicActivity.txtLabel
							//		.setText("Waiting download...");
						}
					}
				}
			}
		};
		mLocalBroadcastManager.registerReceiver(mReceiver, filter);
	}

	public void startDownload() {
		downloadAsyncTask = new DownloadAsyncTask();
		downloadAsyncTask.executeOnExecutor(MainActivity.pool,
				new String[] { downloadUrl });
	}

	private boolean checkDirs() {
		if (!dir.exists()) {
			return dir.mkdirs();
		}
		return true;
	}

	public void cancel() {
		downloadAsyncTask.cancel(true);
	}

	/*
	 * Download file zip to document
	 */

	public class DownloadAsyncTask extends AsyncTask<String, String, String> {
		private boolean isDownloadError = false;

		@Override
		protected void onPreExecute() {
			isDownloadError = false;
		}

		@Override
		protected String doInBackground(String... params) {
			String fileName = downloadUrl.substring(downloadUrl
					.lastIndexOf("/") + 1);
			if (!checkDirs()) {
				return "";
			}
			try {
				URL url = new URL(downloadUrl);
				URLConnection urlConnection = url.openConnection();

				fileSize = urlConnection.getContentLength();
				file = new File(dir, fileName);

				FileOutputStream fos = new FileOutputStream(file);
				InputStream inputStream = new BufferedInputStream(
						urlConnection.getInputStream());
				byte[] buffer = new byte[1024];
				int bufferLength = 0;
				total = 0;
				while ((bufferLength = inputStream.read(buffer)) != -1) {
					databaseHandler = new DatabaseHandler(c);
					if (databaseHandler.getComicDownloadStatus()
							.equalsIgnoreCase("idle")) {
						isDownloadError = true;
						break;
					}
					databaseHandler.close();
					total += bufferLength;
					publishProgress("" + (int) ((total * 100) / fileSize));
					fos.write(buffer, 0, bufferLength);
				}
				inputStream.close();
				fos.flush();
				fos.close();
				file.setReadable(true, false);
			} catch (Exception e) {
				isDownloadError = true;
				Log.e("Download Failed", e.getMessage());
			}
			if (isCancelled()) {
				isDownloadError = true;
				return "";
			}
			return "";
		}

		@Override
		protected void onPostExecute(String string) {
			if (!isDownloadError) {

				try {
					File f = new File(path + "/property.txt");
					FileInputStream fis;
					fis = new FileInputStream(f);
					// Create a BufferedReader to saving time
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(fis));
					String str = "";
					// Create a StringBuffer to store temporary -> saving
					// time
					str = reader.readLine();
					// Check when end of file
					arrayImagesName = new ArrayList<String>();
					// mPages = new ArrayList<Bitmap>();
					while (str != null) {
						// File file = new File(imagePathFull);
						// mPages.add(BitmapFactory.decodeFile(file.getAbsolutePath()));
						arrayImagesName.add(str);
						str = reader.readLine();
					}
					reader.close();
					fis.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				downloadContentAsyncTask = new DownloadContentAsyncTask();
				downloadContentAsyncTask.executeOnExecutor(MainActivity.pool,
						new String[] { path + "/" + databook, path + "/" });
			}

		}
	}

	public class DownloadContentAsyncTask extends
			AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			if (numberImageDownloaded < arrayImagesName.size()) {
				downloadUrl = "http://" + base_url + "/" + url + "/"
						+ arrayImagesName.get(numberImageDownloaded);
				Log.e("Anh Vo", ""+downloadUrl);
				try {
					URL url = new URL(downloadUrl);
					URLConnection urlConnection = url.openConnection();

					fileSize = urlConnection.getContentLength();
					file = new File(dir,
							arrayImagesName.get(numberImageDownloaded));

					FileOutputStream fos = new FileOutputStream(file);
					InputStream inputStream = new BufferedInputStream(
							urlConnection.getInputStream());
					byte[] buffer = new byte[1024];
					int bufferLength = 0;
					total = 0;
					while ((bufferLength = inputStream.read(buffer)) != -1) {
						databaseHandler = new DatabaseHandler(c);
						if (databaseHandler.getComicDownloadStatus()
								.equalsIgnoreCase("idle")) {

							break;
						}
						databaseHandler.close();
						total += bufferLength;
						publishProgress("" + (int) ((total * 100) / fileSize));
						fos.write(buffer, 0, bufferLength);
					}
					inputStream.close();
					fos.flush();
					fos.close();
					file.setReadable(true, false);
				} catch (Exception e) {

					Log.e("Download Failed", e.getMessage());
				}
			}

			if (isCancelled()) {
				return "";
			}

			return null;
		}

		@Override
		protected void onProgressUpdate(String... values) {
			super.onProgressUpdate(values);
		}
		@Override
		protected void onPostExecute(String str) {
			numberImageDownloaded = numberImageDownloaded + 1;
			if(numberImageDownloaded<=arrayImagesName.size()){
				downloadContentAsyncTask = new DownloadContentAsyncTask();
				downloadContentAsyncTask.executeOnExecutor(MainActivity.pool);
			}else{
				mLocalBroadcastManager
				.sendBroadcast(new Intent(stringBroadcast));
			}
		}
	}

	private String removeAccent(String inputString) {
		inputString.replace("d", "d").replace("Ð", "D");
		inputString = Normalizer.normalize(inputString, Normalizer.Form.NFD);
		inputString = inputString.replaceAll(
				"\\p{InCombiningDiacriticalMarks}+", "");
		return inputString;
	}
	
	/////////////////////////////////////////////////////////////
	

}