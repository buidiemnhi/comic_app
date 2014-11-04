package com.bsp.comicapp.adapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.bsp.comicapp.FanComicActivity;
import com.bsp.comicapp.R;
import com.bsp.comicapp.displayimage.util.AsyncTask;
import com.bsp.comicapp.displayimage.util.RecyclingImageView;

public class ShareComicDialogHListViewAdapter extends BaseAdapter {

	private Activity mActivity;
	private LayoutInflater inflater = null;
	private ArrayList<String> arrImageUrl = new ArrayList<String>();
	private int height = 0;
	private int width = 0;
	private static final int EDIT_PHOTOS_OF_BOOK = 3;
	Button btnEdit;

	public ShareComicDialogHListViewAdapter(Activity mActivity,
			ArrayList<String> arrImageUrl, int width, int height) {
		this.mActivity = mActivity;
		inflater = (LayoutInflater) this.mActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
	public View getView(final int position, View convertView, ViewGroup arg2) {
		View vi = convertView;
		if (vi == null) {
			vi = inflater.inflate(R.layout.hlistview_share_comic_item, null);
		}

		RecyclingImageView imgPhoto = (RecyclingImageView) vi
				.findViewById(R.id.imgPhoto);
		 btnEdit = (Button) vi.findViewById(R.id.btnEdit);
		Button btnDeletePhoto = (Button) vi.findViewById(R.id.btnDeletePhoto);

		
		resizeView(btnDeletePhoto, (int) (width / 4.5), (int) (width / 4.5), 0,
				0, 0, 0);
		
		btnEdit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				((FanComicActivity) mActivity).setEditPhotoPosition(position);
				editPhotosOfBook();
			}
		});

		btnDeletePhoto.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				((FanComicActivity) mActivity).deletePhotoAtPosition(position);
			}
		});

		imgPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
		imgPhoto.getLayoutParams().width=width;
		loadBitmap(imgPhoto, arrImageUrl.get(position), width, height);

		return vi;
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
			try {
				return Bitmap.createScaledBitmap(
						BitmapFactory.decodeFile(imagePath), reqWidth,
						reqHeight, true);// width + height of
											// image
			} catch (Exception e) {
				return null;
			}
		}

		// Once complete, see if ImageView is still around and set bitmap.
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			resizeView(btnEdit, reqWidth, reqWidth * 80 / 240, 0, 0, 0, 0);
			if (imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				if (imageView != null) {
					imageView.setImageBitmap(bitmap);
				}
			}
		}
	}

	private void resizeView(View view, int width, int height, int marginLeft,
			int marginTop, int marginRight, int marginBottom) {
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.height = height;
		llp.width = width;
		llp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
		view.setLayoutParams(llp);
	}

	private void editPhotosOfBook() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		mActivity.startActivityForResult(
				Intent.createChooser(intent, "Select Picture"),
				EDIT_PHOTOS_OF_BOOK);
	}
}
