package com.bsp.comicapp.adapter;

import java.util.ArrayList;

import com.bsp.comicapp.R;
import com.bsp.comicapp.displayimage.util.ImageFetcher;
import com.bsp.comicapp.displayimage.util.RecyclingImageView;
import com.bsp.comicapp.model.Comment;
import com.bsp.comicapp.model.User;
import com.bsp.comicapp.util.Config;

import android.app.Activity;
import android.content.Context;
import android.text.Html;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

public class ComicDetailCommentListViewAdapter extends BaseAdapter {
	private Activity mActivity;
	private LayoutInflater inflater = null;
	private ImageFetcher mImageFetcher;
	private ArrayList<Comment> arrData = new ArrayList<Comment>();

	public ComicDetailCommentListViewAdapter(Activity a,
			ArrayList<Comment> arrData, ImageFetcher mImageFetcher) {
		mActivity = a;
		inflater = (LayoutInflater) a
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.arrData = arrData;
		this.mImageFetcher = mImageFetcher;
	}

	@Override
	public int getCount() {
		return arrData.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View vi = convertView;
		if (convertView == null) {
			vi = inflater.inflate(R.layout.listview_comic_detail_item, null);
		}
		LinearLayout layout_comic_detail_list_item = (LinearLayout) vi
				.findViewById(R.id.layout_comic_detail_list_item);
		RecyclingImageView imgAvatar = (RecyclingImageView) vi
				.findViewById(R.id.imgAvatar);
		LinearLayout layout_comment = (LinearLayout) vi
				.findViewById(R.id.layout_comment);
		TextView txtNameAndComment = (TextView) vi
				.findViewById(R.id.txtNameAndComment);

		TextView txtTimeAgo = (TextView) vi.findViewById(R.id.txtTimeAgo);

		// Set text size
		float textsize = (float) (Config.screenHeight * 0.025);
		txtNameAndComment.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
		txtTimeAgo.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);

		txtNameAndComment.setText(Html.fromHtml("<font color=#F2569F>"
				+ arrData.get(position).userName + "</font>")
				+ "\n" + arrData.get(position).content);
		txtTimeAgo.setText("3 hour before");

		// resize item
		resizeRecyclingImageView(imgAvatar,
				(int) (Config.screenHeight * 0.135),
				(int) (Config.screenHeight * 0.135), 0, 0, 0, 0);

		layout_comic_detail_list_item.setPadding(0,
				(int) (Config.screenWidth * 0.012), 0,
				(int) (Config.screenWidth * 0.012));
		layout_comment.setPadding((int) (Config.screenHeight * 0.01), 0, 0, 0);
		txtTimeAgo.setText(arrData.get(position).createDate);

		mImageFetcher.loadImage(arrData.get(position).urlAvatar, imgAvatar);
		return vi;
	}

	private void resizeRecyclingImageView(
			RecyclingImageView resizeRecyclingImageView, int width, int height,
			int marginLeft, int marginTop, int marginRight, int marginBottom) {
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.height = height;
		llp.width = width;
		llp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
		resizeRecyclingImageView.setLayoutParams(llp);
	}
}
