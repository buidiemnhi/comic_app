package com.bsp.comicapp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import at.technikum.mti.fancycoverflow.FancyCoverFlow;

import com.bsp.comicapp.MainActivity.GetBackgroundAnsyntask;
import com.bsp.comicapp.adapter.FancyCoverFlowSampleAdapter;
import com.bsp.comicapp.instagram.InstaImpl;
import com.bsp.comicapp.instagram.InstaImpl.InstagramDialogListener;
import com.bsp.comicapp.model.User;
import com.bsp.comicapp.twitter.FbActionEventSingletonClass;
import com.bsp.comicapp.twitter.TwitterApp;
import com.bsp.comicapp.twitter.TwitterApp.TwDialogListener;
import com.bsp.comicapp.util.Config;
import com.bsp.comicapp.util.ConnectionDetector;
import com.bsp.comicapp.util.JSONParser;
import com.bsp.comicapp.util.TwitterConst;
import com.bsp.comicapp.util.UploadImageToServer;

public class LoginComicAppActivity extends FragmentActivity implements
		OnClickListener {
	
	private JSONParser jsonParser;
	private JSONObject json = null;
	
	public static String lang = "eng";
	private ImageView imgLogo;
	private Button btnLoginDirectly;
	private Button btnLoginFacebook;
	private Button btnLoginTwitter;
	private Button btnLoginInstagram;
	private Button btnSkip;
	private Button btnInformation;
	ImageView imageView;
	ImageView imgUpLoadAvatar;
	Dialog loginDialog;
	Dialog registerDialog;
	private SharedPreferences languagePreferences;

	private FancyCoverFlow fancyCoverFlow;
	JSONArray jsonArray;
	
	Bitmap bitmapAvatar = null;
	String pathImage;

	FbActionEventSingletonClass mfacebook;
	private InstaImpl mInstaImpl;
	ResponseListener mResponseListener;
	TwitterApp mTwitter;
	BroadcastReceiver mReceiver;
	LocalBroadcastManager mLocalBroadcastManager;
	boolean isCancel = false;
	// final String twitter_consumer_key = "3nft7a08J9vtTi2UiJ2og";
	// final String twitter_secret_key =
	// "gVg5Uqxx36gtb97CV1C0iKyXZhxX0qygmIrTgarOQ";
	// Display image

	final int REQUEST_SAVE = 0;
	IntentFilter internetDetect = new IntentFilter(Config.INTERNET_DETECT);
	BroadcastReceiver networkCheckReciver;
	ConnectionDetector connectionDetector;
	boolean isConnect;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_login);

		languagePreferences = getSharedPreferences(
				Config.LANGUAGE_PREFERENCE_NAME, MODE_PRIVATE);
		lang = languagePreferences.getString(Config.PREFERENCE_KEY_LANGUAGE,
				"eng");

		imageView = (ImageView) findViewById(R.id.imgBackground);
		connectionDetector = new ConnectionDetector(this);
		if (connectionDetector.isConnectingToInternet()) {
			isConnect = true;
			new GetBackgroundAnsyntask().execute();
		} else
			isConnect = false;

		ReceiverBroadcastDetectnetwork();

		imgLogo = (ImageView) findViewById(R.id.imgLogo);
		btnLoginDirectly = (Button) findViewById(R.id.btnLoginDirectly);
		btnLoginFacebook = (Button) findViewById(R.id.btnLoginFacebook);
		btnLoginTwitter = (Button) findViewById(R.id.btnLoginTwitter);
		btnLoginInstagram = (Button) findViewById(R.id.btnLoginInstagram);
		btnSkip = (Button) findViewById(R.id.btnSkip);
		btnInformation = (Button) findViewById(R.id.btnInformation);
		fancyCoverFlow = (FancyCoverFlow) findViewById(R.id.fancyCoverFlow);
		fancyCoverFlow.setUnselectedAlpha(0.7f);
		fancyCoverFlow.setUnselectedSaturation(0.0f);
		fancyCoverFlow.setUnselectedScale(0.8f);
		fancyCoverFlow.setMaxRotation(45);
		fancyCoverFlow.setScaleDownGravity(0.5f);
		fancyCoverFlow
				.setSpacing(-((int) Config.screenHeight / 20 * 640 / 280));

		fancyCoverFlow.setAdapter(new FancyCoverFlowSampleAdapter(
				getApplicationContext()));

		// set text font
		Typeface tf = Typeface.createFromAsset(getApplicationContext()
				.getAssets(), "fonts/PAPYRUS.TTF");
		btnLoginFacebook.setTypeface(tf);
		btnLoginTwitter.setTypeface(tf);
		btnLoginInstagram.setTypeface(tf);
		btnLoginDirectly.setTypeface(tf);
		btnSkip.setTypeface(tf);

		// set text size
		float textsizeLoginButton = (float) (Config.screenHeight * 0.032);
		float textsizeSocialLoginButton = (float) (Config.screenHeight * 0.025);
		btnSkip.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsizeLoginButton);
		btnLoginFacebook.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				textsizeSocialLoginButton);
		btnLoginTwitter.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				textsizeSocialLoginButton);
		btnLoginInstagram.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				textsizeSocialLoginButton);
		btnLoginDirectly.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				textsizeSocialLoginButton);

		// resize item
		resizeFancyCoverFlow(fancyCoverFlow, 0,
				(int) (Config.screenHeight * 0.36), 0, 0, 0, 0);

		resizeImageView(imgLogo, (int) (Config.screenWidth / 18),
				(int) (Config.screenWidth / 18 * 120 / 106), 0, 0,
				(int) (Config.screenWidth / 150), 0);

		resizeButton(btnSkip, (int) (Config.screenWidth / 12),
				(int) (Config.screenWidth / 12 * 160 / 190),
				(int) (Config.screenWidth / 12), 0, 0, 0);

		resizeButton(btnLoginFacebook, (int) (Config.screenWidth / 5),
				(int) (Config.screenWidth / 5 * 316 / 383), 0, 0, 0, 0);
		resizeButton(btnLoginInstagram, (int) (Config.screenWidth / 5),
				(int) (Config.screenWidth / 5 * 316 / 383), 0, 0, 0, 0);
		resizeButton(btnLoginTwitter, (int) (Config.screenWidth / 5),
				(int) (Config.screenWidth / 5 * 316 / 383), 0, 0, 0, 0);
		resizeButton(btnLoginDirectly, (int) (Config.screenWidth / 5),
				(int) (Config.screenWidth / 5 * 316 / 383), 0, 0, 0, 0);
		resizeButton(btnInformation, (int) (Config.screenWidth / 25),
				(int) (Config.screenWidth / 25), 0, 0,
				(int) (Config.screenWidth / 30),
				(int) (Config.screenWidth / 30));

		btnLoginFacebook.setOnClickListener(this);
		btnLoginInstagram.setOnClickListener(this);
		btnLoginTwitter.setOnClickListener(this);
		btnLoginDirectly.setOnClickListener(this);
		btnSkip.setOnClickListener(this);
		btnInformation.setOnClickListener(this);

		// ////////////////////
		mResponseListener = new ResponseListener();
		connectionDetector = new ConnectionDetector(this);

		IntentFilter filter = new IntentFilter("Intent_Background");
		// filter.addAction("Intent_Background");

		mReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {

				// if(intent.getAction().equals("Intent_Background"))
				// {
				// Toast.makeText(context, "ssss11", Toast.LENGTH_LONG).show();
				// if (intent.getBooleanExtra("Background", true))

				imageView.setBackgroundDrawable(Config.Bg_Landscape);
				// }
			}
		};
		// mLocalBroadcastManager=LocalBroadcastManager.getInstance(this);
		registerReceiver(mReceiver, filter);

	}

	
	
	private void resizeFancyCoverFlow(FancyCoverFlow resizeFancyCoverFlow,
			int width, int height, int marginLeft, int marginTop,
			int marginRight, int marginBottom) {
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.height = height;
		llp.width = LinearLayout.LayoutParams.MATCH_PARENT;
		llp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
		resizeFancyCoverFlow.setLayoutParams(llp);
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

	private void resizeButtonCancel(Button resizeButton, int width, int height,
			int marginLeft, int marginTop, int marginRight, int marginBottom) {
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.height = height;
		llp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
		llp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
		resizeButton.setLayoutParams(llp);
	}

	private void resizeImageView(ImageView resizeImageView, int width,
			int height, int marginLeft, int marginTop, int marginRight,
			int marginBottom) {
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.height = height;
		llp.width = width;
		llp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
		resizeImageView.setLayoutParams(llp);
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

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.btnSkip:
			// TODO
			Intent intent = new Intent(getApplicationContext(),
					ComicListActivity.class);
			startActivity(intent);

			this.finish();
			break;
		case R.id.btnLoginFacebook:
			if (connectionDetector.isConnectingToInternet()) {
				mfacebook = FbActionEventSingletonClass.getInstance(this);
				new loginFaceBookAsyTask(this).execute();
			} else {
				if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

					showAlertDialog(LoginComicAppActivity.this,
							"No Internet Connection",
							"Please, check your internet connection.");

				} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
					// TODO
					showAlertDialog(
							LoginComicAppActivity.this,
							getResources().getString(
									R.string.ar_no_connect_internet),
							getResources()
									.getString(
											R.string.ar_plaese_check_your_internet_connecttion));
				}
			}
			break;
		case R.id.btnLoginTwitter:
			if (connectionDetector.isConnectingToInternet()) {

				mTwitter = new TwitterApp(this, TwitterConst.CONSUMER_KEY,
						TwitterConst.CONSUMER_SECRET);
				mTwitter.setListener(twDialogListener);
				if (mTwitter.hasAccessToken()) {
					mTwitter.Logout();
				}
				mTwitter.authorize();
			} else {
				if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

					showAlertDialog(LoginComicAppActivity.this,
							"No Internet Connection",
							"Please, check your internet connection.");

				} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
					// TODO
					showAlertDialog(
							LoginComicAppActivity.this,
							getResources().getString(
									R.string.ar_no_connect_internet),
							getResources()
									.getString(
											R.string.ar_plaese_check_your_internet_connecttion));
				}
			}

			break;
		case R.id.btnLoginInstagram:
			if (connectionDetector.isConnectingToInternet()) {
				mInstaImpl = new InstaImpl(this);
				mInstaImpl.setAuthAuthenticationListener(lister);

			} else {
				if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

					showAlertDialog(LoginComicAppActivity.this,
							"No Internet Connection",
							"Please, check your internet connection.");

				} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
					// TODO
					showAlertDialog(
							LoginComicAppActivity.this,
							getResources().getString(
									R.string.ar_no_connect_internet),
							getResources()
									.getString(
											R.string.ar_plaese_check_your_internet_connecttion));
				}
			}

			break;

		case R.id.btnLoginDirectly:
			// open information dialog
			loginDialog = new Dialog(this, R.style.DialogSlideAnim);
			loginDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
			loginDialog.setContentView(R.layout.dialog_login);

			Button btnCancel = (Button) loginDialog
					.findViewById(R.id.btnCancel);
			Button btnLogo = (Button) loginDialog.findViewById(R.id.btnLogo);
			LinearLayout layout_login = (LinearLayout) loginDialog
					.findViewById(R.id.layout_login);
			LinearLayout layout_email = (LinearLayout) loginDialog
					.findViewById(R.id.layout_email);
			LinearLayout layout_password = (LinearLayout) loginDialog
					.findViewById(R.id.layout_password);
			final EditText etEmail = (EditText) loginDialog
					.findViewById(R.id.etEmail);
			final EditText etPassword = (EditText) loginDialog
					.findViewById(R.id.etPassword);
			LinearLayout layout_login_button = (LinearLayout) loginDialog
					.findViewById(R.id.layout_login_button);
			Button btnLogin = (Button) loginDialog.findViewById(R.id.btnLogin);
			LinearLayout layout_register = (LinearLayout) loginDialog
					.findViewById(R.id.layout_register);
			TextView txtRegister1 = (TextView) loginDialog
					.findViewById(R.id.txtRegister1);
			Button btnRegister = (Button) loginDialog
					.findViewById(R.id.btnRegister);
			TextView txtRegister2 = (TextView) loginDialog
					.findViewById(R.id.txtRegister2);

			// TODO
			if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

				btnCancel.setText(R.string.cancel);
				etEmail.setHint(R.string.email);
				etPassword.setHint(R.string.password);
				etPassword.setGravity(Gravity.LEFT);
				etEmail.setGravity(Gravity.LEFT);
				btnLogin.setText(R.string.login);
				txtRegister1.setText(R.string.icanlogin);
				btnRegister.setText(R.string.register);
				txtRegister2.setText(R.string.now);

			} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
				// TODO
				btnCancel.setText(R.string.ar_cancel);
				etEmail.setHint(R.string.ar_email);
				etPassword.setHint(R.string.ar_password);
				etPassword.setGravity(Gravity.RIGHT);
				etEmail.setGravity(Gravity.RIGHT);
				btnLogin.setText(R.string.ar_login);
				txtRegister1.setText(R.string.ar_now);
				btnRegister.setText(R.string.ar_register);
				txtRegister2.setText(R.string.ar_can_not_login);

			}

			// Set font
			Typeface tf = Typeface.createFromAsset(getApplicationContext()
					.getAssets(), "fonts/PAPYRUS.TTF");
			btnCancel.setTypeface(tf);
			btnLogin.setTypeface(tf);
			txtRegister1.setTypeface(tf);
			btnRegister.setTypeface(tf);
			txtRegister2.setTypeface(tf);

			// resize item
			float textsize = (float) (Config.screenHeight * 0.029);
			btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
			btnLogin.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
			txtRegister1.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
			btnRegister.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
			txtRegister2.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
			etEmail.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
			etPassword.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);

			layout_login.setPadding((int) (Config.screenHeight * 0.07),
					(int) (Config.screenHeight * 0.04),
					(int) (Config.screenHeight * 0.07),
					(int) (Config.screenHeight * 0.04));
			layout_login_button.setPadding(0, 0, 0,
					(int) (Config.screenHeight * 0.06));
			layout_register.setPadding(0, 0, 0,
					(int) (Config.screenHeight * 0.08));

			layout_email.getLayoutParams().width = (int) (Config.screenWidth * 0.45);
			layout_email.getLayoutParams().height = (int) (Config.screenWidth * 0.45 * 74 / 731);
			layout_password.getLayoutParams().width = (int) (Config.screenWidth * 0.45);
			layout_password.getLayoutParams().height = (int) (Config.screenWidth * 0.45 * 74 / 731);

			layout_email.setPadding(3, 2,
					(int) (Config.screenWidth * 0.45 * 90 / 731), 2);
			layout_password.setPadding(3, 2,
					(int) (Config.screenWidth * 0.45 * 90 / 731), 2);

			resizeButtonCancel(btnCancel, 0,
					(int) (Config.screenHeight * 0.09),
					(int) (Config.screenWidth * 0.005), 0, 0, 0);
			resizeButton(btnLogo,
					(int) (Config.screenHeight * 0.08 * 136 / 128),
					(int) (Config.screenHeight * 0.08),
					(int) (Config.screenWidth * 0.005), 0, 0, 0);
			resizeButton(btnLogin, (int) (Config.screenWidth * 0.18),
					(int) (Config.screenWidth * 0.18 * 74 / 307), 0, 0, 0, 0);

			// set on click event
			btnCancel.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					loginDialog.cancel();
				}
			});

			btnLogin.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

					if (connectionDetector.isConnectingToInternet()) {
						if (TextUtils.isEmpty(etEmail.getText().toString())) {
							if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

								etEmail.setError("Fill Blank");

							} else if (lang
									.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
								etEmail.setError(getResources().getString(
										R.string.ar_not_empty));
							}

							etEmail.requestFocus();

						} else if (TextUtils.isEmpty(etPassword.getText()
								.toString())) {
							if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

								etPassword.setError("Fill Blank");

							} else if (lang
									.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
								etPassword.setError(getResources().getString(
										R.string.ar_not_empty));
							}
							etPassword.requestFocus();
						} else if (!etEmail.getText().toString().matches(emailPattern)) {
							if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

								etEmail.setError("Email format is invalid");

							} else if (lang
									.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
								etEmail
										.setError(getResources()
												.getString(
														R.string.ar_email_format_is_invalid));
							}
							etEmail.requestFocus();
						} else{
							new LoginByEmailAsyTask(LoginComicAppActivity.this)
									.execute(etEmail.getText().toString()
											.trim(), etPassword.getText()
											.toString());
						}

					} else {
						// TODO
						if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

							showAlertDialog(LoginComicAppActivity.this,
									"No Internet Connection",
									"Please, check your internet connection.");

						} else if (lang
								.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
							// TODO
							showAlertDialog(
									LoginComicAppActivity.this,
									getResources().getString(
											R.string.ar_no_connect_internet),
									getResources()
											.getString(
													R.string.ar_plaese_check_your_internet_connecttion));
						}

					}
				}

			});

			btnRegister.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					loginDialog.dismiss();
					// open register dialog
					registerDialog = new Dialog(LoginComicAppActivity.this,
							R.style.DialogSlideAnim);
					registerDialog
							.requestWindowFeature(Window.FEATURE_NO_TITLE);
					registerDialog.setContentView(R.layout.dialog_register);

					Button btnCancel = (Button) registerDialog
							.findViewById(R.id.btnCancel);
					Button btnLogo = (Button) registerDialog
							.findViewById(R.id.btnLogo);

					LinearLayout linearLayoutBody = (LinearLayout) registerDialog
							.findViewById(R.id.linearRegisterBody);
					linearLayoutBody.getLayoutParams().width = (int) (Config.screenHeight);

					LinearLayout layout_login = (LinearLayout) registerDialog
							.findViewById(R.id.layout_login);
					LinearLayout layout_username = (LinearLayout) registerDialog
							.findViewById(R.id.layout_username);
					LinearLayout layout_email = (LinearLayout) registerDialog
							.findViewById(R.id.layout_email);
					LinearLayout layout_password = (LinearLayout) registerDialog
							.findViewById(R.id.layout_password);
					imgUpLoadAvatar = (ImageView) registerDialog
							.findViewById(R.id.imgUpLoadAvatar);

					final EditText etUserName = (EditText) registerDialog
							.findViewById(R.id.etUsername);
					final EditText etEmail = (EditText) registerDialog
							.findViewById(R.id.etEmail);
					final EditText etPassword = (EditText) registerDialog
							.findViewById(R.id.etPassword);
					LinearLayout layout_login_button = (LinearLayout) registerDialog
							.findViewById(R.id.layout_login_button);
					Button btnRegister = (Button) registerDialog
							.findViewById(R.id.btnRegister);

					// TODO
					if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

						btnCancel.setText(R.string.cancel);
						etUserName.setHint(R.string.username);
						etEmail.setHint(R.string.email);
						etPassword.setHint(R.string.password);
						etPassword.setGravity(Gravity.LEFT);
						etEmail.setGravity(Gravity.LEFT);
						etUserName.setGravity(Gravity.LEFT);
						btnRegister.setText(R.string.register);

					} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
						// TODO
						btnCancel.setText(R.string.ar_cancel);
						etUserName.setHint(R.string.ar_username);
						etEmail.setHint(R.string.ar_email);
						etPassword.setHint(R.string.ar_password);
						etPassword.setGravity(Gravity.RIGHT);
						etEmail.setGravity(Gravity.RIGHT);
						etUserName.setGravity(Gravity.RIGHT);
						btnRegister.setText(R.string.ar_register);

					}

					// Set font
					Typeface tf = Typeface.createFromAsset(
							getApplicationContext().getAssets(),
							"fonts/PAPYRUS.TTF");
					btnCancel.setTypeface(tf);
					btnRegister.setTypeface(tf);

					// resize item
					float textsize = (float) (Config.screenHeight * 0.029);
					btnCancel.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
					btnRegister.setTextSize(TypedValue.COMPLEX_UNIT_PX,
							textsize);
					etEmail.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
					etPassword
							.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
					etUserName
							.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);

					layout_login.setPadding((int) (Config.screenHeight * 0.04),
							(int) (Config.screenHeight * 0.00625),
							(int) (Config.screenHeight * 0.04),
							(int) (Config.screenHeight * 0.04));
					layout_login_button.setPadding(0, 0, 0,
							(int) (Config.screenHeight * 0.06));

					layout_username.getLayoutParams().width = (int) (Config.screenWidth * 0.45);
					layout_username.getLayoutParams().height = (int) (Config.screenWidth * 0.45 * 74 / 731);
					layout_email.getLayoutParams().width = (int) (Config.screenWidth * 0.45);
					layout_email.getLayoutParams().height = (int) (Config.screenWidth * 0.45 * 74 / 731);
					layout_password.getLayoutParams().width = (int) (Config.screenWidth * 0.45);
					layout_password.getLayoutParams().height = (int) (Config.screenWidth * 0.45 * 74 / 731);

					layout_username.setPadding(3, 2,
							(int) (Config.screenWidth * 0.45 * 90 / 731), 2);
					layout_email.setPadding(3, 2,
							(int) (Config.screenWidth * 0.45 * 90 / 731), 2);
					layout_password.setPadding(3, 2,
							(int) (Config.screenWidth * 0.45 * 90 / 731), 2);

					resizeImageView(imgUpLoadAvatar,
							(int) (Config.screenHeight * 0.208),
							(int) (Config.screenHeight * 0.208), 0,
							(int) (Config.screenHeight * 0.01), 0, 0);
					resizeButtonCancel(btnCancel, 0,
							(int) (Config.screenHeight * 0.09),
							(int) (Config.screenWidth * 0.005), 0, 0, 0);
					resizeButton(btnLogo,
							(int) (Config.screenHeight * 0.08 * 136 / 128),
							(int) (Config.screenHeight * 0.08),
							(int) (Config.screenWidth * 0.005), 0, 0, 0);
					resizeButton(btnRegister,
							(int) (Config.screenWidth * 0.18),
							(int) (Config.screenWidth * 0.18 * 74 / 307), 0, 0,
							0, 0);

					// set on click event
					imgUpLoadAvatar.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							Intent intent = new Intent();
							intent.setType("image/*");
							intent.setAction(Intent.ACTION_GET_CONTENT);
							startActivityForResult(intent, REQUEST_SAVE);
						}
					});

					btnCancel.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							registerDialog.dismiss();
							bitmapAvatar = null;
						}
					});
					btnRegister.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							if (bitmapAvatar != null) {
								String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
								if (connectionDetector.isConnectingToInternet()) {

									if (TextUtils.isEmpty(etEmail.getText()
											.toString())) {
										if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

											etEmail.setError("Fill Blank");

										} else if (lang
												.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
											etEmail.setError(getResources()
													.getString(
															R.string.ar_not_empty));
										}

										etEmail.requestFocus();

									} else if (TextUtils.isEmpty(etPassword
											.getText().toString())) {
										if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

											etPassword.setError("Fill Blank");

										} else if (lang
												.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
											etPassword
													.setError(getResources()
															.getString(
																	R.string.ar_not_empty));
										}
										etPassword.requestFocus();
									} else if (TextUtils.isEmpty(etUserName
											.getText().toString())) {
										if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

											etUserName.setError("Fill Blank");

										} else if (lang
												.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
											etUserName
													.setError(getResources()
															.getString(
																	R.string.ar_not_empty));
										}
										etUserName.requestFocus();
									} else if (!etEmail.getText().toString().matches(emailPattern)) {
										if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

											etEmail.setError("Email format is invalid");

										} else if (lang
												.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
											etEmail
													.setError(getResources()
															.getString(
																	R.string.ar_email_format_is_invalid));
										}
										etEmail.requestFocus();
									} else {
										new RgisterAsyTask(
												LoginComicAppActivity.this)
												.execute(etEmail.getText()
														.toString().trim(),
														etPassword.getText()
																.toString(),
														etUserName.getText()
																.toString()
																.trim());
									}
								} else {

									if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

										showAlertDialog(
												LoginComicAppActivity.this,
												"No Internet Connection",
												"Please, check your internet connection.");

									} else if (lang
											.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
										// TODO
										showAlertDialog(
												LoginComicAppActivity.this,
												getResources()
														.getString(
																R.string.ar_no_connect_internet),
												getResources()
														.getString(
																R.string.ar_plaese_check_your_internet_connecttion));
									}
								}
							} else {
								if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

									showAlertDialog(LoginComicAppActivity.this,
											"", "Please, choose avatar");

								} else if (lang
										.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
									// TODO
									showAlertDialog(
											LoginComicAppActivity.this,
											"",
											getResources()
													.getString(
															R.string.ar_plaese_choose_avartar));
								}
							}

						}
					});

					registerDialog.setCancelable(false);
					registerDialog.getWindow().setSoftInputMode(
							WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
					registerDialog.show();

				}
			});
			loginDialog.setCancelable(false);
			loginDialog.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			loginDialog.show();

			break;
		case R.id.btnInformation:
			// open information dialog
			final Dialog dialog = new Dialog(LoginComicAppActivity.this,
					R.style.DialogDoNotDim);
			dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

			dialog.setContentView(R.layout.dialog_information);
			dialog.getWindow().setLayout((int) (Config.screenWidth * 0.8),
					(int) (Config.screenHeight * 0.75));
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
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		unregisterReceiver(mReceiver);
	}

	// *************************************************************************8******//
	// *************************************************************************8******//

	// ......................................................................................
	@SuppressWarnings("deprecation")
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		mfacebook.facebook.authorizeCallback(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {

			Uri mImageCaptureUri;
			switch (requestCode) {
			case REQUEST_SAVE:

				// if (requestCode == PICK_FROM_FILE) {
				mImageCaptureUri = data.getData();
				pathImage = getRealPathFromURI(mImageCaptureUri); // from
																	// Gallery
				if (pathImage == null)
					pathImage = mImageCaptureUri.getPath(); // from File Manager

				if (pathImage != null) {
					File file = new File(pathImage);
					if (file.getName().toLowerCase().endsWith("jpg")
							|| file.getName().toLowerCase().endsWith("png")) {
						bitmapAvatar = BitmapFactory.decodeFile(pathImage);
						imgUpLoadAvatar
								.setBackgroundDrawable(new BitmapDrawable(
										bitmapAvatar));
					}
				}

				break;

			}
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		IntentFilter filter = new IntentFilter(
				"com.varundroid.instademo.responselistener");
		registerReceiver(mResponseListener, filter);

		if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {
			// TODO
			btnLoginDirectly
					.setBackgroundResource(R.drawable.en_directly_login);
			btnLoginFacebook
					.setBackgroundResource(R.drawable.en_login_facebook);
			btnLoginTwitter.setBackgroundResource(R.drawable.en_login_twitter);
			btnLoginInstagram
					.setBackgroundResource(R.drawable.en_login_instagram);
			btnSkip.setBackgroundResource(R.drawable.en_ic_skip);

		} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
			// TODO
			btnLoginDirectly
					.setBackgroundResource(R.drawable.ar_directly_login);
			btnLoginFacebook
					.setBackgroundResource(R.drawable.ar_login_facebook);
			btnLoginTwitter.setBackgroundResource(R.drawable.ar_login_twitter);
			btnLoginInstagram
					.setBackgroundResource(R.drawable.ar_login_instagram);
			btnSkip.setBackgroundResource(R.drawable.button_skip);

		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		unregisterReceiver(mResponseListener);
	}

	// .......................................................................................

	public String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(contentUri, proj, null, null, null);

		if (cursor == null)
			return null;

		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

		cursor.moveToFirst();

		return cursor.getString(column_index);
	}

	/**
	 * Rigister
	 * 
	 * @author Thang
	 * 
	 */
	class RgisterAsyTask extends AsyncTask<String, Void, String> {

		Activity activity;
		ProgressDialog dialog;

		public RgisterAsyTask(Activity activity) {
			this.activity = activity;
			this.dialog = new ProgressDialog(activity);
		}

		// run UI before
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

				dialog.setMessage("Loading...");

			} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
				// TODO
				dialog.setMessage(getResources().getString(R.string.ar_loading)
						+ "...");
			}
			if (dialog != null)
				dialog.show();
		}

		// run UI alter
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);

			if (dialog.isShowing())
				dialog.dismiss();
			if (result.trim().equals("1")) {
				// activity.runOnUiThread(new Runnable() {
				// public void run() {
				registerDialog.dismiss();
				loginDialog.show();
				// Toast.makeText(activity, "success", Toast.LENGTH_LONG)
				// .show();
				// }
				// });
			} else {
				// activity.runOnUiThread(new Runnable() {
				// public void run() {
				if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

					showAlertDialog(activity, "Register invalid",
							"Please, try again.");

				} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
					// TODO
					showAlertDialog(
							activity,
							getResources().getString(R.string.ar_register_fail),
							getResources().getString(
									R.string.ar_please_try_again));
				}

				// Toast.makeText(activity, "success",
				// Toast.LENGTH_LONG).show();
				// }
				// });
			}

		}

		@Override
		protected String doInBackground(String... params) {

			File file = new File(pathImage);
			final UploadImageToServer upload = new UploadImageToServer();
			upload.uploadFile(pathImage, file.getName());
			Log.i("ssssssssssss", upload.urlImage);
			String result = "-1";
			if (!upload.urlImage.equals("")) {

				ArrayList<NameValuePair> arrNameValuePairs = new ArrayList<NameValuePair>();
				arrNameValuePairs
						.add(new BasicNameValuePair("email", params[0]));
				arrNameValuePairs
						.add(new BasicNameValuePair("pass", params[1]));
				arrNameValuePairs.add(new BasicNameValuePair("avatar",
						"http://baraahgroup.com/admin" + upload.urlImage));
				arrNameValuePairs
						.add(new BasicNameValuePair("name", params[2]));

				JSONParser jsonParser = new JSONParser();
				String json = jsonParser.makeHttpRequest(Config.API_REGISTER,
						arrNameValuePairs).trim();
				Log.i("1222222222wwwwwww", json);

				result = json;
			} else {
				result = "-1";
			}

			return result;
		}
	}

	/**
	 * Login face book
	 * 
	 * @author Thang
	 * 
	 */
	class loginFaceBookAsyTask extends AsyncTask<Void, Void, Void> {

		Activity activity;
		ProgressDialog dialog;

		public loginFaceBookAsyTask(Activity activity) {
			this.activity = activity;
			this.dialog = new ProgressDialog(activity);
		}

		// run UI before
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

				dialog.setMessage("Loading...");

			} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
				// TODO
				dialog.setMessage(getResources().getString(R.string.ar_loading)
						+ "...");
			}
			dialog.show();
		}

		// run UI alter
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (dialog.isShowing())
				dialog.dismiss();

		}

		@Override
		protected Void doInBackground(Void... params) {
			mfacebook = FbActionEventSingletonClass
					.getInstance(LoginComicAppActivity.this);
			mfacebook.loginToFacebook();
			// mfacebook.getProfileInformation();
			// loginToFacebook();
			return null;
		}
	}

	/**
	 * Broaddcast Receiver instagram
	 * 
	 */
	public class ResponseListener extends BroadcastReceiver {

		public static final String ACTION_RESPONSE = "com.varundroid.instademo.responselistener";
		public static final String EXTRA_NAME = "90293d69-2eae-4ccd-b36c-a8d0c4c1bec6";
		public static final String EXTRA_ACCESS_TOKEN = "bed6838a-65b0-44c9-ab91-ea404aa9eefc";
		public static final String AVATAR = "com.varundroid.instademo.responselistener";
		public static final String USER_ACCOUNT = "CLUBAPP.BSP.VN";
		public static final String TYPE = "TYPE";

		@Override
		public void onReceive(Context context, Intent intent) {
			// mInstaImpl.dismissDialog();
			Bundle extras = intent.getExtras();
			User user = new User();
			user.fullName = extras.getString(EXTRA_NAME);
			user.email = extras.getString(USER_ACCOUNT);
			user.urlAvatar = extras.getString(AVATAR);
			user.type = extras.getString(TYPE);
			// Toast.makeText(LoginComicAppActivity.this
			// ,user.email+" \n"+user.type ,Toast.LENGTH_LONG).show();

			if (connectionDetector.isConnectingToInternet()) {

				new LoginByNetworkInterNet(LoginComicAppActivity.this)
						.execute(user);
			} else {

				if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {
					showAlertDialog(LoginComicAppActivity.this,
							"No Internet Connection",
							"Please, check your internet connection.");

				} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
					// TODO
					showAlertDialog(
							LoginComicAppActivity.this,
							getResources().getString(
									R.string.ar_no_connect_internet),
							getResources()
									.getString(
											R.string.ar_plaese_check_your_internet_connecttion));
				}
			}

		}
	}

	final InstagramDialogListener lister = new InstagramDialogListener() {

		@Override
		public void onError(String value) {

		}

		@Override
		public void onComplete(String value) {

		}
	};

	final TwDialogListener twDialogListener = new TwDialogListener() {

		@Override
		public void onError(String value) {

		}

		@Override
		public void onComplete(String value) {

			User user = new User();
			user.fullName = mTwitter.getUsername();
			user.urlAvatar = mTwitter.getAvatar();
			user.email = mTwitter.getUserAccout();
			user.type = "twitter";

			new LoginByNetworkInterNet(LoginComicAppActivity.this)
					.execute(user);

		}
	};

	public class LoginByNetworkInterNet extends AsyncTask<User, Void, Void> {

		Activity activity;
		ProgressDialog dialog;

		public LoginByNetworkInterNet(Activity activity) {

			this.activity = activity;
			this.dialog = new ProgressDialog(activity);
		}

		// run UI before
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

				dialog.setMessage("Loading...");

			} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
				// TODO
				dialog.setMessage(getResources().getString(R.string.ar_loading)
						+ "...");
			}

			dialog.show();
		}

		// run UI alter
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (dialog.isShowing())
				dialog.dismiss();
		}

		@Override
		protected Void doInBackground(User... params) {
			ArrayList<NameValuePair> arrNameValuePairs = new ArrayList<NameValuePair>();
			arrNameValuePairs.add(new BasicNameValuePair("username",
					params[0].email));
			arrNameValuePairs
					.add(new BasicNameValuePair("type", params[0].type));
			arrNameValuePairs.add(new BasicNameValuePair("avatar",
					params[0].urlAvatar));
			JSONParser jsonParser = new JSONParser();
			String json = jsonParser.makeHttpRequest(Config.API_LOGIN_TYPE,
					arrNameValuePairs);
			if (json != null)
				if (json.equals("-1") || json.equals("0")) {

					runOnUiThread(new Runnable() {
						public void run() {
							if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

								Toast.makeText(LoginComicAppActivity.this,
										"Log In Invaild", Toast.LENGTH_LONG)
										.show();

							} else if (lang
									.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
								// TODO
								Toast.makeText(
										LoginComicAppActivity.this,
										getResources().getString(
												R.string.ar_login_fail),
										Toast.LENGTH_LONG).show();
							}

						}
					});

				} else {

					Config.USER = params[0];
					Config.USER.idUser = json;
					runOnUiThread(new Runnable() {
						public void run() {
							// Toast.makeText(LoginComicAppActivity.this,
							// String.valueOf(Config.USER.idUser),
							// Toast.LENGTH_LONG).show();
						}
					});
					String avatar = Config.USER.urlAvatar;
					Config.USER.drawableAvatar = DownLoadAvatar(Config.USER.urlAvatar);
					Intent intent = new Intent(getApplicationContext(),
							ComicListActivity.class);
					startActivity(intent);
					LoginComicAppActivity.this.finish();
					MainActivity.logIn(Config.USER.idUser,
							avatar.substring(avatar.lastIndexOf("/") + 1));
				}

			return null;
		}

	}

	/**
	 * 
	 */
	public class LoginByEmailAsyTask extends AsyncTask<String, Void, Void> {
		Activity activity;
		ProgressDialog dialog;

		public LoginByEmailAsyTask(Activity activity) {
			this.activity = activity;
			this.dialog = new ProgressDialog(activity);
		}

		// run UI before
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

				dialog.setMessage("Loading...");

			} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
				// TODO
				dialog.setMessage(getResources().getString(R.string.ar_loading)
						+ "...");
			}

			dialog.show();
		}

		// run UI alter
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (dialog.isShowing())
				dialog.dismiss();
		}

		// run background
		/**
		 * 
		 */
		@Override
		protected Void doInBackground(String... params) {

			ArrayList<NameValuePair> arrNameValuePairs = new ArrayList<NameValuePair>();
			arrNameValuePairs.add(new BasicNameValuePair("email", params[0]));
			arrNameValuePairs.add(new BasicNameValuePair("pass", params[1]));

			JSONParser jsonParser = new JSONParser();
			String json = jsonParser.makeHttpRequest(Config.API_LOGIN_EMAIL,
					arrNameValuePairs).trim();
			if (json.equals("") || json.equals("0")) {

				runOnUiThread(new Runnable() {
					public void run() {
						if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

							Toast.makeText(LoginComicAppActivity.this,
									"Log In Invaild", Toast.LENGTH_LONG).show();

						} else if (lang
								.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
							// TODO
							Toast.makeText(
									LoginComicAppActivity.this,
									getResources().getString(
											R.string.ar_login_fail),
									Toast.LENGTH_LONG).show();
						}

					}
				});

			} else {
				try {
					JSONObject jsonObject = convertJSONObject(json);
					jsonArray = jsonObject.getJSONArray("result");
					getUser();
				} catch (JSONException e) {

				}
			}
			return null;
		}
	}

	void getUser() {

		try {
			for (int i = 0; i < jsonArray.length(); i++) {

				JSONObject item = jsonArray.getJSONObject(i);
				Config.USER = new User();
				Config.USER.idUser = item.getString("userID");
				Config.USER.email = item.getString("email");
				Config.USER.userName = item.getString("username");
				Config.USER.urlAvatar = item.getString("avarta");
				Config.USER.pass = item.getString("pass");
				Config.USER.type = item.getString("type");
				Config.USER.drawableAvatar = DownLoadAvatar(Config.USER.urlAvatar);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						// Toast.makeText(getBaseContext(), ,
						// Toast.LENGTH_LONG).show();
					}
				});

			}

			String avatar = Config.USER.urlAvatar;
			MainActivity.logIn(Config.USER.idUser,
					avatar.substring(avatar.lastIndexOf("/") + 1));
			Intent intent = new Intent(getApplicationContext(),
					ComicListActivity.class);
			startActivity(intent);
			LoginComicAppActivity.this.finish();

		} catch (JSONException e) {
			e.printStackTrace();

			runOnUiThread(new Runnable() {
				public void run() {
					if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

						Toast.makeText(LoginComicAppActivity.this,
								"Log in fail", Toast.LENGTH_LONG).show();

					} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
						// TODO
						Toast.makeText(
								LoginComicAppActivity.this,
								getResources()
										.getString(R.string.ar_login_fail),
								Toast.LENGTH_LONG).show();
					}

				}
			});
		}
	}

	/**
	 */
	Drawable DownLoadAvatar(String urlAvatar) {
		Bitmap myBitmap = null;
		URL url;
		try {
			url = new URL(urlAvatar);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setDoInput(true);
			connection.connect();
			InputStream input = connection.getInputStream();
			myBitmap = BitmapFactory.decodeStream(input);
			writeFile(myBitmap,
					urlAvatar.substring(urlAvatar.lastIndexOf("/") + 1));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new BitmapDrawable(myBitmap);
	}

	/**
	 */
	JSONObject convertJSONObject(String json) {
		JSONObject jObj = null;
		try {

			jObj = new JSONObject(json);

		} catch (JSONException e) {
			Log.e("JSON Parser", "Error parsing data " + e.toString());
		}
		return jObj;
	}

	void writeFile(final Bitmap bitmap, final String nameFile) {

		// Bitmap myBitmap = BitmapFactory.;

		String path = "/data/data/com.bsp.comicapp/avatar";
		final File dir = new File(path);

		if (dir.exists())
			dir.mkdir();

		final File mediaFile = new File(dir, nameFile);
		FileOutputStream stream;
		try {

			stream = new FileOutputStream(mediaFile.getPath());
			bitmap.compress(CompressFormat.JPEG, 100, stream);
			stream.flush();
			stream.close();

		} catch (Exception e) {
			Log.e("Could not save", e.toString());
		}
	}

	// *************************************************************************************//
	class GetBackgroundAnsyntask extends AsyncTask<Void, Void, Integer> {

		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(Integer result) {
			// TODO Auto-generated method stub
			if (result == 1) {
				imageView.setBackgroundDrawable(Config.Bg_Landscape);
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

}
