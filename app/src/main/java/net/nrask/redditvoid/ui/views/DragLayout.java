package net.nrask.redditvoid.ui.views;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.nrask.redditvoid.R;
import net.nrask.srjneeds.util.MathUtil;
import net.nrask.srjneeds.util.SRJUtil;

/**
 * Created by Sebastian Rask on 24-04-2017.
 *
 * Based on the very nice blog post here
 * http://flavienlaurent.com/blog/2013/08/28/each-navigation-drawer-hides-a-viewdraghelper/
 */

public class DragLayout extends ViewGroup {
	private int mCollapsedMargin;

	private ViewDragHelper mDragHelper;
	private DragHelperCallback mCallback;

	private View mHeaderView;
	private View mInnerHeaderView;
	private View mDescView;
	private TextView mTitleView;

	private float mInitialMotionY;

	private boolean mStartCollapsed;
	private int mTop;
	private float mCollapseOffset;
	private boolean isLaidOut;

	int dWidth, dHeight;
	int endWidth, mEndHeight;
	int mCollapseWidth, mCollapseHeight;
	int originalTitleTop = -1;

	public DragLayout(Context context) {
		this(context, null);
	}

	public DragLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public DragLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mDragHelper = ViewDragHelper.create(this, 1f, mCallback = new DragHelperCallback());
		mCollapsedMargin = SRJUtil.dpToPixels(getContext(), 16);
	}

	@Override
	protected void onFinishInflate() {
		mHeaderView = findViewById(R.id.viewHeader);
		mInnerHeaderView = findViewById(R.id.inner_header);
		mDescView = findViewById(R.id.viewDesc);
		mTitleView = (TextView) findViewById(R.id.txt_title);
	}

	public void expand() {
		smoothSlideTo(0f);
	}

	public void collapse() {
		smoothSlideTo(mCollapseOffset);
	}

	private boolean smoothSlideTo(float slideOffset) {
		int y = (int) (slideOffset * getDragRange());

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
				mInitialMotionY = y;
				break;
			}

			case MotionEvent.ACTION_UP: {
				float dy = y - mInitialMotionY;
				float percentageCollapsed = getPercentageCollapsed();

				boolean isCollapsing = dy > 0;
				boolean collapse = isCollapsing
						? percentageCollapsed > 0.25f
						: percentageCollapsed > 0.75f;
				
				if (collapse) {
					collapse();
				} else {
					expand();
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
		if (!isLaidOut) {
			endWidth = getViewWidth(mDescView);
			mEndHeight = getViewHeight(mHeaderView);

			//ToDo: Find suitable values
			int maxWidth = 800;
			int minWidth = 400;

			int maxHeight = 220;
			int minHeight = 150;

			mCollapseWidth = MathUtil.ensureRange(getViewWidth(mTitleView), minWidth, maxWidth);
			mCollapseHeight = MathUtil.ensureRange(getViewHeight(mTitleView), minHeight, maxHeight);

			dWidth = endWidth - mCollapseWidth;
			dHeight = mEndHeight - mCollapseHeight;

			mTop = isStartingCollapsed() ? getHeight() - mCollapsedMargin - getViewHeight(mHeaderView) : 0;
			mCallback.onViewPositionChanged(mHeaderView, 0, mTop, 0, 0);

			if (getPercentageCollapsed() > 0.5f) {
				collapse();
			} else {
				expand();
			}

			isLaidOut = true;
		}

		mDescView.layout(
				0,
				mTop + getViewHeight(mHeaderView),
				right,
				mTop + bottom
		);
	}

	public boolean isStartingCollapsed() {
		return mStartCollapsed;
	}

	public void setStartCollapsed(boolean startCollapsed) {
		this.mStartCollapsed = startCollapsed;
	}

	protected int getViewWidth(View view) {
		return view.getWidth() > 0 ? view.getWidth() : view.getMeasuredWidth();
	}

	protected int getViewHeight(View view) {
		return view.getHeight() > 0 ? view.getHeight() : view.getMeasuredHeight();
	}

	protected float getPercentageCollapsed() {
		return getDragOffset()/mCollapseOffset;
	}

	protected float getDragOffset() {
		return (float) mTop/getDragRange();
	}

	protected int getDragRange() {
		return getHeight() - mCollapseHeight;
	}

	private class DragHelperCallback extends ViewDragHelper.Callback {

		@Override
		public boolean tryCaptureView(View child, int pointerId) {
			return child == mHeaderView;
		}

		@Override
		public void onViewPositionChanged(View changedView, int l, int top, int dx, int dy) {
			int dragRange = getDragRange();

			mTop = top;
			mCollapseOffset = (float) ((dragRange - mCollapsedMargin) / (dragRange * 1.0));
			float percentageCollapsed = getPercentageCollapsed();
			float scale = 1;// - getPercentageCollapsed()/4f;

			int rightMarginBase = SRJUtil.dpToPixels(getContext(), mCollapsedMargin/4);
			int rightMargin = (int) (rightMarginBase * percentageCollapsed);

			int left = (int) ((dWidth - rightMarginBase) * percentageCollapsed);
			int right = (int) (endWidth  - rightMargin);

			int scaleOffsetBase = 150;
			int scaleOffset = (int) (scaleOffsetBase - scaleOffsetBase * scale);
			left = (int) (left ) + scaleOffset;
			right = (int) (right) + scaleOffset;

			mHeaderView.layout(
					left,
					mTop,
					right,
					(int) (mTop + mCollapseHeight + dHeight * (1 - percentageCollapsed))
			);

			mHeaderView.setLayoutParams(new LinearLayout.LayoutParams(
					right - left, LayoutParams.WRAP_CONTENT
			));

			if (originalTitleTop < 0) {
				originalTitleTop = mTitleView.getTop();
			}

			int innerHeaderTop = (int) (-originalTitleTop + originalTitleTop * (1 - percentageCollapsed));
			mInnerHeaderView.layout(
					mInnerHeaderView.getLeft(),
					innerHeaderTop,
					mInnerHeaderView.getRight(),
					mInnerHeaderView.getBottom()
			);

			// "Animate" views
			float sizeScale = 1 + (1 - getDragOffset());

			mHeaderView.setScaleX(scale);
			mHeaderView.setScaleY(scale);

			mDescView.setAlpha(1 - getPercentageCollapsed());

			requestLayout();
		}

		@Override
		public int getViewVerticalDragRange(View child) {
			return getDragRange();
		}

		@Override
		public int clampViewPositionVertical(View child, int top, int dy) {
			int topBound = getPaddingTop();
			int bottomBound = getHeight() - mHeaderView.getHeight() - mHeaderView.getPaddingBottom();

			return Math.min(Math.max(top, topBound), bottomBound);
		}
	}

}
