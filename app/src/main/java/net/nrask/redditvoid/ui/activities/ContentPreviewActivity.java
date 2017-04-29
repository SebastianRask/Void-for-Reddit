package net.nrask.redditvoid.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.ChangeBounds;
import android.transition.ChangeImageTransform;
import android.transition.ChangeTransform;
import android.transition.Fade;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.MenuItem;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.commit451.elasticdragdismisslayout.ElasticDragDismissFrameLayout;
import com.commit451.elasticdragdismisslayout.ElasticDragDismissListener;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import net.dean.jraw.models.Submission;
import net.nrask.redditvoid.R;
import net.nrask.redditvoid.RedditManager;
import net.nrask.redditvoid.ui.views.DragLayout;
import net.nrask.srjneeds.animation.TextSizeTransition;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class ContentPreviewActivity extends BaseActivity implements ImageViewTouch.OnImageViewTouchSingleTapListener {
	@BindView(R.id.preview)
	ImageViewTouch mPreview;

	@BindView(R.id.txt_title)
	TextView mTitle;

	@BindView(R.id.submission_toolbar)
	Toolbar mToolbar;

	@BindView(R.id.drag_layout)
	DragLayout mDragLayout;


	public static Intent createStartIntent(Context context, Submission submission, boolean showPreview) throws JsonProcessingException {
		Intent startIntent = new Intent(context, ContentPreviewActivity.class);
		startIntent.putExtra(context.getString(R.string.intent_reddit_submission_show_review), showPreview);
		startIntent.putExtra(context.getString(R.string.intent_reddit_submission), RedditManager.getInstance().serializeSubmission(submission));
		return startIntent;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_content_preview);
		ButterKnife.bind(this);

		Submission redditSubmission;
		try {
			redditSubmission = getSubmissionExtra(R.string.intent_reddit_submission);
		} catch (IOException e) {
			e.printStackTrace();
			//ToDo: Handle
			return;
		}
		boolean showPreview = getIntent().getBooleanExtra(getString(R.string.intent_reddit_submission_show_review), true);
		mDragLayout.setStartCollapsed(showPreview);

		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setTitle(redditSubmission.getSubredditName());

		String imageUrl = manager.getSubmissionThumbnailUrl(redditSubmission, null);
		if (imageUrl != null) {
			Picasso.with(this)
					.load(imageUrl)
					.into(mPreview);
		}

		mPreview.setSingleTapListener(this);
		mPreview.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);

		mTitle.setText(redditSubmission.getTitle());
	}

	@Override
	protected void onResume() {
		super.onResume();
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
		}

		return super.onOptionsItemSelected(item);
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
