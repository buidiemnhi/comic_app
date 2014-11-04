package com.bsp.comicapp.adapter;

import it.sephiroth.android.library.widget.HListView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bsp.comicapp.displayimage.util.AsyncTask;
import com.bsp.comicapp.displayimage.util.RecyclingImageView;
import com.bsp.comicapp.util.Config;

public class ReadMyComicHListViewAdapter extends BaseAdapter {

	private Context mContext;
	private String imagesPath = "";
	private ArrayList<String> arrImageUrl = new ArrayList<String>();
	private int height = 0;
	private int width = 0;

	public ReadMyComicHListViewAdapter(Context c, String imagesPath,
			ArrayList<String> arrImageUrl, int width, int height) {
		mContext = c;
		this.imagesPath = imagesPath;
		this.arrImageUrl = arrImageUrl;
		this.height = height;
		this.width = width;
	}

	@Override
	public int getCount() {
		return arrImageUrl.size();
	}

	@Override
	public Object getItem(int arg0) {
		return arrImageUrl.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup arg2) {
		HListView.LayoutParams layoutParams = new HListView.LayoutParams(width,
				height);
		RecyclingImageView imageView = new RecyclingImageView(mContext);
		imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		imageView.setLayoutParams(layoutParams);
		imageView.setPadding(0, (int) (Config.screenWidth * 0.01), 0,
				(int) (Config.screenWidth * 0.01));

		loadBitmap(imageView, imagesPath + "/" + arrImageUrl.get(position),
				width, height);

		return imageView;
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