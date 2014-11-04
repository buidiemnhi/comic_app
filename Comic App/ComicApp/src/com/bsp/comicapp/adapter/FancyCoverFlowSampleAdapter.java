/*
 * Copyright 2013 David Schreiber
 *           2013 John Paul Nalog
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.bsp.comicapp.adapter;

import java.util.ArrayList;

import com.bsp.comicapp.R;
import com.bsp.comicapp.displayimage.util.ImageFetcher;
import com.bsp.comicapp.displayimage.util.RecyclingImageView;
import com.bsp.comicapp.util.Config;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import at.technikum.mti.fancycoverflow.FancyCoverFlow;
import at.technikum.mti.fancycoverflow.FancyCoverFlowAdapter;

public class FancyCoverFlowSampleAdapter extends FancyCoverFlowAdapter {
	private Context mContext;

	private int[] images = { R.drawable.coverflow1, R.drawable.coverflow2,
			R.drawable.coverflow3, R.drawable.coverflow4,
			R.drawable.coverflow5, R.drawable.coverflow6, R.drawable.coverflow7 };

	public FancyCoverFlowSampleAdapter(Context c) {
		mContext = c;
	}

	@Override
	public int getCount() {
		return images.length;
	}

	@Override
	public Integer getItem(int i) {
		return images[i];
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getCoverFlowItem(int i, View reuseableView, ViewGroup viewGroup) {
		RecyclingImageView imageView = new RecyclingImageView(mContext);
		imageView.setLayoutParams(new FancyCoverFlow.LayoutParams(
				(int) (Config.screenHeight * 0.36 * 640 / 280),
				(int) (Config.screenHeight * 0.36)));

		imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		imageView.setImageResource(this.getItem(i));

		return imageView;
	}
}
