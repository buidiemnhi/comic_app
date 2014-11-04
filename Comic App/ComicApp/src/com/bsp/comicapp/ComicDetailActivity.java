package com.bsp.comicapp;

import it.sephiroth.android.library.widget.HListView;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.bsp.comicapp.adapter.ComicDetailCommentListViewAdapter;
import com.bsp.comicapp.adapter.ComicDetailHListViewAdapter;
import com.bsp.comicapp.database.DatabaseHandler;
import com.bsp.comicapp.displayimage.util.ImageCache.ImageCacheParams;
import com.bsp.comicapp.displayimage.util.ImageFetcher;
import com.bsp.comicapp.displayimage.util.RecyclingImageView;
import com.bsp.comicapp.model.Comment;
import com.bsp.comicapp.util.Config;
import com.bsp.comicapp.util.ConnectionDetector;
import com.bsp.comicapp.util.JSONParser;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

public class ComicDetailActivity extends FragmentActivity implements
		OnClickListener, OnTouchListener {

	private static final String CONFIG_ENVIRONMENT = PaymentActivity.ENVIRONMENT_LIVE;

	// note that these credentials will differ between live & sandbox
	// environments.
	private static final String CONFIG_CLIENT_ID = "AQB2YBCaykdGksu9OhUCmLUYgX7qqom7jtZMFZKwp5vJfUlM_iFZ3yE0rPBx";
	// when testing in sandbox, this is likely the -facilitator email address.
	private static final String CONFIG_RECEIVER_EMAIL = "nuhaalogaily@gmail.com";

	public static final String build = "10.12.09.8053";
	protected static final int INITIALIZE_SUCCESS = 0;
	protected static final int INITIALIZE_FAILURE = 1;

	private Button btnStore;
	private ImageView imgLogo;
	private Button btnMyComic;
	private LinearLayout layout_content_parent;
	private LinearLayout layout_content;
	private TextView txtComicTitle;
	private RecyclingImageView imgComic;
	private LinearLayout layout_description_rating_download;
	private TextView txtDescriptionTitle;
	private TextView txtDescription;
	private RatingBar ratingBar;
	private Button btnDownload;
	private LinearLayout layout_horizontal_listview;
	private HListView hListView;
	private ListView lvComment;
	private LinearLayout layout_divider;
	private RecyclingImageView imgAvatar;
	private LinearLayout layout_comment;
	private EditText etComment;
	private Button btnCommentDone;
	private Button btnSetting;
	private TextView tvLoadMore;

	private String lang = Config.ENGLISH_LANGUAGUE;
	private SharedPreferences languagePreferences;

	// Book Information
	private String status = "";
	private String error = "";
	private String records = "";
	private String base_url = "";
	private String idBook = "";
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
	private String mTypeBook;

	private boolean isFancomicBook = false;

	boolean isRating = false, isRunning = true, isShowDialog = false;
	JSONArray jsonArray;
	int Count;

	private ArrayList<String> arrImageUrl = new ArrayList<String>();
	private ArrayList<Comment> arrComment = new ArrayList<Comment>();
	ComicDetailCommentListViewAdapter adapter;
	private String IMAGE_CACHE_DIR = "thumbs";
	private ImageFetcher mImageFetcher;

	static ConnectionDetector connectionDetector;
	private DatabaseHandler db;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_comic_detail);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

		db = new DatabaseHandler(this);
		/*
		 * Register service for Paypal
		 */
		Intent intent = new Intent(this, PayPalService.class);

		intent.putExtra(PaymentActivity.EXTRA_PAYPAL_ENVIRONMENT,
				CONFIG_ENVIRONMENT);
		intent.putExtra(PaymentActivity.EXTRA_CLIENT_ID, CONFIG_CLIENT_ID);
		intent.putExtra(PaymentActivity.EXTRA_RECEIVER_EMAIL,
				CONFIG_RECEIVER_EMAIL);

		startService(intent);

		languagePreferences = getSharedPreferences(
				Config.LANGUAGE_PREFERENCE_NAME, MODE_PRIVATE);
		lang = languagePreferences.getString(Config.PREFERENCE_KEY_LANGUAGE,
				Config.ENGLISH_LANGUAGUE);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			isFancomicBook = extras.getBoolean("isfancomicbook");
			status = extras.getString("status");
			error = extras.getString("error");
			records = extras.getString("records");
			base_url = extras.getString("base_url");
			idBook = extras.getString("id");
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
			arrImageUrl = extras.getStringArrayList("urlImage");
		}
		ImageView imageView = (ImageView) findViewById(R.id.imgBackground);
		if (Config.Bg_Portrait != null)
			imageView.setBackgroundDrawable(Config.Bg_Portrait);
		btnStore = (Button) findViewById(R.id.btnStore);
		imgLogo = (ImageView) findViewById(R.id.imgLogo);
		btnMyComic = (Button) findViewById(R.id.btnMyComic);
		layout_content_parent = (LinearLayout) findViewById(R.id.layout_content_parent);
		layout_content = (LinearLayout) findViewById(R.id.layout_comic_detail_list_item);
		txtComicTitle = (TextView) findViewById(R.id.txtComicTitle);
		imgComic = (RecyclingImageView) findViewById(R.id.imgComic);
		layout_description_rating_download = (LinearLayout) findViewById(R.id.layout_description_rating_download);
		txtDescriptionTitle = (TextView) findViewById(R.id.txtDescriptionTitle);
		txtDescription = (TextView) findViewById(R.id.txtDescription);
		ratingBar = (RatingBar) findViewById(R.id.ratingBar);
		btnDownload = (Button) findViewById(R.id.btnDownload);
		layout_horizontal_listview = (LinearLayout) findViewById(R.id.layout_horizontal_listview);
		hListView = (HListView) findViewById(R.id.hListView);
		lvComment = (ListView) findViewById(R.id.lvComment);
		layout_divider = (LinearLayout) findViewById(R.id.layout_divider);
		imgAvatar = (RecyclingImageView) findViewById(R.id.imgAvatar);
		if (Config.USER != null) {

		}
		layout_comment = (LinearLayout) findViewById(R.id.layout_comment);
		etComment = (EditText) findViewById(R.id.etComment);
		btnCommentDone = (Button) findViewById(R.id.btnCommentDone);
		btnCommentDone.setOnClickListener(this);
		btnSetting = (Button) findViewById(R.id.btnSetting);

		// Set font
		Typeface tf = Typeface.createFromAsset(getApplicationContext()
				.getAssets(), "fonts/PAPYRUS.TTF");
		btnMyComic.setTypeface(tf);
		btnStore.setTypeface(tf);
		btnSetting.setTypeface(tf);

		// Set text size
		float textsize2 = (float) (Config.screenHeight * 0.025);
		btnDownload.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize2);
		btnCommentDone.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize2);
		txtComicTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize2);
		txtDescriptionTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize2);
		txtDescription.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize2);
		etComment.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize2);

		// resize item
		layout_content_parent.setPadding((int) (Config.screenHeight * 0.05),
				(int) (Config.screenHeight * 0.02),
				(int) (Config.screenHeight * 0.05), 0);
		layout_content.setPadding((int) (Config.screenHeight * 0.02),
				(int) (Config.screenHeight * 0.02),
				(int) (Config.screenHeight * 0.02),
				(int) (Config.screenHeight * 0.02));
		layout_horizontal_listview.setPadding(0,
				(int) (Config.screenWidth * 0.012), 0,
				(int) (Config.screenWidth * 0.012));
		layout_description_rating_download.setPadding(
				(int) (Config.screenHeight * 0.01), 0, 0, 0);
		layout_divider.setPadding(0, 0, 0, (int) (Config.screenWidth * 0.012));
		layout_comment.setPadding((int) (Config.screenHeight * 0.01),
				(int) (Config.screenHeight * 0.01),
				(int) (Config.screenHeight * 0.01),
				(int) (Config.screenHeight * 0.01));

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
				(int) (Config.screenHeight * 0.01 * 69 / 182), 0,
				(int) (Config.screenHeight * 0.01 * 69 / 182));

		resizeRecyclingImageView(imgComic, (int) (Config.screenHeight * 0.3),
				(int) (Config.screenHeight * 0.3 * 786 / 512), 0, 0, 0, 0);

		resizeButton(btnDownload, (int) (Config.screenHeight * 0.19),
				(int) (Config.screenHeight * 0.055), 0, 0, 0, 0);

		resizeRecyclingImageView(imgAvatar,
				(int) (Config.screenHeight * 0.135),
				(int) (Config.screenHeight * 0.135), 0, 0, 0, 0);
		resizeButton(btnCommentDone, (int) (Config.screenHeight * 0.19),
				(int) (Config.screenHeight * 0.055), 0, 0, 0, 0);

		int ratingbar_element_width = (int) (Config.screenHeight * 0.055 * 80 / 77);
		int ratingbar_element_height = (int) (Config.screenHeight * 0.055);
		Bitmap[] a = new Bitmap[] {
				Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
						getResources(), R.drawable.grey_star),
						ratingbar_element_width, ratingbar_element_height, true),
				Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
						getResources(), R.drawable.grey_star),
						ratingbar_element_width, ratingbar_element_height, true),
				Bitmap.createScaledBitmap(BitmapFactory.decodeResource(
						getResources(), R.drawable.pink_star),
						ratingbar_element_width, ratingbar_element_height, true) };
		ratingBar.setProgressDrawable(buildRatingBarDrawables(a));
		ratingBar.getLayoutParams().height = ratingbar_element_height;
		ratingBar.getLayoutParams().width = ratingbar_element_width * 5;

		hListView.getLayoutParams().height = (int) (Config.screenWidth * 0.135);

		// on click event
		layout_comment.setOnClickListener(this);
		btnStore.setOnClickListener(this);
		btnMyComic.setOnClickListener(this);
		btnSetting.setOnClickListener(this);
		btnDownload.setOnClickListener(this);

		// ******************************************************************************

		if (idBook.indexOf("upload") != -1) {
			mTypeBook = "upload";
		} else
			mTypeBook = "none";

		// ******************************************************************************

		connectionDetector = new ConnectionDetector(this);
		ratingBar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {

			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating,
					boolean fromUser) {

				showAlertInternet(ComicDetailActivity.this, rating);

			}
		});

		txtComicTitle.setText(title);
		txtDescription.setText(description);
		tvLoadMore = (TextView) findViewById(R.id.tvLoading);

		tvLoadMore = (TextView) findViewById(R.id.tvLoading);
		tvLoadMore.setTypeface(tf);
		tvLoadMore.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize2);
		loadMore();

		if (Config.USER.drawableAvatar != null)
			imgAvatar.setImageDrawable(Config.USER.drawableAvatar);
		else {
			String path = "/data/data/com.bsp.comicapp/avatar";
			File mediaStorageDir = new File(path);

			Bitmap myBitmap = BitmapFactory.decodeFile(mediaStorageDir
					.getPath() + "/" + Config.AvatarUser);
			if (myBitmap != null)
				imgAvatar.setImageDrawable(new BitmapDrawable(myBitmap));

		}
		
		if (connectionDetector.isConnectingToInternet()) {
			// bookID=2&row=0&content=&userID=58&type=none
			new LoadCommentAsycTack(this).execute(idBook, "0", "",
					Config.USER.idUser, mTypeBook);
		} else {
			if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

				showAlertDialog(ComicDetailActivity.this,
						"No Internet Connection",
						"Please, check your internet connection.");

			} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
				// TODO
				showAlertDialog(
						ComicDetailActivity.this,
						getResources().getString(
								R.string.ar_no_connect_internet),
						getResources()
								.getString(
										R.string.ar_plaese_check_your_internet_connecttion));
			}
		}

		// btnCommentDone.setBackground(Config.USER.drawableAvatar);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {
			// TODO
			btnStore.setBackgroundResource(R.drawable.en_ic_store);
			imgLogo.setImageResource(R.drawable.en_logo_comic_detail);
			btnMyComic.setBackgroundResource(R.drawable.en_ic_mycomic);
			btnSetting.setBackgroundResource(R.drawable.en_ic_setting);

			btnCommentDone.setText(R.string.post);
			btnDownload.setText(R.string.download);

		} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
			// TODO
			btnStore.setBackgroundResource(R.drawable.ar_ic_store);
			imgLogo.setImageResource(R.drawable.ar_logo_comic_detail);
			btnMyComic.setBackgroundResource(R.drawable.ar_ic_mycomic);
			btnSetting.setBackgroundResource(R.drawable.ar_ic_setting);
			btnDownload.setText(R.string.ar_download);
			btnCommentDone.setText(R.string.ar_post);

		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			mImageFetcher.setExitTasksEarly(true);
			mImageFetcher.flushCache();
			mImageFetcher.clearCache();
			mImageFetcher.closeCache();
		} catch (Exception e) {
		}
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		try {
			mImageFetcher.flushCache();
			mImageFetcher.clearCache();
		} catch (Exception e) {
		}
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		// //////////////////////////////////Test data
		// arrImageUrl.add("http://chipigroup.vacau.com/scrollimage1.jpg");
		// arrImageUrl.add("http://chipigroup.vacau.com/scrollimage1.jpg");
		// arrImageUrl.add("http://chipigroup.vacau.com/scrollimage1.jpg");
		// arrImageUrl.add("http://chipigroup.vacau.com/scrollimage1.jpg");
		// arrImageUrl.add("http://chipigroup.vacau.com/scrollimage1.jpg");
		// arrImageUrl.add("http://chipigroup.vacau.com/scrollimage1.jpg");
		// arrImageUrl.add("http://chipigroup.vacau.com/scrollimage1.jpg");
		// arrImageUrl.add("http://chipigroup.vacau.com/scrollimage1.jpg");
		// arrImageUrl.add("http://chipigroup.vacau.com/scrollimage1.jpg");
		// arrImageUrl.add("http://chipigroup.vacau.com/scrollimage1.jpg");
		// arrImageUrl.add("http://chipigroup.vacau.com/scrollimage1.jpg");

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

		hListView.setAdapter(new ComicDetailHListViewAdapter(
				ComicDetailActivity.this, arrImageUrl, mImageFetcher, hListView
						.getHeight(), hListView.getHeight()));
		hListView.setDividerWidth(hListView.getHeight() * 512 / 786 / 12);

	
		int[] colors = { 0, 0xF2569F, 0 }; // red for the example
		lvComment.setDivider(new GradientDrawable(Orientation.RIGHT_LEFT,
				colors));
		lvComment.setDividerHeight(1);
		adapter = new ComicDetailCommentListViewAdapter(
				ComicDetailActivity.this, arrComment, mImageFetcher);
		lvComment.setAdapter(adapter);

		mImageFetcher.loadImage("http://" + base_url + "/" + url + "/" + cover,
				imgComic);
		// showAlertDialog(ComicDetailActivity.this, "No Internet Connection",
		// "http://" + base_url + url + "/" + cover);
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

	private void resizeRecyclingImageView(
			RecyclingImageView resizeRecyclingImageView, int width, int height,
			int marginLeft, int marginTop, int marginRight, int marginBottom) {
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.height = height;
		llp.width = width;
		llp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
		resizeRecyclingImageView.setLayoutParams(llp);
	}

	private Drawable buildRatingBarDrawables(Bitmap[] images) {
		final int[] requiredIds = { android.R.id.background,
				android.R.id.secondaryProgress, android.R.id.progress };

		final float[] roundedCorners = new float[] { 5, 5, 5, 5, 5, 5, 5, 5 };
		Drawable[] pieces = new Drawable[3];
		for (int i = 0; i < 3; i++) {
			ShapeDrawable sd = new ShapeDrawable(new RoundRectShape(
					roundedCorners, null, null));
			BitmapShader bitmapShader = new BitmapShader(images[i],
					Shader.TileMode.REPEAT, Shader.TileMode.CLAMP);
			sd.getPaint().setShader(bitmapShader);
			ClipDrawable cd = new ClipDrawable(sd, Gravity.LEFT,
					ClipDrawable.HORIZONTAL);
			if (i == 0) {
				pieces[i] = sd;
			} else {
				pieces[i] = cd;
			}
		}
		LayerDrawable ld = new LayerDrawable(pieces);
		for (int i = 0; i < 3; i++) {
			ld.setId(i, requiredIds[i]);
		}
		return ld;
	}

	@Override
	public void onClick(View arg0) {

		InputMethodManager inputManager = (InputMethodManager) this
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(this.getCurrentFocus()
				.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

		int id = arg0.getId();
		switch (id) {
		case R.id.layout_comment:
			etComment.requestFocusFromTouch();
			etComment.setSelection(etComment.getText().length());
			InputMethodManager imm = (InputMethodManager) getApplicationContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.showSoftInput(etComment, InputMethodManager.SHOW_IMPLICIT);
			break;
		case R.id.btnStore:
			// TODO Auto-generated method stub
			Intent intentComicList = new Intent(getApplicationContext(),
					ComicListActivity.class);
			startActivity(intentComicList);
			ComicDetailActivity.this.finish();
			break;
		case R.id.btnMyComic:
			Intent myComic = new Intent(getApplicationContext(),
					MyComicActivity.class);
			startActivity(myComic);
			ComicDetailActivity.this.finish();
			break;
		case R.id.btnSetting:
			// TODO Auto-generated method stub
			Intent setting = new Intent(getApplicationContext(),
					SettingActivity.class);
			startActivity(setting);
			ComicDetailActivity.this.finish();
			break;
		case R.id.btnCommentDone:
			// TODO Auto-generated method stub
			if (connectionDetector.isConnectingToInternet()) {
				//arrComment = null;
			//	arrComment = new ArrayList<Comment>();
				 arrComment.clear();
				isShowDialog = true;
				isRunning = false;
				//adapter.notifyDataSetChanged();
				// loadMore();
				// Toast.makeText(this, Config.u, duration)
				Toast.makeText(this, etComment.getText(), Toast.LENGTH_LONG).show();
				LoadCommentAsycTack comment = new LoadCommentAsycTack(ComicDetailActivity.this);
				comment.execute(this.idBook, "0", etComment.getText().toString(),
						Config.IdUser, mTypeBook);
			} else {
				if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

					showAlertDialog(ComicDetailActivity.this,
							"No Internet Connection",
							"Please, check your internet connection.");

				} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
					// TODO
					showAlertDialog(
							ComicDetailActivity.this,
							getResources().getString(
									R.string.ar_no_connect_internet),
							getResources()
									.getString(
											R.string.ar_plaese_check_your_internet_connecttion));
				}
			}

			etComment.setText("");
			break;
			
			
		case R.id.btnDownload:
			// TODO Auto-generated method stub
			if(db.getBookInformationById(idBook).isEmpty()){
				if (Float.parseFloat(price) == 0.0) {
					this.sendInformationBookForDownload();
				} else {
					this.onBuyPressed();
				}
			}else{
				if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

					showAlertDialog(ComicDetailActivity.this,
							getResources().getString(
									R.string.notification),
							"You have downloaded it to My Comic!");

				} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
					// TODO
					showAlertDialog(
							ComicDetailActivity.this,
							getResources().getString(
									R.string.ar_notification),
							   			"لقد نزلت القصة مسبقأً");
				}
			}
		
			break;

		default:
			break;
		}

	}

	// //**************************************************************************

	void loadMore() {
		lvComment.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				// TODO Auto-generated method stub
				

				if (firstVisibleItem + visibleItemCount >= totalItemCount) {
					if (connectionDetector.isConnectingToInternet()) {
						if (isRunning) {
							isRunning = false;
							if (arrComment.size() < Count) {
								
								//Toast.makeText(ComicDetailActivity.this, "sss"+String.valueOf(arrComment.size()),
									//	Toast.LENGTH_LONG).show();
								tvLoadMore.setText("...");
								tvLoadMore.setVisibility(View.VISIBLE);
								// bookID=2&row=0&content=&userID=58&type=none
								new LoadCommentAsycTack(
										ComicDetailActivity.this).execute(idBook,
										String.valueOf(arrComment.size()), "",
										Config.IdUser, mTypeBook);
							} else {
								tvLoadMore.setText("");
								tvLoadMore.setVisibility(View.GONE);
							}
						}
					}
				} else {
					 tvLoadMore.setText("");
					 tvLoadMore.setVisibility(View.GONE);
					
				}
			}
		});
	}

	class RatingAsycTack extends AsyncTask<String, Void, Void> {

		Activity activity;
		ProgressDialog dialog;

		public RatingAsycTack(Activity activity) {
			// TODO Auto-generated constructor stub

			this.activity = activity;
			this.dialog = new ProgressDialog(activity);
		}

		// run UI before
		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			dialog.setMessage("....");
			dialog.show();

		}

		// run UI alter
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (dialog.isShowing())
				dialog.dismiss();

			// Toast.makeText(activity, String.valueOf(arrClubComments.size()),
			// Toast.LENGTH_LONG).show();
		}

		@Override
		protected Void doInBackground(String... params) {

			ArrayList<NameValuePair> arrNameValuePairs = new ArrayList<NameValuePair>();
			arrNameValuePairs.add(new BasicNameValuePair("bookID", params[0]));
			arrNameValuePairs.add(new BasicNameValuePair("userID", params[1]));
			arrNameValuePairs.add(new BasicNameValuePair("num", params[2]));
			arrNameValuePairs.add(new BasicNameValuePair("type", params[3]));
			JSONParser jsonParser = new JSONParser();

			String string = jsonParser.makeHttpRequest(Config.API_POST_RATING,
					arrNameValuePairs);

			runOnUiThread(new Runnable() {
				public void run() {

					if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

						// Toast.makeText(activity, "succefully",
						// Toast.LENGTH_LONG).show();
					} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
						// TODO
						String d = getResources().getString(
								R.string.ar_succefully);
						// Toast.makeText(activity, d,
						// Toast.LENGTH_LONG).show();
					}

				}
			});

			return null;
		}

	}

	
	/**
	 * 
	 * @author Thang
	 * 
	 */
	class LoadCommentAsycTack extends AsyncTask<String, Void, Void> {

		Activity activity;
		ProgressDialog dialog;

		public LoadCommentAsycTack(Activity activity) {
			// TODO Auto-generated constructor stub

			this.activity = activity;
			this.dialog = new ProgressDialog(activity);
			
		}

		// run UI before
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (isShowDialog) {
				dialog.setMessage("....");
				dialog.show();
			}
		}

		// run UI alter
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (dialog.isShowing())
				dialog.dismiss();
			isRunning = true;
			isShowDialog = false;
			adapter.notifyDataSetChanged();
			
		Log.i("aaaaaaaa",	arrComment.toString());
		
		}

		@Override
		protected Void doInBackground(String... params) {
			// bookID=2&row=0&content=&userID=58&type=none
			// bookID=2&row=0&content=&userID=58&type=none

			ArrayList<NameValuePair> arrNameValuePairs = new ArrayList<NameValuePair>();
			arrNameValuePairs.add(new BasicNameValuePair("bookID", params[0]));
			arrNameValuePairs.add(new BasicNameValuePair("row", params[1]));
			arrNameValuePairs.add(new BasicNameValuePair("content", params[2]));
			arrNameValuePairs.add(new BasicNameValuePair("userID", params[3]));
			arrNameValuePairs.add(new BasicNameValuePair("type", params[4]));

			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = jsonParser.makeHttpRequest(
					Config.API_COMMENT, "POST", arrNameValuePairs);
			try {
				JSONArray jsonArray = jsonObject.getJSONArray("result");
				getListComment(jsonArray);
				Log.d("ssssssssssss", jsonObject.toString());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return null;
		}

	}

	public void getListComment(JSONArray jsonArray) {
		try {
			// arrComment.clear();
			for (int i = 0; i < jsonArray.length(); i++) {

				JSONObject item = jsonArray.getJSONObject(i);
				Count = item.getInt("count");

				JSONArray jsonArrayComment = item
						.getJSONArray("result_commnent");
				for (int j = 0; j < jsonArrayComment.length(); j++) {
					JSONObject itemComment = jsonArrayComment.getJSONObject(j);
					Comment comment = new Comment();
					comment.urlAvatar = itemComment.getString("avatar");
					comment.userName = itemComment.getString("username");
					comment.createDate = itemComment.getString("createdate");
					comment.content = itemComment.getString("content");
					arrComment.add(comment);
					// Log.d("1222", )
				}

			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public void showAlertInternet(Activity act, final float rating) {
		final AlertDialog alertDialog = new AlertDialog.Builder(act).create();

		String cancel = "Cancel";
		String ok = "OK";
		if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {
			alertDialog.setMessage("Do you want rating this book?");
			cancel = "Cancel";
			ok = "OK";
		} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
			alertDialog.setMessage(getResources().getString(
					R.string.ar_do_you_want_rating_this_book));
			cancel = getResources().getString(R.string.ar_cancel);
			ok = getResources().getString(R.string.ar_ok);
			// TODO
		}
		// alertDialog.setMessage("Do you want rating this book?");

		// Setting OK Button
		alertDialog.setButton(ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				if (connectionDetector.isConnectingToInternet()) {

					new RatingAsycTack(ComicDetailActivity.this).execute(idBook,
							Config.USER.idUser, String.valueOf(rating),
							mTypeBook);
				} else {
					if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

						showAlertDialog(ComicDetailActivity.this,
								"No Internet Connection",
								"Please, check your internet connection.");

					} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
						// TODO
						showAlertDialog(
								ComicDetailActivity.this,
								getResources().getString(
										R.string.ar_no_connect_internet),
								getResources()
										.getString(
												R.string.ar_plaese_check_your_internet_connecttion));
					}
				}
				alertDialog.dismiss();
			}
		});

		alertDialog.setButton2(cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				alertDialog.dismiss();
			}
		});

		alertDialog.show();

	}

	@SuppressWarnings("deprecation")
	private void showAlertDialog(Context context, String title, String message) {
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
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
			}
		});
		// Showing Alert Message
		alertDialog.show();
	}

	/*
	 * send book information to Paypal
	 */
	public void onBuyPressed() {
		String subtotal = null;
		String itemID = null;
		String itemName = null;
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			subtotal = price;
			itemID = idBook;
			itemName = title;
		}
		PayPalPayment thingToBuy = new PayPalPayment(new BigDecimal(subtotal),
				"USD", itemName);

		Intent intent = new Intent(this, PaymentActivity.class);
		
		intent.putExtra(PaymentActivity.EXTRA_PAYPAL_ENVIRONMENT,
				CONFIG_ENVIRONMENT);
		intent.putExtra(PaymentActivity.EXTRA_CLIENT_ID, CONFIG_CLIENT_ID);
		intent.putExtra(PaymentActivity.EXTRA_RECEIVER_EMAIL,
				CONFIG_RECEIVER_EMAIL);

		// It's important to repeat the clientId here so that the SDK has it if
		// Android restarts your
		// app midway through the payment UI flow.
		intent.putExtra(PaymentActivity.EXTRA_CLIENT_ID,
				"credential-from-developer.paypal.com");
		intent.putExtra(PaymentActivity.EXTRA_PAYER_ID,
				"your-customer-id-in-your-system");
		intent.putExtra(PaymentActivity.EXTRA_PAYMENT, thingToBuy);

		startActivityForResult(intent, 0);
	}

	/*
	 * received infor from paypal server
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == 0) {
				PaymentConfirmation confirm = data
						.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
				if (confirm != null) {
					try {
						Log.i("paymentExample", confirm.toJSONObject()
								.toString(4));
						// TODO: send 'confirm' to your server for verification.
						// see
						// https://developer.paypal.com/webapps/developer/docs/integration/mobile/verify-mobile-payment/
						// for more details.
						this.sendInformationBookForDownload();

					} catch (JSONException e) {
						Log.e("paymentExample",
								"an extremely unlikely failure occurred: ", e);
					}
				}

			}
		} else if (resultCode == Activity.RESULT_CANCELED) {
			Log.i("paymentExample", "The user canceled.");
		} else if (resultCode == PaymentActivity.RESULT_PAYMENT_INVALID) {
			Log.i("paymentExample",
					"An invalid payment was submitted. Please see the docs.");
		}
	}

	protected void sendInformationBookForDownload() {
		Intent intent = new Intent(getApplicationContext(),
				MyComicActivity.class);
		intent.putExtra("isfancomicbook", isFancomicBook);
		intent.putExtra("status", status);
		intent.putExtra("error", error);
		intent.putExtra("records", records);
		intent.putExtra("base_url", base_url);
		intent.putExtra("id", ComicDetailActivity.this.idBook);
		intent.putExtra("title", title);
		intent.putExtra("description", description);
		intent.putExtra("series", series);
		intent.putExtra("cover", cover);
		intent.putExtra("databook", databook);
		intent.putExtra("price", price);
		intent.putExtra("author", author);
		intent.putExtra("rating", rating);
		intent.putExtra("type", type);
		intent.putExtra("date_create", date_create);
		intent.putExtra("language", language);
		intent.putExtra("url", url);
		intent.putExtra("target", target);
		startActivity(intent);
		ComicDetailActivity.this.finish();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		InputMethodManager inputManager = (InputMethodManager) this
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(this.getCurrentFocus()
				.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		return true;
	}

}
