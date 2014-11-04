package com.bsp.comicapp.twitter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import oauth.signpost.OAuth;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

public class TwitterApp {
	private Twitter mTwitter;
	private TwitterSession mSession;
	private AccessToken mAccessToken;
	private CommonsHttpOAuthConsumer mHttpOauthConsumer;
	private CommonsHttpOAuthProvider mHttpOauthprovider;
	private String mConsumerKey;
	private String mSecretKey;
	private ProgressDialog mProgressDlg;
	private TwDialogListener mListener;
	private Activity context;
	private String picture = "";
	private String userAccout = "";
	// http://otweet.com/authenticated
	public static final String OAUTH_CALLBACK_SCHEME = "x-oauthflow-twitter";
	public static final String OAUTH_CALLBACK_HOST = "callback";
	public static final String CALLBACK_URL = OAUTH_CALLBACK_SCHEME + "://"
			+ OAUTH_CALLBACK_HOST;

	// public static final String CALLBACK_URL ="http://dev.twitter.com";
	// "http://abhinavasblog.blogspot.com/";

	static String base_link_url = "http://www.google.co.in/";
	private static final String TWITTER_ACCESS_TOKEN_URL = "https://api.twitter.com/oauth/access_token";

	private static final String TWITTER_AUTHORZE_URL = "https://api.twitter.com/oauth/authorize";
	private static final String TWITTER_REQUEST_URL = "https://api.twitter.com/oauth/request_token";
	public static final String MESSAGE = "MonkeySays";

	// + "<a href= " + base_link_url + "</a>";
	Message msg = new Message();

	public TwitterApp(Activity context, String consumerKey, String secretKey) {
		this.context = context;

		mTwitter = new TwitterFactory().getInstance();
		mSession = new TwitterSession(context);
		mProgressDlg = new ProgressDialog(context);

		mProgressDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

		mConsumerKey = consumerKey;
		mSecretKey = secretKey;

		mHttpOauthConsumer = new CommonsHttpOAuthConsumer(mConsumerKey,
				mSecretKey);

		String request_url = TWITTER_REQUEST_URL;
		String access_token_url = TWITTER_ACCESS_TOKEN_URL;
		String authorize_url = TWITTER_AUTHORZE_URL;
		mHttpOauthprovider = new CommonsHttpOAuthProvider(request_url,
				access_token_url, authorize_url);
		// mHttpOauthprovider = new DefaultOAuthProvider(request_url,
		// access_token_url, authorize_url);
		mAccessToken = mSession.getAccessToken();
		configureToken();
		//Logout();
	}

	public void setListener(TwDialogListener listener) {
		mListener = listener;
	}

	private void configureToken() {
		if (mAccessToken != null) {
			mTwitter.setOAuthConsumer(mConsumerKey, mSecretKey);
			mTwitter.setOAuthAccessToken(mAccessToken);
		}
	}

	public boolean hasAccessToken() {
		return (mAccessToken == null) ? false : true;
	}

	public void resetAccessToken() {
		if (mAccessToken != null) {
			mSession.resetAccessToken();
			mAccessToken = null;
		}
	}

	public String getUsername() {
		return mSession.getUsername();
	}

	public String getAvatar() {
		return picture;
	}

	// public void updateStatus(String status) throws Exception {
	// try {
	// mTwitter.updateStatus(status);
	// // File f = new File("/mnt/sdcard/74.jpg");
	// // mTwitter.updateProfileImage(f);
	// } catch (TwitterException e) {
	// throw e;
	// }
	// }

	public void uploadPic(Bitmap file, String message) throws Exception {
		try {
			ConfigurationBuilder builder = new ConfigurationBuilder();
			builder.setOAuthConsumerKey("your consumer key");
			builder.setOAuthConsumerSecret("your secret key")
					.setOAuthAccessToken(mHttpOauthConsumer.getToken())
					.setOAuthAccessTokenSecret(
							mHttpOauthConsumer.getTokenSecret());
			
			Configuration configuration = builder.build();
			TwitterFactory twi = new TwitterFactory(configuration);
			Twitter twitter = twi.getInstance();
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			file.compress(Bitmap.CompressFormat.PNG, 100, stream);
			byte[] byteArray = stream.toByteArray();
			ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
			StatusUpdate status = new StatusUpdate("Monkey Says");
			status.setMedia("newyear", bis);

			twitter.updateStatus(status);
		} catch (TwitterException e) {
			// /Log.d("TAG", "Pic Upload error" + e.getExceptionCode());
			throw e;
		}
	}

	public void authorize() {
		mProgressDlg.setMessage("Initializing ...");
		mProgressDlg.show();

		new Thread() {
			@Override
			public void run() {

				String authUrl = "";
				int what = 1;
				Log.d("@@@@@@@@", mHttpOauthConsumer.toString());
				try {
					authUrl = mHttpOauthprovider.retrieveRequestToken(
							mHttpOauthConsumer, CALLBACK_URL);

					what = 0;
				} catch (Exception e) {
					e.printStackTrace();
				}
				Log.d("#######AAAAAAAAAAAAAA", String.valueOf(what));
				mHandler.sendMessage(mHandler
						.obtainMessage(what, 1, 0, authUrl));

			}

		}.start();

	}

	public void processToken(String callbackUrl) {

		// mProgressDlg.setMessage("Finalizing ...");
		// mProgressDlg.show();

		final String verifier = getVerifier(callbackUrl);
		int what = 1;
		// Toast.makeText(context, verifier, Toast.LENGTH_LONG).show();

		try {
			mHttpOauthprovider
					.retrieveAccessToken(mHttpOauthConsumer, verifier);
			// Toast.makeText(context, "new...", Toast.LENGTH_LONG).show();

			mAccessToken = new AccessToken(mHttpOauthConsumer.getToken(),
					mHttpOauthConsumer.getTokenSecret());

			configureToken();

			User user = mTwitter.verifyCredentials();

			userAccout = user.getScreenName();

			mSession.storeAccessToken(mAccessToken, user.getName());
			Log.d("picture !@#", user.getProfileImageURLHttps());
			picture = user.getProfileImageURL();

			what = 0;
		} catch (Exception e) {
			e.printStackTrace();
			// Toast.makeText(context,e.getMessage(), Toast.LENGTH_LONG).show();
		}

		mHandler.sendMessage(mHandler.obtainMessage(what, 2, 0));
		new Thread() {
			@Override
			public void run() {

			}
		}.start();
	}

	private String getVerifier(String callbackUrl) {
		String verifier = "";

		try {
			callbackUrl = callbackUrl.replace(OAUTH_CALLBACK_SCHEME, "http");

			URL url = new URL(callbackUrl);
			String query = url.getQuery();

			String array[] = query.split("&");

			for (String parameter : array) {
				String v[] = parameter.split("=");

				if (URLDecoder.decode(v[0]).equals(
						oauth.signpost.OAuth.OAUTH_VERIFIER)) {
					verifier = URLDecoder.decode(v[1]);
					break;
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return verifier;
	}

	public String getUserAccout() {
		return userAccout;
	}

	private void showLoginDialog(String url) {
		final TwDialogListener listener = new TwDialogListener() {

			public void onComplete(String value) {
				new ProcessAnsyTask().execute(value);

			}

			public void onError(String value) {
				mListener.onError("Failed opening authorization page");
			}
		};

		new TwitterDialog(context, url, listener).show();
	}

	public class ProcessAnsyTask extends AsyncTask<String, Void, Void> {

		@Override
		protected void onPostExecute(Void result) {

			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			mProgressDlg.setMessage("Finalizing ...");
			mProgressDlg.show();
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(String... params) {
			processToken(params[0]);
			return null;
		}
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			mProgressDlg.dismiss();

			if (msg.what == 1) {
				if (msg.arg1 == 1)
					mListener.onError("Error getting request token");
				else
					mListener.onError("Error getting access token");
			} else {
				if (msg.arg1 == 1)
					showLoginDialog((String) msg.obj);
				else
					mListener.onComplete("");
			}

		}

	};

	public void Logout() {

		CookieSyncManager.createInstance(context);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeSessionCookie();
		mSession.resetAccessToken();
		mSession.remove();
		mTwitter.shutdown();

	}

	public interface TwDialogListener {
		public void onComplete(String value);

		public void onError(String value);
	}
}