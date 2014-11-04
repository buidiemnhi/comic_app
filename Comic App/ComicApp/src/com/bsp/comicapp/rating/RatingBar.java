package com.bsp.comicapp.rating;


import com.bsp.comicapp.R;

import android.R.animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class RatingBar extends View {

	int heightRating, widthRating;
	Rect selRect = new Rect();
	int xStart = 0, yStart = 0;
	boolean arrRating[] = new boolean[5];
	double xss;
	public int number = 0;
	boolean enable = true;

	OnRatingListener mOnRatingListener;
	OnActionUpListener mOnActionUpListener;

	void getRect(int x, int y) {

		selRect = new Rect();
		selRect.set(x * widthRating, y * getHeight(), x * widthRating
				+ widthRating, y * getHeight() + getHeight());
	}

	public RatingBar(Context context) {
		super(context);
		for (int i = 0; i < arrRating.length; i++)
			arrRating[i] = false;
		number = 0;

	}

	public RatingBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public RatingBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// setMeasuredDimension(widthMeasureSpec, widthMeasureSpec);

		// heightRating = heightMeasureSpec / 5;
		// widthRating = widthMeasureSpec / 5;
		Log.d("qqqqq", String.valueOf(heightMeasureSpec));
	}

	/**
	 * ve
	 */
	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		// heightRating = getHeight() / 5;
		widthRating = getWidth() / 5;
		super.onDraw(canvas);
		Paint background = new Paint();
		background.setColor(getResources()
				.getColor(android.R.color.transparent));
		Paint line = new Paint();
		// line.setColor(getResources().getColor(R.color.blue));
		canvas.drawRect(0, 0, getWidth(), getHeight(), background);
		for (int i = 1; i < 5; i++) {
			// canvas.drawLine(i * widthRating, 0, i * widthRating, getHeight(),
			// line);

		}
		Bitmap bmRating = BitmapFactory.decodeResource(getResources(),
				R.drawable.pink_star);
		bmRating = getResizedBitmap(bmRating, getHeight(), widthRating);

		Bitmap bmNotRating = BitmapFactory.decodeResource(getResources(),
				R.drawable.grey_star);
		bmNotRating = getResizedBitmap(bmNotRating, getHeight(), widthRating);
		for (int i = 0; i < arrRating.length; i++) {

			select1(i, 0);
			if (arrRating[i] == false) {
				canvas.drawBitmap(
						bmNotRating,
						new Rect(0, 0, bmNotRating.getWidth(), bmNotRating
								.getHeight()), selRect, line);
			} else {
				canvas.drawBitmap(bmRating, new Rect(0, 0, bmRating.getWidth(),
						bmRating.getHeight()), selRect, line);
			}
		}

	}

	public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		// CREATE A MATRIX FOR THE MANIPULATION
		Matrix matrix = new Matrix();
		// RESIZE THE BIT MAP
		matrix.postScale(scaleWidth, scaleHeight);

		// "RECREATE" THE NEW BITMAP
		Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height,
				matrix, true);
		return resizedBitmap;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (enable)
			if (event.getAction() == MotionEvent.ACTION_MOVE) {
				select((int) event.getX() / widthRating, (int) event.getY()
						/ getHeight());
				xss = (double) (event.getX() / widthRating);

			}
		if (event.getAction() == MotionEvent.ACTION_UP) {
			onActionUpListener.onActionUp(number);
		}
		return true;
	}

	int columnOld = -13223;

	// tô nhưng đã được chọn
	void select(int x, int y) {

		Log.d("aaaaa000", String.valueOf(y) + "," + String.valueOf(x));
		int columnNew = Math.min(Math.max(x, 0), 4);
		int row = Math.min(Math.max(y, 0), 0);
		// Log.d("333", String.valueOf(xx) + "," + String.valueOf(xss));

		//
		if (columnOld != columnNew || columnOld == 0) {
			columnOld = columnNew;
			number = columnOld;
			Log.d("333", String.valueOf(columnOld));
			setRatingNumber(row * 5 + columnNew);

		}
		// neu tọa đô move nho hon 0.1 va dang ở ô đầu tiên (0) thì bổ chọn

	}

	// đanh dâu nhưng ô đã được chọn
	public void setRatingNumber(int x) {

		for (int i = 0; i < arrRating.length; i++) {
			if (i <= x) {

				arrRating[i] = true;
				Log.d("aaaaa1",
						String.valueOf(i) + "," + String.valueOf(arrRating[i]));

			} else {
				arrRating[i] = false;
				Log.d("aaaaa122222",
						String.valueOf(i) + "," + String.valueOf(arrRating[i]));
			}

		}

		Log.d("sss", String.valueOf(x));
		if (xss < 0.1 && x == 0) {
			arrRating[0] = false;
		}
		getRect(x, 0);
		invalidate(selRect);
		onRatingListener.onRating(x);

	}

	// truyền thông với biên ngoài
	public OnRatingListener onRatingListener = new OnRatingListener() {

		@Override
		public void onRating(int number) {

			if (mOnRatingListener != null)
				if (arrRating[0])
					mOnRatingListener.onRating(number + 1);
				else
					mOnRatingListener.onRating(0);
		}

	};

	public OnActionUpListener onActionUpListener = new OnActionUpListener() {

		@Override
		public void onActionUp(int numberStar) {
			if (mOnActionUpListener != null)
				if (arrRating[0])
					mOnActionUpListener.onActionUp(number + 1);
				else
					mOnActionUpListener.onActionUp(0);

		}
	};

	public void setActionUpListener(OnActionUpListener actionUpListener) {
		mOnActionUpListener = actionUpListener;
	}

	public int getStart() {
		return number;
	}

	// set số sáo được đánh giá
	public void setStart(int numberStart) {
		number = numberStart - 1;
		setRatingNumber(numberStart - 1);

		getRect(numberStart - 1, 0);
		invalidate(selRect);
		// onRatingListener.onRating(numberStart);

	}

	void select1(int x, int y) {

		int rx = Math.min(Math.max(x, 0), 4);
		int ry = Math.min(Math.max(y, 0), 0);
		getRect(rx, ry);
		invalidate(selRect);
	}

	// set enable không cho rating
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		this.enable = enabled;
	}

	// giao tiếp với bên ngoài
	public void setOnRatingListenr(OnRatingListener onRatingListener) {
		mOnRatingListener = onRatingListener;
	}

	/**
	 * bay sự kiện ratings
	 * 
	 * @author Thang
	 * 
	 */
	public interface OnRatingListener {
		public void onRating(int number);

	}

	public interface OnActionUpListener {
		public void onActionUp(int numberStar);
	}

}
