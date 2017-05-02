package net.nrask.redditvoid.ui.viewholders;

import android.graphics.Color;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.squareup.picasso.Picasso;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.nrask.redditvoid.R;
import net.nrask.redditvoid.RedditManager;
import net.nrask.redditvoid.data.GetSubredditTask;
import net.nrask.redditvoid.ui.span.SubredditClickableSpan;
import net.nrask.srjneeds.SRJViewHolder;
import net.nrask.srjneeds.util.SRJSpanBuilder;
import net.nrask.srjneeds.util.SRJUtil;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Sebastian Rask on 22-04-2017.
 */

public class RedditSubmissionViewHolder extends SRJViewHolder<Submission> implements GetSubredditTask.SubredditTaskCallback, RequestListener<String, GlideDrawable> {
	private final float IMAGE_ASPECT_RATIO = 16f/9f;

	@BindView(R.id.img_thumbnail)
	protected ImageView mThumbnail;

	@BindView(R.id.img_submission_subreddit_header)
	protected ImageView mSubredditHeader;

	@BindView(R.id.img_submission_header_key)
	protected ImageView mSubredditHeaderKeyImage;

	@BindView(R.id.txt_title)
	protected TextView mTitle;

	@BindView(R.id.txt_author)
	protected TextView mAuthor;

	@BindView(R.id.img_placeholder)
	protected ImageView mImgPlaceholderView;

	@BindView(R.id.txt_meta_data)
	protected TextView mMetaData;

	@BindView(R.id.cell_background)
	protected View mBackground;

	private Subreddit subreddit;
	private RedditManager manager = RedditManager.getInstance();

	public RedditSubmissionViewHolder(View itemView) {
		super(itemView);
		ButterKnife.bind(this, itemView);

		mThumbnail.setLayoutParams(
				new LinearLayout.LayoutParams(
						LinearLayout.LayoutParams.MATCH_PARENT,
						(int) (SRJUtil.getScreenWidth(itemView.getContext()) / IMAGE_ASPECT_RATIO) // New Height
				)
		);
	}

	@Override
	protected void onDataBinded(Submission data) {
		String imageUrl = manager.getSubmissionThumbnailUrl(data, null);
		if (imageUrl != null) {
			Picasso.with(itemView.getContext())
					.load(imageUrl)
					.into(mThumbnail);
		}

		mTitle.setText(data.getTitle());
		mMetaData.setText(data.getScore() + "");
		mImgPlaceholderView.setVisibility(View.VISIBLE);

		mAuthor.setMovementMethod(LinkMovementMethod.getInstance());
		mAuthor.setText(
				SRJSpanBuilder.init()
						.append("In ")
						.append(data.getSubredditName())
						.withClickable(new SubredditClickableSpan(subreddit))
						.withColor(R.color.colorHighOpacityBlackText, itemView.getContext())
						.append(" by ")
						.append(data.getAuthor())
						.withClickable(new SubredditClickableSpan(subreddit))
						.withColor(R.color.colorHighOpacityBlackText, itemView.getContext())
						.append("\n" + manager.getReadablePostDate(data.getCreated(), itemView.getContext()))
						.build()
		);

		manager.getSubreddit(data.getSubredditName(), this, true);
	}

	public ImageView getThumbnailView() {
		return mThumbnail;
	}

	public TextView getTitleView() {
		return mTitle;
	}

	public View getBackground() {
		return mBackground;
	}

	@Override
	public void onTaskCompleted(Subreddit subreddit) {
		// Make sure the loaded subreddit is the same we requested
		if (subreddit == null || !subreddit.getDisplayName().equals(data.getSubredditName())) {
			return;
		}

		this.subreddit = subreddit;

		String subredditColor = manager.getSubredditKeyColor(subreddit);
		String subredditIconUrl = manager.getSubredditIcon(subreddit);

		// Set key color as background if it is available
		if (subredditColor != null && subredditColor.length() > 0) {
			mSubredditHeaderKeyImage.setColorFilter(Color.parseColor(manager.getSubredditKeyColor(subreddit)));
		}

		Glide.with(getContext())
				.load(subredditIconUrl)
				.centerCrop()
				.listener(this)
				.crossFade()
				.into(mSubredditHeader);
	}

	@Override
	public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
		return false;
	}

	@Override
	public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
		mImgPlaceholderView.setVisibility(View.GONE);
		return false;
	}
}
