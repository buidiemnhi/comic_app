package com.bsp.comicapp;

import java.util.ArrayList;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import com.bsp.comicapp.adapter.FancyCoverFlowSampleAdapter;
import com.bsp.comicapp.displayimage.util.ImageCache.ImageCacheParams;
import com.bsp.comicapp.displayimage.util.ImageFetcher;
import com.bsp.comicapp.instagram.InstaImpl;
import com.bsp.comicapp.instagram.InstaImpl.InstagramDialogListener;
import com.bsp.comicapp.util.Config;
import com.bsp.comicapp.util.ConnectionDetector;
import com.bsp.comicapp.util.TwitterConst;
import com.facebook.AppEventsLogger;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.TypedValue;
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

public class LoginActivity extends FragmentActivity implements OnClickListener {

	private String lang = "eng";
	private ImageView imgLogo;
	private Button btnLoginDirectly;
	private Button btnLoginFacebook;
	private Button btnLoginTwitter;
	private Button btnLoginInstagram;
	private Button btnSkip;
	private Button btnInformation;
	private FancyCoverFlow fancyCoverFlow;
	private ConnectionDetector cd;

	// Display image
	private static final String IMAGE_CACHE_DIR = "thumbs";
	private ImageFetcher mImageFetcher;
	private ArrayList<String> arrImageUrl = new ArrayList<String>();

	// Twitter variable
	public static Twitter twitter = null;
	private static RequestToken requestToken;
	private static SharedPreferences twitterSharedPreferences;
	private AskOAuth askOAuth = null;
	private HandleTwitterOAuthCallback handleTwitterOAuthCallback = null;

	// Facebook variable
	private UiLifecycleHelper uiHelper;
	private GraphUser user = null;
	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};
	private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
		@Override
		public void onError(FacebookDialog.PendingCall pendingCall,
				Exception error, Bundle data) {
			Log.d("Nam Dinh", String.format("Error: %s", error.toString()));
		}

		@Override
		public void onComplete(FacebookDialog.PendingCall pendingCall,
				Bundle data) {
			Log.d("Nam Dinh", "Success!");
		}
	};

	// Instagram variable
	private InstagramDialogListener instagramDialogListener = new InstagramDialogListener() {
		// TODO
		@Override
		public void onError(String value) {
			Toast.makeText(getApplicationContext(),
					"Login via Instagram error. Please try again!",
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void onComplete(String value) {
			Intent intent = new Intent(getApplicationContext(),
					ComicListActivity.class);
			intent.putExtra("lang", lang);
			startActivity(intent);
			LoginActivity.this.finish();
		}
	};
	private InstaImpl mInstaImpl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_login);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			lang = extras.getString("lang");
		}

		cd = new ConnectionDetector(getApplicationContext());

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

		// Load image
		ImageCacheParams cacheParams = new ImageCacheParams(
				getApplicationContext(), IMAGE_CACHE_DIR);
		cacheParams.setMemCacheSizePercent(0.1f); // Set memory cache to
													// 25% of
													// app memory
		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		mImageFetcher = new ImageFetcher(getApplicationContext(),
				(int) Config.screenHeight / 5 * 2 * 640 / 280,
				(int) Config.screenHeight / 5 * 2);

		mImageFetcher.setLoadingImage(R.drawable.banner_image_load);
		mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);

		// ////////////////////////////////////////
		arrImageUrl.add("http://chipigroup.vacau.com/0.jpg");
		arrImageUrl.add("http://chipigroup.vacau.com/0.jpg");
		arrImageUrl.add("http://chipigroup.vacau.com/0.jpg");
		arrImageUrl.add("http://chipigroup.vacau.com/0.jpg");
		arrImageUrl.add("http://chipigroup.vacau.com/0.jpg");
		arrImageUrl.add("http://chipigroup.vacau.com/0.jpg");
		arrImageUrl.add("http://chipigroup.vacau.com/0.jpg");
		arrImageUrl.add("http://chipigroup.vacau.com/0.jpg");
		arrImageUrl.add("http://chipigroup.vacau.com/0.jpg");
		arrImageUrl.add("http://chipigroup.vacau.com/0.jpg");
		arrImageUrl.add("http://chipigroup.vacau.com/0.jpg");

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

		/**
		 * Facebook
		 */
		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		/**
		 * Twitter
		 */
		twitterSharedPreferences = getSharedPreferences(
				TwitterConst.PREFERENCE_NAME, MODE_PRIVATE);

		Uri uri = getIntent().getData();
		if (uri != null && uri.toString().startsWith(TwitterConst.CALLBACK_URL)) {

			String verifier = uri
					.getQueryParameter(TwitterConst.IEXTRA_OAUTH_VERIFIER);
			String[] params = new String[] { verifier };
			handleTwitterOAuthCallback = new HandleTwitterOAuthCallback();
			handleTwitterOAuthCallback.execute(params);
		}

		btnLoginFacebook.setOnClickListener(this);
		btnLoginInstagram.setOnClickListener(this);
		btnLoginTwitter.setOnClickListener(this);
		btnLoginDirectly.setOnClickListener(this);
		btnSkip.setOnClickListener(this);
		btnInformation.setOnClickListener(this);

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// -------Login Facebook--------//
		if (Session.getActiveSession() != null) {
			Session.getActiveSession().onActivityResult(this, requestCode,
					resultCode, data);
		}
		uiHelper.onActivityResult(requestCode, resultCode, data, dialogCallback);

	}

	@Override
	protected void onResume() {
		super.onResume();

		/**
		 * Facebook
		 */
		uiHelper.onResume();
		AppEventsLogger.activateApp(this);
		updateFacebookUI();

		/**
		 * Twitter
		 */
		if (isConnectedTwitter()) {
			if (twitter != null) {
				String account = twitterSharedPreferences.getString(
						TwitterConst.PREF_KEY_ACCOUNT, "");

				// findPreference("pref_twitter")
				// .setSummary("Account: " + account);

			} else {
				// findPreference("pref_twitter").setSummary("Not set");
				disconnectTwitter();
			}

		} else {
			// findPreference("pref_twitter").setSummary("Not set");
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
		if (askOAuth != null) {
			askOAuth.cancel(true);
		}
		if (handleTwitterOAuthCallback != null) {
			handleTwitterOAuthCallback.cancel(true);
		}
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
		AlertDialog alertDialog = new AlertDialog.Builder(context).create();
		// Setting Dialog Title
		alertDialog.setTitle(title);
		// Setting Dialog Message
		alertDialog.setMessage(message);

		// Setting alert dialog icon
		alertDialog.setIcon(R.drawable.ic_fail);

		// Setting OK Button
		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		updateFacebookUI();
	}

	@SuppressWarnings("deprecation")
	private void updateFacebookUI() {
		Session session = Session.getActiveSession();
		boolean enableButtons = (session != null && session.isOpened());

		if (enableButtons) {
			// make request to the /me API
			Request.executeMeRequestAsync(session,
					new Request.GraphUserCallback() {
						// callback after Graph API response with user object
						public void onCompleted(GraphUser user,
								Response response) {
							if (user != null) {
								LoginActivity.this.user = user;
								Intent intent = new Intent(
										getApplicationContext(),
										ComicListActivity.class);
								intent.putExtra("lang", lang);
								startActivity(intent);
								LoginActivity.this.finish();
							} else {
								// findPreference("pref_facebook").setSummary(
								// "Not set");
							}
						}

					});
		} else {
			// findPreference("pref_facebook").setSummary("Not set");
		}
	}

	private boolean isConnectedTwitter() {
		return twitterSharedPreferences.getString(TwitterConst.PREF_KEY_TOKEN,
				null) != null;
	}

	private class HandleTwitterOAuthCallback extends
			AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {

			try {
				AccessToken accessToken = twitter.getOAuthAccessToken(
						requestToken, params[0]);
				long userID = accessToken.getUserId();
				User user = twitter.showUser(userID);

				Editor e = twitterSharedPreferences.edit();
				e.putString(TwitterConst.PREF_KEY_TOKEN, accessToken.getToken());
				e.putString(TwitterConst.PREF_KEY_SECRET,
						accessToken.getTokenSecret());
				e.putString(TwitterConst.PREF_KEY_ACCOUNT, user.getName());

				e.commit();
			} catch (Exception e) {

			}
			return "Executed";
		}

		@Override
		protected void onPostExecute(String result) {
			if (isConnectedTwitter()) {
				if (twitter != null) {
					Intent intent = new Intent(getApplicationContext(),
							ComicListActivity.class);
					intent.putExtra("lang", lang);
					startActivity(intent);
					LoginActivity.this.finish();

				} else {
					// findPreference("pref_twitter").setSummary("Not set");
					disconnectTwitter();
				}

			} else {
				// findPreference("pref_twitter").setSummary("Not set");
			}
		}
	}

	private class AskOAuth extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
			configurationBuilder.setOAuthConsumerKey(TwitterConst.CONSUMER_KEY);
			configurationBuilder
					.setOAuthConsumerSecret(TwitterConst.CONSUMER_SECRET);
			Configuration configuration = configurationBuilder.build();
			twitter = new TwitterFactory(configuration).getInstance();
			try {
				requestToken = twitter
						.getOAuthRequestToken(TwitterConst.CALLBACK_URL);

				startActivity(new Intent(Intent.ACTION_VIEW,
						Uri.parse(requestToken.getAuthenticationURL())));
			} catch (TwitterException e) {
				e.printStackTrace();
			}
			return "Executed";
		}

		@Override
		protected void onPostExecute(String result) {

		}
	}

	private void disconnectTwitter() {
		SharedPreferences.Editor editor = twitterSharedPreferences.edit();
		editor.remove(TwitterConst.PREF_KEY_TOKEN);
		editor.remove(TwitterConst.PREF_KEY_SECRET);
		editor.commit();
	}

	/**
	 * Instagram
	 */

	public class ResponseListener extends BroadcastReceiver {

		public static final String ACTION_RESPONSE = "com.varundroid.instademo.responselistener";
		public static final String EXTRA_NAME = "90293d69-2eae-4ccd-b36c-a8d0c4c1bec6";
		public static final String EXTRA_ACCESS_TOKEN = "bed6838a-65b0-44c9-ab91-ea404aa9eefc";

		@Override
		public void onReceive(Context context, Intent intent) {
			// mInstaImpl.dismissDialog();
			Bundle extras = intent.getExtras();
			String name = extras.getString(EXTRA_NAME);
			String accessToken = extras.getString(EXTRA_ACCESS_TOKEN);
			final AlertDialog.Builder alertDialog = new AlertDialog.Builder(
					LoginActivity.this);
			alertDialog.setTitle("Your Details");
			alertDialog.setMessage("Name - " + name + ", Access Token - "
					+ accessToken);
			alertDialog.setPositiveButton("Ok",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					});
			alertDialog.show();
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.btnSkip:
			// TODO
			Intent intent = new Intent(getApplicationContext(),
					ComicListActivity.class);
			intent.putExtra("lang", lang);
			startActivity(intent);

			LoginActivity.this.finish();
			break;
		case R.id.btnLoginFacebook:
			Session session = Session.getActiveSession();
			boolean enableButtons = (session != null && session.isOpened());
			if (enableButtons) {
				if (LoginActivity.this.user != null) {
					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
							LoginActivity.this);
					// set title
					alertDialogBuilder.setTitle("Facebook Information");
					// set dialog message
					alertDialogBuilder
							.setMessage(
									"You are logged in as " + user.getName()
											+ ". Do you want to logout?")
							.setCancelable(false)
							.setPositiveButton("Yes",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											Session session = Session
													.getActiveSession();
											session.closeAndClearTokenInformation();

											dialog.cancel();
										}
									})
							.setNegativeButton("No",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {

											dialog.cancel();
										}

									});

					alertDialogBuilder.setCancelable(false);
					alertDialogBuilder.show();
				} else {
					if (!cd.isConnectingToInternet()) {
						showAlertDialog(LoginActivity.this,
								"No Internet Connection",
								"To login Facebook you need to be connected to the Internet.");

					} else {
						// start Facebook Login
						Session.openActiveSession(this, true,
								new Session.StatusCallback() {
									// callback when session changes state
									@SuppressWarnings("deprecation")
									@Override
									public void call(Session session,
											SessionState state,
											Exception exception) {
										if (session.isOpened()) {
											// make request to the /me API
											Request.executeMeRequestAsync(
													session,
													new Request.GraphUserCallback() {

														// callback after Graph
														// API
														// response with user
														// object
														@Override
														public void onCompleted(
																GraphUser user,
																Response response) {
															if (user != null) {
																LoginActivity.this.user = user;
																Intent intent = new Intent(
																		getApplicationContext(),
																		ComicListActivity.class);
																intent.putExtra(
																		"lang",
																		lang);
																startActivity(intent);
																LoginActivity.this
																		.finish();

															}
														}
													});
										}
									}
								});
					}

				}

			} else {
				if (!cd.isConnectingToInternet()) {
					showAlertDialog(LoginActivity.this,
							"No Internet Connection",
							"To login Facebook you need to be connected to the Internet.");

				} else {
					// start Facebook Login
					Session.openActiveSession(this, true,
							new Session.StatusCallback() {
								// callback when session changes state
								@SuppressWarnings("deprecation")
								@Override
								public void call(Session session,
										SessionState state, Exception exception) {
									if (session.isOpened()) {
										// make request to the /me API
										Request.executeMeRequestAsync(
												session,
												new Request.GraphUserCallback() {
													// callback after Graph API
													// response with user object

													@Override
													public void onCompleted(
															GraphUser user,
															Response response) {
														if (user != null) {
															// LoginActivity.this.user
															// = user;

														}

													}
												});
									}
								}
							});
				}

			}

			break;
		case R.id.btnLoginTwitter:

			if (isConnectedTwitter()) {
				if (twitter != null) {
					String account = twitterSharedPreferences.getString(
							TwitterConst.PREF_KEY_ACCOUNT, "");

					AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
							LoginActivity.this);
					// set title
					alertDialogBuilder.setTitle("Twitter Information");
					// set dialog message
					alertDialogBuilder
							.setMessage(
									"You are logged in as " + account
											+ ". Do you want to logout?")
							.setCancelable(false)
							.setPositiveButton("Yes",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											disconnectTwitter();
											dialog.cancel();
										}
									})
							.setNegativeButton("No",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {

											dialog.cancel();
										}

									});

					alertDialogBuilder.setCancelable(false);
					alertDialogBuilder.show();
				} else {
					if (!cd.isConnectingToInternet()) {
						showAlertDialog(LoginActivity.this,
								"No Internet Connection",
								"To login Twitter you need to be connected to the Internet.");
					} else {
						askOAuth = new AskOAuth();
						askOAuth.execute();
					}

				}

			} else {
				if (!cd.isConnectingToInternet()) {
					showAlertDialog(LoginActivity.this,
							"No Internet Connection",
							"To login Twitter you need to be connected to the Internet.");

				} else {
					askOAuth = new AskOAuth();
					askOAuth.execute();
				}
			}

			break;
		case R.id.btnLoginInstagram:
			mInstaImpl = new InstaImpl(LoginActivity.this);
			mInstaImpl.setAuthAuthenticationListener(instagramDialogListener);
			break;

		case R.id.btnLoginDirectly:
			// open information dialog
			final Dialog loginDialog = new Dialog(LoginActivity.this,
					R.style.DialogSlideAnim);
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
			EditText etEmail = (EditText) loginDialog
					.findViewById(R.id.etEmail);
			EditText etPassword = (EditText) loginDialog
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

			layout_email.setPadding(
					(int) (Config.screenWidth * 0.45 * 90 / 731), 2, 3, 2);
			layout_password.setPadding(
					(int) (Config.screenWidth * 0.45 * 90 / 731), 2, 3, 2);

			resizeButton(btnCancel, (int) (Config.screenWidth * 0.11),
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
					loginDialog.dismiss();
				}
			});

			btnLogin.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub

				}
			});

			btnRegister.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					loginDialog.dismiss();
					// open register dialog
					final Dialog registerDialog = new Dialog(
							LoginActivity.this, R.style.DialogSlideAnim);
					registerDialog
							.requestWindowFeature(Window.FEATURE_NO_TITLE);
					registerDialog.setContentView(R.layout.dialog_register);

					Button btnCancel = (Button) registerDialog
							.findViewById(R.id.btnCancel);
					Button btnLogo = (Button) registerDialog
							.findViewById(R.id.btnLogo);
					LinearLayout layout_login = (LinearLayout) registerDialog
							.findViewById(R.id.layout_login);
					LinearLayout layout_username = (LinearLayout) registerDialog
							.findViewById(R.id.layout_username);
					LinearLayout layout_email = (LinearLayout) registerDialog
							.findViewById(R.id.layout_email);
					LinearLayout layout_password = (LinearLayout) registerDialog
							.findViewById(R.id.layout_password);
					EditText etUserName = (EditText) registerDialog
							.findViewById(R.id.etUsername);
					EditText etEmail = (EditText) registerDialog
							.findViewById(R.id.etEmail);
					EditText etPassword = (EditText) registerDialog
							.findViewById(R.id.etPassword);
					LinearLayout layout_login_button = (LinearLayout) registerDialog
							.findViewById(R.id.layout_login_button);
					Button btnRegister = (Button) registerDialog
							.findViewById(R.id.btnRegister);

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

					layout_login.setPadding((int) (Config.screenHeight * 0.07),
							(int) (Config.screenHeight * 0.04),
							(int) (Config.screenHeight * 0.07),
							(int) (Config.screenHeight * 0.04));
					layout_login_button.setPadding(0, 0, 0,
							(int) (Config.screenHeight * 0.06));

					layout_username.getLayoutParams().width = (int) (Config.screenWidth * 0.45);
					layout_username.getLayoutParams().height = (int) (Config.screenWidth * 0.45 * 74 / 731);
					layout_email.getLayoutParams().width = (int) (Config.screenWidth * 0.45);
					layout_email.getLayoutParams().height = (int) (Config.screenWidth * 0.45 * 74 / 731);
					layout_password.getLayoutParams().width = (int) (Config.screenWidth * 0.45);
					layout_password.getLayoutParams().height = (int) (Config.screenWidth * 0.45 * 74 / 731);

					layout_username.setPadding(
							(int) (Config.screenWidth * 0.45 * 90 / 731), 2, 3,
							2);
					layout_email.setPadding(
							(int) (Config.screenWidth * 0.45 * 90 / 731), 2, 3,
							2);
					layout_password.setPadding(
							(int) (Config.screenWidth * 0.45 * 90 / 731), 2, 3,
							2);

					resizeButton(btnCancel, (int) (Config.screenWidth * 0.11),
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
					btnCancel.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							registerDialog.dismiss();
						}
					});
					btnRegister.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub

						}
					});

					registerDialog.setCancelable(false);
					registerDialog.getWindow().setSoftInputMode(
							WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
					registerDialog.show();

				}
			});

			loginDialog.setCanceledOnTouchOutside(false);
			loginDialog.getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			loginDialog.show();

			break;
		case R.id.btnInformation:
			// open information dialog
			final Dialog dialog = new Dialog(LoginActivity.this,
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
			dialog.setCancelable(false);
			dialog.show();
			break;

		default:
			break;
		}

	}
}
