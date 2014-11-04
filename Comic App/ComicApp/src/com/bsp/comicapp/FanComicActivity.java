package com.bsp.comicapp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.sephiroth.android.library.widget.HListView;

import com.bsp.comicapp.adapter.FanComicGridViewAdapter;
import com.bsp.comicapp.adapter.ShareComicDialogHListViewAdapter;
import com.bsp.comicapp.displayimage.util.ImageFetcher;
import com.bsp.comicapp.displayimage.util.RecyclingImageView;
import com.bsp.comicapp.displayimage.util.ImageCache.ImageCacheParams;
import com.bsp.comicapp.util.Config;
import com.bsp.comicapp.util.ConnectionDetector;
import com.bsp.comicapp.util.JSONParser;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.RelativeLayout.LayoutParams;

public class FanComicActivity extends FragmentActivity implements
		OnClickListener {
	private Button btnStore;
	private ImageView imgLogo;
	private Button btnMyComic;
	private LinearLayout layout_content;
	private GridView gvFanComic;
	private Button btnSetting;
	private Button btnShareComic;

	// variable for share comic dialog
	private Dialog shareComicDialog;
	private RelativeLayout layout_share_comic_dialog;
	private LinearLayout layout_share_comic_content;
	private RecyclingImageView imgCoverComic;
	private ToggleButton languageToggleButton;
	private TextView txtTitle;
	private EditText etTitle;
	private TextView txtDescription;
	private LinearLayout layout_description;
	private EditText etDescription;
	private LinearLayout layout_divider;
	private RecyclingImageView imgAddImage;
	private LinearLayout layout_hlistview_parent;
	private HListView hListView;
	private Button btnPostInfoBook;
	private Button btnDelete;
	private Button btnPostImageBook;
	private Button btnClose;
	private ImageView imgDescriptionExclamation;
	private ImageView imgTitleExclamation;
	private boolean isOnDeletePhoto = false;

	private String lang = Config.ENGLISH_LANGUAGUE;
	private SharedPreferences languagePreferences;

	private CreateComicAsyncTask createComicAsyncTask = null;
	private UploadPhotoToServerAsyncTask uploadPhotoToServerAsyncTask = null;
	private String langUpload = "0";
	private static final int SELECT_COVER_PHOTO_BOOK = 1;
	private static final int SELECT_PHOTOS_OF_BOOK = 2;
	private static final int EDIT_PHOTOS_OF_BOOK = 3;
	private int editPhotoPosition = -1;
	private String selectedImagePath;
	private String filemanagerstring;
	private String coverPhotoBookPath = null;
	private ArrayList<String> arrayPhotosOfBook = new ArrayList<String>();
	private ShareComicDialogHListViewAdapter adapter;
	private String createComicStatus = "";
	private String uploadBookId = "";

	private ConnectionDetector cd;
	private boolean isInternetPresent = false;

	ArrayList<String> arrUrlImage = new ArrayList<String>();

	// Creating JSON Parser object
	private JSONParser jsonParser;
	private JSONObject json = null;
	private ArrayList<HashMap<String, String>> arrayBookInformation = new ArrayList<HashMap<String, String>>();
	private int status = -1;
	private String error = "";
	private int records = -1;
	private String base_url = "";
	private JSONArray data = null;

	private LoadDataAsyncTask loadDataAsyncTask = null;
	private String IMAGE_CACHE_DIR = "thumbs";
	private ImageFetcher mImageFetcher;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_fancomic);

		languagePreferences = getSharedPreferences(
				Config.LANGUAGE_PREFERENCE_NAME, MODE_PRIVATE);
		lang = languagePreferences.getString(Config.PREFERENCE_KEY_LANGUAGE,
				Config.ENGLISH_LANGUAGUE);

		ImageView imageView = (ImageView) findViewById(R.id.imgBackground);
		if (Config.Bg_Portrait != null)
			imageView.setBackgroundDrawable(Config.Bg_Portrait);
		// Display image
		ImageCacheParams cacheParams = new ImageCacheParams(
				getApplicationContext(), IMAGE_CACHE_DIR);
		cacheParams.setMemCacheSizePercent(0.1f); // Set memory cache to
													// 25% of
													// app memory
		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		mImageFetcher = new ImageFetcher(getApplicationContext(),
				(int) (Config.screenHeight * 0.9 * 0.21),
				(int) (Config.screenHeight * 0.9 * 0.21 * 786 / 512));
		mImageFetcher.setLoadingImage(R.drawable.gallery_image_load);
		mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);

		btnStore = (Button) findViewById(R.id.btnStore);
		imgLogo = (ImageView) findViewById(R.id.imgLogo);
		btnMyComic = (Button) findViewById(R.id.btnMyComic);
		layout_content = (LinearLayout) findViewById(R.id.layout_content);
		gvFanComic = (GridView) findViewById(R.id.gvFanComic);
		btnSetting = (Button) findViewById(R.id.btnSetting);
		btnShareComic = (Button) findViewById(R.id.btnShareComic);

		// resize item
		layout_content.setPadding((int) (Config.screenHeight * 0.05),
				(int) (Config.screenHeight * 0.02),
				(int) (Config.screenHeight * 0.05), 0);
		gvFanComic
				.setVerticalSpacing((int) ((int) (Config.screenHeight * 0.9) * 0.02));

		resizeLogo(imgLogo, 0, (int) (Config.screenHeight * 0.075 * 150 / 175),
				0, 0, 0, 0);
		resizeView(btnMyComic, (int) (Config.screenHeight * 0.1),
				(int) (Config.screenHeight * 0.1 * 150 / 175), 0, 0, 0, 0);
		resizeView(btnStore, (int) (Config.screenHeight * 0.1),
				(int) (Config.screenHeight * 0.1 * 150 / 175), 0, 0, 0, 0);
		resizeView(btnSetting, (int) (Config.screenHeight * 0.15),
				(int) (Config.screenHeight * 0.15 * 69 / 182),
				(int) (Config.screenHeight * 0.01 * 69 / 182),
				(int) (Config.screenHeight * 0.01 * 69 / 182), 0,
				(int) (Config.screenHeight * 0.01 * 69 / 182));
		resizeView(btnShareComic, (int) (Config.screenHeight * 0.15),
				(int) (Config.screenHeight * 0.15 * 66 / 152),
				(int) (Config.screenHeight * 0.01 * 66 / 152),
				(int) (Config.screenHeight * 0.01 * 66 / 152), 0,
				(int) (Config.screenHeight * 0.01 * 66 / 152));

		btnStore.setOnClickListener(this);
		btnMyComic.setOnClickListener(this);
		btnSetting.setOnClickListener(this);
		btnShareComic.setOnClickListener(this);

		loadDataAsyncTask = new LoadDataAsyncTask();
		loadDataAsyncTask.execute();
		init();

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {
			// TODO
			btnStore.setBackgroundResource(R.drawable.en_ic_store);
			imgLogo.setImageResource(R.drawable.en_logo_fan_comic);
			btnMyComic.setBackgroundResource(R.drawable.en_ic_mycomic);
			btnSetting.setBackgroundResource(R.drawable.en_ic_setting);
			btnShareComic.setBackgroundResource(R.drawable.en_ic_share_comic);

		} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
			// TODO
			btnStore.setBackgroundResource(R.drawable.ar_ic_store);
			imgLogo.setImageResource(R.drawable.ar_logo_comic_detail);
			btnMyComic.setBackgroundResource(R.drawable.ar_ic_mycomic);
			btnSetting.setBackgroundResource(R.drawable.ar_ic_setting);
			btnShareComic.setBackgroundResource(R.drawable.ar_ic_share_comic);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (createComicAsyncTask != null) {
			createComicAsyncTask.cancel(true);
		}
		if (loadDataAsyncTask != null) {
			loadDataAsyncTask.cancel(true);
		}
		if (uploadPhotoToServerAsyncTask != null) {
			uploadPhotoToServerAsyncTask.cancel(true);
		}

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
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, requestCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_COVER_PHOTO_BOOK) {
				Uri selectedImageUri = data.getData();

				// OI FILE Manager
				filemanagerstring = selectedImageUri.getPath();
				// MEDIA GALLERY
				selectedImagePath = getPath(selectedImageUri);

				// NOW WE HAVE OUR WANTED STRING
				if (selectedImagePath != null) {
					// Image path = selectedImagePath
					coverPhotoBookPath = selectedImagePath;
					if (imgCoverComic != null) {
						loadBitmap(
								imgCoverComic,
								selectedImagePath,
								(int) (Config.screenHeight * 0.3 * 0.95),
								(int) (Config.screenHeight * 0.3 * 0.95 * 382 / 271));
					}

				} else {
					// Image path = filemanagerstring
					coverPhotoBookPath = filemanagerstring;
					if (imgCoverComic != null) {
						loadBitmap(
								imgCoverComic,
								filemanagerstring,
								(int) (Config.screenHeight * 0.3 * 0.95),
								(int) (Config.screenHeight * 0.3 * 0.95 * 382 / 271));
					}
				}
			} else if (requestCode == SELECT_PHOTOS_OF_BOOK) {
				Uri selectedImageUri = data.getData();

				// OI FILE Manager
				filemanagerstring = selectedImageUri.getPath();
				// MEDIA GALLERY
				selectedImagePath = getPath(selectedImageUri);
				// NOW WE HAVE OUR WANTED STRING
				if (selectedImagePath != null) {
					// Image path = selectedImagePath
					arrayPhotosOfBook.add(selectedImagePath);
				} else {
					// Image path = filemanagerstring
					arrayPhotosOfBook.add(filemanagerstring);
				}
				adapter = new ShareComicDialogHListViewAdapter(
						FanComicActivity.this, arrayPhotosOfBook,
						(int) (Config.screenHeight * 0.23 * 0.95),
						(int) (Config.screenHeight * 0.23 * 0.95 * 382 / 271));
				hListView.setAdapter(adapter);

			} else if (requestCode == EDIT_PHOTOS_OF_BOOK) {
				Uri selectedImageUri = data.getData();

				// OI FILE Manager
				filemanagerstring = selectedImageUri.getPath();
				// MEDIA GALLERY
				selectedImagePath = getPath(selectedImageUri);
				// NOW WE HAVE OUR WANTED STRING
				if (selectedImagePath != null) {
					arrayPhotosOfBook.remove(editPhotoPosition);
					arrayPhotosOfBook.add(editPhotoPosition, selectedImagePath);
				} else {
					arrayPhotosOfBook.remove(editPhotoPosition);
					arrayPhotosOfBook.add(editPhotoPosition, filemanagerstring);
				}
				adapter = new ShareComicDialogHListViewAdapter(
						FanComicActivity.this, arrayPhotosOfBook,
						(int) (Config.screenHeight * 0.23 * 0.95),
						(int) (Config.screenHeight * 0.23 * 0.95 * 382 / 271));
				hListView.setAdapter(adapter);

			}
		}
	}

	private void resizeLogo(View view, int width, int height, int marginLeft,
			int marginTop, int marginRight, int marginBottom) {
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.height = height;
		llp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
		llp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
		view.setLayoutParams(llp);
	}

	private void resizeView(View view, int width, int height, int marginLeft,
			int marginTop, int marginRight, int marginBottom) {
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.height = height;
		llp.width = width;
		llp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
		view.setLayoutParams(llp);
	}

	private void resizeViewWidthMatchParent(View view, int width, int height,
			int marginLeft, int marginTop, int marginRight, int marginBottom) {
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.height = height;
		llp.width = LinearLayout.LayoutParams.MATCH_PARENT;
		llp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
		view.setLayoutParams(llp);
	}

	private void selectCoverPhotoBook() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, "Select Picture"),
				SELECT_COVER_PHOTO_BOOK);
	}

	private void selectPhotosOfBook() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, "Select Picture"),
				SELECT_PHOTOS_OF_BOOK);
	}

	public void setEditPhotoPosition(int position) {
		this.editPhotoPosition = position;
	}

	public void deletePhotoAtPosition(int position) {
		arrayPhotosOfBook.remove(position);
		adapter = new ShareComicDialogHListViewAdapter(FanComicActivity.this,
				arrayPhotosOfBook, (int) (Config.screenHeight * 0.23 * 0.95),
				(int) (Config.screenHeight * 0.23 * 0.95 * 382 / 271));
		hListView.setAdapter(adapter);

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
			try {
				return Bitmap.createScaledBitmap(
						BitmapFactory.decodeFile(imagePath), reqWidth,
						reqHeight, true);// width + height of
											// image
			} catch (Exception e) {
				return null;
			}

		}

		// Once complete, see if ImageView is still around and set bitmap.
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				if (imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
	}

	public String getPath(Uri uri) {
		String[] projection = { MediaStore.Images.Media.DATA };
		@SuppressWarnings("deprecation")
		Cursor cursor = FanComicActivity.this.managedQuery(uri, projection,
				null, null, null);
		if (cursor != null) {
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		} else
			return null;
	}

	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();
		switch (id) {
		case R.id.btnMyComic:
			Intent readerComic = new Intent(getApplicationContext(),
					FanComicActivity.class);
			startActivity(readerComic);
			FanComicActivity.this.finish();
			break;

		case R.id.btnStore:
			Intent store = new Intent(getApplicationContext(),
					ComicListActivity.class);
			startActivity(store);
			FanComicActivity.this.finish();
			break;
		case R.id.btnSetting:
			Intent setting = new Intent(getApplicationContext(),
					SettingActivity.class);
			startActivity(setting);
			FanComicActivity.this.finish();
			break;

		case R.id.btnShareComic:
			arrayPhotosOfBook = new ArrayList<String>();
			shareComicDialog = new Dialog(FanComicActivity.this,
					R.style.DialogSlideAnim);
			shareComicDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			shareComicDialog.setContentView(R.layout.dialog_share_comic);

			layout_share_comic_dialog = (RelativeLayout) shareComicDialog
					.findViewById(R.id.layout_share_comic_dialog);
			layout_share_comic_content = (LinearLayout) shareComicDialog
					.findViewById(R.id.layout_share_comic_content);
			imgCoverComic = (RecyclingImageView) shareComicDialog
					.findViewById(R.id.imgCoverComic);
			languageToggleButton = (ToggleButton) shareComicDialog
					.findViewById(R.id.languageToggleButton);
			txtTitle = (TextView) shareComicDialog.findViewById(R.id.txtTitle);
			etTitle = (EditText) shareComicDialog.findViewById(R.id.etTitle);
			imgTitleExclamation = (ImageView) shareComicDialog
					.findViewById(R.id.imgTitleExclamation);
			txtDescription = (TextView) shareComicDialog
					.findViewById(R.id.txtDescription);
			layout_description = (LinearLayout) shareComicDialog
					.findViewById(R.id.layout_description);
			etDescription = (EditText) shareComicDialog
					.findViewById(R.id.etDescription);
			imgDescriptionExclamation = (ImageView) shareComicDialog
					.findViewById(R.id.imgDescriptionExclamation);
			layout_divider = (LinearLayout) shareComicDialog
					.findViewById(R.id.layout_divider);
			imgAddImage = (RecyclingImageView) shareComicDialog
					.findViewById(R.id.imgAddImage);
			layout_hlistview_parent = (LinearLayout) shareComicDialog
					.findViewById(R.id.layout_hlistview_parent);
			hListView = (HListView) shareComicDialog
					.findViewById(R.id.hListView);
			btnPostInfoBook = (Button) shareComicDialog
					.findViewById(R.id.btnPostInfoBook);
			btnDelete = (Button) shareComicDialog.findViewById(R.id.btnDelete);
			btnPostImageBook = (Button) shareComicDialog
					.findViewById(R.id.btnPostImageBook);
			btnClose = (Button) shareComicDialog.findViewById(R.id.btnClose);

			// TODO
			if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {
				languageToggleButton
						.setBackgroundResource(R.drawable.bgtoggle_english);
				btnDelete.setBackgroundResource(R.drawable.en_ic_delete);
				btnPostImageBook
						.setBackgroundResource(R.drawable.en_ic_post_image_book);
				btnPostInfoBook
						.setBackgroundResource(R.drawable.en_ic_post_info_book);
				txtTitle.setText(R.string.title);
				txtDescription.setText(R.string.description);

			} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
				languageToggleButton
						.setBackgroundResource(R.drawable.bgtoggle_arabic);
				btnDelete.setBackgroundResource(R.drawable.ar_ic_delete);
				btnPostImageBook
						.setBackgroundResource(R.drawable.ar_ic_post_image_book);
				btnPostInfoBook
						.setBackgroundResource(R.drawable.ar_ic_post_info_book);
				txtTitle.setText(R.string.ar_title);
				txtDescription.setText(R.string.ar_description);
			}

			// resize item
			float textsize = (float) (Config.screenHeight * 0.031);
			txtTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
			etTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
			txtDescription.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
			etDescription.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);

			layout_share_comic_dialog.getLayoutParams().width = (int) (Config.screenHeight * 0.95);
			// layout_share_comic_dialog.getLayoutParams().height =
			// LinearLayout.LayoutParams.WRAP_CONTENT;

			layout_share_comic_content.setPadding(
					(int) (Config.screenHeight * 0.02 * 0.95),
					(int) (Config.screenHeight * 0.02 * 0.95),
					(int) (Config.screenHeight * 0.02 * 0.95),
					(int) (Config.screenHeight * 0.02 * 0.95));
			layout_hlistview_parent.setPadding(
					(int) (Config.screenHeight * 0.02 * 0.95), 0,
					(int) (Config.screenHeight * 0.02 * 0.95), 0);
			layout_divider.getLayoutParams().height = (int) (Config.screenWidth * 0.02);

			resizeView(btnClose, (int) (Config.screenHeight * 0.07),
					(int) (Config.screenHeight * 0.07), 0, 0, 0, 0);
			resizeView(imgCoverComic, (int) (Config.screenHeight * 0.3 * 0.95),
					(int) (Config.screenHeight * 0.3 * 0.95 * 382 / 271), 0, 0,
					(int) (Config.screenHeight * 0.03 * 0.95), 0);
			resizeView(languageToggleButton,
					(int) (Config.screenHeight * 0.23 * 0.95),
					(int) (Config.screenHeight * 0.23 * 0.95 * 29 / 123), 0, 0,
					0, 0);
			resizeView(imgAddImage, (int) (Config.screenHeight * 0.23 * 0.95),
					(int) (Config.screenHeight * 0.23 * 0.95 * 382 / 271), 0,
					0, 0, 0);
			resizeViewWidthMatchParent(hListView, 0,
					(int) (Config.screenHeight * 0.23 * 0.95 * 382 / 271), 0,
					0, 0, 0);
			resizeView(btnPostInfoBook,
					(int) (Config.screenHeight * 0.2 * 0.95),
					(int) (Config.screenHeight * 0.2 * 0.95 * 59 / 290), 0, 0,
					0, 0);
			resizeView(btnDelete, (int) (Config.screenHeight * 0.2 * 0.95),
					(int) (Config.screenHeight * 0.2 * 0.95 * 59 / 290), 0, 0,
					0, (int) (Config.screenWidth * 0.01));
			resizeView(btnPostImageBook,
					(int) (Config.screenHeight * 0.2 * 0.95),
					(int) (Config.screenHeight * 0.2 * 0.95 * 59 / 290), 0, 0,
					0, 0);

			hListView
					.setDividerWidth((int) (Config.screenHeight * 0.02 * 0.95));
			adapter = new ShareComicDialogHListViewAdapter(
					FanComicActivity.this, arrayPhotosOfBook,
					(int) (Config.screenHeight * 0.23 * 0.95),
					(int) (Config.screenHeight * 0.23 * 0.95 * 382 / 271));
			hListView.setAdapter(adapter);

			// set onclick event
			layout_description.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					etDescription.requestFocusFromTouch();
					etDescription
							.setSelection(etDescription.getText().length());
					InputMethodManager imm = (InputMethodManager) getApplicationContext()
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.showSoftInput(etDescription,
							InputMethodManager.SHOW_IMPLICIT);
				}
			});

			btnClose.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					shareComicDialog.dismiss();
					if (!uploadBookId.equals("") && !uploadBookId.equals(" ")) {
						Toast.makeText(FanComicActivity.this, uploadBookId,
								Toast.LENGTH_LONG).show();
						new DeteleBookAsyncTask().execute(uploadBookId.trim());
					}
				}
			});

			btnPostInfoBook.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (imgDescriptionExclamation != null
							&& imgTitleExclamation != null) {
						imgDescriptionExclamation.setVisibility(View.INVISIBLE);
						imgTitleExclamation.setVisibility(View.INVISIBLE);
					}
					if (etTitle.getText().toString().trim()
							.equalsIgnoreCase("")
							|| etDescription.getText().toString().trim()
									.equalsIgnoreCase("")) {
						if (etTitle.getText().toString().trim()
								.equalsIgnoreCase("")) {
							imgTitleExclamation.setVisibility(View.VISIBLE);

						}
						if (etDescription.getText().toString().trim()
								.equalsIgnoreCase("")) {
							imgDescriptionExclamation
									.setVisibility(View.VISIBLE);
						}

					} else {
						createComicAsyncTask = new CreateComicAsyncTask();
						createComicAsyncTask
								.executeOnExecutor(MainActivity.pool);
					}
				}
			});
			imgCoverComic.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					selectCoverPhotoBook();
				}
			});
			imgAddImage.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					selectPhotosOfBook();

				}
			});
			btnDelete.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (isOnDeletePhoto == true) {
						isOnDeletePhoto = false;
						for (int i = 0; i < arrayPhotosOfBook.size(); i++) {
							View vi = hListView.getChildAt(i);
							Button btnDeletePhoto = (Button) vi
									.findViewById(R.id.btnDeletePhoto);
							btnDeletePhoto.setVisibility(View.INVISIBLE);
						}
					} else {
						isOnDeletePhoto = true;
						for (int i = 0; i < arrayPhotosOfBook.size(); i++) {
							View vi = hListView.getChildAt(i);
							Button btnDeletePhoto = (Button) vi
									.findViewById(R.id.btnDeletePhoto);
							btnDeletePhoto.setVisibility(View.VISIBLE);
						}
					}

				}
			});
			btnPostImageBook.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (coverPhotoBookPath != null
							&& arrayPhotosOfBook.size() > 0) {
						uploadPhotoToServerAsyncTask = new UploadPhotoToServerAsyncTask();
						uploadPhotoToServerAsyncTask.execute();

					} else {
						String message = "";
						if (coverPhotoBookPath == null) {
							message = "You forgot to pick the book cover";
						}
						if (arrayPhotosOfBook.size() == 0) {
							message = "You forgot to pick book's photos content";
						}
						if (coverPhotoBookPath == null
								&& arrayPhotosOfBook.size() == 0) {
							message = "You forgot to pick the book cover and book's photos content";
						}
						//showAlertDialog("Information", message);
					}
				}
			});

			languageToggleButton
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {
						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							if (isChecked == true) {
								langUpload = "1";
							} else {
								langUpload = "0";
							}
						}
					});

			// disable some button
			btnPostImageBook.setAlpha(0.5f);
			btnDelete.setAlpha(0.5f);
			imgCoverComic.setAlpha(0.5f);
			imgAddImage.setAlpha(0.5f);
			btnPostImageBook.setEnabled(false);
			btnDelete.setEnabled(false);
			imgCoverComic.setEnabled(false);
			imgAddImage.setEnabled(false);

			arrayPhotosOfBook = new ArrayList<String>();
			shareComicDialog.setCanceledOnTouchOutside(false);
			shareComicDialog.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			shareComicDialog.show();

			break;
		default:
			break;
		}

	}

	private class LoadDataAsyncTask extends AsyncTask<String, Void, String> {
		private boolean isParseJSONError = false;

		@Override
		protected String doInBackground(String... params) {
			String url_get_upload_book = Config.GETUPLOADBOOK_URL;
			json = null;
			json = jsonParser.getJSONFromUrl(url_get_upload_book);
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
						arrayBookInformation.add(bookInformation);
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
				if (arrayBookInformation.size() > 0) {
					init();
					gvFanComic
							.setAdapter(new FanComicGridViewAdapter(
									FanComicActivity.this,
									status,
									error,
									records,
									base_url,
									arrayBookInformation,
									mImageFetcher,
									(int) (Config.screenHeight * 0.9 * 0.21),
									(int) (Config.screenHeight * 0.9 * 0.21 * 786 / 512)));
					gvFanComic
							.setOnItemClickListener(new OnItemClickListener() {
								@Override
								public void onItemClick(AdapterView<?> arg0,
										View arg1, int position, long arg3) {
									Intent intent = new Intent(
											getApplicationContext(),
											ComicDetailActivity.class);
									intent.putExtra("isfancomicbook", true);
									intent.putExtra("status",
											String.valueOf(status));
									intent.putExtra("error", error);
									intent.putExtra("records",
											String.valueOf(records));
									intent.putExtra("base_url", base_url);
									intent.putExtra("id", arrayBookInformation
											.get(position).get("id"));
									intent.putExtra("title",
											arrayBookInformation.get(position)
													.get("title"));
									intent.putExtra("description",
											arrayBookInformation.get(position)
													.get("description"));
									intent.putExtra("series",
											arrayBookInformation.get(position)
													.get("series"));
									intent.putExtra("cover",
											arrayBookInformation.get(position)
													.get("cover"));
									intent.putExtra("databook",
											arrayBookInformation.get(position)
													.get("databook"));
									intent.putExtra("price",
											arrayBookInformation.get(position)
													.get("price"));
									intent.putExtra("author",
											arrayBookInformation.get(position)
													.get("author"));
									intent.putExtra("rating",
											arrayBookInformation.get(position)
													.get("rating"));
									intent.putExtra("type",
											arrayBookInformation.get(position)
													.get("type"));
									intent.putExtra("date_create",
											arrayBookInformation.get(position)
													.get("date_create"));
									intent.putExtra("language",
											arrayBookInformation.get(position)
													.get("language"));
									intent.putExtra("url", arrayBookInformation
											.get(position).get("url"));
									intent.putExtra("target",
											arrayBookInformation.get(position)
													.get("target"));
									intent.putStringArrayListExtra("urlImage",
											arrUrlImage);
									FanComicActivity.this.startActivity(intent);
									FanComicActivity.this.finish();

								}
							});

				}
			}
		}

		@Override
		protected void onPreExecute() {
			isParseJSONError = false;
			jsonParser = new JSONParser();
			arrayBookInformation.clear();
			arrayBookInformation = new ArrayList<HashMap<String, String>>();
		}
	}

	class CreateComicAsyncTask extends AsyncTask<String, String, String> {
		private String responeString = "";
		private ProgressDialog pDialog = null;
		private int responseCode = 0;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(FanComicActivity.this);
			if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {
				pDialog.setMessage("Please wait...");
			} else {
				pDialog.setMessage(getResources().getString(
						R.string.ar_please_wait));
			}
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		protected String doInBackground(String... args) {
			String title = etTitle.getText().toString();
			String description = etDescription.getText().toString();
			// TODO
			String user_id = Config.IdUser;

			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("title", title));
			params.add(new BasicNameValuePair("lang", langUpload));
			params.add(new BasicNameValuePair("des", description));
			params.add(new BasicNameValuePair("userID", user_id));

			try {
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(Config.CREATE_FAN_COMIC);
				httpPost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
				HttpConnectionParams.setConnectionTimeout(
						httpClient.getParams(), Config.TIME_OUT_CONNECTION);
				HttpConnectionParams.setSoTimeout(httpClient.getParams(),
						Config.TIME_OUT_GET_DATA);
				HttpResponse httpResponse = httpClient.execute(httpPost);
				responseCode = httpResponse.getStatusLine().getStatusCode();

				HttpEntity entity = httpResponse.getEntity();

				if (entity != null) {
					try {
						BufferedReader r = new BufferedReader(
								new InputStreamReader(entity.getContent(),
										HTTP.UTF_8));
						StringBuilder sb = new StringBuilder();
						String line;
						while ((line = r.readLine()) != null) {
							sb.append(line);
						}
						responeString = sb.toString();
					} finally {
						entity.consumeContent();
					}
				}

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return responeString;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String responeString) {
			pDialog.dismiss();
			if (responseCode == HttpStatus.SC_OK) {
				try {
					JSONObject json = new JSONObject(responeString);
					uploadBookId = json.getString("id");
					createComicStatus = json.getString("status");
					if (createComicStatus.equalsIgnoreCase("1")) {

						String title = "";
						String message = "";
						String ok = "Ok";

						if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

							ok = "OK";
							title = "Information";
							message = "Information Book had uploaded success. Please post upload book and images book to server.";
						} else if (lang
								.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
							ok = getResources().getString(R.string.ar_ok);
							title = getResources().getString(
									R.string.ar_information);
							message = getResources()
									.getString(
											R.string.ar_information_book_had_uploaded_success);
						}
						AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
								FanComicActivity.this);
						// set title
						alertDialogBuilder.setTitle(title);
						// set dialog message
						alertDialogBuilder.setMessage(message)
								.setPositiveButton(ok,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												btnPostImageBook.setAlpha(1f);
												btnDelete.setAlpha(1f);
												imgCoverComic.setAlpha(1f);
												imgAddImage.setAlpha(1f);
												btnPostImageBook
														.setEnabled(true);
												btnDelete.setEnabled(true);
												imgCoverComic.setEnabled(true);
												imgAddImage.setEnabled(true);

												btnPostInfoBook.setAlpha(0.5f);
												btnPostInfoBook
														.setEnabled(false);
												etDescription.setEnabled(false);
												etTitle.setEnabled(false);
												layout_description
														.setEnabled(false);
												languageToggleButton
														.setEnabled(false);

												dialog.dismiss();
											}
										});
						// create alert dialog
						AlertDialog alertDialog = alertDialogBuilder.create();
						alertDialog.setCancelable(false);
						// show it
						alertDialog.show();
					} else {

						if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

							showAlertDialog("Error",
									"Upload the information book failed. Please try again ");
						} else if (lang
								.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
							showAlertDialog(
									getResources().getString(R.string.ar_error),
									getResources()
											.getString(
													R.string.ar_upload_the_information_book_failed_please_try_again));

						}

					}

				} catch (JSONException e) {
					e.printStackTrace();

					if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

						showAlertDialog("Error",
								"Upload the information book failed. Please try again ");
					} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
						showAlertDialog(
								getResources().getString(R.string.ar_error),
								getResources()
										.getString(
												R.string.ar_upload_the_information_book_failed_please_try_again));

					}

				}

			} else {
				if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

					showAlertDialog("Error",
							"Upload the information book failed. Please try again ");
				} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
					showAlertDialog(
							getResources().getString(R.string.ar_error),
							getResources()
									.getString(
											R.string.ar_upload_the_information_book_failed_please_try_again));

				}
			}
		}
	}

	class DeteleBookAsyncTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... params) {
			JSONParser jsonParser = new JSONParser();
			jsonParser
					.getJSONFromUrl("http://baraahgroup.com/admin/api/deleterecord?id="
							+ params[0]);
			return null;
		}

	}

	class UploadPhotoToServerAsyncTask extends
			AsyncTask<String, String, String> {
		private String serverResponse = "";
		private ProgressDialog pDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(FanComicActivity.this);
			if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {
				pDialog.setMessage("Please wait...");

			} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
				pDialog.setMessage(getApplicationContext().getResources()
						.getString(R.string.ar_please_wait));

			}

			pDialog.setIndeterminate(false);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		protected String doInBackground(String... args) {
			cd = new ConnectionDetector(getApplicationContext());
			isInternetPresent = cd.isConnectingToInternet();
			if (isInternetPresent) {
				serverResponse = uploadPhoto(coverPhotoBookPath, uploadBookId
						+ "-" + "coverbook.jpg");
				if (serverResponse.equalsIgnoreCase("1")) {
					for (int i = 0; i < arrayPhotosOfBook.size(); i++) {
						serverResponse = uploadPhoto(arrayPhotosOfBook.get(i),
								uploadBookId + "-" + (i + 1) + ".jpg");
						if (!serverResponse.equalsIgnoreCase("1")) {
							break;
						}
					}
				}

			}
			return serverResponse;
		}

		protected void onPostExecute(String serverResponse) {
			pDialog.dismiss();
			if (isInternetPresent) {
				if (serverResponse.equalsIgnoreCase("1")) {
					if (shareComicDialog != null) {
						shareComicDialog.dismiss();
					}
					loadDataAsyncTask = new LoadDataAsyncTask();
					loadDataAsyncTask.execute();
					if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

						showAlertDialog("Successfull",
								"Book have been uploaded.");

					} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {

						showAlertDialog(
								getResources()
										.getString(R.string.ar_succefully),
								getResources()
										.getString(
												R.string.ar_successfullb_book_have_been_uploaded));
					}

				} else {

					if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

						showAlertDialog("Error",
								"Upload images error. Please try again.");

					} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
						showAlertDialog(
								getResources().getString(R.string.ar_error),
								getResources()
										.getString(
												R.string.ar_upload_images_erorr_please_try_again));
					}

				}
			} else {
				showAlertDialog(
						getResources().getString(
								R.string.ar_no_connect_internet),
						getResources()
								.getString(
										R.string.ar_plaese_check_your_internet_connecttion));
			}
		}
	}

	private String uploadPhoto(String photoPath, String photoName) {
		try {
			Bitmap bm = Bitmap.createScaledBitmap(
					BitmapFactory.decodeFile(photoPath), 768, 1024, true);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bm.compress(CompressFormat.JPEG, 100, bos);
			byte[] data = bos.toByteArray();
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost postRequest = new HttpPost(Config.UPLOAD_PHOTO);
			ByteArrayBody bab = new ByteArrayBody(data, photoName);
			// File file= new File("/mnt/sdcard/forest.png");
			// FileBody bin = new FileBody(file);
			MultipartEntity reqEntity = new MultipartEntity(
					HttpMultipartMode.BROWSER_COMPATIBLE);
			reqEntity.addPart("data", bab);

			postRequest.setEntity(reqEntity);
			// postRequest.setEntity(reqEntity);
			HttpResponse response = httpClient.execute(postRequest);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent(), "UTF-8"));
			String sResponse;
			StringBuilder s = new StringBuilder();

			while ((sResponse = reader.readLine()) != null) {
				s = s.append(sResponse);
			}
			System.out.println("Response: " + s);
			Log.i("Nam Dinh", "photoPath:" + photoPath);
			return s.toString();
		} catch (Exception e) {
			// handle exception here
			Log.e(e.getClass().getName(), "error: " + e.getMessage());
			return "";
		}
	}

	private void showAlertDialog(String Title, String Message) {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				FanComicActivity.this);
		// set title
		alertDialogBuilder.setTitle(Title);
		String ok = "Ok";

		if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

			ok = "OK";

		} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
			ok = getResources().getString(R.string.ar_ok);
		}
		// set dialog message
		alertDialogBuilder.setMessage(Message).setCancelable(false)
				.setPositiveButton(ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		alertDialogBuilder.setCancelable(false);
		alertDialogBuilder.show();
	}

	void init() {
		for (int i = 0; i < arrayBookInformation.size(); i++) {
			arrUrlImage.add("http://" + base_url + "/"
					+ arrayBookInformation.get(i).get("url") + "/"
					+ arrayBookInformation.get(i).get("cover"));
		}
	}

}
