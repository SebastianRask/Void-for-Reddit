package net.nrask.redditvoid.data;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.oauth.Credentials;
import net.dean.jraw.http.oauth.OAuthData;
import net.dean.jraw.http.oauth.OAuthException;

import java.net.SocketTimeoutException;
import java.util.UUID;

/**
 * Created by Sebastian Rask on 19-04-2017.
 */

public class AuthenticateRedditTask extends RedditAsyncTask<OAuthData> {
	private RedditClient mRedditClient;
	private Credentials mCredentials;

	private AuthenticationCallback mCallback;

	public AuthenticateRedditTask(@NonNull RedditClient redditClient, @NonNull String clientId, @Nullable AuthenticationCallback listener) {
		mRedditClient = redditClient;
		mCallback = listener;
		mCredentials = Credentials.userlessApp(clientId, UUID.randomUUID());
	}

	@Override
	public void start() {
		executeOnExecutor(THREAD_POOL_EXECUTOR);
	}

	@Override
	protected OAuthData doInBackground(Void... voids) {
		OAuthData authData = null;

		try {
			authData = mRedditClient.getOAuthHelper().easyAuth(mCredentials);
			mRedditClient.authenticate(authData);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return authData;
	}

	@Override
	protected void onPostExecute(OAuthData taskResult) {
		super.onPostExecute(taskResult);
		if (mCallback == null) {
			return;
		}

		mCallback.handleRedditAuthenticated();
	}

	public interface AuthenticationCallback {
		void handleRedditAuthenticated();
		void handleRedditAuthenticationFailed();
	}
}
