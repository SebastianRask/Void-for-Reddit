package net.nrask.atmos.ui.viewholders;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.squareup.picasso.Picasso;

import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.nrask.atmos.MiscUtil;
import net.nrask.atmos.R;
import net.nrask.atmos.RedditManager;
import net.nrask.atmos.data.GetSubredditTask;
import net.nrask.atmos.ui.span.SubredditClickableSpan;
import net.nrask.srjneeds.SRJViewHolder;
import net.nrask.srjneeds.util.ColorUtil;
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

	private String thumbnailUrl;
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
		thumbnailUrl = data.getThumbnail() != null
				? data.getThumbnails().getVariations()[data.getThumbnails().getVariations().length - 1].getUrl()
				: data.getThumbnail();

		if (thumbnailUrl != null && thumbnailUrl.length() > 0) {
			thumbnailUrl = Html.fromHtml(thumbnailUrl).toString(); // Encode GET Parameters
			Picasso.with(itemView.getContext())
					.load(thumbnailUrl)
					.into(mThumbnail);
		}

		mTitle.setText(data.getTitle());

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

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	@Override
	public void onTaskCompleted(Subreddit subreddit) {
		// Make sure the loaded subreddit is
		this.subreddit = subreddit;
		if (!subreddit.getDisplayName().equals(data.getSubredditName())) {
			return;
		}

		String subredditColor = manager.getSubredditKeyColor(subreddit);
		String subredditIconUrl = manager.getSubredditIcon(subreddit);

		// Set key color as background if it is available
		if (subredditColor != null && subredditColor.length() > 0) {
			mSubredditHeaderKeyImage.setColorFilter(Color.parseColor(manager.getSubredditKeyColor(subreddit)));
		}
		DrawableTypeRequest noHeaderImage = Glide.with(getContext()).load(R.drawable.ic_reddit_snoo_24dp);
		Glide.with(getContext())
				.load(subredditIconUrl)
				.thumbnail(noHeaderImage)
				.error(R.drawable.ic_reddit_snoo_24dp)
				.fallback(R.drawable.ic_reddit_snoo_24dp)
				.centerCrop()
				.into(mSubredditHeader);
	}

	@Override
	public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
		return false;
	}

	@Override
	public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
		final Bitmap bitmap = MiscUtil.getBitmap(resource);
		final int twentyFourDip = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, itemView.getContext().getResources().getDisplayMetrics());
		if (bitmap == null) {
			return false;
		}
		Palette.from(bitmap)
				.maximumColorCount(3)
				.clearFilters()
				.setRegion(0, 0, bitmap.getWidth() - 1, twentyFourDip)
				.generate(new Palette.PaletteAsyncListener() {
					@Override
					public void onGenerated(Palette palette) {
						boolean isDark;
						@ColorUtil.Lightness int lightness = ColorUtil.isDark(palette);
						if (lightness == ColorUtil.LIGHTNESS_UNKNOWN) {
							isDark = ColorUtil.isDark(bitmap, bitmap.getWidth() / 2, 0);
						} else {
							isDark = lightness == ColorUtil.IS_DARK;
						}

						// color the status bar. Set a complementary dark color on L,
						// light or dark color on M (with matching status bar icons)
						final Palette.Swatch topColor = ColorUtil.getMostPopulousSwatch(palette);
						if (topColor != null && (isDark || Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)) {
							int backgroundColor = ColorUtil.scrimify(topColor.getRgb(), isDark, 0.075f);
							mSubredditHeader.setBackgroundColor(backgroundColor);
						}
					}
				});
		return false;
	}
}
