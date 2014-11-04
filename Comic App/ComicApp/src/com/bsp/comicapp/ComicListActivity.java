package com.bsp.comicapp;

import it.sephiroth.android.library.widget.AbsHListView.OnScrollListener;
import it.sephiroth.android.library.widget.AbsHListView;
import it.sephiroth.android.library.widget.HListView;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bsp.comicapp.adapter.ComicListGridViewAdapter;
import com.bsp.comicapp.adapter.ComicListHListViewAdapter;
import com.bsp.comicapp.adapter.ComicListListViewAdapter;
import com.bsp.comicapp.displayimage.util.ImageFetcher;
import com.bsp.comicapp.displayimage.util.ImageCache.ImageCacheParams;
import com.bsp.comicapp.util.Config;
import com.bsp.comicapp.util.JSONParser;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.Toast;

public class ComicListActivity extends FragmentActivity implements
		OnClickListener {
	private ImageView imgLogo;
	private Button btnMyComic;
	private Button btnStore;
	private LinearLayout layout_horizontal_listview;
	private Button btnPrevious;
	private Button btnNext;
	private HListView hListView;
	private int currentHListViewPostion = 0;

	private String IMAGE_CACHE_DIR = "thumbs";
	private ImageFetcher mImageFetcher;
	private ImageFetcher mGridViewImageFetcher;

	private Button btnFree;
	private Button btnPay;
	private Button btnTop;
	private Button btnNew;
	private Button btnArabicEnglishComic;
	private Button btnReaderComic;
	private Button btnArabic;
	private LinearLayout layout_search;
	private EditText etSearch;
	private Button btnSearch;

	private Button btnGridViewDisplay;
	private Button btnListViewDisplay;
	private Button btnSetting;
	private String lang = Config.ENGLISH_LANGUAGUE;
	private SharedPreferences languagePreferences;

	private LinearLayout layoutCenterBar;
	private GridView gvComic;
	private ListView lvComic;
	private int displayMode = 1;

	// Creating JSON Parser object
	private JSONParser jsonParser;
	private JSONObject json = null;
	private ArrayList<HashMap<String, String>> arrayBookInformation = new ArrayList<HashMap<String, String>>();

	private int status = -1;
	private String error = "";
	private int records = -1;
	private String base_url = "";
	private JSONArray data = null;

	private String currentType = "pay";
	private String currentLang = Config.ENGLISH_LANGUAGUE;
	private LoadDataAsyncTask loadDataAsyncTask = null;
	private BroadcastReceiver mReceiver;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_comic_list);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		languagePreferences = getSharedPreferences(
				Config.LANGUAGE_PREFERENCE_NAME, MODE_PRIVATE);
		lang = currentLang = languagePreferences.getString(
				Config.PREFERENCE_KEY_LANGUAGE, Config.ENGLISH_LANGUAGUE);

		DisplayMetrics display = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(display);
		if (display.heightPixels > display.widthPixels) {
			Config.screenHeight = display.widthPixels;
			Config.screenWidth = display.heightPixels;
		} else {
			Config.screenHeight = display.heightPixels;
			Config.screenWidth = display.widthPixels;
		}
		final ImageView imageView = (ImageView) findViewById(R.id.imgBackground);
		if (Config.Bg_Portrait != null)
			imageView.setBackgroundDrawable(Config.Bg_Portrait);

		imgLogo = (ImageView) findViewById(R.id.imgLogo);
		btnMyComic = (Button) findViewById(R.id.btnMyComic);
		btnStore = (Button) findViewById(R.id.btnStore);
		layout_horizontal_listview = (LinearLayout) findViewById(R.id.layout_horizontal_listview);
		btnPrevious = (Button) findViewById(R.id.btnPrevious);
		btnNext = (Button) findViewById(R.id.btnNext);
		hListView = (HListView) findViewById(R.id.hListView);
		layoutCenterBar = (LinearLayout) findViewById(R.id.layoutCenterBar);
		layoutCenterBar.getLayoutParams().height = (int) (Config.screenWidth / 27);
		gvComic = (GridView) findViewById(R.id.gvComic);
		lvComic = (ListView) findViewById(R.id.lvComic);
		btnFree = (Button) findViewById(R.id.btnFree);
		btnPay = (Button) findViewById(R.id.btnPay);
		btnTop = (Button) findViewById(R.id.btnTop);
		btnNew = (Button) findViewById(R.id.btnNew);
		btnArabicEnglishComic = (Button) findViewById(R.id.btnArabicEnglishComic);
		// btnArabic = (Button) findViewById(R.id.btnArabic);
		btnReaderComic = (Button) findViewById(R.id.btnReaderComic);
		layout_search = (LinearLayout) findViewById(R.id.layout_search);
		etSearch = (EditText) findViewById(R.id.etSearch);

		btnSearch = (Button) findViewById(R.id.btnSearch);

		btnSetting = (Button) findViewById(R.id.btnSetting);
		btnGridViewDisplay = (Button) findViewById(R.id.btnGridViewDisplay);
		btnListViewDisplay = (Button) findViewById(R.id.btnListViewDisplay);

		// Display image
		ImageCacheParams cacheParams = new ImageCacheParams(
				getApplicationContext(), IMAGE_CACHE_DIR);
		cacheParams.setMemCacheSizePercent(0.1f); // Set memory cache to
													// 25% of
													// app memory
		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		mImageFetcher = new ImageFetcher(getApplicationContext(),
				hListView.getHeight() * 512 / 786, hListView.getHeight());

		mImageFetcher.setLoadingImage(R.drawable.gallery_image_load);
		mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);

		mGridViewImageFetcher = new ImageFetcher(getApplicationContext(),
				gvComic.getHeight() / 2 * 512 / 786, gvComic.getHeight() / 2);

		mGridViewImageFetcher.setLoadingImage(R.drawable.gridview_image_load);
		mGridViewImageFetcher.addImageCache(getSupportFragmentManager(),
				cacheParams);

		// Set font
		Typeface tf = Typeface.createFromAsset(getApplicationContext()
				.getAssets(), "fonts/PAPYRUS.TTF");
		btnMyComic.setTypeface(tf);
		btnStore.setTypeface(tf);
		btnNew.setTypeface(tf);
		btnPay.setTypeface(tf);
		btnTop.setTypeface(tf);
		btnFree.setTypeface(tf);
		btnArabicEnglishComic.setTypeface(tf);
		btnReaderComic.setTypeface(tf);
		btnSetting.setTypeface(tf);
		btnGridViewDisplay.setTypeface(tf);
		btnListViewDisplay.setTypeface(tf);
		// btnArabic.setTypeface(tf);
		etSearch.setTypeface(tf);

		// Set text size
		float textsize = (float) (Config.screenHeight * 0.016);
		float textsizeSearch = (float) (Config.screenHeight * 0.026);
		btnSetting.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
		btnGridViewDisplay.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
		btnListViewDisplay.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
		btnNew.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
		btnPay.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
		btnTop.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
		btnFree.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
		btnArabicEnglishComic.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
		// btnArabic.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
		btnReaderComic.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
		etSearch.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsizeSearch);

		// Resize item
		layout_horizontal_listview.setPadding(0,
				(int) (Config.screenWidth * 0.009), 0,
				(int) (Config.screenWidth * 0.009));
		resizeImageView(imgLogo, 0,
				(int) (Config.screenHeight * 0.075 * 150 / 175), 0, 0, 0, 0);
		resizeButton(btnMyComic, (int) (Config.screenHeight * 0.1),
				(int) (Config.screenHeight * 0.1 * 150 / 175), 0, 0, 0, 0);
		resizeButton(btnStore, (int) (Config.screenHeight * 0.21),
				(int) (Config.screenHeight * 0.1 * 150 / 175), 0, 0, 0, 0);

		resizeButton(btnNext, (int) (Config.screenWidth / 30),
				(int) (Config.screenWidth / 30),
				(int) (Config.screenWidth / 100),
				(int) (Config.screenWidth / 100),
				(int) (Config.screenWidth / 100),
				(int) (Config.screenWidth / 100));
		resizeButton(btnPrevious, (int) (Config.screenWidth / 30),
				(int) (Config.screenWidth / 30),
				(int) (Config.screenWidth / 100),
				(int) (Config.screenWidth / 100),
				(int) (Config.screenWidth / 100),
				(int) (Config.screenWidth / 100));
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
		resizeButton(btnGridViewDisplay, (int) (Config.screenHeight * 0.15),
				(int) (Config.screenHeight * 0.15 * 82 / 196), 0,
				(int) (Config.screenHeight * 0.01 * 82 / 196), 0,
				(int) (Config.screenHeight * 0.01 * 82 / 196));
		resizeButton(btnListViewDisplay, (int) (Config.screenHeight * 0.15),
				(int) (Config.screenHeight * 0.15 * 82 / 196), 0,
				(int) (Config.screenHeight * 0.01 * 82 / 196), 0,
				(int) (Config.screenHeight * 0.01 * 82 / 196));

		layout_search.getLayoutParams().height = (int) (Config.screenWidth / 33);
		layout_search.getLayoutParams().width = (int) (Config.screenWidth / 6);
		layout_search.setX((int) (Config.screenWidth / 100));
		
		resizeButtonCenterBar(btnFree, 0, (int) (Config.screenWidth / 33), 5,
				0, 0, 0);
		resizeButtonCenterBar(btnPay, 0, (int) (Config.screenWidth / 33), (int) (Config.screenHeight / 60), 0,
				0, 0);
		resizeButtonCenterBar(btnTop, 0, (int) (Config.screenWidth / 33), (int) (Config.screenHeight / 60), 0,
				0, 0);
		resizeButtonCenterBar(btnNew, 0, (int) (Config.screenWidth / 33), (int) (Config.screenHeight / 60), 0,
				0, 0);
		resizeButtonCenterBar(btnArabicEnglishComic, 0,
				(int) (Config.screenWidth / 33),
				(int) (Config.screenHeight / 60), 0,
				(int) (Config.screenHeight / 36), 0);

		// resizeButtonCenterBar(btnArabic, 0, (int) (Config.screenWidth / 33),
		// (int) (Config.screenHeight / 90), 0,
		// (int) (Config.screenHeight / 36), 0);

		resizeButtonCenterBar(btnReaderComic, 0,
				(int) (Config.screenWidth / 20), (int) (Config.screenHeight / 60), 0,
				(int) (Config.screenHeight / 36), 0);
		resizeButton(btnSearch, (int) (Config.screenWidth / 55),
				(int) (Config.screenWidth / 55), 1, 1, 1, 1);

		btnMyComic.setOnClickListener(this);
		btnNext.setOnClickListener(this);
		btnPrevious.setOnClickListener(this);
		btnGridViewDisplay.setOnClickListener(this);
		btnListViewDisplay.setOnClickListener(this);
		btnSetting.setOnClickListener(this);
		btnFree.setOnClickListener(this);
		btnPay.setOnClickListener(this);
		btnNew.setOnClickListener(this);
		btnTop.setOnClickListener(this);
		btnArabicEnglishComic.setOnClickListener(this);
		// btnArabic.setOnClickListener(this);
		btnReaderComic.setOnClickListener(this);
		btnStore.setOnClickListener(this);

		Button butInfo = (Button) findViewById(R.id.btnInformationComicList);
		butInfo.setOnClickListener(this);
		resizeButton(butInfo, (int) (Config.screenHeight * 0.045),
				(int) (Config.screenHeight * 0.15 * 69 / 182),
				(int) (Config.screenHeight * 0.01), 0,
				(int) (Config.screenHeight * 0.01), 0);

		String[] params = new String[] { currentLang, currentType };
		loadDataAsyncTask = new LoadDataAsyncTask();
		loadDataAsyncTask.execute(params);

		etSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				searchBook(etSearch.getText().toString().trim());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});

		IntentFilter filter = new IntentFilter("Intent_Background");
		// filter.addAction("Intent_Background");

		mReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				imageView.setBackgroundDrawable(Config.Bg_Portrait);

			}
		};
		// mLocalBroadcastManager=LocalBroadcastManager.getInstance(this);
		registerReceiver(mReceiver, filter);
	}

	@SuppressWarnings("unused")
	private void searchBook(String name) {

		ArrayList<HashMap<String, String>> arrayBookTemp = new ArrayList<HashMap<String, String>>();
		// TODO Auto-generated method stub
		// setTitle(name);
		String nameBook1 = arrayBookInformation.get(0).get("title")
				.toUpperCase();
		// Toast.makeText(this, nameBook1+" "+name, Toast.LENGTH_LONG).show();
		for (int i = 0; i < arrayBookInformation.size(); i++) {
			String nameBook = arrayBookInformation.get(i).get("title")
					.toUpperCase();

			if (nameBook.indexOf(name.toString().toUpperCase()) != -1) {
				// search listview
				arrayBookTemp.add(arrayBookInformation.get(i));

			}

		}
		if (arrayBookTemp == null && arrayBookTemp.size() == 0) {
			arrayBookTemp = arrayBookInformation;
			// Toast.makeText(this, String.valueOf(arrayBookTemp.size()),
			// Toast.LENGTH_LONG).show();
		}
		// Toast.makeText(this, String.valueOf(arrayBookInformation.size()),
		// Toast.LENGTH_LONG).show();

		// arrClub.clear();
		gvComic.setAdapter(new ComicListGridViewAdapter(ComicListActivity.this,
				currentType, status, error, records, base_url, arrayBookTemp,
				mGridViewImageFetcher, gvComic.getWidth() / 2, gvComic
						.getHeight() / 2));
		lvComic.setAdapter(new ComicListListViewAdapter(ComicListActivity.this,
				currentType, status, error, records, base_url, arrayBookTemp,
				mGridViewImageFetcher, lvComic.getWidth(),
				lvComic.getHeight() / 3));

	}

	@Override
	protected void onResume() {
		super.onResume();

		if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {
			// TODO
			btnStore.setBackgroundResource(R.drawable.en_ic_store);
			imgLogo.setImageResource(R.drawable.en_logo_comic_list);
			btnMyComic.setBackgroundResource(R.drawable.en_ic_mycomic);
			btnGridViewDisplay.setBackgroundResource(R.drawable.en_ic_gridview);
			btnListViewDisplay.setBackgroundResource(R.drawable.en_ic_listview);
			btnSetting.setBackgroundResource(R.drawable.en_ic_setting);

			btnFree.setText(R.string.free);
			btnPay.setText(R.string.pay);
			btnNew.setText(R.string.comic_new);
			btnTop.setText(R.string.top);
			btnArabicEnglishComic.setText("ARABIC");
			// btnArabic.setText("ARABIC");

			btnReaderComic.setText(R.string.readercomic);
			etSearch.setHint(R.string.search);
			etSearch.setGravity(Gravity.LEFT);
			if (Config.IdUser.equals(" ")) {
				btnStore.setBackgroundResource(R.drawable.sighnin);
			} else
				btnStore.setBackgroundResource(R.drawable.sighnout);

		} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
			// TODO
			btnStore.setBackgroundResource(R.drawable.ar_ic_store);
			imgLogo.setImageResource(R.drawable.ar_logo_comic_list);
			btnMyComic.setBackgroundResource(R.drawable.ar_ic_mycomic);
			btnGridViewDisplay.setBackgroundResource(R.drawable.ar_ic_gridview);
			btnListViewDisplay.setBackgroundResource(R.drawable.ar_ic_listview);
			btnSetting.setBackgroundResource(R.drawable.ar_ic_setting);
			if (Config.IdUser.equals(" ")) {
				btnStore.setBackgroundResource(R.drawable.signin_arabic);
			} else
				btnStore.setBackgroundResource(R.drawable.signout_arabic);
			etSearch.setGravity(Gravity.RIGHT);
			btnFree.setText(R.string.ar_free);
			btnPay.setText(R.string.ar_pay);
			btnNew.setText(R.string.ar_comic_new);
			btnTop.setText(R.string.ar_top);
			btnArabicEnglishComic.setText(R.string.ar_english);
			// btnArabic.setText(R.string.ar_arabic);
			btnReaderComic.setText(R.string.ar_readercomic);
			etSearch.setHint(R.string.ar_search);
			etSearch.setGravity(Gravity.LEFT);
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (loadDataAsyncTask != null) {
			loadDataAsyncTask.cancel(true);
		}
		try {
			mImageFetcher.setExitTasksEarly(true);
			mGridViewImageFetcher.setExitTasksEarly(true);
			mImageFetcher.flushCache();
			mGridViewImageFetcher.flushCache();
			mImageFetcher.clearCache();
			mImageFetcher.closeCache();
			mGridViewImageFetcher.clearCache();
			mGridViewImageFetcher.closeCache();
		} catch (Exception e) {

		}
		unregisterReceiver(mReceiver);
	}

	// @Override
	// public void onLowMemory() {
	// super.onLowMemory();
	//
	// try {
	// mImageFetcher.setExitTasksEarly(true);
	// mGridViewImageFetcher.setExitTasksEarly(true);
	// mImageFetcher.flushCache();
	// mGridViewImageFetcher.flushCache();
	// mImageFetcher.clearCache();
	// mImageFetcher.closeCache();
	// mGridViewImageFetcher.clearCache();
	// mGridViewImageFetcher.closeCache();
	// } catch (Exception e) {
	// }
	// }

	private void resizeButton(Button resizeButton, int width, int height,
			int marginLeft, int marginTop, int marginRight, int marginBottom) {
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.height = height;
		llp.width = width;
		llp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
		resizeButton.setLayoutParams(llp);
	}

	private void resizeButtonCenterBar(Button resizeButton, int width,
			int height, int marginLeft, int marginTop, int marginRight,
			int marginBottom) {
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.height = height;
		llp.width = LayoutParams.WRAP_CONTENT;
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

	private class LoadDataAsyncTask extends AsyncTask<String, Void, String> {
		private boolean isParseJSONError = false;

		@Override
		protected String doInBackground(String... params) {
			String url_get_book = String.format(Config.GETBOOK_URL, params[0],
					params[1]);
			json = null;
			json = jsonParser.getJSONFromUrl(url_get_book);
			if (json != null) {
				try {
					status = json.getInt("status");
					error = json.getString("error");
					records = json.getInt("records");
					base_url = json.getString("base_url");
					data = json.getJSONArray("data");
					for (int i = 0; i < data.length(); i++) {
						JSONObject jObj = data.getJSONObject(i);
						HashMap<String, String> bookInformation = new HashMap<String, String>();
						bookInformation.put("id", jObj.getString("id"));
						bookInformation.put("title", jObj.getString("title"));
						bookInformation.put("description",
								jObj.getString("description"));
						bookInformation.put("series", jObj.getString("series"));
						bookInformation.put("cover", jObj.getString("cover"));
						bookInformation.put("databook",
								jObj.getString("databook"));
						bookInformation.put("price", jObj.getString("price"));
						bookInformation.put("author", jObj.getString("author"));
						bookInformation.put("rating", jObj.getString("rating"));
						bookInformation.put("type", jObj.getString("type"));
						bookInformation.put("date_create",
								jObj.getString("date_create"));
						bookInformation.put("language",
								jObj.getString("language"));
						bookInformation.put("url", jObj.getString("url"));
						bookInformation.put("target", jObj.getString("target"));
						final String u = jObj.getString("url");
						arrayBookInformation.add(bookInformation);

						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								// TODO Auto-generated method stub
								// Toast.makeText(ComicListActivity.this, u,
								// Toast.LENGTH_LONG).show();
							}
						});
					}
				} catch (JSONException e) {
					Log.i("Nam Dinh", "JSONException");
					e.printStackTrace();
				}

			} else {
				isParseJSONError = true;
			}

			return "Executed";
		}

		@Override
		protected void onPostExecute(String result) {
			if (isParseJSONError) {

			} else {
				gvComic.setAdapter(new ComicListGridViewAdapter(
						ComicListActivity.this, currentType, status, error,
						records, base_url, arrayBookInformation,
						mGridViewImageFetcher, gvComic.getWidth() / 2, gvComic
								.getHeight() / 2));
				lvComic.setAdapter(new ComicListListViewAdapter(
						ComicListActivity.this, currentType, status, error,
						records, base_url, arrayBookInformation,
						mGridViewImageFetcher, lvComic.getWidth(), lvComic
								.getHeight() / 3));

				if (displayMode == 1) {
					gvComic.setVisibility(View.VISIBLE);
				} else if (displayMode == 2) {
					lvComic.setVisibility(View.VISIBLE);
				}

				hListView.setAdapter(new ComicListHListViewAdapter(
						ComicListActivity.this, arrayBookInformation,
						currentType, status, error, records, base_url,
						mImageFetcher, hListView.getHeight() * 512 / 786,
						hListView.getHeight()));
				hListView
						.setDividerWidth(hListView.getHeight() * 512 / 786 / 12);

				hListView.setOnScrollListener(new OnScrollListener() {
					@Override
					public void onScrollStateChanged(AbsHListView view,
							int scrollState) {
					}

					@Override
					public void onScroll(AbsHListView view,
							int firstVisibleItem, int visibleItemCount,
							int totalItemCount) {
						currentHListViewPostion = firstVisibleItem;
					}
				});

			}
		}

		@Override
		protected void onPreExecute() {
			isParseJSONError = false;
			jsonParser = new JSONParser();
			arrayBookInformation.clear();
			arrayBookInformation = new ArrayList<HashMap<String, String>>();
			lvComic.setVisibility(View.INVISIBLE);
			gvComic.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onClick(View v) {

		try {
			InputMethodManager inputManager = (InputMethodManager) this
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(this.getCurrentFocus()
					.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		} catch (Exception ex) {
		}

		int id = v.getId();
		switch (id) {
		case R.id.btnNext:
			int a = 0;
			while (a < hListView.getWidth()
					/ ((hListView.getHeight() * 512 / 786) + hListView
							.getHeight() * 512 / 786 / 12)) {
				a++;
			}

			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

			if (currentHListViewPostion < arrayBookInformation.size() - a) {
				currentHListViewPostion = currentHListViewPostion + 1;
				hListView.smoothScrollToPositionFromLeft(
						currentHListViewPostion, 0, 350);
			}
			break;

		case R.id.btnPrevious:
			if (currentHListViewPostion > 0) {
				currentHListViewPostion = currentHListViewPostion - 1;
				hListView.smoothScrollToPositionFromLeft(
						currentHListViewPostion, 0, 350);
			}
			break;

		case R.id.btnGridViewDisplay:
			if (displayMode != 1) {
				Animation in = AnimationUtils.loadAnimation(this,
						android.R.anim.fade_in);
				gvComic.startAnimation(in);
				gvComic.setVisibility(View.VISIBLE);
				// Animation out = AnimationUtils.makeOutAnimation(this, true);
				// lvComic.startAnimation(out);
				lvComic.setVisibility(View.INVISIBLE);
				displayMode = 1;
			}
			break;
		case R.id.btnListViewDisplay:
			if (displayMode != 2) {
				Animation in = AnimationUtils.loadAnimation(this,
						android.R.anim.fade_in);
				lvComic.startAnimation(in);
				lvComic.setVisibility(View.VISIBLE);
				// Animation out = AnimationUtils.makeOutAnimation(this, true);
				// gvComic.startAnimation(out);
				gvComic.setVisibility(View.INVISIBLE);
				displayMode = 2;
			}
			break;

		case R.id.btnMyComic:
			Intent myComic = new Intent(getApplicationContext(),
					MyComicActivity.class);
			startActivity(myComic);
			ComicListActivity.this.finish();
			break;

		case R.id.btnSetting:

			if (Config.IdUser.equals(" ")) {

				if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

					showAlertDialog(this, "", "Please login to enter this page");

				} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
					showAlertDialog(this, "", " رجا الدخول للصفحة");
				}
			} else {
				Intent setting = new Intent(getApplicationContext(),
						SettingActivity.class);
				startActivity(setting);
				ComicListActivity.this.finish();
			}

			break;
		case R.id.btnFree:
			if (!currentType.equals("free")) {
				if (loadDataAsyncTask != null) {
					loadDataAsyncTask.cancel(true);
				}
				currentType = "free";
				loadDataAsyncTask = new LoadDataAsyncTask();
				String[] params = new String[] { currentLang, currentType };
				loadDataAsyncTask.execute(params);
			}
			setEnbaleBtnFree(false);
			btnArabicEnglishComic.setBackgroundColor(getResources().getColor(
					android.R.color.transparent));
			break;

		case R.id.btnPay:
			if (!currentType.equals("pay")) {
				if (loadDataAsyncTask != null) {
					loadDataAsyncTask.cancel(true);
				}
				currentType = "pay";
				loadDataAsyncTask = new LoadDataAsyncTask();
				String[] params = new String[] { currentLang, currentType };
				loadDataAsyncTask.execute(params);
			}
			setEnbalebtnPay(false);
			btnArabicEnglishComic.setBackgroundColor(getResources().getColor(
					android.R.color.transparent));
			break;
		case R.id.btnNew:
			if (!currentType.equals("new")) {
				if (loadDataAsyncTask != null) {
					loadDataAsyncTask.cancel(true);
				}
				currentType = "new";
				loadDataAsyncTask = new LoadDataAsyncTask();
				String[] params = new String[] { currentLang, currentType };
				loadDataAsyncTask.execute(params);
			}
			setEnbalebtnNew(false);
			btnArabicEnglishComic.setBackgroundColor(getResources().getColor(
					android.R.color.transparent));
			break;
		case R.id.btnTop:
			if (!currentType.equals("top")) {
				if (loadDataAsyncTask != null) {
					loadDataAsyncTask.cancel(true);
				}
				currentType = "top";
				loadDataAsyncTask = new LoadDataAsyncTask();
				String[] params = new String[] { currentLang, currentType };
				loadDataAsyncTask.execute(params);
			}
			setEnbalebtnTop(false);
			btnArabicEnglishComic.setBackgroundColor(getResources().getColor(
					android.R.color.transparent));
			break;
		case R.id.btnArabicEnglishComic:
			btnArabicEnglishComic.setBackgroundColor(getResources().getColor(
					R.color.aa));
			 setEnbalebtnArabicEnglishComi(false);
			if (loadDataAsyncTask != null) {
				loadDataAsyncTask.cancel(true);
			}
			if (!currentLang.equals(Config.ENGLISH_LANGUAGUE)) {
				currentLang = Config.ENGLISH_LANGUAGUE;
				loadDataAsyncTask = new LoadDataAsyncTask();
				String[] params = new String[] { currentLang, currentType };
				loadDataAsyncTask.execute(params);

				if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {
					btnArabicEnglishComic.setText("ARABIC");
				} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
					btnArabicEnglishComic.setText(R.string.ar_arabic);
				}

			} else if (!currentLang.equals(Config.ARABIC_LANGUAGE)) {

				if (loadDataAsyncTask != null) {
					loadDataAsyncTask.cancel(true);
				}
				currentLang = Config.ARABIC_LANGUAGE;
				loadDataAsyncTask = new LoadDataAsyncTask();
				String[] params1 = new String[] { currentLang, currentType };
				loadDataAsyncTask.execute(params1);

				if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {
					btnArabicEnglishComic.setText("ENGLISH");
				} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
					btnArabicEnglishComic.setText(R.string.ar_english);
				}

			}

			// btnArabic.setEnabled(true);
			// btnArabicEnglishComic.setEnabled(false);
			// btnArabic.setTextColor(getResources().getColor(android.R.color.white));
			// btnArabicEnglishComic.setTextColor(getResources().getColor(R.color.gray_enable));
			break;
		// case R.id.btnArabic:
		//
		// if (loadDataAsyncTask != null) {
		// loadDataAsyncTask.cancel(true);
		// }
		// currentLang = Config.ARABIC_LANGUAGE;
		// loadDataAsyncTask = new LoadDataAsyncTask();
		// String[] params1 = new String[] { currentLang, currentType };
		// loadDataAsyncTask.execute(params1);
		//
		// // btnArabic.setEnabled(false);
		// //
		// btnArabic.setTextColor(getResources().getColor(R.color.gray_enable));
		// //
		// btnArabicEnglishComic.setTextColor(getResources().getColor(android.R.color.white));
		// // btnArabicEnglishComic.setEnabled(true);
		//
		// break;
		case R.id.btnReaderComic:
			setEnbaleReaderComic(false);
			if (Config.IdUser.equals(" ")) {

				if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

					showAlertDialog(this, "", "Please login to enter this page");

				} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {

					showAlertDialog(this, "", " رجا الدخول للصفحة");

				}
			} else {
				Intent readerComic = new Intent(getApplicationContext(),
						FanComicActivity.class);
				startActivity(readerComic);
				ComicListActivity.this.finish();
			}
			//

			break;
		case R.id.btnStore:

			if (Config.IdUser.equals(" ")) {

				Intent intentLogin = new Intent(getApplicationContext(),
						LoginComicAppActivity.class);
				startActivity(intentLogin);
				ComicListActivity.this.finish();

			} else {

				MainActivity.logout();

				Intent intentLogin = new Intent(getApplicationContext(),
						LoginComicAppActivity.class);
				startActivity(intentLogin);
				ComicListActivity.this.finish();
			}

			break;

		case R.id.btnInformationComicList:
			// open information dialog
			final Dialog dialog = new Dialog(ComicListActivity.this,
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

	void setEnbaleBtnFree(boolean var) {
		btnFree.setEnabled(var);
		btnPay.setEnabled(!var);
		;
		btnTop.setEnabled(!var);
		;
		btnNew.setEnabled(!var);
		;
		btnArabicEnglishComic.setEnabled(!var);
		;
		btnReaderComic.setEnabled(!var);
		;
		//btnArabic.setEnabled(!var);
		;

	}

	void setEnbalebtnPay(boolean var) {
		btnFree.setEnabled(!var);
		btnPay.setEnabled(var);
		;
		btnTop.setEnabled(!var);
		;
		btnNew.setEnabled(!var);
		;
		btnArabicEnglishComic.setEnabled(!var);
		;
		btnReaderComic.setEnabled(!var);
		;
		// btnArabic.setEnabled(!var);;

	}

	void setEnbalebtnTop(boolean var) {
		btnFree.setEnabled(!var);
		btnPay.setEnabled(!var);
		;
		btnTop.setEnabled(var);
		;
		btnNew.setEnabled(!var);
		;
		btnArabicEnglishComic.setEnabled(!var);
		;
		btnReaderComic.setEnabled(!var);
		;
		// btnArabic.setEnabled(!var);;

	}

	void setEnbalebtnNew(boolean var) {
		btnFree.setEnabled(!var);
		btnPay.setEnabled(!var);
		;
		btnTop.setEnabled(!var);
		;
		btnNew.setEnabled(var);
		;
		btnArabicEnglishComic.setEnabled(!var);
		;
		btnReaderComic.setEnabled(!var);
		;
		// btnArabic.setEnabled(!var);;

	}

	void setEnbalebtnArabicEnglishComi(boolean var) {
		btnFree.setEnabled(!var);
		btnPay.setEnabled(!var);
		;
		btnTop.setEnabled(!var);
		;
		btnNew.setEnabled(!var);
		;
		btnArabicEnglishComic.setEnabled(!var);
		;
		btnReaderComic.setEnabled(!var);
		;
		// btnArabic.setEnabled(!var);;

	}

	void setEnbaleReaderComic(boolean var) {
		btnFree.setEnabled(!var);
		btnPay.setEnabled(!var);
		;
		btnTop.setEnabled(!var);
		;
		btnNew.setEnabled(!var);
		;
		btnArabicEnglishComic.setEnabled(!var);
		;
		btnReaderComic.setEnabled(var);
		;
		// btnArabic.setEnabled(!var);;

	}
	// void setEnbaleBtnFree(boolean var) {
	// btnFree.setEnabled(var);
	// btnPay.setEnabled(!var);;
	// btnTop.setEnabled(!var);;
	// btnNew.setEnabled(!var);;
	// btnArabicEnglishComic.setEnabled(!var);;
	// btnReaderComic.setEnabled(!var);;
	// btnArabic.setEnabled(!var);;
	//
	//
	// }
}
