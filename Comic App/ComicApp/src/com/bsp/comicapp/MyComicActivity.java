package com.bsp.comicapp;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import com.bsp.comicapp.adapter.MyComicGridViewAdapter;
import com.bsp.comicapp.database.DatabaseHandler;
import com.bsp.comicapp.displayimage.util.AsyncTask;
import com.bsp.comicapp.displayimage.util.RecyclingImageView;
import com.bsp.comicapp.util.Config;
import com.bsp.comicapp.util.DownloadFanComicService;
import com.bsp.comicapp.util.DownloadService;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class MyComicActivity extends FragmentActivity implements
		OnClickListener {
	private Button btnStore;
	private ImageView imgLogo;
	private Button btnMyComic;
	private LinearLayout layout_content;
	private GridView gvMyComic;
	private Button btnSetting;
	private boolean isShowDeleteBook;
	private int deleteBookIndex = 0;

	private String lang = Config.ENGLISH_LANGUAGUE;
	private SharedPreferences languagePreferences;

	private ArrayList<HashMap<String, String>> arrayMyComic = new ArrayList<HashMap<String, String>>();
	private DatabaseHandler db;
	// Book Information
	private boolean isFancomicBook = false;
	private String status = "";
	private String error = "";
	private String records = "";
	private String base_url = "";
	private String id = "";
	private String title = "";
	private String description = "";
	private String series = "";
	private String cover = "";
	private String databook = "";
	private String price = "";
	private String author = "";
	private String rating = "";
	private String type = "";
	private String date_create = "";
	private String language = "";
	private String url = "";
	private String target = "";

	private String DOWNLOAD_CANCEL = "pitdpn.readbook.download.DOWNLOAD_CANCEL";
	private String INTERNET_CONNECTED = "namdinh.comicapp.download.INTERNET_CONNECTED";
	private String DOWNLOAD_FINISHED = "namdinh.comicapp.download.DOWNLOAD_FINISHED";
	private LocalBroadcastManager mLocalBroadcastManager;
	private BroadcastReceiver mReceiver;

	public static TextView txtLabel;

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

	int i = 0;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_mycomic);

		languagePreferences = getSharedPreferences(
				Config.LANGUAGE_PREFERENCE_NAME, MODE_PRIVATE);
		lang = languagePreferences.getString(Config.PREFERENCE_KEY_LANGUAGE,
				Config.ENGLISH_LANGUAGUE);

		isShowDeleteBook = false;
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			isFancomicBook = extras.getBoolean("isfancomicbook");
			status = extras.getString("status");
			error = extras.getString("error");
			records = extras.getString("records");
			base_url = extras.getString("base_url");
			id = extras.getString("id");
			title = extras.getString("title");
			description = extras.getString("description");
			series = extras.getString("series");
			cover = extras.getString("cover");
			databook = extras.getString("databook");
			price = extras.getString("price");
			author = extras.getString("author");
			rating = extras.getString("rating");
			type = extras.getString("type");
			date_create = extras.getString("date_create");
			language = extras.getString("language");
			url = extras.getString("url");
			target = extras.getString("target");
			db = new DatabaseHandler(getApplicationContext());
			if (isFancomicBook) {
				db.addBookInformation(status, error, records, base_url, id
						+ "fancomic", title, description, series, cover,
						databook, price, author, rating, type, date_create,
						language, url, target, "false");
				arrayMyComic = db.getAllBooksInformation();
				DownloadFanComicService dsf = new DownloadFanComicService(
						getApplicationContext(), id + "fancomic",
						DOWNLOAD_FINISHED);
				dsf.startDownload();
			} else if (!isFancomicBook) {
				db.addBookInformation(status, error, records, base_url, id,
						title, description, series, cover, databook, price,
						author, rating, type, date_create, language, url,
						target, "false");
				arrayMyComic = db.getAllBooksInformation();

				// Toast.makeText(this, " " +arrayMyComic.size(),
				// Toast.LENGTH_LONG).show();
				DownloadService ds = new DownloadService(this, id,
						DOWNLOAD_FINISHED);
				ds.startDownload();
			}

			db.close();

		}

		ImageView imageView = (ImageView) findViewById(R.id.imgBackground);
		if (Config.Bg_Portrait != null)
			imageView.setBackgroundDrawable(Config.Bg_Portrait);
		btnStore = (Button) findViewById(R.id.btnStore);
		imgLogo = (ImageView) findViewById(R.id.imgLogo);
		btnMyComic = (Button) findViewById(R.id.btnMyComic);
		layout_content = (LinearLayout) findViewById(R.id.layout_content);
		gvMyComic = (GridView) findViewById(R.id.gvMyComic);
		btnSetting = (Button) findViewById(R.id.btnSetting);

		// resize item
		layout_content.setPadding((int) (Config.screenHeight * 0.05),
				(int) (Config.screenHeight * 0.02),
				(int) (Config.screenHeight * 0.05), 0);
		resizeImageView(imgLogo, 0,
				(int) (Config.screenHeight * 0.075 * 150 / 175), 0, 0, 0, 0);
		resizeButton(btnMyComic, (int) (Config.screenHeight * 0.1),
				(int) (Config.screenHeight * 0.1 * 150 / 175), 0, 0, 0, 0);
		resizeButton(btnStore, (int) (Config.screenHeight * 0.1),
				(int) (Config.screenHeight * 0.1 * 150 / 175), 0, 0, 0, 0);
		// resizeButton(btnSetting, (int) (Config.screenHeight * 0.15),
		// (int) (Config.screenHeight * 0.15 * 82 / 196),
		// (int) (Config.screenHeight * 0.01 * 82 / 196),
		// (int) (Config.screenHeight * 0.01 * 82 / 196), 0,
		// (int) (Config.screenHeight * 0.01 * 82 / 196));
		resizeButton(btnSetting, (int) (Config.screenHeight * 0.15),
				(int) (Config.screenHeight * 0.15 * 69 / 182),
				(int) (Config.screenHeight * 0.01 * 69 / 182),
				(int) (Config.screenHeight * 0.01 * 69 / 182),
				(int) (Config.screenHeight * 0.01),
				(int) (Config.screenHeight * 0.01 * 69 / 182));

		Button butInfo = (Button) findViewById(R.id.btnInformationMycomic);
		butInfo.setOnClickListener(this);
		resizeButton(butInfo, (int) (Config.screenHeight * 0.043),
				(int) (Config.screenHeight * 0.15 * 69 / 182),
				(int) (Config.screenHeight * 0.01), 0,
				(int) (Config.screenHeight * 0.01), 0);

		db = new DatabaseHandler(getApplicationContext());
		arrayMyComic = db.getAllBooksInformation();
		// Toast.makeText(this, arrayMyComic.size()+
		// db.getAllBooksInformation().get(0).get(""),
		// Toast.LENGTH_LONG).show();

		db.close();
		gvMyComic.setAdapter(new MyComicGridViewAdapter(MyComicActivity.this,
				arrayMyComic, (int) (Config.screenHeight * 0.9 * 0.21),
				(int) (Config.screenHeight * 0.9 * 0.21 * 786 / 512)));
		gvMyComic.setVerticalSpacing((int) (gvMyComic.getWidth() * 0.02));
		gvMyComic.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				openComic(arg2);
			}
		});

		gvMyComic.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				View vi = new View(getApplicationContext());
				if (isShowDeleteBook==false) {
					isShowDeleteBook = true;
					for (int i = 0; i < arrayMyComic.size(); i++) {
						deleteBookIndex = i;
						vi = gvMyComic.getChildAt(i);

						final Button btnDeleteBook = (Button) vi
								.findViewById(R.id.btnDeleteBook);
						btnDeleteBook.setVisibility(View.VISIBLE);
						btnDeleteBook.setText(String.valueOf(i));
						btnDeleteBook.setTextColor(getResources().getColor(
								android.R.color.transparent));
						btnDeleteBook.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								showDeciveDeleteForUser(btnDeleteBook.getText().toString());
							}
						});
					}

				} else {
					isShowDeleteBook = false;
					// Hide all delete icon off each book
					for (int i = 0; i < arrayMyComic.size(); i++) {
						vi = gvMyComic.getChildAt(i);
						Button btnDeleteBook = (Button) vi
								.findViewById(R.id.btnDeleteBook);
						btnDeleteBook.setVisibility(View.INVISIBLE);
					}

				}
				return true;
			}
		});

		btnStore.setOnClickListener(this);
		btnSetting.setOnClickListener(this);

		/*******************************************/
		/* Broadcast Receiver */
		/*******************************************/
		mLocalBroadcastManager = LocalBroadcastManager
				.getInstance(getApplicationContext());
		IntentFilter filter = new IntentFilter();
		filter.addAction(INTERNET_CONNECTED);
		filter.addAction(DOWNLOAD_FINISHED);
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(INTERNET_CONNECTED)) {
					if (arrayMyComic.get(arrayMyComic.size() - 1)
							.get("isdownloadfinished")
							.equalsIgnoreCase("false")) {
						if (arrayMyComic.get(arrayMyComic.size() - 1).get("id")
								.endsWith("fancomic")) {
							DownloadFanComicService ds = new DownloadFanComicService(
									getApplicationContext(), arrayMyComic.get(
											arrayMyComic.size() - 1).get("id"),
									DOWNLOAD_FINISHED);

						} else {
							DownloadService ds = new DownloadService(
									MyComicActivity.this, arrayMyComic.get(
											arrayMyComic.size() - 1).get("id"),
									DOWNLOAD_FINISHED);

						}
					}
				} else if (intent.getAction().equals(DOWNLOAD_FINISHED)) {

					int size = arrayMyComic.size();
					size = size - 1;
					if (size != -1) {

						db = new DatabaseHandler(getApplicationContext());
						db.updateIsDownloadFinished(
								arrayMyComic.get(size).get("id"), "true");
						db.close();
						HashMap<String, String> bookInformation = new HashMap<String, String>();
						bookInformation.put(KEY_STATUS, arrayMyComic.get(size)
								.get(KEY_STATUS));
						bookInformation.put(KEY_ERROR, arrayMyComic.get(size)
								.get(KEY_ERROR));
						bookInformation.put(KEY_RECORDS, arrayMyComic.get(size)
								.get(KEY_RECORDS));
						bookInformation.put(KEY_BASE_URL, arrayMyComic
								.get(size).get(KEY_BASE_URL));
						bookInformation.put(KEY_ID,
								arrayMyComic.get(size).get(KEY_ID));
						bookInformation.put(KEY_TITLE, arrayMyComic.get(size)
								.get(KEY_TITLE));
						bookInformation.put(KEY_DESCRIPTION,
								arrayMyComic.get(size).get(KEY_DESCRIPTION));
						bookInformation.put(KEY_SERIES, arrayMyComic.get(size)
								.get(KEY_SERIES));
						bookInformation.put(KEY_COVER, arrayMyComic.get(size)
								.get(KEY_COVER));
						bookInformation.put(KEY_DATABOOK, arrayMyComic
								.get(size).get(KEY_DATABOOK));
						bookInformation.put(KEY_PRICE, arrayMyComic.get(size)
								.get(KEY_PRICE));
						bookInformation.put(KEY_AUTHOR, arrayMyComic.get(size)
								.get(KEY_AUTHOR));
						bookInformation.put(KEY_RATING, arrayMyComic.get(size)
								.get(KEY_RATING));
						bookInformation.put(KEY_TYPE, arrayMyComic.get(size)
								.get(KEY_TYPE));
						bookInformation.put(KEY_DATE_CREATE,
								arrayMyComic.get(size).get(KEY_DATE_CREATE));
						bookInformation.put(KEY_LANGUAGUE,
								arrayMyComic.get(size).get(KEY_LANGUAGUE));
						bookInformation.put(KEY_URL, arrayMyComic.get(size)
								.get(KEY_URL));
						bookInformation.put(KEY_TARGET, arrayMyComic.get(size)
								.get(KEY_TARGET));
						bookInformation.put(KEY_IS_DOWNLOAD_FINISHED, "true");

						arrayMyComic.remove(size);
						arrayMyComic.add(bookInformation);

						View vi = gvMyComic.getChildAt(size);
						ProgressBar progressBarMyBook = (ProgressBar) vi
								.findViewById(R.id.progressBarMyBook);
						TextView textViewMyBook = (TextView) vi
								.findViewById(R.id.txtLabel);
						progressBarMyBook.setVisibility(View.GONE);
						// textViewMyBook.setVisibility(View.GONE);
						RecyclingImageView imgComic = (RecyclingImageView) vi
								.findViewById(R.id.imgComic);
						String comicTitle = removeAccent(arrayMyComic.get(
								arrayMyComic.size() - 1).get("title"));
						String imagePath = "data/data/"
								+ getApplicationContext().getPackageName()
								+ "/Documents/"
								+ comicTitle
								+ "-"
								+ arrayMyComic.get(arrayMyComic.size() - 1)
										.get("id") + "/"
								+ "/data_zip/coverbook.jpg";

						int width = (int) (Config.screenHeight * 0.9 * 0.21);
						int height = (int) (Config.screenHeight * 0.9 * 0.21 * 786 / 512);

						// int width = (int) (gvMyComic.getWidth() * 0.21);
						// int height = (int) (gvMyComic.getWidth() * 0.21 * 786
						// / 512);
						loadBitmap(imgComic, imagePath, width, height);
					}
				}
			}
		};
		mLocalBroadcastManager.registerReceiver(mReceiver, filter);
	}

	protected void showDeciveDeleteForUser(String index){
		if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

			showAlertDialogForDeleteBook(this, "Information", "You really want delete comic!",index);

		} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
			showAlertDialogForDeleteBook(this, "معلومات", "هل تود حذف القصة",index);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {
			// TODO
			btnStore.setBackgroundResource(R.drawable.en_ic_store);
			imgLogo.setImageResource(R.drawable.en_logo_my_comic);
			btnMyComic.setBackgroundResource(R.drawable.en_ic_mycomic);
			btnSetting.setBackgroundResource(R.drawable.en_ic_setting);

		} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
			// TODO
			btnStore.setBackgroundResource(R.drawable.ar_ic_store);
			imgLogo.setImageResource(R.drawable.ar_logo_my_comic);
			btnMyComic.setBackgroundResource(R.drawable.ar_ic_mycomic);
			btnSetting.setBackgroundResource(R.drawable.ar_ic_setting);
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		txtLabel = null;
		if (arrayMyComic.size() > 0) {
			if (arrayMyComic.get(arrayMyComic.size() - 1)
					.get("isdownloadfinished").equalsIgnoreCase("false")) {
				View vi = gvMyComic.getChildAt(arrayMyComic.size() - 1);
				if (vi != null) {
					txtLabel = (TextView) vi.findViewById(R.id.txtLabel);
				}
			}
		}
	}

	private void openComic(int index) {
		if (arrayMyComic.get(index).get("isdownloadfinished")
				.equalsIgnoreCase("true")) {
			String comicTitle = removeAccent(arrayMyComic.get(index).get(
					"title"));
			String imagesPath = "data/data/"
					+ getApplicationContext().getPackageName() + "/Documents/"
					+ comicTitle + "-" + arrayMyComic.get(index).get("id")
					+ "/data_zip/";
			Intent intent = new Intent(getApplicationContext(),
					ReadMyComicActivity.class);
			intent.putExtra("imagesPath", imagesPath);
			intent.putExtra("title", arrayMyComic.get(index).get("title"));
			startActivity(intent);
		} else {
			// Do nothing
		}

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

	private void resizeImageView(ImageView resizeImageView, int width,
			int height, int marginLeft, int marginTop, int marginRight,
			int marginBottom) {
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.height = height;
		llp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
		llp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
		resizeImageView.setLayoutParams(llp);
	}

	private String removeAccent(String inputString) {
		inputString.replace("d", "d").replace("Ð", "D");
		inputString = Normalizer.normalize(inputString, Normalizer.Form.NFD);
		inputString = inputString.replaceAll(
				"\\p{InCombiningDiacriticalMarks}+", "");
		return inputString;
	}

	private void deleteBook(int index) {
		mLocalBroadcastManager = LocalBroadcastManager
				.getInstance(getApplicationContext());
		if (arrayMyComic.get(index).get("isdownloadfinished").equals("false")) {
			mLocalBroadcastManager.sendBroadcast(new Intent(DOWNLOAD_CANCEL));
		}
		db = new DatabaseHandler(getApplicationContext());
		String id = arrayMyComic.get(index).get("id");
		db.deleteBook(id);
		String comicTitle = removeAccent(arrayMyComic.get(index).get("title"));

		// Delete all folder related with this book
		File dir = new File("data/data/"
				+ getApplicationContext().getPackageName() + "/Documents/"
				+ comicTitle + "-" + arrayMyComic.get(index).get("id"));
		deleteFilesInFolder(dir);
		dir.delete();
		arrayMyComic = db.getAllBooksInformation();
		db.close();

		gvMyComic.setAdapter(new MyComicGridViewAdapter(MyComicActivity.this,
				arrayMyComic, (int) (Config.screenHeight * 0.9 * 0.21),
				(int) (Config.screenHeight * 0.9 * 0.21 * 786 / 512)));
		gvMyComic.setVerticalSpacing((int) (gvMyComic.getWidth() * 0.02));
		gvMyComic.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				openComic(arg2);
			}
		});
		gvMyComic.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				View vi = new View(getApplicationContext());
				if (!isShowDeleteBook) {
					isShowDeleteBook = true;
					for (int i = 0; i < arrayMyComic.size(); i++) {
						deleteBookIndex = i;
						vi = gvMyComic.getChildAt(i);
						final Button btnDeleteBook = (Button) vi
								.findViewById(R.id.btnDeleteBook);
						btnDeleteBook.setVisibility(View.VISIBLE);
						btnDeleteBook.setText(String.valueOf(i));
						btnDeleteBook.setTextColor(getResources().getColor(
								android.R.color.transparent));
						btnDeleteBook.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								deleteBook(Integer.valueOf(btnDeleteBook
										.getText().toString()));
							}
						});
					}

				} else {
					isShowDeleteBook = false;
					// Hide all delete icon off each book
					for (int i = 0; i < arrayMyComic.size(); i++) {
						vi = gvMyComic.getChildAt(i);
						Button btnDeleteBook = (Button) vi
								.findViewById(R.id.btnDeleteBook);
						btnDeleteBook.setVisibility(View.INVISIBLE);
					}
				}
				return false;
			}
		});

	}

	// Use to delete all trial file then download full version
	private void deleteFilesInFolder(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				if (new File(dir, children[i]).isDirectory()) {
					deleteDirectory(new File(dir, children[i]));
				} else {
					new File(dir, children[i]).delete();
				}
			}
		}
	}

	// Use to delete all trial sub folder
	private static boolean deleteDirectory(File path) {
		if (path.exists()) {
			File[] files = path.listFiles();
			if (files == null) {
				return true;
			}
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					deleteDirectory(files[i]);
				} else {
					files[i].delete();
				}
			}
		}
		return (path.delete());
	}

	private void loadBitmap(RecyclingImageView mImageView, String imagePath,
			int reqWidth, int reqHeight) {
		// mImageView.setImageResource(R.drawable.gridview_image_load);
		BitmapWorkerTask task = new BitmapWorkerTask(mImageView);
		String[] params = new String[] { imagePath, String.valueOf(reqWidth),
				String.valueOf(reqHeight) };
		task.execute(params);
	}

	class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;
		private String imagePath = "";
		private int reqWidth = 0;
		private int reqHeight = 0;

		public BitmapWorkerTask(ImageView imageView) {
			// Use a WeakReference to ensure the ImageView can be garbage
			// collected
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		// Decode image in background.
		@Override
		protected Bitmap doInBackground(String... params) {
			imagePath = params[0];
			reqWidth = Integer.parseInt(params[1]);
			reqHeight = Integer.parseInt(params[2]);
			return Bitmap.createScaledBitmap(
					BitmapFactory.decodeFile(imagePath), reqWidth, reqHeight,
					true);// width + height of
							// image
		}

		// Once complete, see if ImageView is still around and set bitmap.
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				if (imageView != null) {
					imageView.setImageBitmap(bitmap);
					View vi = gvMyComic.getChildAt(arrayMyComic.size() - 1);
					ProgressBar progressBarMyBook = (ProgressBar) vi
							.findViewById(R.id.progressBarMyBook);
					TextView textViewMyBook = (TextView) vi
							.findViewById(R.id.txtLabel);
					progressBarMyBook.setVisibility(View.GONE);
					textViewMyBook.setVisibility(View.GONE);
				}
			}
		}
	}

	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();
		switch (id) {
		case R.id.btnSetting:
			if (Config.IdUser.equals(" ")) {

				if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

					showAlertDialog(this, "", "Please login to enter this page");

				} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
					showAlertDialog(this, "", "رجا الدخول للصفحة");
				}
			} else {
				Intent setting = new Intent(getApplicationContext(),
						SettingActivity.class);
				startActivity(setting);
				this.finish();
			}

			break;

		case R.id.btnStore:
			Intent store = new Intent(getApplicationContext(),
					ComicListActivity.class);
			startActivity(store);
			MyComicActivity.this.finish();
			break;
		case R.id.btnInformationMycomic:
			// open information dialog
			final Dialog dialog = new Dialog(MyComicActivity.this,
					R.style.DialogDoNotDim);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

			dialog.setContentView(R.layout.dialog_information);
			dialog.getWindow().setLayout((int) (Config.screenHeight * 0.8),
					(int) (Config.screenWidth * 0.5));
			Button btnClose = (Button) dialog.findViewById(R.id.btnClose);
			LinearLayout layout_bottom = (LinearLayout) dialog
					.findViewById(R.id.layout_bottom);
			Button btnInstagramInformation = (Button) dialog
					.findViewById(R.id.btnInstagramInformation);
			Button btnYoutubeInformation = (Button) dialog
					.findViewById(R.id.btnYoutubeInformation);
			Button btnTwitterInformation = (Button) dialog
					.findViewById(R.id.btnTwitterInformation);
			Button btnFacebookInformation = (Button) dialog
					.findViewById(R.id.btnFacebookInformation);

			layout_bottom.setPadding(0, 0, 0,
					(int) (Config.screenHeight * 0.018));
			resizeButton(btnClose, (int) (Config.screenWidth * 0.05),
					(int) (Config.screenWidth * 0.05), 0,
					(int) (Config.screenWidth * 0.005),
					(int) (Config.screenWidth * 0.005), 0);
			resizeButton(btnInstagramInformation,
					(int) (Config.screenWidth * 0.08),
					(int) (Config.screenWidth * 0.08), 0, 0, 0, 0);
			resizeButton(btnYoutubeInformation,
					(int) (Config.screenWidth * 0.08),
					(int) (Config.screenWidth * 0.08),
					(int) (Config.screenWidth * 0.035), 0,
					(int) (Config.screenWidth * 0.035), 0);
			resizeButton(btnTwitterInformation,
					(int) (Config.screenWidth * 0.08),
					(int) (Config.screenWidth * 0.08), 0, 0,
					(int) (Config.screenWidth * 0.035), 0);
			resizeButton(btnFacebookInformation,
					(int) (Config.screenWidth * 0.08),
					(int) (Config.screenWidth * 0.08), 0, 0, 0, 0);

			btnClose.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
				}
			});

			btnFacebookInformation.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
							.parse("https://www.facebook.com/baraahgroup"));
					startActivity(browserIntent);

				}
			});
			btnTwitterInformation.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
							.parse("https://twitter.com/BaraahGroup"));
					startActivity(browserIntent);
				}
			});
			btnInstagramInformation.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
							.parse("http://instagram.com/baraah_group#"));
					startActivity(browserIntent);
				}
			});
			btnYoutubeInformation.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
							.parse("http://www.youtube.com/shakabet5"));
					startActivity(browserIntent);
				}
			});

			dialog.setCancelable(false);
			dialog.show();
			break;
		default:
			break;
		}

	}

	@SuppressWarnings("deprecation")
	private void showAlertDialog(Context context, String title, String message) {
		final AlertDialog alertDialog = new AlertDialog.Builder(context)
				.create();
		// Setting Dialog Title
		alertDialog.setTitle(title);
		// Setting Dialog Message
		alertDialog.setMessage(message);

		// Setting alert dialog icon
		alertDialog.setIcon(R.drawable.ic_fail);
		String ok = "Ok";

		if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

			ok = "OK";

		} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
			ok = getResources().getString(R.string.ar_ok);
		}

		// Setting OK Button
		alertDialog.setButton(ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				alertDialog.dismiss();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

	@SuppressWarnings("deprecation")
	private void showAlertDialogForDeleteBook(Context context, String title, String message, final String index) {
		final AlertDialog alertDialog = new AlertDialog.Builder(context)
				.create();
		// Setting Dialog Title
		alertDialog.setTitle(title);
		// Setting Dialog Message
		alertDialog.setMessage(message);

		// Setting alert dialog icon
		alertDialog.setIcon(R.drawable.ic_fail);
		String ok = "Ok";

		if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

			ok = "OK";

		} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
			ok = getResources().getString(R.string.ar_ok);
		}

		// Setting OK Button
		alertDialog.setButton(ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				deleteBook(Integer.valueOf(index));
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		finish();
	}
}
