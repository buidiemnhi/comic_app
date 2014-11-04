package com.bsp.comicapp.adapter;

import it.sephiroth.android.library.widget.HListView;

import java.util.ArrayList;
import java.util.HashMap;

import com.bsp.comicapp.ComicDetailActivity;
import com.bsp.comicapp.R;
import com.bsp.comicapp.displayimage.util.ImageFetcher;
import com.bsp.comicapp.displayimage.util.RecyclingImageView;
import com.bsp.comicapp.util.Config;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ComicListHListViewAdapter extends BaseAdapter {

	private Activity mContext;
	private ImageFetcher mImageFetcher;
	private ArrayList<HashMap<String, String>> arrayBookInformation = new ArrayList<HashMap<String, String>>();
	private int height = 0;
	private int width = 0;
	private String base_url = "";

	private String lang = Config.ENGLISH_LANGUAGUE;
	private SharedPreferences languagePreferences;

	private Activity mActivity;
	private String currentType = "";

	ArrayList<String> arrUrlImage = new ArrayList<String>();

	private int status = -1;
	private String error = "";
	private int records = -1;
	private int animationCount = 0;

	public ComicListHListViewAdapter(Activity c,
			ArrayList<HashMap<String, String>> arrayBookInformation,
			String currentType, int status2, String error2, int records2,
			String base_url2, ImageFetcher mImageFetcher, int width, int height) {
		mContext = c;
		mActivity = c;
		this.arrayBookInformation = arrayBookInformation;
		this.base_url = base_url2;
		this.mImageFetcher = mImageFetcher;
		this.height = height;
		this.width = width;
		this.status = status2;
		this.error = error2;
		this.currentType = currentType;

		languagePreferences = mActivity.getSharedPreferences(
				Config.LANGUAGE_PREFERENCE_NAME, Activity.MODE_PRIVATE);
		lang = languagePreferences.getString(Config.PREFERENCE_KEY_LANGUAGE,
				Config.ENGLISH_LANGUAGUE);
		init();
	}

	@Override
	public int getCount() {
		return arrayBookInformation.size();
	}

	@Override
	public Object getItem(int arg0) {
		return arrayBookInformation.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup arg2) {
		HListView.LayoutParams layoutParams = new HListView.LayoutParams(width,
				height);

		RecyclingImageView imageView = new RecyclingImageView(mContext);
		imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		imageView.setLayoutParams(layoutParams);
		// mImageFetcher.loadImage(arrayBookInformation.get(position).get(key),
		// imageView);

		mImageFetcher.loadImage("http://" + base_url + "/"
				+ arrayBookInformation.get(position).get("url") + "/"
				+ arrayBookInformation.get(position).get("cover"), imageView);

		imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if (Config.IdUser.equals(" ")) {

					if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {

						showAlertDialog(mActivity, "",
								"Please login to enter this page");

					} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
						showAlertDialog(mActivity, "", " رجا الدخول للصفحة");
					}
				} else if (currentType.equalsIgnoreCase("free")) {
					animationCount = 0;
					// Animation shake = AnimationUtils.loadAnimation(mActivity,
					// R.anim.shake);
					// imgFree.startAnimation(shake);
					final Animation right_rotation = AnimationUtils
							.loadAnimation(mActivity, R.anim.right_rotation);
					final Animation left_rotation = AnimationUtils
							.loadAnimation(mActivity, R.anim.left_rotation);

					Intent intent = new Intent(mActivity
							.getApplicationContext(), ComicDetailActivity.class);
					intent.putExtra("isfancomicbook", false);
					intent.putExtra("status", String.valueOf(status));
					intent.putExtra("error", error);
					intent.putExtra("records", String.valueOf(records));
					intent.putExtra("base_url", base_url);
					intent.putExtra("id", arrayBookInformation.get(position)
							.get("id"));
					intent.putExtra("title", arrayBookInformation.get(position)
							.get("title"));
					intent.putExtra(
							"description",
							arrayBookInformation.get(position).get(
									"description"));
					intent.putExtra("series", arrayBookInformation
							.get(position).get("series"));
					intent.putExtra("cover", arrayBookInformation.get(position)
							.get("cover"));
					intent.putExtra("databook",
							arrayBookInformation.get(position).get("databook"));
					intent.putExtra("price", arrayBookInformation.get(position)
							.get("price"));
					intent.putExtra("author", arrayBookInformation
							.get(position).get("author"));
					intent.putExtra("rating", arrayBookInformation
							.get(position).get("rating"));
					intent.putExtra("type", arrayBookInformation.get(position)
							.get("type"));
					intent.putExtra(
							"date_create",
							arrayBookInformation.get(position).get(
									"date_create"));
					intent.putExtra("language",
							arrayBookInformation.get(position).get("language"));
					intent.putExtra("url", arrayBookInformation.get(position)
							.get("url"));
					intent.putExtra("target", arrayBookInformation
							.get(position).get("target"));
					intent.putStringArrayListExtra("urlImage", arrUrlImage);
					mActivity.startActivity(intent);
					mActivity.finish();
					animationCount = 0;

					left_rotation.setAnimationListener(new AnimationListener() {

						@Override
						public void onAnimationStart(Animation animation) {

						}

						@Override
						public void onAnimationRepeat(Animation animation) {

						}

						@Override
						public void onAnimationEnd(Animation animation) {
							animationCount++;
							if (animationCount >= 7) {
								Intent intent = new Intent(mActivity
										.getApplicationContext(),
										ComicDetailActivity.class);
								intent.putExtra("isfancomicbook", false);
								intent.putExtra("status",
										String.valueOf(status));
								intent.putExtra("error", error);
								intent.putExtra("records",
										String.valueOf(records));
								intent.putExtra("base_url", base_url);
								intent.putExtra(
										"id",
										arrayBookInformation.get(position).get(
												"id"));
								intent.putExtra("title", arrayBookInformation
										.get(position).get("title"));
								intent.putExtra(
										"description",
										arrayBookInformation.get(position).get(
												"description"));
								intent.putExtra("series", arrayBookInformation
										.get(position).get("series"));
								intent.putExtra("cover", arrayBookInformation
										.get(position).get("cover"));
								intent.putExtra(
										"databook",
										arrayBookInformation.get(position).get(
												"databook"));
								intent.putExtra("price", arrayBookInformation
										.get(position).get("price"));
								intent.putExtra("author", arrayBookInformation
										.get(position).get("author"));
								intent.putExtra("rating", arrayBookInformation
										.get(position).get("rating"));
								intent.putExtra("type", arrayBookInformation
										.get(position).get("type"));
								intent.putExtra(
										"date_create",
										arrayBookInformation.get(position).get(
												"date_create"));
								intent.putExtra(
										"language",
										arrayBookInformation.get(position).get(
												"language"));
								intent.putExtra("url", arrayBookInformation
										.get(position).get("url"));
								intent.putExtra("target", arrayBookInformation
										.get(position).get("target"));
								intent.putStringArrayListExtra("urlImage",
										arrUrlImage);
								mActivity.startActivity(intent);
								mActivity.finish();
								animationCount = 0;
							} else {
								// imgFree.startAnimation(right_rotation);
							}
							Log.i("Nam Dinh", "Animation");

						}
					});

					// imgFree.startAnimation(right_rotation);

				} else {
					Intent intent = new Intent(mActivity
							.getApplicationContext(), ComicDetailActivity.class);
					intent.putExtra("isfancomicbook", false);
					intent.putExtra("status", String.valueOf(status));
					intent.putExtra("error", error);
					intent.putExtra("records", String.valueOf(records));
					intent.putExtra("base_url", base_url);
					intent.putExtra("id", arrayBookInformation.get(position)
							.get("id"));
					intent.putExtra("title", arrayBookInformation.get(position)
							.get("title"));
					intent.putExtra(
							"description",
							arrayBookInformation.get(position).get(
									"description"));
					intent.putExtra("series", arrayBookInformation
							.get(position).get("series"));
					intent.putExtra("cover", arrayBookInformation.get(position)
							.get("cover"));
					intent.putExtra("databook",
							arrayBookInformation.get(position).get("databook"));
					intent.putExtra("price", arrayBookInformation.get(position)
							.get("price"));
					intent.putExtra("author", arrayBookInformation
							.get(position).get("author"));
					intent.putExtra("rating", arrayBookInformation
							.get(position).get("rating"));
					intent.putExtra("type", arrayBookInformation.get(position)
							.get("type"));
					intent.putExtra(
							"date_create",
							arrayBookInformation.get(position).get(
									"date_create"));
					intent.putExtra("language",
							arrayBookInformation.get(position).get("language"));
					intent.putExtra("url", arrayBookInformation.get(position)
							.get("url"));
					intent.putExtra("target", arrayBookInformation
							.get(position).get("target"));
					intent.putStringArrayListExtra("urlImage", arrUrlImage);
					mActivity.startActivity(intent);
					mActivity.finish();
				}

			}
		});

		return imageView;
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
			ok = mActivity.getResources().getString(R.string.ar_ok);
		}

		// Setting OK Button
		alertDialog.setButton(ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				alertDialog.dismiss();
			}
		});
		// Showing Alert Message
		// http://www.haivl.com/photo/1848240
		alertDialog.show();
	}

	void init() {
		for (int i = 0; i < arrayBookInformation.size(); i++) {
			arrUrlImage.add("http://" + base_url + "/"
					+ arrayBookInformation.get(i).get("url") + "/"
					+ arrayBookInformation.get(i).get("cover"));
		}
	}

}
