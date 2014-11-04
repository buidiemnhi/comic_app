package com.bsp.comicapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bsp.comicapp.adapter.SettingGridViewAdapter;
import com.bsp.comicapp.displayimage.util.ImageFetcher;
import com.bsp.comicapp.displayimage.util.RecyclingImageView;
import com.bsp.comicapp.displayimage.util.ImageCache.ImageCacheParams;
import com.bsp.comicapp.util.Config;
import com.bsp.comicapp.util.JSONParser;
import com.paypal.android.sdk.M;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;

import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class SettingActivity extends FragmentActivity implements
		OnClickListener {
	private Button btnStore;
	private ImageView imgLogo;
	private Button btnMyComic;
	private Button btnSetting;

	private LinearLayout layout_user_setting;
	private LinearLayout layout_setting;
	private TextView txtSetting;
	private RecyclingImageView imgUserAvatar;
	private LinearLayout layout_user_information_title;
	private TextView txtFullNameTitle;
	private TextView txtEmailTitle;
	private TextView txtMemberSinceTitle;
	private EditText txtFullName;
	private EditText txtEmail;
	private EditText txtMemberSince;
	private LinearLayout layout_language_text, layout_change_profile,
			layout_user_information_edit;
	private LinearLayout layout_notification_text;
	private TextView txtLanguage;
	private TextView txtNotification, txtEditButton;
	private ToggleButton languageToggleButton;
	private Switch switchNotification;
	private Button butEnglish, butArabic, butEdit;

	private SharedPreferences languagePreferences;
	private Editor e;

	private LinearLayout layout_social;
	private LinearLayout layout_display_social_mode;
	private Button btnALlUsers;
	private GridView gvAllUsers;

	// Load user
	private JSONParser jsonParser;
	private JSONObject json = null;
	private ArrayList<HashMap<String, String>> arrayUsersInformation = new ArrayList<HashMap<String, String>>();
	private int status = -1;
	private JSONArray data = null;
	private String IMAGE_CACHE_DIR = "thumbs";
	private ImageFetcher mImageFetcher;
	private ImageFetcher mUserImageFetcher;
	private LoadAllUsersAsyncTask loadAllUsersAsyncTask = null;

	private LoadUserAsyncTask loadUserAsyncTask = null;
	private String id = "56";
	private String avatar = "";
	private String email = "";
	private String pass = "";
	private String name = "";
	private String type = "";
	private String date_re = "";

	private String lang = "";

	public boolean isEdit = false;
	public boolean isEnglish = false;

	TableLayout table_profile;
	TableRow row_fullname, row_email, row_membersince, row_editbutton,
			row_language, row_notification;
	
	android.widget.TableRow.LayoutParams txtFullName_param,txtEmail_param,txtMemberSince_param,btnEnglish_param,btnEdit_param,switchNotification_param;
	
	int height_20 = (int)Math.round(Config.screenHeight * 0.025);
	int width_15 = (int)Math.round(Config.screenWidth * 15/480);

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_setting);

		languagePreferences = getSharedPreferences(
				Config.LANGUAGE_PREFERENCE_NAME, MODE_PRIVATE);
		e = languagePreferences.edit();
		lang = languagePreferences.getString(Config.PREFERENCE_KEY_LANGUAGE,
				Config.ENGLISH_LANGUAGUE);

		ImageView imageView = (ImageView) findViewById(R.id.imgBackground);
		if (Config.Bg_Portrait != null)
			imageView.setBackgroundDrawable(Config.Bg_Portrait);
		btnStore = (Button) findViewById(R.id.btnStore);
		imgLogo = (ImageView) findViewById(R.id.imgLogo);
		btnMyComic = (Button) findViewById(R.id.btnMyComic);
		btnSetting = (Button) findViewById(R.id.btnSetting);

		layout_user_setting = (LinearLayout) findViewById(R.id.layout_user_setting);
		// layout_setting = (LinearLayout) findViewById(R.id.layout_setting);
		txtSetting = (TextView) findViewById(R.id.txtSetting);
		imgUserAvatar = (RecyclingImageView) findViewById(R.id.imgUserAvatar);
		initTableProfile();
		// = (LinearLayout) findViewById(R.id.layout_user_information_title);
		txtFullNameTitle = (TextView) findViewById(R.id.txtFullNameTitle);
		txtEmailTitle = (TextView) findViewById(R.id.txtEmailTitle);
		txtMemberSinceTitle = (TextView) findViewById(R.id.txtMemberSinceTitle);
		txtFullName = (EditText) findViewById(R.id.txtFullName);
		txtFullName_param = (android.widget.TableRow.LayoutParams) txtFullName.getLayoutParams();
		
		txtEmail = (EditText) findViewById(R.id.txtEmail);
		txtEmail_param = (android.widget.TableRow.LayoutParams) txtEmail.getLayoutParams();
		
		// layout_user_information_edit =
		// (LinearLayout)findViewById(R.id.layout_user_information_edit);
		layout_change_profile = (LinearLayout) findViewById(R.id.layout_change_profile);

		txtMemberSince = (EditText) findViewById(R.id.txtMemberSince);
		txtMemberSince_param = (android.widget.TableRow.LayoutParams) txtMemberSince.getLayoutParams();
		
		layout_language_text = (LinearLayout) findViewById(R.id.layout_language_text);
		layout_notification_text = (LinearLayout) findViewById(R.id.layout_notification_text);

		txtLanguage = (TextView) findViewById(R.id.txtLanguage);
		txtNotification = (TextView) findViewById(R.id.txtNotification);
		// languageToggleButton = (ToggleButton)
		// findViewById(R.id.languageToggleButton);
		switchNotification = (Switch) findViewById(R.id.switchNotification);
		switchNotification_param = (android.widget.TableRow.LayoutParams) switchNotification.getLayoutParams();
		
		layout_social = (LinearLayout) findViewById(R.id.layout_social);
		layout_display_social_mode = (LinearLayout) findViewById(R.id.layout_display_social_mode);
		btnALlUsers = (Button) findViewById(R.id.btnAllUsers);
		gvAllUsers = (GridView) findViewById(R.id.gvAllUsers);
		// butArabic = (Button) findViewById(R.id.btnArabic);
		butEnglish = (Button) findViewById(R.id.btnEnglish);
		btnEnglish_param = (android.widget.TableRow.LayoutParams) butEnglish.getLayoutParams();

		butEdit = (Button) findViewById(R.id.butEditProfile);
		btnEdit_param = (android.widget.TableRow.LayoutParams) butEdit.getLayoutParams();
		
		// Set font
		Typeface tf = Typeface.createFromAsset(getApplicationContext()
				.getAssets(), "fonts/PAPYRUS.TTF");
		txtSetting.setTypeface(tf);
		btnALlUsers.setTypeface(tf);

		// Set text size
		float textsize = (float) (Config.screenHeight * 0.02);
		float textsize2 = (float) (Config.screenHeight * 0.027);
		txtSetting.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
		btnALlUsers.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
		txtFullNameTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize2);
		txtEmailTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize2);
		txtMemberSinceTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize2);

		
//		txtEditButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize2);
		txtFullName.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize2);
		txtEmail.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize2);
		txtMemberSince.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize2);
		txtNotification.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize2);
		txtLanguage.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize2);
		// butArabic.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize2);
		butEnglish.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize2);
		butEdit.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize2);

		// resize item
		layout_user_setting.setPadding((int) (Config.screenHeight * 0.04),
				(int) (Config.screenHeight * 0.05),
				(int) (Config.screenHeight * 0.04),
				(int) (Config.screenHeight * 0.05));
		layout_social.setPadding((int) (Config.screenHeight * 0.04),
				(int) (Config.screenHeight * 0.05),
				(int) (Config.screenHeight * 0.04), 0);
		// layout_user_information_title.setPadding(0, 0,(int)
		// (Config.screenHeight * 0.035), 0);
		layout_display_social_mode.setPadding(0, 0,
				(int) (Config.screenHeight * 0.02), 0);

		resizeView(imgLogo, (int) (Config.screenHeight * 0.075 * 150 / 175
				* 345 / 69), (int) (Config.screenHeight * 0.075 * 150 / 175),
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
		resizeView(imgUserAvatar, (int) (Config.screenHeight * 0.3),
				(int) (Config.screenHeight * 0.3 * 786 / 512), 0, 0,
				(int) (Config.screenHeight * 0.02), 0);
		// resizeView(languageToggleButton,
		// (int) (Config.screenHeight * 0.23 * 0.95),
		// (int) (Config.screenHeight * 0.23 * 0.95 * 29 / 123), 0, 0, 0,
		// 0);

		txtSetting.setPadding((int) (Config.screenHeight * 0.01), 10, 0, 0);
		btnALlUsers.setPadding((int) (Config.screenHeight * 0.01),
				(int) (Config.screenHeight * 0.01),
				(int) (Config.screenHeight * 0.01),
				(int) (Config.screenHeight * 0.01));

		btnStore.setOnClickListener(this);
		btnMyComic.setOnClickListener(this);
		// butArabic.setOnClickListener(this);
		butEnglish.setOnClickListener(this);
		butEdit.setOnClickListener(this);
		txtEmail.setEnabled(false);
		txtFullName.setEnabled(false);
		txtMemberSince.setEnabled(false);
		isEdit = false;
		final MainActivity instanceOfMainActivity = MainActivity
				.getActivityInstance();
		switchNotification
				.setOnCheckedChangeListener(new OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							// if(MainActivity.mTimer.)
							MainActivity.mTimer = new Timer();
							MainActivity.mTimer
									.schedule(
											instanceOfMainActivity.new TimerTaskNotification(),
											0, 3600000);
						} else {
							MainActivity.mTimer.cancel();
							MainActivity.mTimer.purge();
						}

					}
				});
		//swapviewProfile();

	}

	void initTableProfile() {
		table_profile = (TableLayout) findViewById(R.id.table_profile);
		row_fullname = (TableRow) table_profile.findViewById(R.id.row_fullname);
		android.widget.TableLayout.LayoutParams row_fullname_param = (android.widget.TableLayout.LayoutParams)row_fullname.getLayoutParams();
		row_fullname_param.setMargins(0, height_20, 0, 0);
		
		row_email = (TableRow) table_profile.findViewById(R.id.row_email);
		android.widget.TableLayout.LayoutParams row_email_param = (android.widget.TableLayout.LayoutParams)row_email.getLayoutParams();
		row_email_param.setMargins(0, height_20, 0, 0);
		
		
		row_language = (TableRow) table_profile.findViewById(R.id.row_language);
		android.widget.TableLayout.LayoutParams row_language_param = (android.widget.TableLayout.LayoutParams)row_language.getLayoutParams();
		row_language_param.setMargins(0, height_20, 0, 0);
		
		row_membersince = (TableRow) table_profile
				.findViewById(R.id.row_membersince);
		android.widget.TableLayout.LayoutParams row_membersince_param = (android.widget.TableLayout.LayoutParams)row_membersince.getLayoutParams();
		row_membersince_param.setMargins(0, height_20, 0, 0);
		
		row_editbutton = (TableRow) table_profile
				.findViewById(R.id.row_editbutton);
		android.widget.TableLayout.LayoutParams row_editbutton_param = (android.widget.TableLayout.LayoutParams)row_editbutton.getLayoutParams();
		row_editbutton_param.setMargins(0, height_20, 0, 0);
		
		row_notification = (TableRow) table_profile
				.findViewById(R.id.row_notification);
		android.widget.TableLayout.LayoutParams row_notification_param = (android.widget.TableLayout.LayoutParams)row_notification.getLayoutParams();
		row_notification_param.setMargins(0, height_20, 0, 0);

	}

	void swapviewProfile() {
		// layout_change_profile.removeAllViews();
		// layout_change_profile.addView(layout_user_information_edit);
		// layout_change_profile.addView(layout_user_information_title);
		row_fullname.removeAllViews();
		row_email.removeAllViews();
		row_language.removeAllViews();
		row_membersince.removeAllViews();
		row_editbutton.removeAllViews();
		row_notification.removeAllViews();

		if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) 
		{
			row_fullname.setGravity(Gravity.RIGHT);
			txtFullName.setGravity(Gravity.RIGHT);
			txtFullName_param.setMargins(0, 0, width_15, 0);
			
			row_fullname.addView(txtFullName);
			txtFullNameTitle.setGravity(Gravity.RIGHT);
			row_fullname.addView(txtFullNameTitle);

			txtEmail.setGravity(Gravity.RIGHT);
			txtEmail_param.setMargins(0, 0, width_15, 0);
			row_email.addView(txtEmail);
			txtEmailTitle.setGravity(Gravity.RIGHT);
			row_email.addView(txtEmailTitle);

			txtLanguage.setGravity(Gravity.RIGHT);
			btnEnglish_param.setMargins(0, 0, width_15, 0);
			row_language.addView(butEnglish);
			row_language.addView(txtLanguage);

			txtMemberSince.setGravity(Gravity.RIGHT);
			txtMemberSince_param.setMargins(0, 0, width_15, 0);
			row_membersince.addView(txtMemberSince);
			txtMemberSince.setGravity(Gravity.RIGHT);
			row_membersince.addView(txtMemberSinceTitle);

			
			row_editbutton.addView(butEdit);
			

			txtNotification.setGravity(Gravity.RIGHT);
			switchNotification_param.setMargins(0, 0, width_15, 0);
			row_notification.addView(switchNotification);
			row_notification.addView(txtNotification);
		}else
		{
			row_fullname.setGravity(Gravity.LEFT);
			txtFullName.setGravity(Gravity.LEFT);
			txtFullName_param.setMargins(width_15, 0, 0, 0);
			
			txtFullNameTitle.setGravity(Gravity.LEFT);
			row_fullname.addView(txtFullNameTitle);
			row_fullname.addView(txtFullName);

			txtEmail.setGravity(Gravity.LEFT);
			txtEmail_param.setMargins(width_15, 0, 0, 0);
			txtEmailTitle.setGravity(Gravity.LEFT);
			row_email.addView(txtEmailTitle);
			row_email.addView(txtEmail);

			txtLanguage.setGravity(Gravity.LEFT);
			row_language.addView(txtLanguage);
			row_language.addView(butEnglish);
			btnEnglish_param.setMargins(width_15, 0, 0, 0);

			txtMemberSince.setGravity(Gravity.LEFT);
			txtMemberSince_param.setMargins(width_15, 0, 0, 0);
			txtMemberSinceTitle.setGravity(Gravity.LEFT);
			row_membersince.addView(txtMemberSinceTitle);
			row_membersince.addView(txtMemberSince);

			row_editbutton.addView(butEdit);

			txtNotification.setGravity(Gravity.LEFT);
			row_notification.addView(txtNotification);
			row_notification.addView(switchNotification);
			switchNotification_param.setMargins(width_15, 0, 0, 0);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();

		if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {
			// TODO
			btnStore.setBackgroundResource(R.drawable.en_ic_store);
			imgLogo.setImageResource(R.drawable.en_logo_setting);
			btnMyComic.setBackgroundResource(R.drawable.en_ic_mycomic);
			btnSetting.setBackgroundResource(R.drawable.en_ic_setting);

			txtSetting.setText(R.string.setting);
			txtFullNameTitle.setText(R.string.fullname);
			txtEmailTitle.setText(R.string.email);
			txtMemberSinceTitle.setText(R.string.membersince);
			txtLanguage.setText(R.string.language);
			txtNotification.setText(R.string.notification);
			btnALlUsers.setText(R.string.allusers);
			// butArabic.setText("Arabic");
			butEnglish.setText("Arabic");
			// butArabic.setEnabled(true);
			// butEnglish.setEnabled(false);
			butEnglish.setBackgroundResource(R.color.unchoose);
			isEnglish = true;
			switchNotification.setTextOn("ON");
			switchNotification.setTextOff("OFF");

			if (isEdit) {

				butEdit.setText("Done");

			} else
				butEdit.setText("Edit");
			// languageToggleButton.setChecked(false);
		} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
			// TODO
			btnStore.setBackgroundResource(R.drawable.ar_ic_store);
			imgLogo.setImageResource(R.drawable.ar_logo_setting);
			btnMyComic.setBackgroundResource(R.drawable.ar_ic_mycomic);
			btnSetting.setBackgroundResource(R.drawable.ar_ic_setting);
			txtSetting.setText(R.string.ar_setting);
			txtFullNameTitle.setText(R.string.ar_fullname);
			txtEmailTitle.setText(R.string.ar_email);
			txtMemberSinceTitle.setText(R.string.ar_membersince);

			txtNotification.setText(R.string.ar_notification);
			btnALlUsers.setText(R.string.ar_all_users);
			// butArabic.setText(R.string.ar_arabic);
			butEnglish.setText(R.string.ar_english);
			butEnglish.setBackgroundResource(R.color.unchoose);
			isEnglish = false;
			// butArabic.setEnabled(false);
			switchNotification.setTextOn(getResources().getString(
					R.string.ar_on));
			switchNotification.setTextOff(getResources().getString(
					R.string.ar_off));
			butEnglish.setEnabled(true);
			if (isEdit) {

				butEdit.setText(R.string.ar_edit);

			} else
				butEdit.setText(R.string.ar_done);

			txtLanguage.setText(getResources().getString(R.string.ar_lanuage));
			// butEnglish.setText(txtLanguage.getText());
			// languageToggleButton
			// .setBackgroundResource(R.drawable.bgtoggle_arabic);
			// languageToggleButton.setChecked(true);
		}
		swapviewProfile();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (loadAllUsersAsyncTask != null) {
			loadAllUsersAsyncTask.cancel(true);
		}
		if (loadUserAsyncTask != null) {
			loadUserAsyncTask.cancel(true);
		}

		try {
			mImageFetcher.setPauseWork(true);
			mImageFetcher.setExitTasksEarly(true);
			mUserImageFetcher.setPauseWork(true);
			mUserImageFetcher.setExitTasksEarly(true);
			mImageFetcher.flushCache();
			mUserImageFetcher.flushCache();
			mImageFetcher.clearCache();
			mImageFetcher.closeCache();
			mUserImageFetcher.clearCache();
			mUserImageFetcher.closeCache();
		} catch (Exception e) {

		}
	}

	// @Override
	// public void onLowMemory() {
	// super.onLowMemory();
	// try {
	// mImageFetcher.flushCache();
	// mUserImageFetcher.flushCache();
	// mImageFetcher.clearCache();
	// mUserImageFetcher.clearCache();
	// } catch (Exception e) {
	// }
	// }

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		// layout_setting.getLayoutParams().width =
		// layout_display_social_mode.getWidth();
		// layout_language_text.getLayoutParams().height = languageToggleButton
		// .getHeight();
		/*
		 * layout_notification_text.getLayoutParams().height =
		 * switchNotification .getHeight();
		 */
		// Display image
		ImageCacheParams cacheParams = new ImageCacheParams(
				getApplicationContext(), IMAGE_CACHE_DIR);
		cacheParams.setMemCacheSizePercent(0.1f); // Set memory cache to
													// 25% of
													// app memory
		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		mImageFetcher = new ImageFetcher(getApplicationContext(),
				(int) (gvAllUsers.getWidth() * 0.24),
				(int) (gvAllUsers.getHeight() * 0.24));
		mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
		// mImageFetcher.setLoadingImage(R.drawable.gallery_image_load);

		mUserImageFetcher = new ImageFetcher(getApplicationContext(),
				imgUserAvatar.getWidth(), imgUserAvatar.getHeight());
		mUserImageFetcher.addImageCache(getSupportFragmentManager(),
				cacheParams);
		mUserImageFetcher.setLoadingImage(R.drawable.gallery_image_load);

		loadUserAsyncTask = new LoadUserAsyncTask();
		loadUserAsyncTask.executeOnExecutor(MainActivity.pool);
		loadAllUsersAsyncTask = new LoadAllUsersAsyncTask();
		loadAllUsersAsyncTask.executeOnExecutor(MainActivity.pool);
	}

	private void resizeView(View resizeView, int width, int height,
			int marginLeft, int marginTop, int marginRight, int marginBottom) {
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.height = height;
		llp.width = width;
		llp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
		resizeView.setLayoutParams(llp);
	}

	private class LoadUserAsyncTask extends AsyncTask<String, Void, String> {
		private boolean isParseJSONError = false;

		@Override
		protected String doInBackground(String... params) {
			String url_get_user = Config.GET_USER + "?userID=" + Config.IdUser;
			json = null;
			json = jsonParser.getJSONFromUrl(url_get_user);
			if (json != null) {
				try {
					id = json.getString("ID");
					avatar = json.getString("avatar");
					email = json.getString("email");
					pass = json.getString("pass");
					name = json.getString("name");
					type = json.getString("type");
					date_re = json.getString("date_re");

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
				// TODO
			} else {
				txtFullName.setText(name);
				txtEmail.setText(email);
				txtMemberSince.setText(date_re);
				if (!avatar.equalsIgnoreCase("")) {
					mUserImageFetcher.loadImage(avatar, imgUserAvatar);
				}
			}
		}

		@Override
		protected void onPreExecute() {
			isParseJSONError = false;
			jsonParser = new JSONParser();
		}
	}

	private class LoadAllUsersAsyncTask extends AsyncTask<String, Void, String> {
		private boolean isParseJSONError = false;

		@Override
		protected String doInBackground(String... params) {
			String url_get_user = Config.GET_USER;
			json = null;
			json = jsonParser.getJSONFromUrl(url_get_user);
			if (json != null) {
				try {
					status = json.getInt("status");
					data = json.getJSONArray("data");
					for (int i = 0; i < data.length(); i++) {
						JSONObject jObj = data.getJSONObject(i);
						HashMap<String, String> user = new HashMap<String, String>();
						user.put("ID", jObj.getString("ID"));
						user.put("avatar", jObj.getString("avatar"));
						user.put("email", jObj.getString("email"));
						user.put("pass", jObj.getString("pass"));
						user.put("name", jObj.getString("name"));
						user.put("type", jObj.getString("type"));
						user.put("date_re", jObj.getString("date_re"));
						arrayUsersInformation.add(user);
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
				// TODO
			} else {
				gvAllUsers.setAdapter(new SettingGridViewAdapter(
						SettingActivity.this, arrayUsersInformation,
						mImageFetcher, (int) (gvAllUsers.getWidth() * 0.19),
						(int) (gvAllUsers.getWidth() * 0.19)));
				gvAllUsers
						.setVerticalSpacing((int) (gvAllUsers.getWidth() * 0.01));
			}
		}

		@Override
		protected void onPreExecute() {
			isParseJSONError = false;
			jsonParser = new JSONParser();
			arrayUsersInformation.clear();
			arrayUsersInformation = new ArrayList<HashMap<String, String>>();
		}
	}

	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();
		switch (id) {
		case R.id.btnMyComic:
			Intent myComic = new Intent(getApplicationContext(),
					MyComicActivity.class);
			startActivity(myComic);
			SettingActivity.this.finish();
			break;

		case R.id.btnStore:
			Intent store = new Intent(getApplicationContext(),
					ComicListActivity.class);
			startActivity(store);
			SettingActivity.this.finish();
			break;
		case R.id.btnEnglish:
			
			if (isEnglish) {
				butEnglish.setText(getResources()
						.getString(R.string.ar_english));
				butEnglish.setBackgroundResource(R.color.unchoose);
				setLanguageArabic();
				isEnglish = false;
				
			} else {

				butEnglish.setText(getResources().getString(R.string.arabic));

				butEnglish.setBackgroundResource(R.color.unchoose);
				setlanguageEnglish();
				isEnglish = true;
			}
			
			swapviewProfile();
			break;

		case R.id.butEditProfile:

			if (isEdit == false) {
				isEdit = true;
				txtEmail.setEnabled(true);
				txtFullName.setEnabled(true);
				txtMemberSince.setEnabled(true);
				if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {
					// TODO
					butEdit.setText("Done");

				} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
					// TODO
					butEdit.setText(R.string.ar_done);
				}
			} else {
				isEdit = false;
				txtEmail.setEnabled(false);
				txtFullName.setEnabled(false);
				txtMemberSince.setEnabled(false);
				if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {
					// TODO
					butEdit.setText("Edit");

				} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
					// TODO
					butEdit.setText(R.string.ar_edit);
				}
				new UpdateUserAsyntask().execute();
			}

			break;

		default:
			break;
		}

	}

	class UpdateUserAsyntask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			ArrayList<NameValuePair> arrNameValuePairs = new ArrayList<NameValuePair>();
			arrNameValuePairs.add(new BasicNameValuePair("id", Config.IdUser));
			arrNameValuePairs.add(new BasicNameValuePair("name", txtFullName
					.getText().toString().trim()));
			arrNameValuePairs.add(new BasicNameValuePair("email", txtEmail
					.getText().toString().trim()));

			JSONParser jsonParser = new JSONParser();
			String result = jsonParser.makeHttpRequest(
					"http://baraahgroup.com/admin/user/updateuser",
					arrNameValuePairs);
			if (result.trim().equalsIgnoreCase("1")) {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						// Toast.makeText(SettingActivity.this, "Successfully",
						// Toast.LENGTH_LONG).show();
					}
				});
			}
			return null;
		}

	}

	void setLanguageArabic() {
		e.putString(Config.PREFERENCE_KEY_LANGUAGE, Config.ARABIC_LANGUAGE);
		e.commit();
		languagePreferences = getSharedPreferences(
				Config.LANGUAGE_PREFERENCE_NAME, MODE_PRIVATE);
		lang = languagePreferences.getString(Config.PREFERENCE_KEY_LANGUAGE,
				Config.ENGLISH_LANGUAGUE);
		// TODO
		btnStore.setBackgroundResource(R.drawable.ar_ic_store);
		imgLogo.setImageResource(R.drawable.ar_logo_setting);
		btnMyComic.setBackgroundResource(R.drawable.ar_ic_mycomic);
		btnSetting.setBackgroundResource(R.drawable.ar_ic_setting);
		txtSetting.setText(R.string.ar_setting);
		txtFullNameTitle.setText(R.string.ar_fullname);
		txtEmailTitle.setText(R.string.ar_email);
		txtMemberSinceTitle.setText(R.string.ar_membersince);
		txtLanguage.setText(R.string.ar_lanuage);
		txtNotification.setText(R.string.ar_notification);
		btnALlUsers.setText(R.string.ar_all_users);
	

		switchNotification.setTextOn(getResources().getString(R.string.ar_on));
		switchNotification
				.setTextOff(getResources().getString(R.string.ar_off));
		if (isEdit) {

			butEdit.setText(R.string.ar_done);

		} else
			butEdit.setText(R.string.ar_edit);
	}

	void setlanguageEnglish() {
		e.putString(Config.PREFERENCE_KEY_LANGUAGE, Config.ENGLISH_LANGUAGUE);
		e.commit();

		languagePreferences = getSharedPreferences(
				Config.LANGUAGE_PREFERENCE_NAME, MODE_PRIVATE);
		lang = languagePreferences.getString(Config.PREFERENCE_KEY_LANGUAGE,
				Config.ENGLISH_LANGUAGUE);
		// TODO
		btnStore.setBackgroundResource(R.drawable.en_ic_store);
		imgLogo.setImageResource(R.drawable.en_logo_setting);
		btnMyComic.setBackgroundResource(R.drawable.en_ic_mycomic);
		btnSetting.setBackgroundResource(R.drawable.en_ic_setting);

		txtSetting.setText(R.string.setting);
		txtFullNameTitle.setText(R.string.fullname);
		txtEmailTitle.setText(R.string.email);
		txtMemberSinceTitle.setText(R.string.membersince);
		txtLanguage.setText(R.string.language);
		txtNotification.setText(R.string.notification);
		btnALlUsers.setText(R.string.allusers);
		switchNotification.setTextOn(getResources().getString(R.string.on));
		switchNotification.setTextOff(getResources().getString(R.string.off));

		if (isEdit) {

			butEdit.setText("Done");

		} else
			butEdit.setText("Edit");
	}
}
