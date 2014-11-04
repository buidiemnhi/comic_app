/*
 Copyright (c) 2012 Roman Truba

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial
 portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.bsp.vn.zoomimage;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView.ScaleType;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bsp.comicapp.R;

import com.bsp.vn.zoomimage.InputStreamWrapper.InputStreamProgressListener;

public class UrlTouchImageView extends RelativeLayout {
    protected ProgressBar mProgressBar;
    protected TouchImageView mImageView;

    protected Context mContext;
    private int width = 0;
	private int height = 0;
	
    public UrlTouchImageView(Context ctx,int width, int height)
    {
        super(ctx);
        mContext = ctx;
        this.height=height;
		this.width=width;
        init();

    }
    public UrlTouchImageView(Context ctx, AttributeSet attrs)
    {
        super(ctx, attrs);
        mContext = ctx;
        init();
    }
    public TouchImageView getImageView() { return mImageView; }

    @SuppressWarnings("deprecation")
    protected void init() {
        mImageView = new TouchImageView(mContext);
        LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        mImageView.setLayoutParams(params);
        this.addView(mImageView);
      //  mImageView.setVisibility(GONE);

        mProgressBar = new ProgressBar(mContext, null, android.R.attr.progressBarStyleHorizontal);
        params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        params.setMargins(30, 0, 30, 0);
        mProgressBar.setLayoutParams(params);
        mProgressBar.setIndeterminate(false);
        mProgressBar.setMax(100);
        this.addView(mProgressBar);
    }

    public void setUrl(String imageUrl)
    {
    	loadBitmap(mImageView, imageUrl, width, height);
        new ImageLoadTask().execute(imageUrl);
    }
    //No caching load
    public class ImageLoadTask extends AsyncTask<String, Integer, Bitmap>
    {
        @Override
        protected Bitmap doInBackground(String... strings) {
            String url = strings[0];
            Bitmap bm = null;
            try {
                URL aURL = new URL(url);
                URLConnection conn = aURL.openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();
                int totalLen = conn.getContentLength();
                InputStreamWrapper bis = new InputStreamWrapper(is, 8192, totalLen);
                bis.setProgressListener(new InputStreamProgressListener()
				{					
					@Override
					public void onProgress(float progressValue, long bytesLoaded,
							long bytesTotal)
					{
						publishProgress((int)(progressValue * 100));
					}
				});
                bm = BitmapFactory.decodeStream(bis);
                bis.close();
                is.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bm;
        }
        
        @Override
        protected void onPostExecute(Bitmap bitmap) {
        	if (bitmap == null) 
        	{
        		mImageView.setScaleType(ScaleType.CENTER);
        	//	bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.);
        		//mImageView.setImageBitmap(bitmap);
        	}
        	else 
        	{
        		mImageView.setScaleType(ScaleType.MATRIX);
	            mImageView.setImageBitmap(bitmap);
        	}
            mImageView.setVisibility(VISIBLE);
            mProgressBar.setVisibility(GONE);
        }

		@Override
		protected void onProgressUpdate(Integer... values)
		{
			mProgressBar.setProgress(values[0]);
		}
    }
    private void loadBitmap(TouchImageView iv,
			String imagePath, int reqWidth, int reqHeight) {
		// mImageView.setImageResource(R.drawable.gridview_image_load);
		BitmapWorkerTask task = new BitmapWorkerTask(iv);
		String[] params = new String[] { imagePath, String.valueOf(reqWidth),
				String.valueOf(reqHeight) };
		task.execute(params);
	}

	class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
		private final WeakReference<ImageView> imageViewReference;
		private String imagePath = "";
		private int reqWidth = 0;
		private int reqHeight = 0;

		public BitmapWorkerTask(TouchImageView iv) {
			// Use a WeakReference to ensure the ImageView can be garbage
			// collected
			imageViewReference = new WeakReference<ImageView>(iv);
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
							// initiatePopupWindow(bitmap);
						}
					});
				}
			}
		}
	}
}
