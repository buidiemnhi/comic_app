package com.bsp.comicapp.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import com.bsp.comicapp.ComicDetailActivity;
import com.bsp.comicapp.R;
import com.bsp.comicapp.displayimage.util.ImageFetcher;
import com.bsp.comicapp.displayimage.util.RecyclingImageView;

public class ComicGridViewAdapter extends BaseAdapter {

	private Activity mActivity;
	private LayoutInflater inflater = null;
	private ImageFetcher mImageFetcher;

	private ArrayList<HashMap<String, String>> arrayBookInformation = new ArrayList<HashMap<String, String>>();
	private int height = 0;
	private int width = 0;
	private int mSize = 0;

	private int status = -1;
	private String error = "";
	private int records = -1;
	private String base_url = "";

	private int animationCount = 0;

	public ComicGridViewAdapter(Activity a, int status, String error,
			int records, String base_url,
			ArrayList<HashMap<String, String>> arrayBookInformation,
			ImageFetcher mImageFetcher, int width, int height) {
		mActivity = a;
		inflater = (LayoutInflater) a
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.status = status;
		this.error = error;
		this.records = records;
		this.base_url = base_url;
		this.arrayBookInformation = arrayBookInformation;
		this.mImageFetcher = mImageFetcher;
		this.height = height;
		this.width = width;
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
			vi = inflater.inflate(R.layout.gridview_comic_list_item, null);
		}

		LinearLayout layout_gridview_comic_item = (LinearLayout) vi
				.findViewById(R.id.layout_gridview_comic_item);
		RelativeLayout layout_content = (RelativeLayout) vi
				.findViewById(R.id.layout_comic_detail_list_item);
		LinearLayout layout_comic = (LinearLayout) vi
				.findViewById(R.id.layout_comic);
		LinearLayout layout_comic_image = (LinearLayout) vi
				.findViewById(R.id.layout_comic_image);
		LinearLayout layout_text_detail = (LinearLayout) vi
				.findViewById(R.id.layout_text_detail);
		RecyclingImageView imgComic = (RecyclingImageView) vi
				.findViewById(R.id.imgComic);
		TextView txtTitle = (TextView) vi.findViewById(R.id.txtTitle);
		TextView txtTarget = (TextView) vi.findViewById(R.id.txtTarget);
		TextView txtPrice = (TextView) vi.findViewById(R.id.txtPrice);
		Button btnRead = (Button) vi.findViewById(R.id.btnRead);
		RelativeLayout layout_sticky_free = (RelativeLayout) vi
				.findViewById(R.id.layout_sticky_free);
		ImageView imgSticky = (ImageView) vi.findViewById(R.id.imgSticky);
		final ImageView imgFree = (ImageView) vi.findViewById(R.id.imgFree);

		// Set font
		Typeface tf = Typeface.createFromAsset(mActivity
				.getApplicationContext().getAssets(),
				"fonts/UTM Helve Bold.ttf");
		txtTitle.setTypeface(tf);
		txtTarget.setTypeface(tf);
		txtPrice.setTypeface(tf);
		btnRead.setTypeface(tf);
		// Set text size
		float textsize = (float) (height * 0.038);
		//txtTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
		//txtTarget.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
		//txtPrice.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
	//	btnRead.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);

		// resize item
		layout_gridview_comic_item.getLayoutParams().height = height;
		layout_gridview_comic_item.getLayoutParams().width = width;
		if (width >= height) {
			layout_content.getLayoutParams().height = height;
			layout_content.getLayoutParams().width = height;
			layout_comic.getLayoutParams().height = (int) (height * 0.79);
			layout_comic.getLayoutParams().width = (int) (height * 0.91);
			mSize = height;
		} else {
			layout_content.getLayoutParams().height = width;
			layout_content.getLayoutParams().width = width;
			layout_comic.getLayoutParams().height = (int) (width * 0.79);
			layout_comic.getLayoutParams().width = (int) (width * 0.93);
			mSize = width;
		}
		
		layout_comic_image.setPadding((int) (mSize / 45), (int) (mSize / 45),
				0, 0);

		layout_text_detail.setPadding((int) (mSize / 100), (int) (mSize / 45),
				(int) (mSize / 100), (int) (mSize / 100));

		layout_sticky_free.getLayoutParams().height = (int) (mSize * 0.48);
		layout_sticky_free.getLayoutParams().width = (int) (mSize * 0.385);

		resizeImageView(imgSticky, (int) (mSize / 6),
				(int) (mSize / 6 * 87 / 172), (int) (mSize * 0.022), 0, 0, 0);
		resizeImageView(imgFree, (int) (mSize / 3),
				(int) (mSize / 3 * 437 / 340), (int) (mSize * 0.022),
				(int) (mSize * 0.03), 0, 0);

		resizeButton(btnRead, (int) (mSize / 4), (int) (mSize / 14), 0,
				(int) (mSize / 40), 0, (int) (mSize / 40));
		btnRead.setPadding(0, 0, 0, (int) (textsize / 10));

		resizeRecyclingImageView(imgComic, (int) (mSize * 0.415),
				(int) (mSize * 0.415 * 786 / 512), 0, 0, 0, 0);

		txtTitle.setText(arrayBookInformation.get(position).get("title"));
		txtTarget.setText(arrayBookInformation.get(position).get("target"));
		txtPrice.setText(arrayBookInformation.get(position).get("price"));

		mImageFetcher.loadImage("http://" + base_url + "/"
				+ arrayBookInformation.get(position).get("url") + "/"
				+ arrayBookInformation.get(position).get("cover"), imgComic);

		btnRead.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				animationCount = 0;
				final Animation right_rotation = AnimationUtils.loadAnimation(
						mActivity, R.anim.right_rotation);
				final Animation left_rotation = AnimationUtils.loadAnimation(
						mActivity, R.anim.left_rotation);

				right_rotation.setAnimationListener(new AnimationListener() {

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
							intent.putExtra("status", String.valueOf(status));
							intent.putExtra("error", error);
							intent.putExtra("records", String.valueOf(records));
							intent.putExtra("base_url", base_url);
							intent.putExtra("id",
									arrayBookInformation.get(position)
											.get("id"));
							intent.putExtra(
									"title",
									arrayBookInformation.get(position).get(
											"title"));
							intent.putExtra("description", arrayBookInformation
									.get(position).get("description"));
							intent.putExtra(
									"series",
									arrayBookInformation.get(position).get(
											"series"));
							intent.putExtra(
									"cover",
									arrayBookInformation.get(position).get(
											"cover"));
							intent.putExtra("databook", arrayBookInformation
									.get(position).get("databook"));
							intent.putExtra(
									"price",
									arrayBookInformation.get(position).get(
											"price"));
							intent.putExtra(
									"author",
									arrayBookInformation.get(position).get(
											"author"));
							intent.putExtra(
									"rating",
									arrayBookInformation.get(position).get(
											"rating"));
							intent.putExtra(
									"type",
									arrayBookInformation.get(position).get(
											"type"));
							intent.putExtra("date_create", arrayBookInformation
									.get(position).get("date_create"));
							intent.putExtra("language", arrayBookInformation
									.get(position).get("language"));
							intent.putExtra(
									"url",
									arrayBookInformation.get(position).get(
											"url"));
							intent.putExtra(
									"target",
									arrayBookInformation.get(position).get(
											"target"));
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
							intent.putExtra("status", String.valueOf(status));
							intent.putExtra("error", error);
							intent.putExtra("records", String.valueOf(records));
							intent.putExtra("base_url", base_url);
							intent.putExtra("id",
									arrayBookInformation.get(position)
											.get("id"));
							intent.putExtra(
									"title",
									arrayBookInformation.get(position).get(
											"title"));
							intent.putExtra("description", arrayBookInformation
									.get(position).get("description"));
							intent.putExtra(
									"series",
									arrayBookInformation.get(position).get(
											"series"));
							intent.putExtra(
									"cover",
									arrayBookInformation.get(position).get(
											"cover"));
							intent.putExtra("databook", arrayBookInformation
									.get(position).get("databook"));
							intent.putExtra(
									"price",
									arrayBookInformation.get(position).get(
											"price"));
							intent.putExtra(
									"author",
									arrayBookInformation.get(position).get(
											"author"));
							intent.putExtra(
									"rating",
									arrayBookInformation.get(position).get(
											"rating"));
							intent.putExtra(
									"type",
									arrayBookInformation.get(position).get(
											"type"));
							intent.putExtra("date_create", arrayBookInformation
									.get(position).get("date_create"));
							intent.putExtra("language", arrayBookInformation
									.get(position).get("language"));
							intent.putExtra(
									"url",
									arrayBookInformation.get(position).get(
											"url"));
							intent.putExtra(
									"target",
									arrayBookInformation.get(position).get(
											"target"));
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
}
