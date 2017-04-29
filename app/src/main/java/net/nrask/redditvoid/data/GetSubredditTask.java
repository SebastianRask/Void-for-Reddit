package net.nrask.redditvoid.data;

import net.dean.jraw.RedditClient;
import net.dean.jraw.models.Subreddit;

/**
 * Created by Sebastian Rask on 23-04-2017.
 */

public class GetSubredditTask extends RedditAsyncTask<Subreddit> {
	private String subredditName;
	private RedditClient redditClient;
	public SubredditTaskCallback[] callbacks;

	public GetSubredditTask(String subredditName, RedditClient redditClient, SubredditTaskCallback... callbacks) {
		this.subredditName = subredditName;
		this.redditClient = redditClient;
		this.callbacks = callbacks;
	}

	@Override
	protected Subreddit doInBackground(Void... voids) {
		return redditClient.getSubreddit(subredditName);
	}

	@Override
	protected void onPostExecute(Subreddit taskResult) {
		super.onPostExecute(taskResult);
		if (callbacks == null) {
			return;
		}

		for (SubredditTaskCallback callback : callbacks) {
			callback.onTaskCompleted(taskResult);
		}
	}

	public interface SubredditTaskCallback extends SimpleCallback<Subreddit> {}
}
