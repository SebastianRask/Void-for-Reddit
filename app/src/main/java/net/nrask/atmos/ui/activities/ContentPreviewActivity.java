package net.nrask.atmos.ui.activities;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ScrollView;

import com.commit451.elasticdragdismisslayout.ElasticDragDismissFrameLayout;
import com.commit451.elasticdragdismisslayout.ElasticDragDismissListener;
import com.squareup.picasso.Picasso;

import net.nrask.atmos.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class ContentPreviewActivity extends AppCompatActivity implements ImageViewTouch.OnImageViewTouchSingleTapListener {
	@BindView(R.id.draggable_frame)
	ElasticDragDismissFrameLayout mDragView;

	@BindView(R.id.scrollview)
	ScrollView mScrollView;

	@BindView(R.id.preview)
	ImageViewTouch mPreview;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_content_preview);
		ButterKnife.bind(this);

		String wallpaperUrl = getIntent().getStringExtra(getString(R.string.shared_image_url));
		Picasso.with(this)
				.load(wallpaperUrl)
				.into(mPreview);

		mPreview.setSingleTapListener(this);
		mPreview.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);

		mDragView.addListener(new ElasticDragDismissListener() {
			@Override
			public void onDrag(float elasticOffset, float elasticOffsetPixels, float rawOffset, float rawOffsetPixels) {}

			@Override
			public void onDragDismissed() {
				if (Build.VERSION.SDK_INT >= 21) {
					finishAfterTransition();
				} else {
					finish();
				}
			}
		});

		if (Build.VERSION.SDK_INT >= 21) {
			//mDragView.addListener(new SystemChromeFader(this));
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onSingleTapConfirmed() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			finishAfterTransition();
		} else {
			finish();
		}
	}
}
