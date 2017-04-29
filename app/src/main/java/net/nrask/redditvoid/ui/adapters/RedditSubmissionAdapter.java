package net.nrask.redditvoid.ui.adapters;

import android.view.View;

import net.dean.jraw.models.Submission;
import net.nrask.redditvoid.R;
import net.nrask.redditvoid.data.RedditAsyncTask;
import net.nrask.redditvoid.ui.viewholders.RedditSubmissionViewHolder;
import net.nrask.srjneeds.SRJAdapter;

/**
 * Created by Sebastian Rask on 22-04-2017.
 */

public class RedditSubmissionAdapter extends SRJAdapter<Submission, RedditSubmissionViewHolder> {
	private SubmissionClickListener mClickListener;

	@Override
	protected int getLayoutResource(int viewType) {
		return R.layout.cell_submission;
	}

	@Override
	protected ViewHolderFactory<RedditSubmissionViewHolder> getViewHolderCreator(int viewType) {
		return new ViewHolderFactory<RedditSubmissionViewHolder>() {
			@Override
			public RedditSubmissionViewHolder create(View itemView) {
				return new RedditSubmissionViewHolder(itemView);
			}
		};
	}

	@Override
	public void onBindViewHolder(final RedditSubmissionViewHolder holder, int position) {
		super.onBindViewHolder(holder, position);
		if (mClickListener != null) {
			holder.getThumbnailView().setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					mClickListener.onPreviewClicked(view, holder);
				}
			});
		}

	}

	@Override
	public void setItemCallback(ItemCallback<RedditSubmissionViewHolder> mItemCallback) {
		super.setItemCallback(mItemCallback);
		throw new IllegalStateException("This adapter does not support this callback. Instead use setSubmissionClickListener");
	}

	public void setSubmissionClickListener(SubmissionClickListener mClickListener) {
		super.setItemCallback(mClickListener);
		this.mClickListener = mClickListener;
	}

	public interface SubmissionClickListener extends ItemCallback<RedditSubmissionViewHolder> {
		void onPreviewClicked(View clickedView, RedditSubmissionViewHolder viewHolder);
	}
}
