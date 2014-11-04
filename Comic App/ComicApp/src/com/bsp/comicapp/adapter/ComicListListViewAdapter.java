package com.bsp.comicapp.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

import com.bsp.comicapp.ComicDetailActivity;
import com.bsp.comicapp.R;
import com.bsp.comicapp.displayimage.util.ImageFetcher;
import com.bsp.comicapp.displayimage.util.RecyclingImageView;
import com.bsp.comicapp.util.Config;

public class ComicListListViewAdapter extends BaseAdapter {

	private Activity mActivity;
	private LayoutInflater inflater = null;
	private ImageFetcher mImageFetcher;
	private String currentType = "";
	private ArrayList<HashMap<String, String>> arrayBookInformation = new ArrayList<HashMap<String, String>>();
	ArrayList<String> arrUrlImage = new ArrayList<String>();
	private int height = 0;
	private int width = 0;
	private int status = -1;
	private String error = "";
	private int records = -1;
	private String base_url = "";
	private int animationCount = 0;

	private String lang = Config.ENGLISH_LANGUAGUE;
	private SharedPreferences languagePreferences;

	public ComicListListViewAdapter(Activity a, String currentType, int status,
			String error, int records, String base_url,
			ArrayList<HashMap<String, String>> arrayBookInformation,
			ImageFetcher mImageFetcher, int width, int height) {
		mActivity = a;
		inflater = (LayoutInflater) a
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.currentType = currentType;
		this.status = status;
		this.error = error;
		this.records = records;
		this.base_url = base_url;
		this.arrayBookInformation = arrayBookInformation;
		this.mImageFetcher = mImageFetcher;
		this.height = height;
		this.width = width;

		languagePreferences = a.getSharedPreferences(
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

		// RecyclingImageView imageView = new RecyclingImageView(mContext);
		// imageView
		// .setLayoutParams(new FancyCoverFlow.LayoutParams(width, height));
		// imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		// mImageFetcher.loadImage(arrImageUrl.get(position), imageView);

		View vi = convertView;
		if (convertView == null) {
			vi = inflater.inflate(R.layout.listview_comic_list_item, null);
		}

		LinearLayout layout_listview_comic_item = (LinearLayout) vi
				.findViewById(R.id.layout_listview_comic_item);
		RelativeLayout layout_content = (RelativeLayout) vi
				.findViewById(R.id.layout_comic_detail_list_item);
		LinearLayout layout_comic = (LinearLayout) vi
				.findViewById(R.id.layout_comic);
		LinearLayout layout_comic_image = (LinearLayout) vi
				.findViewById(R.id.layout_comic_image);
		LinearLayout layout_comic_text_and_button = (LinearLayout) vi
				.findViewById(R.id.layout_comic_text_and_button);
		RecyclingImageView imgComic = (RecyclingImageView) vi
				.findViewById(R.id.imgComic);
		TextView txtTitle = (TextView) vi.findViewById(R.id.txtTitle);
		TextView txtTargetTitle = (TextView) vi
				.findViewById(R.id.txtTargetTitle);
		TextView txtTarget = (TextView) vi.findViewById(R.id.txtTarget);
		TextView txtPriceTitle = (TextView) vi.findViewById(R.id.txtPriceTitle);
		TextView txtPrice = (TextView) vi.findViewById(R.id.txtPrice);
		Button btnRead = (Button) vi.findViewById(R.id.btnRead);
		RelativeLayout layout_sticky_free = (RelativeLayout) vi
				.findViewById(R.id.layout_sticky_free);
		ImageView imgSticky = (ImageView) vi.findViewById(R.id.imgSticky);
		final ImageView imgFree = (ImageView) vi.findViewById(R.id.imgFree);

		if (currentType.equalsIgnoreCase("free")) {
			imgSticky.setVisibility(View.VISIBLE);
			imgFree.setVisibility(View.VISIBLE);
		}
		if (lang.equalsIgnoreCase(Config.ENGLISH_LANGUAGUE)) {
			imgFree.setImageResource(R.drawable.en_ic_free);
			txtTargetTitle.setText(R.string.target);
			txtPriceTitle.setText(R.string.prices);
			btnRead.setText(R.string.read);

		} else if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) {
			imgFree.setImageResource(R.drawable.ar_ic_free);
			txtTargetTitle.setText(R.string.ar_target);
			txtPriceTitle.setText(R.string.ar_prices);
			btnRead.setText(R.string.ar_read);
		}
		
		String s = txtTargetTitle.getText().toString();
		s += ":";
		txtTargetTitle.setText(s);
		// Set font
		Typeface tf = Typeface.createFromAsset(mActivity
				.getApplicationContext().getAssets(),
				"fonts/UTM Helve Bold.ttf");
		txtTitle.setTypeface(tf);
		txtTarget.setTypeface(tf);
		txtPrice.setTypeface(tf);
		btnRead.setTypeface(tf);
		// Set text size
		float textsize = (float) (height * 0.07);
		txtTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
		txtTarget.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
		txtPrice.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
		btnRead.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);

		// resize item
		layout_listview_comic_item.getLayoutParams().height = height;
		layout_listview_comic_item.getLayoutParams().width = width;
		layout_content.getLayoutParams().height = height;
		layout_content.getLayoutParams().width = (int) (width * 0.95);
		layout_comic.getLayoutParams().height = (int) (height * 0.9);
		layout_comic.getLayoutParams().width = (int) (width * 0.95);
		layout_comic_text_and_button.getLayoutParams().width = (int) (width * 0.47);

		layout_comic_image.setPadding((int) (width / 65),
				(int) (height * 0.05), (int) (width / 65), 0);
		// layout_sticky_free.setPadding(0, 0, (int) (width * 0.05), 0);
		layout_sticky_free.getLayoutParams().height = (int) (height * 0.77);
		layout_sticky_free.getLayoutParams().width = (int) (height * 0.8);

		layout_comic_text_and_button.setPadding(0, (int) (height * 0.05),
				(int) (height / 100), 0);

		resizeImageView(imgSticky, (int) (height * 0.32),
				(int) (height * 0.32 * 87 / 172), (int) (height * 0.12), 0, 0,
				0);
		resizeImageView(imgFree, (int) (height * 0.52),
				(int) (height * 0.52 * 437 / 340), (int) (height * 0.12),
				(int) (height * 0.068), 0, 0);

		resizeButton(btnRead, (int) (width / 7), (int) (height / 7), 0, 0, 0, 0);
		btnRead.setPadding(0, 0, 0, (int) (textsize / 10));

		resizeRecyclingImageView(imgComic, (int) (height * 0.8 * 512 / 786),
				(int) (height * 0.8), 0, 0, 0, 0);

		txtTitle.setText(arrayBookInformation.get(position).get("title"));
		txtTarget.setText(arrayBookInformation.get(position).get("target"));
		txtPrice.setText(": " + arrayBookInformation.get(position).get("price"));

		mImageFetcher.loadImage("http://" + base_url + "/"
				+ arrayBookInformation.get(position).get("url") + "/"
				+ arrayBookInformation.get(position).get("cover"), imgComic);

		btnRead.setOnClickListener(new OnClickListener() {
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

					right_rotation
							.setAnimationListener(new AnimationListener() {

								@Override
								public void onAnimationStart(Animation animation) {

								}

								@Override
								public void onAnimationRepeat(
										Animation animation) {

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
												arrayBookInformation.get(
														position).get("id"));
										intent.putExtra(
												"title",
												arrayBookInformation.get(
														position).get("title"));
										intent.putExtra(
												"description",
												arrayBookInformation.get(
														position).get(
														"description"));
										intent.putExtra(
												"series",
												arrayBookInformation.get(
														position).get("series"));
										intent.putExtra(
												"cover",
												arrayBookInformation.get(
														position).get("cover"));
										intent.putExtra(
												"databook",
												arrayBookInformation.get(
														position).get(
														"databook"));
										intent.putExtra(
												"price",
												arrayBookInformation.get(
														position).get("price"));
										intent.putExtra(
												"author",
												arrayBookInformation.get(
														position).get("author"));
										intent.putExtra(
												"rating",
												arrayBookInformation.get(
														position).get("rating"));
										intent.putExtra(
												"type",
												arrayBookInformation.get(
														position).get("type"));
										intent.putExtra(
												"date_create",
												arrayBookInformation.get(
														position).get(
														"date_create"));
										intent.putExtra(
												"language",
												arrayBookInformation.get(
														position).get(
														"language"));
										intent.putExtra(
												"url",
												arrayBookInformation.get(
														position).get("url"));
										intent.putExtra(
												"target",
												arrayBookInformation.get(
														position).get("target"));
										intent.putStringArrayListExtra(
												"urlImage", arrUrlImage);
										mActivity.startActivity(intent);
										mActivity.finish();
										animationCount = 0;
									} else {
										imgFree.startAnimation(left_rotation);
									}
									Log.i("Nam Dinh", "Animation");

								}
							});
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
								imgFree.startAnimation(right_rotation);
							}
							Log.i("Nam Dinh", "Animation");

						}
					});

					imgFree.startAnimation(right_rotation);

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

		return vi;
	}

	private void resizeImageView(ImageView resizeImageView, int width,
			int height, int marginLeft, int marginTop, int marginRight,
			int marginBottom) {
		RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		rlp.height = height;
		rlp.width = width;
		rlp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
		resizeImageView.setLayoutParams(rlp);
	}

	private void resizeRecyclingImageView(
			RecyclingImageView resizeRecyclingImageView, int width, int height,
			int marginLeft, int marginTop, int marginRight, int marginBottom) {
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		llp.height = height;
		llp.width = width;
		llp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
		resizeRecyclingImageView.setLayoutParams(llp);
	}

	private void resizeButton(Button resizeButton, int width, int height,
			int marginLeft, int marginTop, int marginRight, int marginBottom) {
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		llp.height = height;
		llp.width = width;
		llp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
		resizeButton.setLayoutParams(llp);
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
