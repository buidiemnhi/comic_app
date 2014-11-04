package com.bsp.comicapp.adapter;

import java.lang.ref.WeakReference;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.bsp.comicapp.R;
import com.bsp.comicapp.displayimage.util.AsyncTask;
import com.bsp.comicapp.displayimage.util.RecyclingImageView;
import com.bsp.comicapp.util.Config;

public class MyComicGridViewAdapter extends BaseAdapter {

	private Activity mActivity;
	private LayoutInflater inflater = null;
	private ArrayList<HashMap<String, String>> arrayMyComic = new ArrayList<HashMap<String, String>>();
	private int height = 0;
	private int width = 0;

	public MyComicGridViewAdapter(Activity mActivity,
			ArrayList<HashMap<String, String>> arrayMyComic, int width,
			int height) {
		this.mActivity = mActivity;
		inflater = (LayoutInflater) this.mActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.arrayMyComic = arrayMyComic;
		this.height = height;
		this.width = width;
	}

	@Override
	public int getCount() {
		return arrayMyComic.size();
	}

	@Override
	public Object getItem(int arg0) {
		return arrayMyComic.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		View vi = convertView;
		if (vi == null) {
			vi = inflater.inflate(R.layout.gridview_mycomic_item, null);
		}
		RecyclingImageView imgComic = (RecyclingImageView) vi
				.findViewById(R.id.imgComic);
		Button btnDeleteBook = (Button) vi.findViewById(R.id.btnDeleteBook);
		// TextView txtLabel = (TextView) vi.findViewById(R.id.txtLabel);
		resizeView(btnDeleteBook, (int) (Config.screenWidth * 0.025),
				(int) (Config.screenWidth * 0.025), 0, 0, 0,
				(int) (Config.screenWidth * 0.002));

		if (arrayMyComic.get(position).get("isdownloadfinished")
				.equalsIgnoreCase("false")) {
			ProgressBar progressBarMyBook = (ProgressBar) vi
					.findViewById(R.id.progressBarMyBook);
			TextView textViewMyBook = (TextView) vi.findViewById(R.id.txtLabel);
			// resize item
			float textSize = (float) (Config.screenWidth * 0.012);
			textViewMyBook.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
			resizeView(progressBarMyBook, (int) (Config.screenWidth * 0.023),
					(int) (Config.screenWidth * 0.023), 0, 0, 0,
					(int) (Config.screenWidth * 0.002));
			progressBarMyBook.setVisibility(View.VISIBLE);
			textViewMyBook.setVisibility(View.VISIBLE);

		} else if ((arrayMyComic.get(position).get("isdownloadfinished")
				.equalsIgnoreCase("true"))) {
			String comicTitle = removeAccent(arrayMyComic.get(position).get(
					"title"));
			String imagePath = "data/data/"
					+ mActivity.getApplicationContext().getPackageName()
					+ "/Documents/" + comicTitle + "-"
					+ arrayMyComic.get(position).get("id") + "/"
					+ "/data_zip/coverbook.jpg";
			loadBitmap(imgComic, imagePath, width, height);
		}
		return vi;
	}

	// private void resizeRecyclingImageView(
	// RecyclingImageView resizeRecyclingImageView, int width, int height,
	// int marginLeft, int marginTop, int marginRight, int marginBottom) {
	// LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
	// LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
	// llp.height = height;
	// llp.width = width;
	// llp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
	// resizeRecyclingImageView.setLayoutParams(llp);
	// }

	private String removeAccent(String inputString) {
		inputString.replace("d", "d").replace("Ð", "D");
		inputString = Normalizer.normalize(inputString, Normalizer.Form.NFD);
		inputString = inputString.replaceAll(
				"\\p{InCombiningDiacriticalMarks}+", "");
		return inputString;
	}

	private void resizeView(View resizeView, int height, int width,
			int marginLeft, int marginTop, int marginRight, int marginBottom) {
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.height = height;
		llp.width = width;
		llp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
		resizeView.setLayoutParams(llp);
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
			return Bitmap.createScaledBitmap(
					BitmapFactory.decodeFile(imagePath), reqWidth, reqHeight,
					true);// width + height of
							// image
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
}
