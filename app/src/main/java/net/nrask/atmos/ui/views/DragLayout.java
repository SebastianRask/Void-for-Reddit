package net.nrask.atmos.ui.views;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import net.nrask.atmos.R;

/**
 * Created by Sebastian Rask on 24-04-2017.
 *
 * Based on the very nice blog post here
 * http://flavienlaurent.com/blog/2013/08/28/each-navigation-drawer-hides-a-viewdraghelper/
 */

public class DragLayout extends ViewGroup {

	private ViewDragHelper mDragHelper;
	private DragHelperCallback mCallback;

	private View mHeaderView;
	private View mDescView;
	private View mTitleView;

	private float mInitialMotionX;
	private float mInitialMotionY;

	private int mDragRange;
	private int mTop;
	private int mRight;
	private float mDragOffset;

	private boolean isLaidOut;

	public DragLayout(Context context) {
		this(context, null);
	}

	public DragLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DragLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mDragHelper = ViewDragHelper.create(this, 1f, mCallback = new DragHelperCallback());
	}

	@Override
	protected void onFinishInflate() {
		mHeaderView = findViewById(R.id.viewHeader);
		mDescView = findViewById(R.id.viewDesc);
		mTitleView = findViewById(R.id.txt_title);
	}

	public void maximize() {
		smoothSlideTo(0f);
	}

	public void minimize() {
		smoothSlideTo(0.95f);
	}

	private boolean smoothSlideTo(float slideOffset) {
		int topBound = getPaddingTop();
		int y = (int) (topBound + slideOffset * mDragRange);

		if (mDragHelper.smoothSlideViewTo(mHeaderView, mHeaderView.getLeft(), y)) {
			ViewCompat.postInvalidateOnAnimation(this);
			return true;
		}
		return false;
	}

	@Override
	public void computeScroll() {
		if (mDragHelper.continueSettling(true)) {
			ViewCompat.postInvalidateOnAnimation(this);
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		int action = MotionEventCompat.getActionMasked(ev);

		if (action != MotionEvent.ACTION_DOWN) {
			mDragHelper.cancel();
			return super.onInterceptTouchEvent(ev);
		}

		float x = ev.getX();
		float y = ev.getY();
		boolean interceptTap = false;

		switch (action) {
			case MotionEvent.ACTION_DOWN: {
				mInitialMotionX = x;
				mInitialMotionY = y;
				interceptTap = mDragHelper.isViewUnder(mHeaderView, (int) x, (int) y);
				break;
			}
		}

		return mDragHelper.shouldInterceptTouchEvent(ev) || interceptTap;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		mDragHelper.processTouchEvent(ev);

		int action = ev.getAction();
		float x = ev.getX();
		float y = ev.getY();

		boolean isHeaderViewUnder = mDragHelper.isViewUnder(mHeaderView, (int) x, (int) y);
		switch (action & MotionEventCompat.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN: {
				mInitialMotionX = x;
				mInitialMotionY = y;
				break;
			}

			case MotionEvent.ACTION_UP: {
				float dy = y - mInitialMotionY;

				boolean isCollapsing = dy > 0;
				boolean collapse = isCollapsing
						? mDragOffset > 0.25f
						: mDragOffset > 0.75f;
				
				if (collapse) {
					minimize();
				} else {
					maximize();
				}
				break;
			}
		}

		return isHeaderViewUnder && isViewHit(mHeaderView, (int) x, (int) y) || isViewHit(mDescView, (int) x, (int) y);
	}


	private boolean isViewHit(View view, int x, int y) {
		int[] viewLocation = new int[2];
		view.getLocationOnScreen(viewLocation);
		int[] parentLocation = new int[2];
		this.getLocationOnScreen(parentLocation);
		int screenX = parentLocation[0] + x;
		int screenY = parentLocation[1] + y;
		return screenX >= viewLocation[0] && screenX < viewLocation[0] + view.getWidth() &&
				screenY >= viewLocation[1] && screenY < viewLocation[1] + view.getHeight();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		measureChildren(widthMeasureSpec, heightMeasureSpec);

		int maxWidth = View.MeasureSpec.getSize(widthMeasureSpec);
		int maxHeight = View.MeasureSpec.getSize(heightMeasureSpec);

		setMeasuredDimension(
				resolveSizeAndState(maxWidth, widthMeasureSpec, 0),
				resolveSizeAndState(maxHeight, heightMeasureSpec, 0)
		);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		mDragRange = getHeight() - mHeaderView.getHeight();

		if (!isLaidOut) {
			//mTop = 1400;
			mCallback.onViewPositionChanged(mHeaderView, 0, 1400, 0, 0);
			isLaidOut = true;
		}

//		int endWidth = getViewWidthEstimate(mDescView);
//		int endHeight = getViewHeightEstimate(mHeaderView); // Find out how to find end height
//
//		int collapseWidth = getViewWidthEstimate(mTitleView);
//		int collapseHeight = getViewHeightEstimate(mTitleView);
//
//		int dy = endHeight - collapseHeight;
//		int dx = endWidth - collapseWidth;
//
//		int rightMargin = (int) (100 * mDragOffset);
//		int leftMargin = (int) (400 * mDragOffset);
//
//		float percentExpanded = 1 + mDragOffset;
//
//		mHeaderView.layout(
//				leftMargin,
//				mTop,
//				right - rightMargin,
//				mTop + getViewHeightEstimate(mTitleView)
//		);

		mDescView.layout(
				0,
				mTop + mHeaderView.getMeasuredHeight(),
				right,
				mTop + bottom
		);
	}

	protected int getViewWidthEstimate(View view) {
		return view.getWidth() > 0 ? view.getWidth() : view.getMeasuredWidth();
	}

	protected int getViewHeightEstimate(View view) {
		return view.getHeight() > 0 ? view.getHeight() : view.getMeasuredHeight();
	}

	private class DragHelperCallback extends ViewDragHelper.Callback {

		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			return child == mHeaderView;
		}

		@Override
		public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
			int endWidth = getViewWidthEstimate(mDescView);
			int endHeight = getViewHeightEstimate(mHeaderView); // Find out how to find end height

			int collapseWidth = getViewWidthEstimate(mTitleView);
			int collapseHeight = getViewHeightEstimate(mTitleView);

			mTop = top;
			mDragOffset = (float) top / mDragRange;
			mRight = endWidth;

			int rightMargin = (int) (100 * mDragOffset);
			int leftMargin = (int) (400 * mDragOffset);

			float percentExpanded = 1 + mDragOffset;

			mHeaderView.layout(
					leftMargin,
					mTop,
					endWidth - rightMargin,
					mTop + getViewHeightEstimate(mTitleView)
			);

			// "Animate" views
			float sizeScale = 1 + (1 - mDragOffset);
//			mHeaderView.setPivotX(width);
//			mHeaderView.setPivotY(height);
//			mHeaderView.setLayoutParams(new LayoutParams(
//					(int) (titleWidth * sizeScale),
//					(int) (titleHeight * sizeScale)
//			));
//			mHeaderView.setScaleX(sizeScale);
//			mHeaderView.setScaleY(sizeScale);

			mDescView.setAlpha(1 - mDragOffset);

			requestLayout();
		}

		@Override
		public void onViewReleased(View releasedChild, float xvel, float yvel) {
			int top = getPaddingTop();
			if (yvel > 0 || (yvel == 0 && mDragOffset > 0.5f)) {
				top += mDragRange;
			}

			if (releasedChild != null) {
				mDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top);
			}
		}

		@Override
		public int getViewVerticalDragRange(View child) {
			return mDragRange;
		}

		@Override
		public int clampViewPositionVertical(View child, int top, int dy) {
			int topBound = getPaddingTop();
			int bottomBound = getHeight() - mHeaderView.getHeight() - mHeaderView.getPaddingBottom();

			return Math.min(Math.max(top, topBound), bottomBound);
		}
	}

}
