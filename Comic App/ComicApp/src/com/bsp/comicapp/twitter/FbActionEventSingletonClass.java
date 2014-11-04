package com.bsp.comicapp.twitter;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bsp.comicapp.LoginComicAppActivity.ResponseListener;
import com.bsp.comicapp.model.User;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.AsyncFacebookRunner.RequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.Util;

@SuppressWarnings({ "deprecation", "unused" })
public class FbActionEventSingletonClass {

	private static FbActionEventSingletonClass fbActionEvents = new FbActionEventSingletonClass();

	// Your Facebook APP ID
	private static String APP_ID = "1425073074375322";
	public static Facebook facebook = new Facebook(APP_ID);
	private static AsyncFacebookRunner mAsyncRunner;
	public static SharedPreferences mPrefs;
	private static Activity activity;
	private static int MODE_PRIVATE = 0;

	private String mAccessToken;
	private static final String TAG = "FbEvent";
	private static final int AUTO_POST = 0;
	private static ProgressDialog dialogLoading;
	String urlAvatar;

	public FbActionEventSingletonClass() {
		// facebook = new Facebook(APP_ID);
		// activity = act;
		// mAsyncRunner = new AsyncFacebookRunner(facebook);

	}

	public static FbActionEventSingletonClass getInstance(Activity act) {

		activity = act;
		mAsyncRunner = new AsyncFacebookRunner(facebook);
		logoutFromFacebook();
		return fbActionEvents;
	}

	public boolean loginToFacebook() {

		mPrefs = activity.getPreferences(MODE_PRIVATE);
		String access_token = mPrefs.getString("access_token", null);
		long expires = mPrefs.getLong("access_expires", 0);

		if (access_token != null && facebook.isSessionValid()) {
			facebook.setAccessToken(access_token);
			Log.w(TAG, "First if excuted");

			// MyTask postofwall=new MyTask();
			// postofwall.execute(0);

			Log.d("FB Sessions", "" + facebook.isSessionValid());
		}// end of if

		if (expires != 0) {
			facebook.setAccessExpires(expires);
		}// end of if
		
		logoutFromFacebook();
		
		if (!facebook.isSessionValid()) {
			facebook.authorize(activity, new String[] { "email",
					"publish_stream" }, new DialogListener() {

				@Override
				public void onCancel() {
					// Function to handle cancel event
				}// end of cancel

				@Override
				public void onComplete(Bundle values) {
					// Function to handle complete event
					// Edit Preferences and update facebook acess_token
					SharedPreferences.Editor editor = mPrefs.edit();
					editor.putString("access_token", facebook.getAccessToken());
					editor.putLong("access_expires",
							facebook.getAccessExpires());
					editor.commit();
					getProfileInformation();
					Log.w(TAG, "onComplte called");
					// getProfileInformation();
					// MyTask postofwall=new MyTask();
					// postofwall.execute(0);

				}

				@Override
				public void onError(DialogError error) {
					// Function to handle error
					System.out.print(error);

				}

				@Override
				public void onFacebookError(FacebookError fberror) {
					// Function to handle Facebook errors
					System.out.print(fberror);

				}

			});
		}// end of if
			// getProfileInformation();
		if (!facebook.isSessionValid()) {
			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					// Toast.makeText(activity, "aa", Toast.LENGTH_LONG).show();
				}
			});
			return false;
		} else {
			getProfileInformation();
		}
		return true;

	}// end of logintofacebook

	public static Drawable drawableFromUrl(String id) throws IOException {
		Bitmap x;

		HttpURLConnection connection = (HttpURLConnection) new URL(
				"http://graph.facebook.com/" + id + "/picture?type=large")
				.openConnection();
		connection.setDoInput(true);
		connection.connect();
		InputStream input = connection.getInputStream();
		x = BitmapFactory.decodeStream(input);
		// Bitmap myBitmap = BitmapFactory.decodeStream(input);

		ByteArrayOutputStream outstream = new ByteArrayOutputStream();
		x.compress(Bitmap.CompressFormat.JPEG, 100, outstream);

		return new BitmapDrawable(x);
	}

	void postWall(String message) {
		new MyTask(activity).execute(message);
	}

	public class MyTask extends AsyncTask<String, String, String> {
		String message;
		Activity act;
		ProgressDialog dialog;

		public MyTask(Activity act) {
			this.act = act;
			dialog = new ProgressDialog(act);
			dialog.setMessage("Sharing...");

		}

		protected void onPostExecute() {
			if (dialog.isShowing())
				dialog.dismiss();
			super.execute();
		}

		@Override
		protected void onPreExecute() {
			dialog.show();
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(String... params) {

			if (facebook != null) {
				if (facebook.isSessionValid()

				&& facebook.getAccessToken() != null) {
					getProfileInformation();
					Bundle param = new Bundle();
					param.putString("message", params[0]);

					try {
						String strRet = "";
						strRet = facebook.request("me/feed", param, "POST");
						Log.i(TAG, "" + strRet);
						JSONObject json;
						try {
							json = Util.parseJson(strRet);
							if (!json.isNull("id")) {

								activity.runOnUiThread(new Runnable() {

									@Override
									public void run() {
										if (dialog.isShowing())
											dialog.dismiss();
										Toast.makeText(
												activity.getApplicationContext(),
												"Share success",
												Toast.LENGTH_LONG).show();
									}

								});
								Log.i("Facebook",
										"Post has been submitted on your wall.");
								message = "Post Successfull";
							} else {
								Log.e("Facebook", "Error: " + strRet);
							}
						} // end of inner try
						catch (FacebookError e) {
							activity.runOnUiThread(new Runnable() {

								@Override
								public void run() {
									if (dialog.isShowing())
										dialog.dismiss();
									Toast.makeText(
											activity.getApplicationContext(),
											"Share fail", Toast.LENGTH_LONG)
											.show();
								}

							});
							Log.e("Facebook", "Error: " + e.getMessage());
							message = e.getMessage();
						}
					} // end of outer try
					catch (Exception e) {
						activity.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								if (dialog.isShowing())
									dialog.dismiss();
								Toast.makeText(
										activity.getApplicationContext(),
										"Share fail", Toast.LENGTH_LONG).show();
							}

						});
						Log.e("Facebook", "Error: " + e.getMessage());
						message = e.getMessage();
					}
				}// end of inner if
				else {
					facebook.authorize(activity, new String[] { "email",
							"publish_stream" }, new DialogListener() {

						@Override
						public void onCancel() {
							// Function to handle cancel event
						}

						@Override
						public void onComplete(Bundle values) {
							// Function to handle complete event
							// Edit Preferences and update facebook
							// acess_token
							SharedPreferences.Editor editor = mPrefs.edit();
							editor.putString("access_token",
									facebook.getAccessToken());
							editor.putLong("access_expires",
									facebook.getAccessExpires());
							editor.commit();
							// getProfileInformation();
						}

						@Override
						public void onError(DialogError error) {
							// Function to handle error
							System.out.print(error);
						}

						@Override
						public void onFacebookError(FacebookError fberror) {
							// Function to handle Facebook errors
							System.out.print(fberror);
						}
					});
				}

			}
			return null;
		}// end of doInBackgroud

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Toast.makeText(activity.getApplicationContext(), message,
					Toast.LENGTH_SHORT).show();
		}

	}// end of AsyncTask

	ProgressDialog dialog;

	/**
	 * Get Profile information by making request to Facebook Graph API
	 * */
	public void getProfileInformation() {

		activity.runOnUiThread(new Runnable() {

			@Override
			public void run() {
				dialog = new ProgressDialog(activity);
				dialog.setMessage("Loading...");
				dialog.show();

			}

		});

		mAsyncRunner.request("me", new RequestListener() {
			@Override
			public void onComplete(String response, Object state) {
				// Log.d("Profile", response);
				String json = response;
				try {
					// Facebook Profile JSON data
					JSONObject profile = new JSONObject(json);

					// getting name of the user
					final String name = profile.getString("name");
					// Config.USER_NAME = name;
					Log.d("#@#@#@#@#", profile.toString());
					// getting email of the user
					final String email = profile.getString("email");
					final String id = profile.getString("id");
					URL img_value = new URL("http://graph.facebook.com/" + id
							+ "/picture?type=large");
					// Bitmap mIcon1 = BitmapFactory.decodeStream(img_value
					// .openConnection().getInputStream());

					final User user = new User();
					user.drawableAvatar = drawableFromUrl(id);
					user.userName = name;
					user.fullName = name;
					user.urlAvatar = img_value.toString();
					user.type = "facebook";

					activity.runOnUiThread(new Runnable() {

						@Override
						public void run() {
							dialog.dismiss();
							// Toast.makeText(activity.getApplicationContext(),
							// "Name: " + name + "\nEmail: " + email,
							// Toast.LENGTH_LONG).show();
							showResponseDialog(user);

						}

					});

				} catch (JSONException e) {
					e.printStackTrace();
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}

			@Override
			public void onIOException(IOException e, Object state) {
			}

			@Override
			public void onFileNotFoundException(FileNotFoundException e,
					Object state) {
			}

			@Override
			public void onMalformedURLException(MalformedURLException e,
					Object state) {
			}

			@Override
			public void onFacebookError(FacebookError e, Object state) {
			}
		});
	}

	private void getHTTPConnection() {
		try {
			mAccessToken = facebook.getAccessToken();
			HttpClient httpclient = new DefaultHttpClient();
			String result = null;
			HttpGet httpget = new HttpGet(
					"https://graph.facebook.com/me/friends?access_token="
							+ mAccessToken
							+ "&fields=id,first_name,last_name,location,picture");
			HttpResponse response;
			response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				result = EntityUtils.toString(entity);
				parseJSON(result);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void parseJSON(String data1) throws Exception,
			NullPointerException, JSONException {
		try {
			JSONObject jObj = new JSONObject(data1);
			JSONArray jObjArr = jObj.optJSONArray("data");
			int lon = jObjArr.length();

			for (int i = 0; i < lon; i++) {
				JSONObject tmp = jObjArr.optJSONObject(i);

				String temp_image = tmp.getString("picture");
				String temp_fname = tmp.getString("first_name");

				String temp_lname = tmp.getString("last_name");

				String temp_loc = null;

				JSONObject loc = tmp.getJSONObject("location");
				temp_loc = loc.getString("name");
				Log.i("Exception1 is Here>> ", temp_lname.toString());
			}
		} catch (Exception e) {
			Log.i("Exception1 is Here>> ", e.toString());
			e.printStackTrace();
		}
	}

	// logOut from Facebook
	public static void logoutFromFacebook() {
		if (facebook != null) {
			if (facebook.isSessionValid() && facebook.getAccessToken() != null) {
				new AsyncFacebookRunner(facebook).logout(activity,
						new RequestListener() {
							@Override
							public void onMalformedURLException(
									MalformedURLException e, Object state) {

							}

							@Override
							public void onIOException(IOException e,
									Object state) {

							}

							@Override
							public void onFileNotFoundException(
									FileNotFoundException e, Object state) {

							}

							@Override
							public void onFacebookError(FacebookError e,
									Object state) {

							}

							@Override
							public void onComplete(String response, Object state) {
								activity.runOnUiThread(new Runnable() {
									@Override
									public void run() {

									}
								});
							}
						});

			}
		}

	}// end of logoutFromFacebook

	public void showResponseDialog(User user) {
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ResponseListener.ACTION_RESPONSE);
		broadcastIntent.putExtra(ResponseListener.EXTRA_NAME, user.userName);
		broadcastIntent.putExtra(ResponseListener.USER_ACCOUNT, user.userName);
		broadcastIntent.putExtra(ResponseListener.AVATAR, user.urlAvatar);
		broadcastIntent.putExtra(ResponseListener.TYPE, user.type);
		activity.sendBroadcast(broadcastIntent);
	}
}// end of FbActionEvent
