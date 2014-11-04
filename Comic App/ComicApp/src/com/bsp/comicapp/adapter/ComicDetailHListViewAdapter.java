package com.bsp.comicapp.adapter;

import it.sephiroth.android.library.widget.HListView;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bsp.comicapp.displayimage.util.ImageFetcher;
import com.bsp.comicapp.displayimage.util.RecyclingImageView;

public class ComicDetailHListViewAdapter extends BaseAdapter {

	private Context mContext;
	private ImageFetcher mImageFetcher;
	private ArrayList<String> arrImageUrl = new ArrayList<String>();
	private int height = 0;
	private int width = 0;

	public ComicDetailHListViewAdapter(Context c,
			ArrayList<String> arrImageUrl, ImageFetcher mImageFetcher,
			int width, int height) {
		mContext = c;
		this.arrImageUrl = arrImageUrl;
		this.mImageFetcher = mImageFetcher;
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
		LinearLayout layout = new LinearLayout(mContext);
		layout.setPadding(1, 1, 1, 1);
		layout.setBackgroundColor(Color.parseColor("#F3569D"));
		HListView.LayoutParams layoutParams = new HListView.LayoutParams(width,
				height);
		RecyclingImageView imageView = new RecyclingImageView(mContext);
		imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		imageView.setLayoutParams(layoutParams);
		mImageFetcher.loadImage(arrImageUrl.get(position), imageView);

		layout.addView(imageView);
		return layout;
	}
}