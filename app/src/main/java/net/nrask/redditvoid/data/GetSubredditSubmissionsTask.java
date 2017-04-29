package net.nrask.redditvoid.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;

/**
 * Created by Sebastian Rask on 21-04-2017.
 */

public class GetSubredditSubmissionsTask extends RedditAsyncTask<Listing<Submission>> {
	private SubredditPaginator mPaginator;

	public GetSubredditSubmissionsTask(@NonNull SubredditPaginator paginator, @Nullable SimpleCallback<Listing<Submission>> callback) {
		this.mPaginator = paginator;
		this.mSimpleTaskCallback = callback;
	}

	@Override
	protected Listing<Submission> doInBackground(Void... voids) {
		return mPaginator.next();
	}
}
