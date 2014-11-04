package com.bsp.comicapp;

import io.card.payment.a;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;


import com.bsp.comicapp.database.DatabaseHandler;
import com.bsp.comicapp.util.Config;
import com.bsp.comicapp.util.ConnectionDetector;
import com.bsp.comicapp.util.JSONParser;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.Facebook;


public class MainActivity extends Activity implements OnClickListener {
	public static Facebook facebook;
	public static AsyncFacebookRunner mAsyncRunner;
	private Button btnLogo;
	private Button btnEnglish;
	private Button btnArabic;
	private ImageView imageBackground;

	private SharedPreferences languagePreferences;
	private Editor e;

	public static ExecutorService pool;

	private DatabaseHandler databaseHandler;
	public static String myPackage = "comicapp.bsp.vn";
	public static String myNumberBook = "NumberBook";
	public static SharedPreferences mLogin;
	public static SharedPreferences numberBook;

	LocalBroadcastManager mLocalBroadcastManager;
	IntentFilter internetDetect = new IntentFilter(Config.INTERNET_DETECT);
	BroadcastReceiver networkCheckReciver;
	ConnectionDetector connectionDetector;
	boolean isConnect;
	public static Timer mTimer = new Timer();
	public static TimerTask task;
	static MainActivity activityInstance;
	String lang = "";

	public static MainActivity getActivityInstance() {
		return activityInstance;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.activity_main);
		imageBackground = (ImageView) findViewById(R.id.imgBackground);
		
		try  {

		      PackageInfo info = getPackageManager().
		           getPackageInfo(this.getPackageName(), PackageManager.GET_SIGNATURES);

		      for (android.content.pm.Signature signature : info.signatures) {

		          MessageDigest md = MessageDigest.getInstance("SHA");
		          md.update(signature.toByteArray());
		          Log.d("====Hash Key===",Base64.encodeToString(md.digest(), 
		                   Base64.DEFAULT));

		      }

		  } catch (NameNotFoundException e) {

		      e.printStackTrace();

		  } catch (NoSuchAlgorithmException ex) {

		      ex.printStackTrace();

		  }		
		activityInstance = this;
		connectionDetector = new ConnectionDetector(this);
		if (connectionDetector.isConnectingToInternet()) {
			isConnect = true;
			new GetBackgroundAnsyntask().execute();
		} else
			isConnect = false;

		ReceiverBroadcastDetectnetwork();

		languagePreferences = getSharedPreferences(
				Config.LANGUAGE_PREFERENCE_NAME, MODE_PRIVATE);
		e = languagePreferences.edit();
		lang = languagePreferences.getString(Config.PREFERENCE_KEY_LANGUAGE,
				"eng");
		mLogin = getPreferences(MODE_PRIVATE);
		String b = mLogin.getString(myPackage, " ");
		if (!b.equals(" ")) {
			Config.IdUser = b;
			Config.AvatarUser = mLogin.getString(Config.AVATAR, " ");
			Intent intent = new Intent(this, ComicListActivity.class);
			startActivity(intent);
			finish();
		}

		pool = Executors.newFixedThreadPool(5);
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		Config.screenHeight = metrics.heightPixels;
		Config.screenWidth = metrics.widthPixels;
		databaseHandler = new DatabaseHandler(getApplicationContext());
		if (databaseHandler.getComicDownloadStatus().equals("")) {
			databaseHandler.addComicDownloadStatus("01", "idle");
		}
		databaseHandler.close();

		File direct = new File("data/data/" + getPackageName() + "/Documents");
		if (!direct.exists()) {
			if (direct.mkdir()) {
			}
		}
		

		btnLogo = (Button) findViewById(R.id.btnLogo);
		btnEnglish = (Button) findViewById(R.id.btnEnglish);
		btnArabic = (Button) findViewById(R.id.btnArabic);

		// Set font
		Typeface tf = Typeface.createFromAsset(getApplicationContext()
				.getAssets(), "fonts/PAPYRUS.TTF");
		btnEnglish.setTypeface(tf);
		btnArabic.setTypeface(tf);

		// resize item
		float textsize = (float) (Config.screenHeight * 0.027);
		btnEnglish.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
		btnArabic.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);

		resizeButton(btnLogo, (int) (Config.screenWidth / 5),
				(int) (Config.screenWidth / 5 * 128 / 136), 0, 0, 0, 0);

		resizeButton(btnEnglish, (int) (Config.screenWidth / 12),
				(int) (Config.screenWidth / 12 * 159 / 190), 0, 0,
				(int) (Config.screenWidth / 24 * 159 / 190), 0);
		resizeButton(btnArabic, (int) (Config.screenWidth / 12),
				(int) (Config.screenWidth / 12 * 159 / 190),
				(int) (Config.screenWidth / 24 * 159 / 190), 0, 0, 0);

		btnEnglish.setPadding(0, (int) (Config.screenWidth / 20 * 159 / 190),
				0, 0);
		btnArabic.setPadding(0, (int) (Config.screenWidth / 20 * 159 / 190), 0,
				0);

		btnLogo.setOnClickListener(this);
		btnEnglish.setOnClickListener(this);
		btnArabic.setOnClickListener(this);
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
		// generateNotification(this, "aaaaaaaaaaa");
		// runTimer();
		mTimer.schedule(new TimerTaskNotification(), 0, 3600000);
	}

	private void resizeButton(Button resizeButton, int width, int height,
			int marginLeft, int marginTop, int marginRight, int marginBottom) {
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.height = height;
		llp.width = width;
		llp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
		resizeButton.setLayoutParams(llp);
	}

	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();
		switch (id) {
		case R.id.btnLogo:
			// Intent intent = new Intent(getApplicationContext(),
			// LoginActivity.class);
			// startActivity(intent);
			// MainActivity.this.finish();
			break;
		case R.id.btnEnglish:
			e.putString(Config.PREFERENCE_KEY_LANGUAGE,
					Config.ENGLISH_LANGUAGUE);
			e.commit();
			Intent englishIntent = new Intent(getApplicationContext(),
					LoginComicAppActivity.class);
			startActivity(englishIntent);
			MainActivity.this.finish();
			break;
		case R.id.btnArabic:
			e.putString(Config.PREFERENCE_KEY_LANGUAGE, Config.ARABIC_LANGUAGE);
			e.commit();
			Intent arabicIntent = new Intent(getApplicationContext(),
					LoginComicAppActivity.class);
			startActivity(arabicIntent);
			MainActivity.this.finish();
			break;

		default:
			break;
		}

	}

	public static void logIn(String idUser, String avatar) {
		SharedPreferences.Editor editor = mLogin.edit();
		editor.putString(myPackage, idUser);
		editor.putString(Config.AVATAR, avatar);
		Config.IdUser = idUser;
		Config.AvatarUser = avatar;
		editor.commit();
	}

	public static void logout() {
		SharedPreferences.Editor editor = mLogin.edit();
		Config.IdUser = " ";
		Config.AvatarUser = " ";
		editor.putString(myPackage, " ");
		editor.putString(Config.AVATAR, " ");
		editor.commit();
		editor.remove(myPackage);
		editor.commit();
		Config.USER = null;

	}

	class GetBackgroundAnsyntask extends AsyncTask<Void, Void, Integer> {

		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			if (result == 1) {
				imageBackground.setBackgroundDrawable(Config.Bg_Landscape);
				Intent i = new Intent(
						"com.bsp.comicapp.util.ChangeBackgroundReceiver");
				sendBroadcast(i);
				mLocalBroadcastManager.sendBroadcast(new Intent("Background"));
			}
			super.onPostExecute(result);
		}

		@SuppressWarnings("unused")
		@Override
		protected Integer doInBackground(Void... params) {

			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = jsonParser
					.getJSONFromUrl(Config.API_GETBACKGRUOND);
			int result = 0;
			try {
				JSONArray jsonArray = jsonObject.getJSONArray("data");
					getBackground(jsonArray);
					result = 1;
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return result;
		}

		private void getBackground(JSONArray jsonArray) {

			if (jsonArray != null) {
				try {

					JSONObject item = jsonArray.getJSONObject(0);
					JSONArray array = item.getJSONArray("anroid");

					// Landscape
					JSONObject jsonObject = array.getJSONObject(0);
					Config.Bg_Landscape = drawableFromUrl("http://baraahgroup.com/admin/"
							+ jsonObject.getString("Images"));
					final String a = "http://baraahgroup.com/admin/"
							+ jsonObject.getString("Images");
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
							// Toast.makeText(MainActivity.this, a,
							// Toast.LENGTH_LONG).show();
						}
					});
					// Portrait
					jsonObject = new JSONObject();
					jsonObject = array.getJSONObject(1);
					Config.Bg_Portrait = drawableFromUrl("http://baraahgroup.com/admin/"
							+ jsonObject.getString("Images"));

				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

	}

	public static Drawable drawableFromUrl(String url) throws IOException {
		Bitmap x;

		HttpURLConnection connection = (HttpURLConnection) new URL(url)
				.openConnection();
		connection.setDoInput(true);
		connection.connect();
		InputStream input = connection.getInputStream();
		x = BitmapFactory.decodeStream(input);
		// Bitmap myBitmap = BitmapFactory.decodeStream(input);

		// ByteArrayOutputStream outstream = new ByteArrayOutputStream();
		// x.compress(Bitmap.CompressFormat.JPEG, 80, outstream);
		// byteArray = outstream.toByteArray();

		return new BitmapDrawable(x);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		unregisterReceiver(networkCheckReciver);
	}

	void ReceiverBroadcastDetectnetwork() {
		this.networkCheckReciver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				// TODO Auto-generated method stub
				Boolean isconnected = intent.getBooleanExtra(
						Config.EXTRA_ISCONNECTED, false);
				if (isconnected == true) {

					if (isConnect == false)
						new GetBackgroundAnsyntask();

				} else {

				}
			}
		};

		// mLocalBroadcastManager = LocalBroadcastManager.getInstance(c);
		registerReceiver(networkCheckReciver, internetDetect);
	}

	public static void generateNotification(Context context, String message) {
		int icon = R.drawable.ic_launcher;
		long when = System.currentTimeMillis();
		NotificationManager notificationManager = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(icon, message, when);

		String title = context.getString(R.string.app_name);
		// mLogin = getPreferences(MODE_PRIVATE);
		Intent notificationIntent;
		notificationIntent = new Intent(context, MainActivity.class);

		// set intent so it does not start a new activity
		notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent intent = PendingIntent.getActivity(context, 0,
				notificationIntent, 0);
		notification.setLatestEventInfo(context, title, message, intent);
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// Play default notification sound
		notification.defaults |= Notification.DEFAULT_SOUND;

		// notification.sound = Uri.parse("android.resource://" +
		// context.getPackageName() + "your_sound_file_name.mp3");

		// Vibrate if vibrate is enabled
		notification.defaults |= Notification.DEFAULT_VIBRATE;
		notificationManager.notify(0, notification);

	}

	public LoadNotificationAsyncTask asyncTasknew;
	boolean isBackGround = false;

	public void runTimer() {

		numberBook = getPreferences(MODE_PRIVATE);
		final String number = numberBook.getString(myNumberBook, "0");
		// Toast.makeText(this, number, Toast.LENGTH_LONG).show();
		task = new TimerTask() {

			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					public void run() {
						if (isBackGround == false) {
							isBackGround = true;
							asyncTasknew = new LoadNotificationAsyncTask();
							asyncTasknew.execute(number);
						}
					}
				});

			}
		};
		mTimer.schedule(task, 0, 1000);
	}

	public class TimerTaskNotification extends TimerTask {

		@Override
		public void run() {

			numberBook = getPreferences(MODE_PRIVATE);
			final String number = numberBook.getString(myNumberBook, "0");
			if (isBackGround == false) {
				isBackGround = true;
				asyncTasknew = new LoadNotificationAsyncTask();
				asyncTasknew.execute(number);
			}
		}

	}

	public class LoadNotificationAsyncTask extends
			AsyncTask<String, Void, String> {

		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			numberBook = getPreferences(MODE_PRIVATE);
			int number = Integer.valueOf(numberBook
					.getString(myNumberBook, "0"));
			int n = Integer.valueOf(result) + number;
			if (n != 0) {
				if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

					generateNotification(MainActivity.this,
							"There are new comics in store. Check it out!");

				} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
					generateNotification(MainActivity.this, getResources()
							.getString(R.string.ar_email_format_is_invalid));
				}

			}
			isBackGround = false;
			SharedPreferences.Editor editor = numberBook.edit();
			editor.putString(myNumberBook, String.valueOf(n));
			editor.commit();
			if (!asyncTasknew.isCancelled())
				asyncTasknew.cancel(true);
		}

		@Override
		protected String doInBackground(String... params) {
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = jsonParser
					.getJSONFromUrl("http://baraahgroup.com/admin/books/record?record="
							+ params[0]);
			String result = "0";
			try {
				if (jsonObject != null)
					result = jsonObject.getString("result");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return result;
		}

	}
}
