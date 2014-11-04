package com.bsp.comicapp.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.Normalizer;
import java.util.HashMap;

import com.bsp.comicapp.MainActivity;
import com.bsp.comicapp.MyComicActivity;
import com.bsp.comicapp.R;
import com.bsp.comicapp.database.DatabaseHandler;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import android.app.Activity;
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

public class DownloadService extends Service {
	private String downloadUrl;
	private String stringBroadcast;
	private LocalBroadcastManager mLocalBroadcastManager;
	private BroadcastReceiver mReceiver;
	SharedPreferences langPrefeences;

	IntentFilter internetDetect = new IntentFilter(Config.INTERNET_DETECT);
	BroadcastReceiver networkCheckReciver;

	private File dir = null;
	private File file = null;

	private long fileSize = 0;
	private long total;
	public static DownloadAsyncTask downloadAsyncTask;
	public static UnzipAsyncTask unZipAsynTask;
	// private String pdfDirectory, audioDirectory, propertyDirectory;

	private HashMap<String, String> bookInformation;
	private String path = "";

	private String base_url = "";
	private String url = "";
	private String databook = "";
	private static final String KEY_ID = "id";
	private static final String KEY_TITLE = "title";
	private static final String KEY_BASE_URL = "base_url";
	private static final String KEY_URL = "url";
	private static final String KEY_DATABOOK = "databook";

	private String DOWNLOAD_CANCEL = "pitdpn.readbook.download.DOWNLOAD_CANCEL";
	private String INTERNET_NOT_CONNECTED = "pitdpn.readbook.download.INTERNET_NOT_CONNECTED";

	private DatabaseHandler databaseHandler;
	private Context c;
	String lang = "";
	boolean isDownloadError = false;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	public DownloadService(final Activity c, String id, String broadcast) {
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
				+ nameOfBook + "-" + bookInformation.get(KEY_ID);
		this.base_url = bookInformation.get(KEY_BASE_URL);
		this.url = bookInformation.get(KEY_URL);
		this.databook = bookInformation.get(KEY_DATABOOK);

		downloadUrl = "http://" + base_url + "/" + url + "/" + databook;
		Log.d("------------------------->", downloadUrl);
		c.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				// Toast.makeText(c, downloadUrl, Toast.LENGTH_LONG).show();
			}
		});

		this.stringBroadcast = broadcast;
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(c);
		dir = new File(path);

		if (!dir.exists()) {
			dir.mkdir();
		}

		mLocalBroadcastManager = LocalBroadcastManager.getInstance(c);
		IntentFilter filter = new IntentFilter(Config.INTERNET_DETECT);
		filter.addAction(DOWNLOAD_CANCEL);
		filter.addAction(INTERNET_NOT_CONNECTED);
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {

				if (intent.getAction().equals(DOWNLOAD_CANCEL)) {
					databaseHandler = new DatabaseHandler(context);
					databaseHandler.updateIsComicDownloadStatus("01", "idle");
					databaseHandler.close();
					if (downloadAsyncTask != null) {
						downloadAsyncTask.cancel(true);
					}
					if (unZipAsynTask != null) {
						unZipAsynTask.cancel(true);
					}

				} else if (intent.getAction().equals(INTERNET_NOT_CONNECTED)) {
					if (downloadAsyncTask != null) {
						downloadAsyncTask.cancel(true);
					}
					if (unZipAsynTask != null) {
						if (unZipAsynTask.getStatus() != Status.RUNNING) {
							if (MyComicActivity.txtLabel != null) {
								
								// if (file != null) {
								// file.deleteOnExit();
								// }
							}
						}
					} else {
						if (MyComicActivity.txtLabel != null) {
							
						}
					}
				}
			}
		};
		// c.registerReceiver(mReceiver, filter);
		mLocalBroadcastManager.registerReceiver(mReceiver, filter);
		ReceiverBroadcastDetectnetwork();
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
		protected void onProgressUpdate(String... values) {
			if (MyComicActivity.txtLabel != null) {
				MyComicActivity.txtLabel.setText(values[0] + "%");
			}

		}

		@Override
		protected void onPostExecute(String str) {
			if (!isDownloadError) {
				if (MyComicActivity.txtLabel != null) {
					
				}
				unZipAsynTask = new UnzipAsyncTask();
				unZipAsynTask.executeOnExecutor(MainActivity.pool,
						new String[] { path + "/" + databook, path + "/" });
			}
		}
	}

	public class UnzipAsyncTask extends AsyncTask<String, String, String> {
		private String _zipFile = "";
		private String _location = "";

		private boolean isUnzipSuccess = false;

		@Override
		protected String doInBackground(String... params) {
			_zipFile = params[0];
			_location = params[1];
			try {
				ZipFile zipFile = new ZipFile(_zipFile);
				// if (zipFile.isEncrypted()) {
				// zipFile.setPassword(_password);
				// }
				zipFile.extractAll(_location);
				isUnzipSuccess = true;
			} catch (ZipException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(String str) {

			if (isUnzipSuccess) {
				// Unzip file success
				mLocalBroadcastManager
						.sendBroadcast(new Intent(stringBroadcast));
				databaseHandler = new DatabaseHandler(c);
				// Toast.makeText(c,
				// "unzip success" +
				// databaseHandler.getAllBooksInformation().size(),
				// Toast.LENGTH_LONG).show();

			} else {
				// Unzip file unsucess
				unZipAsynTask = new UnzipAsyncTask();
				unZipAsynTask.executeOnExecutor(MainActivity.pool,
						new String[] { path + "/" + databook, path + "/" });
				databaseHandler = new DatabaseHandler(c);
				// Toast.makeText(c,
				// "unzin fail" +
				// databaseHandler.getAllBooksInformation().size(),
				// Toast.LENGTH_LONG).show();
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

	void ReceiverBroadcastDetectnetwork() {
		this.networkCheckReciver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				Boolean isconnected = intent.getBooleanExtra(
						Config.EXTRA_ISCONNECTED, false);
				if (isconnected == true) {
					if (isDownloadError) {
						file.deleteOnExit();
						startDownload();
					}

				} else {
					downloadAsyncTask.cancel(true);
					if (MyComicActivity.txtLabel != null) {
						
					}

				}
			}
		};

		// mLocalBroadcastManager = LocalBroadcastManager.getInstance(c);
		c.registerReceiver(networkCheckReciver, internetDetect);
	}
	// this.networkCheckReciver.setDelegate(this);

}