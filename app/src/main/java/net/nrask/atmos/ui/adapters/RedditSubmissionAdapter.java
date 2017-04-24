package net.nrask.atmos.ui.adapters;

import android.view.View;

import net.dean.jraw.models.Submission;
import net.nrask.atmos.R;
import net.nrask.atmos.ui.viewholders.RedditSubmissionViewHolder;
import net.nrask.srjneeds.SRJAdapter;

/**
 * Created by Sebastian Rask on 22-04-2017.
 */

public class RedditSubmissionAdapter extends SRJAdapter<Submission, RedditSubmissionViewHolder> {

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
}
