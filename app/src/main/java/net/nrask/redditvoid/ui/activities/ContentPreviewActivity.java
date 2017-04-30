package net.nrask.redditvoid.ui.activities;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionSet;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.squareup.picasso.Picasso;

import net.dean.jraw.models.Submission;
import net.nrask.redditvoid.R;
import net.nrask.redditvoid.RedditManager;
import net.nrask.redditvoid.ui.views.DragLayout;
import net.nrask.srjneeds.animation.interpolators.ReverseInterpolator;
import net.nrask.srjneeds.util.SRJUtil;

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

	@BindView(R.id.background)
	View mTranslucentBackground;

	@BindView(R.id.comments_container)
	View mCommentsContainer;

	@BindView(R.id.viewHeader)
	View mSubmissionHeader;

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
		setupTransitions(showPreview);
		setupToolbar(mToolbar, redditSubmission);
		setupSubmissionImageView(mPreview);

		if (showPreview) {
			mTitle.setLayoutParams(new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
			));
		}

		tryShowImageUrl(mPreview, manager.getSubmissionThumbnailUrl(redditSubmission, null));

		mTitle.setText(redditSubmission.getTitle());
	}

	@Override
	public void onBackPressed() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			getWindow().setSharedElementReturnTransition(constructSharedReturnTransition(mDragLayout.isCollapsed()));
		}

		if (!mDragLayout.isCollapsed()) {
			mPreview.setVisibility(View.GONE);
			mTranslucentBackground.setVisibility(View.GONE);
		}

		super.onBackPressed();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSingleTapConfirmed() {
		onBackPressed();
	}

	private void setupToolbar(Toolbar toolbar, Submission submission) {
		setSupportActionBar(toolbar);
		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setTitle(submission.getSubredditName());
		}
	}


	private void setupSubmissionImageView(ImageViewTouch imageViewTouch) {
		imageViewTouch.setSingleTapListener(this);
		imageViewTouch.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);
	}

	private void setupTransitions(boolean sharedPreview) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
			return;
		}

		getWindow().setReturnTransition(constructEnterTransition());
		getWindow().setEnterTransition(constructEnterTransition());
		getWindow().setSharedElementEnterTransition(constructSharedEnterTransition(sharedPreview));

		if (!sharedPreview) {
			// Hide these views while the shared enter transition is in progress.
			mPreview.setVisibility(View.INVISIBLE);
			mTranslucentBackground.setVisibility(View.INVISIBLE);

			getWindow().getEnterTransition().addListener(new Transition.TransitionListener() {

				@Override
				public void onTransitionEnd(Transition transition) {
					mPreview.setVisibility(View.VISIBLE);
					mTranslucentBackground.setVisibility(View.VISIBLE);
				}

				public void onTransitionCancel(Transition transition) {
					onTransitionEnd(transition);
				}

				public void onTransitionStart(Transition transition) {}
				public void onTransitionPause(Transition transition) {}
				public void onTransitionResume(Transition transition) {}
			});
		}
	}

	private boolean tryShowImageUrl(ImageView imageView, @Nullable String imageUrl) {
		if (imageUrl != null) {
			Picasso.with(this)
					.load(imageUrl)
					.into(imageView);

			return true;
		}

		return false;
	}

	private Transition constructEnterTransition() {
		int slideEnterTransDuration = 340;
		Transition commentsTrans = new Slide(Gravity.BOTTOM);
		commentsTrans.setDuration(slideEnterTransDuration);
		commentsTrans.addTarget(mCommentsContainer);

		Transition toolbarTrans = new Slide(Gravity.TOP);
		toolbarTrans.addTarget(mSubmissionHeader);

//		Transition titleTrans = new Slide(Gravity.RIGHT);
//		titleTrans.addTarget(R.id.viewHeader);

		Transition fadeEnterTrans = new Fade();
		fadeEnterTrans.addTarget(mPreview);
		fadeEnterTrans.addTarget(mTranslucentBackground);
		fadeEnterTrans.setStartDelay(slideEnterTransDuration);
		fadeEnterTrans.setDuration(0);

		Transition allViewsTrans = new Fade();
		allViewsTrans.excludeTarget(mCommentsContainer, true);

		TransitionSet enterTrans = new TransitionSet();
		enterTrans.addTransition(fadeEnterTrans);
		enterTrans.addTransition(commentsTrans);
		enterTrans.addTransition(allViewsTrans);
		enterTrans.addTransition(toolbarTrans);

		return enterTrans;
	}

	private Transition constructSharedEnterTransition(boolean isCollapsed) {
		TransitionSet result = new TransitionSet();
		Transition moveTrans = TransitionInflater.from(this).inflateTransition(android.R.transition.move);

		if (isCollapsed) {
			moveTrans.addTarget(mPreview);
			moveTrans.addTarget(mTranslucentBackground);
		} else {
			moveTrans.addTarget(mTitle);
		}

		result.addTransition(moveTrans);
		return result;
	}

	private Transition constructSharedReturnTransition(boolean isCollapsed) {
		Transition trans = TransitionInflater.from(this).inflateTransition(android.R.transition.move);
		trans.addTarget(mTitle);
		if (isCollapsed) {
			trans.addTarget(mPreview);
			trans.addTarget(mTranslucentBackground);
		}

		return trans;
	}
}
