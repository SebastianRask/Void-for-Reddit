package net.nrask.atmos.data;

import android.os.AsyncTask;

import net.nrask.atmos.RedditManager;

/**
 * Created by Sebastian Rask on 21-04-2017.
 */

public abstract class RedditAsyncTask<T> extends AsyncTask<Void, Void, T> {
	protected SimpleCallback<T> mSimpleTaskCallback;

	public void start() {
		RedditManager.getInstance().authenticateIfNeeded(new AuthenticateRedditTask.AuthenticationCallback() {
			@Override
			public void handleRedditAuthenticated() {
				executeOnExecutor(THREAD_POOL_EXECUTOR);
			}

			@Override
			public void handleRedditAuthenticationFailed() {

			}
		});
	}

	@Override
	protected void onPostExecute(T taskResult) {
		super.onPostExecute(taskResult);
		if (mSimpleTaskCallback != null) {
			mSimpleTaskCallback.onTaskCompleted(taskResult);
		}
	}

	public interface SimpleCallback<T> {
		void onTaskCompleted(T result);
	}
}
