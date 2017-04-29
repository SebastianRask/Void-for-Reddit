package net.nrask.redditvoid.ui.views;

import android.content.Context;
import android.util.AttributeSet;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;

/**
 * Created by Sebastian Rask on 22-04-2017.
 */

public class ImprovedImageZoomView extends ImageViewTouch {
	public ImprovedImageZoomView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ImprovedImageZoomView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	private void init() {
		setMinScale(1f);
	}

	@Override
	public float getMinScale() {
		return 1f;
	}

//	@Override
//	public float getMaxScale() {
//		return 10f;
//	}
}
