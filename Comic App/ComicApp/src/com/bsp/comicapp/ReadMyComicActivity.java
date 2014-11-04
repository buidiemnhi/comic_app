package com.bsp.comicapp;

import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.AdapterView.OnItemClickListener;
import it.sephiroth.android.library.widget.HListView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.bsp.comicapp.adapter.ReadMyComicHListViewAdapter;
import com.bsp.comicapp.adapter.ReadMyComicPagerAdapter;
import com.bsp.comicapp.util.Config;
import com.bsp.vn.zoomimage.BasePagerAdapter.OnItemChangeListener;
import com.bsp.vn.zoomimage.FilePagerAdapter;
import com.bsp.vn.zoomimage.GalleryViewPager;

public class ReadMyComicActivity extends FragmentActivity {
	private ViewPager viewPager;
	private HListView hListView;
	private ReadMyComicPagerAdapter mAdapter;

	private View currentHListViewItem = null;

	private String imagesPath = "";
	private ArrayList<String> arrayImagesName = new ArrayList<String>();

	GalleryViewPager mViewPager;
	private Button backBtn;
	private TextView titleTv;
	private String lang = "", title="";
	private SharedPreferences languagePreferences;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_read_mycomic);
		backBtn =  (Button) findViewById(R.id.backBtnId);
		titleTv = (TextView) findViewById(R.id.titleTvId);
		
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.setMargins((int)Config.screenWidth/5, 0, 0, 0);
		titleTv.setLayoutParams(llp);
		
		float textsize2 = (float) (Config.screenHeight * 0.04);
		titleTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize2);
		
		languagePreferences = getSharedPreferences(
				Config.LANGUAGE_PREFERENCE_NAME, MODE_PRIVATE);
		lang = languagePreferences.getString(Config.PREFERENCE_KEY_LANGUAGE,
				Config.ENGLISH_LANGUAGUE);
		
		if (lang.equalsIgnoreCase(Config.ARABIC_LANGUAGE)) 
		{
			backBtn.setBackgroundResource(R.drawable.back_arabic);
		}else{
			backBtn.setBackgroundResource(R.drawable.back_english);
		}
		
		backBtn.setOnClickListener(new Button.OnClickListener() {  
        public void onClick(View v)
            {
                finish();
            }
         });
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			imagesPath = extras.getString("imagesPath");
			title = extras.getString("title");
		}

		titleTv.setText(title);
		
		try {
			File f = new File(imagesPath + "property.txt");
			FileInputStream fis;
			fis = new FileInputStream(f);
			// Create a BufferedReader to saving time
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					fis));
			String str = "";
			// Create a StringBuffer to store temporary -> saving
			// time
			str = reader.readLine();
			// Check when end of file
			arrayImagesName = new ArrayList<String>();
			// mPages = new ArrayList<Bitmap>();
			while (str != null) {
				// File file = new File(imagePathFull);
				// mPages.add(BitmapFactory.decodeFile(file.getAbsolutePath()));
				arrayImagesName.add(str);
				str = reader.readLine();
			}
			reader.close();
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		// viewPager = (ViewPager) findViewById(R.id.viewPager);
		mViewPager = (GalleryViewPager) findViewById(R.id.viewPager);
		hListView = (HListView) findViewById(R.id.hListView);
		hListView.setAdapter(new ReadMyComicHListViewAdapter(
				getApplicationContext(), imagesPath, arrayImagesName,
				(int) (Config.screenWidth * 0.1 * 512 / 786),
				(int) (Config.screenWidth * 0.1)));

		resizeHListView(hListView, 0, (int) (Config.screenWidth * 0.1), 0, 0,
				0, 0);

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		int width = 0;
		int height = 0;

		if ((int) (mViewPager.getWidth() * 786 / 512) > (int) (mViewPager
				.getHeight())) {
			height = mViewPager.getHeight();
			width = mViewPager.getHeight() * 512 / 786;

		} else {
			height = mViewPager.getWidth() * 786 / 512;
			width = mViewPager.getWidth();
		}

		FilePagerAdapter pagerAdapter = new FilePagerAdapter(this, imagesPath,
				arrayImagesName, width, height);
		pagerAdapter.setOnItemChangeListener(new OnItemChangeListener() {
			@Override
			public void onItemChange(int currentPosition) {

				// currentHListViewItem = hListView.getChildAt(currentPosition);
				// currentHListViewItem.setPadding(0, 0, 0, 0);
				// currentHListViewItem.setPadding(0,
				// (int) (Config.screenWidth * 0.01), 0,
				// (int) (Config.screenWidth * 0.01));
				// hListView.setSelection(currentPosition);
				// Toast.makeText(GalleryFileActivity.this, "Current item is " +
				// currentPosition, Toast.LENGTH_SHORT).show();
			}
		});

		mViewPager = (GalleryViewPager) findViewById(R.id.viewPager);
		// mViewPager.setOffscreenPageLimit(3);
		mViewPager.setAdapter(pagerAdapter);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub

				currentHListViewItem.setPadding(0,
						(int) (Config.screenWidth * 0.01), 0,
						(int) (Config.screenWidth * 0.01));
				currentHListViewItem = hListView.getChildAt(arg0);
				currentHListViewItem.setPadding(0, 0, 0, 0);

				hListView.setSelection(arg0);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub

			}
		});

		// mAdapter = new ReadMyComicPagerAdapter(ReadMyComicActivity.this,
		// imagesPath, arrayImagesName, width, height);
		// viewPager.setAdapter(mAdapter);
		//
		// viewPager.setOnPageChangeListener(new OnPageChangeListener() {
		// @Override
		// public void onPageSelected(int arg0) {
		// currentHListViewItem.setPadding(0,
		// (int) (Config.screenWidth * 0.01), 0,
		// (int) (Config.screenWidth * 0.01));
		// currentHListViewItem = hListView.getChildAt(arg0);
		// currentHListViewItem.setPadding(0, 0, 0, 0);
		// hListView.setSelection(arg0);
		// }
		//
		// @Override
		// public void onPageScrolled(int arg0, float arg1, int arg2) {
		// }
		//
		// @Override
		// public void onPageScrollStateChanged(int arg0) {
		// }
		// });

		hListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				currentHListViewItem.setPadding(0,
						(int) (Config.screenWidth * 0.01), 0,
						(int) (Config.screenWidth * 0.01));
				currentHListViewItem = hListView.getChildAt(position);
				currentHListViewItem.setPadding(0, 0, 0, 0);
				mViewPager.setCurrentItem(position);
			}
		});
		currentHListViewItem = hListView.getChildAt(0);
		currentHListViewItem.setPadding(0, 0, 0, 0);

	}

	private void resizeHListView(HListView resizeHListView, int width,
			int height, int marginLeft, int marginTop, int marginRight,
			int marginBottom) {
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.height = height;
		llp.width = LayoutParams.WRAP_CONTENT;
		llp.setMargins(marginLeft, marginTop, marginRight, marginBottom);
		resizeHListView.setLayoutParams(llp);
	}
}
