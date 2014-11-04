package com.bsp.comicapp.adapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.bsp.comicapp.R;
import com.bsp.comicapp.displayimage.util.AsyncTask;
import com.bsp.comicapp.displayimage.util.RecyclingImageView;
import com.bsp.comicapp.util.Config;
import com.bsp.vn.zoomimage.TouchImageView;


public class ReadMyComicPagerAdapter extends PagerAdapter {
	private String imagePath;
	private ArrayList<String> arrayImagesName = new ArrayList<String>();
	private Activity mActivity;
	private int width = 0;
	private int height = 0;
	private PopupWindow pwindo;

	public ReadMyComicPagerAdapter(Activity activity, String imagePath,
			ArrayList<String> arrayImagesName, int width, int height) {
		this.mActivity = activity;
		this.arrayImagesName = arrayImagesName;
		this.imagePath = imagePath;
		this.width = width;
		this.height = height;
	}

	@Override
	public int getCount() {
		return arrayImagesName.size();
	}

	@Override
	public boolean isViewFromObject(View arg0, Object arg1) {
		return arg0.equals(arg1);
	}

	@Override
	public Object instantiateItem(View container, int position) {
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		RecyclingImageView imageView = new RecyclingImageView(
				mActivity.getApplicationContext());
		TouchImageView imageTouchImageView = new TouchImageView(
				mActivity.getApplicationContext());
		imageTouchImageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
		imageTouchImageView.setLayoutParams(layoutParams);
		//imageTouchImageView.setMaxZoom(4f);
		loadBitmap(imageTouchImageView,
				imagePath + "/" + arrayImagesName.get(position), width, height);
		((ViewPager) container).addView(imageTouchImageView);
		return imageTouchImageView;
	}

	@Override
	public void destroyItem(View container, int position, Object object) {
		((ViewPager) container).removeView((View) object);
	}

	private void loadBitmap(TouchImageView imageTouchImageView,
			String imagePath, int reqWidth, int reqHeight) {
		// mImageView.setImageResource(R.drawable.gridview_image_load);
		BitmapWorkerTask task = new BitmapWorkerTask(imageTouchImageView);
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
		protected void onPostExecute(final Bitmap bitmap) {
			if (imageViewReference != null && bitmap != null) {
				final ImageView imageView = imageViewReference.get();
				if (imageView != null) {
					imageView.setImageBitmap(bitmap);
					imageView.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							// TODO Auto-generated method stub
							//initiatePopupWindow(bitmap);
						}
					});
				}
			}
		}
	}

	void initiatePopupWindow(Bitmap bitmap) {

		LayoutInflater inflater = (LayoutInflater) mActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.popup_image, null);
		TouchImageView imageView = (TouchImageView) layout
				.findViewById(R.id.imgeViewTouch);
		imageView.setImageBitmap(bitmap);
		imageView.getLayoutParams().height=(int)Config.screenWidth;
		imageView.getLayoutParams().width=(int)Config.screenHeight;
		
		pwindo = new PopupWindow(layout);
		pwindo.setContentView(layout);
		pwindo.setWidth((int) Config.screenHeight);
		pwindo.setHeight((int) Config.screenWidth);
		pwindo.showAtLocation(layout, Gravity.TOP|Gravity.LEFT, 0, 0);
		//imageView.setSaveScale(2f);
//		imageView.setOnZoomListener(new OnZoomListener() {
//			
//			@Override
//			public void onZoomListener(float saveScale) {
//				// TODO Auto-generated method stub
//				//Toast.makeText(mActivity, ""+saveScale, Toast.LENGTH_LONG).show();
//				if(saveScale==0.97f)
//				{
//					pwindo.dismiss();
//				}
//			}
//		});
		// imageView.setOnClickListener(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// // TODO Auto-generated method stub
		// pwindo.dismiss();
		// }
		// });
		
		
	}
}
