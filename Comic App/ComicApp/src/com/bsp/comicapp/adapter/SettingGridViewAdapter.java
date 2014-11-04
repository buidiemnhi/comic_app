package com.bsp.comicapp.adapter;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import com.bsp.comicapp.R;
import com.bsp.comicapp.displayimage.util.ImageFetcher;
import com.bsp.comicapp.displayimage.util.RecyclingImageView;

public class SettingGridViewAdapter extends BaseAdapter {

	private Activity mActivity;
	private LayoutInflater inflater = null;
	private ImageFetcher mImageFetcher;

	private ArrayList<HashMap<String, String>> arrayUsersInformation = new ArrayList<HashMap<String, String>>();
	private int height = 0;
	private int width = 0;

	public SettingGridViewAdapter(Activity a,
			ArrayList<HashMap<String, String>> arrayUsersInformation,
			ImageFetcher mImageFetcher, int width, int height) {
		mActivity = a;
		inflater = (LayoutInflater) mActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.arrayUsersInformation = arrayUsersInformation;
		this.mImageFetcher = mImageFetcher;
		this.height = height;
		this.width = width;
	}

	@Override
	public int getCount() {
		return arrayUsersInformation.size();
	}

	@Override
	public Object getItem(int arg0) {
		return arrayUsersInformation.get(arg0);
	}

	@Override
	public long getItemId(int arg0) {
		return arg0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup arg2) {
		View vi = convertView;
		if (convertView == null) {
			vi = inflater.inflate(R.layout.gridview_setting_item, null);
		}
		RecyclingImageView imgUserAvatar = (RecyclingImageView) vi
				.findViewById(R.id.imgUserAvatar);
		TextView txtUserName = (TextView) vi.findViewById(R.id.txtUserName);

		// Set text size
		float textsize = (float) (height * 0.17);
		txtUserName.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
		txtUserName.setText(arrayUsersInformation.get(position).get("name"));
		resizeView(imgUserAvatar, width, height, 0, 0, 0, 0);
		mImageFetcher.loadImage(
				arrayUsersInformation.get(position).get("avatar"),
				imgUserAvatar);
		return vi;
	}

	private void resizeView(View resizeView, int width, int height,
			int marginLeft, int marginTop, int marginRight, int marginBottom) {
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.height = height;
		llp.width = width;
		llp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
		resizeView.setLayoutParams(llp);
	}
}
