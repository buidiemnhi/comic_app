package com.bsp.comicapp.instagram;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;
import org.json.JSONTokener;

import com.bsp.comicapp.LoginComicAppActivity;
import com.bsp.comicapp.LoginComicAppActivity.ResponseListener;
import com.bsp.comicapp.util.Config;
import com.bsp.comicapp.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

public class InstaImpl {

	private static final String TOKENURL = "https://api.instagram.com/oauth/access_token";
	private static final String AUTHURL = "https://api.instagram.com/oauth/authorize/";
	public static final String APIURL = "https://api.instagram.com/v1";
	public static String CALLBACKURL;
	// private static final String TAG = "Instagram Demo";

	private SessionStore mSessionStore;

	private String mAuthURLString;
	private String mTokenURLString;
	private String mAccessTokenString;
	public String mClient_id;
	public String mClient_secret;
	private String mToken;
	private ProgressDialog mProgressDialog;
	private Activity mContext;
	InstagramDialog instaLoginDialog;
	String urlAvatar;
	String userAccout;

	public InstaImpl(Activity context) {
		mContext = context;
		mSessionStore = new SessionStore(context);
		mClient_id = context.getResources().getString(R.string.instagram_id); // Recommended:
																				// Put
																				// your
																				// Instagram
																				// ID
																				// in
																				// string
																				// class
		mClient_secret = context.getResources().getString(
				R.string.instagram_secret); // Recommended: Put your Instagram
											// Secret in string class
		CALLBACKURL = context.getResources().getString(R.string.callbackurl);

		mAuthURLString = AUTHURL
				+ "?client_id="
				+ mClient_id
				+ "&redirect_uri="
				+ CALLBACKURL
				+ "&response_type=code&display=touch&scope=likes+comments+relationships";
		mTokenURLString = TOKENURL + "?client_id=" + mClient_id
				+ "&client_secret=" + mClient_secret + "&redirect_uri="
				+ CALLBACKURL + "&grant_type=authorization_code";

		mAccessTokenString = mSessionStore.getInstaAccessToken();
		instaLoginDialog = new InstagramDialog(context, mAuthURLString,
				mListener);
		instaLoginDialog.show();
		mProgressDialog = new ProgressDialog(context);
		if (LoginComicAppActivity.lang
				.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {
			mProgressDialog.setTitle("Please Wait");
		} else if (LoginComicAppActivity.lang
				.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
			mProgressDialog.setTitle(context.getResources().getString(
					R.string.ar_please_wait));
		}

		mProgressDialog.setCancelable(false);
		logOutInstargam();

	}

	void logOutInstargam() {
		CookieSyncManager.createInstance(mContext);
		CookieManager cookieManager = CookieManager.getInstance();
		cookieManager.removeSessionCookie();
		mSessionStore.resetInstagram();
	}

	private InstagramDialogListener mListener = new InstagramDialogListener() {

		@Override
		public void onError(String value) {

		}

		@Override
		public void onComplete(String value) {
			logOutInstargam();
			getAccessToken(value);
		}
	};

	private void getAccessToken(String token) {
		this.mToken = token;
		new GetInstagramTokenAsyncTask().execute();
	}

	public class GetInstagramTokenAsyncTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			try {
				URL url = new URL(mTokenURLString);
				HttpsURLConnection httpsURLConnection = (HttpsURLConnection) url
						.openConnection();
				httpsURLConnection.setRequestMethod("POST");
				httpsURLConnection.setDoInput(true);
				httpsURLConnection.setDoOutput(true);

				OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
						httpsURLConnection.getOutputStream());
				outputStreamWriter.write("client_id=" + mClient_id
						+ "&client_secret=" + mClient_secret
						+ "&grant_type=authorization_code" + "&redirect_uri="
						+ CALLBACKURL + "&code=" + mToken);

				outputStreamWriter.flush();

				// Response would be a JSON response sent by instagram
				String response = streamToString(httpsURLConnection
						.getInputStream());
				// Log.e("USER Response", response);
				JSONObject jsonObject = (JSONObject) new JSONTokener(response)
						.nextValue();
				Log.d("@#@#@#@#@#@#@#", jsonObject.toString());

				// Your access token that you can use to make future request
				mAccessTokenString = jsonObject.getString("access_token");
				// Log.e(TAG, mAccessTokenString);

				// User details like, name, id, tagline, profile pic etc.
				JSONObject userJsonObject = jsonObject.getJSONObject("user");
				// Log.e("USER DETAIL", userJsonObject.toString());

				// User ID
				String id = userJsonObject.getString("id");
				// Log.e(TAG, id);

				// Username
				String username = userJsonObject.getString("username");
				// Log.e(TAG, username);
				userAccout = username;
				// User full name
				String fullName = userJsonObject.getString("full_name");
				urlAvatar = userJsonObject.getString("profile_picture");
				// Log.e(TAG, name);
				mSessionStore.saveInstagramSession(mAccessTokenString, id,
						username, fullName);
				showResponseDialog(fullName, mAccessTokenString);
			} catch (Exception e) {
				mListener.onError("Failed to get access token");
				e.printStackTrace();

			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);

			dismissDialog();
			mListener.onComplete("");
			instaLoginDialog.dismiss();
		}

		@Override
		protected void onPreExecute() {
			if (LoginComicAppActivity.lang
					.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {
				showDialog("Getting Access Token..");
			} else if (LoginComicAppActivity.lang
					.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
				showDialog(mContext.getResources().getString(
						R.string.ar_getting_access_token));
			}

			super.onPreExecute();
		}

	}

	public void setAuthAuthenticationListener(
			InstagramDialogListener authAuthenticationListener) {
		this.mListener = authAuthenticationListener;
	}

	public String streamToString(InputStream is) throws IOException {
		String string = "";

		if (is != null) {
			StringBuilder stringBuilder = new StringBuilder();
			String line;

			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is));

				while ((line = reader.readLine()) != null) {
					stringBuilder.append(line);
				}

				reader.close();
			} finally {
				is.close();
			}

			string = stringBuilder.toString();
		}

		return string;
	}

	public void showDialog(String message) {
		mProgressDialog.setMessage(message);
		mProgressDialog.show();
	}

	public void dismissDialog() {
		if (mProgressDialog.isShowing()) {
			mProgressDialog.dismiss();
		}
	}

	public void showResponseDialog(String name, String accessToken) {
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ResponseListener.ACTION_RESPONSE);
		broadcastIntent.putExtra(ResponseListener.EXTRA_NAME, name);
		broadcastIntent.putExtra(ResponseListener.EXTRA_ACCESS_TOKEN,
				accessToken);
		broadcastIntent.putExtra(ResponseListener.USER_ACCOUNT, userAccout);
		broadcastIntent.putExtra(ResponseListener.AVATAR, urlAvatar);
		broadcastIntent.putExtra(ResponseListener.TYPE, "instagram");
		mContext.sendBroadcast(broadcastIntent);
	}

	public interface InstagramDialogListener {
		public void onComplete(String value);

		public void onError(String value);
	}
}
