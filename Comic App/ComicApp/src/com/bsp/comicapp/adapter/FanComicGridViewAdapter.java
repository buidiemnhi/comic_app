package com.bsp.comicapp.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bsp.comicapp.R;
import com.bsp.comicapp.displayimage.util.ImageFetcher;
import com.bsp.comicapp.displayimage.util.RecyclingImageView;

public class FanComicGridViewAdapter extends BaseAdapter {
	private Activity mActivity;
	private LayoutInflater inflater = null;
	private ImageFetcher mImageFetcher;

	private ArrayList<HashMap<String, String>> arrayBookInformation = new ArrayList<HashMap<String, String>>();
	ArrayList<String> arrUrlImage = new ArrayList<String>();
	private int height = 0;
	private int width = 0;

	private int status = -1;
	private String error = "";
	private int records = -1;
	private String base_url = "";

	public FanComicGridViewAdapter(Activity a, int status, String error,
			int records, String base_url,
			ArrayList<HashMap<String, String>> arrayBookInformation,
			ImageFetcher mImageFetcher, int width, int height) {
		mActivity = a;
		inflater = (LayoutInflater) mActivity
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

		View vi = convertView;
		if (convertView == null) {
			vi = inflater.inflate(R.layout.gridview_fancomic_item, null);
		}

		RecyclingImageView imgComic = (RecyclingImageView) vi
				.findViewById(R.id.imgComic);
		TextView txtComicTitle = (TextView) vi.findViewById(R.id.txtComicTitle);
		TextView txtAuthor = (TextView) vi.findViewById(R.id.txtAuthor);
		TextView txtDateCreate = (TextView) vi.findViewById(R.id.txtDateCreate);

		// Set text size
		float textsize = (float) (height * 0.08);
		txtComicTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
		txtAuthor.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
		txtDateCreate.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);

		// resize item
		resizeView(imgComic, width, height, 0, 0, 0, 0);
		txtComicTitle.setText(arrayBookInformation.get(position).get("title"));
		txtAuthor.setText(arrayBookInformation.get(position).get("author"));
		String[] data = arrayBookInformation.get(position).get("date_create")
				.split(" ");
		txtDateCreate.setText(data[0]);
		mImageFetcher.loadImage("http://" + base_url + "/"
				+ arrayBookInformation.get(position).get("url") + "/"
				+ arrayBookInformation.get(position).get("cover"), imgComic);

		return vi;
	}

	private void resizeView(View view, int width, int height, int marginLeft,
			int marginTop, int marginRight, int marginBottom) {
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		llp.height = height;
		llp.width = width;
		llp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
		view.setLayoutParams(llp);
	}

	void init() {
		for (int i = 0; i < arrayBookInformation.size(); i++) {
			arrUrlImage.add("http://" + base_url + "/"
					+ arrayBookInformation.get(i).get("url") + "/"
					+ arrayBookInformation.get(i).get("cover"));
		}
	}

}