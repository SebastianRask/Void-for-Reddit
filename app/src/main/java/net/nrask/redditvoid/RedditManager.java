package net.nrask.redditvoid;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.Html;
import android.util.LruCache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;

import net.dean.jraw.RedditClient;
import net.dean.jraw.auth.AuthenticationManager;
import net.dean.jraw.auth.AuthenticationState;
import net.dean.jraw.auth.RefreshTokenHandler;
import net.dean.jraw.auth.TokenStore;
import net.dean.jraw.auth.VolatileTokenStore;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.SubredditPaginator;
import net.nrask.redditvoid.data.AuthenticateRedditTask;
import net.nrask.redditvoid.data.GetSubredditSubmissionsTask;
import net.nrask.redditvoid.data.GetSubredditTask;
import net.nrask.redditvoid.data.RedditAsyncTask;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Created by Sebastian Rask on 20-04-2017.
 */

public class RedditManager implements GetSubredditTask.SubredditTaskCallback {
	private Context mContext;
	private LruCache<String, Subreddit> cachedSubreddits = new LruCache<>(4 * 1024 * 1024);
	private final String CLIENT_ID = "3d1_ddDCTD-RRw";
	private final String[] scopes = { // ToDo: Update for actually needed endpoints.
			"identity", "modcontributors", "modconfig", "modothers", "modwiki", "creddits",
			"livemanage", "account", "privatemessages", "modflair", "modlog", "report",
			"modposts", "modwiki", "read", "vote", "edit", "submit", "subscribe", "save",
			"wikiread", "flair", "history", "mysubreddits"
	};

	private static RedditManager instance;
	public static RedditManager getInstance() {
		if (instance == null){
			instance = new RedditManager();
		}

		return instance;
	}

	public void init(Context context) {
		mContext = context;

		UserAgent myUserAgent = UserAgent.of("Android", "net.nrask.awwit", "v0.1", "Awwit");
		final RedditClient redditClient = new RedditClient(myUserAgent);

		TokenStore tokenStore = new VolatileTokenStore();
		AuthenticationManager authenticationManager = AuthenticationManager.get();
		authenticationManager.init(redditClient, new RefreshTokenHandler(tokenStore, redditClient));
	}

	public void authenticateIfNeeded(AuthenticateRedditTask.AuthenticationCallback listener) {
		if (AuthenticationManager.get().checkAuthState() == AuthenticationState.READY) {
			listener.handleRedditAuthenticated();
		} else {
			AuthenticateRedditTask authenticateRedditTask = new AuthenticateRedditTask(getClient(), CLIENT_ID, listener);
			authenticateRedditTask.start();
		}
	}

	public String getReadablePostDate(Date date, Context context) {
		Calendar currentCal = GregorianCalendar.getInstance();
		Calendar postCal = GregorianCalendar.getInstance();
		postCal.setTime(date);

		long timeDiffMillis = currentCal.getTimeInMillis() - postCal.getTimeInMillis();

		String result;
		if (timeDiffMillis < TimeUnit.MINUTES.toMillis(1)) {
			// Same minute
			long seconds = TimeUnit.MILLISECONDS.toSeconds(timeDiffMillis);
			@StringRes int stringRessource = seconds > 1 ? R.string.posted_seconds_ago : R.string.posted_second_ago;
			result = context.getString(stringRessource, seconds);
		} else if (timeDiffMillis < TimeUnit.HOURS.toMillis(1)) {
			// Same hour
			long minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiffMillis);
			@StringRes int stringRessource = minutes > 1 ? R.string.posted_minutes_ago : R.string.posted_minute_ago;
			result = context.getString(stringRessource, minutes);
		} else if (timeDiffMillis < TimeUnit.DAYS.toMillis(1)) {
			// Same day
			long hours = TimeUnit.MILLISECONDS.toHours(timeDiffMillis);
			@StringRes int stringRessource = hours > 1 ? R.string.posted_hours_ago : R.string.posted_hour_ago;
			result = context.getString(stringRessource, hours);
		} else if (timeDiffMillis < TimeUnit.DAYS.toMillis(7)) {
			// No more than a week apart
			long days = TimeUnit.MILLISECONDS.toHours(timeDiffMillis);
			@StringRes int stringRessource = days > 1 ? R.string.posted_days_ago : R.string.posted_day_ago;
			result = context.getString(stringRessource, days);
		} else {
			// More than a week apart
			DateFormat dateFormat = new SimpleDateFormat(
					// Append year, if more than a year apart.
					currentCal.get(Calendar.YEAR) != postCal.get(Calendar.YEAR) ? "dd MMM. YYYY" : "dd MMM.",
					Locale.getDefault()
			);
			result = context.getString(R.string.posted_on_date, dateFormat.format(date));
		}

		return result;
	}

	public void getFrontpage() {

	}

	public String getSubmissionThumbnailUrl(Submission submission, String fallback) {
		if (submission == null || submission.getThumbnails() == null) {
			return fallback;
		}

		String thumbnailUrl = submission.getThumbnail() != null
				? submission.getThumbnails().getVariations()[submission.getThumbnails().getVariations().length - 1].getUrl()
				: submission.getThumbnail();

		if (thumbnailUrl != null && thumbnailUrl.length() > 0) {
			return Html.fromHtml(thumbnailUrl).toString(); // Encode GET Parameters
		} else {
			return fallback;
		}
	}

	public String serializeSubmission(Submission submission) throws JsonProcessingException {
		ObjectMapper mapper = new ObjectMapper();

		return mapper.writeValueAsString(submission.getDataNode());
	}

	public Submission deserializeSubmission(String jsonSubmission) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ObjectReader reader = mapper.reader();
		JsonNode jsonNode = reader.readTree(jsonSubmission);


		return new Submission(jsonNode);
	}

	public SubredditPaginator getSubredditFeed(@Nullable RedditAsyncTask.SimpleCallback<Listing<Submission>> callback, String subredditName, String... moreSubreddits) {
		SubredditPaginator feedPaginator = new SubredditPaginator(getClient(), subredditName, moreSubreddits);
		new GetSubredditSubmissionsTask(feedPaginator, callback).start();
		return feedPaginator;
	}

	public void getSubreddit(String name, GetSubredditTask.SubredditTaskCallback callback) {
		getSubreddit(name, callback, true);
	}

	public void getSubreddit(String name, GetSubredditTask.SubredditTaskCallback callback, boolean acceptCache) {
		if (acceptCache && getCachedSubreddit(name) != null) {
			callback.onTaskCompleted(getCachedSubreddit(name));
		} else {
			// Fetch Subreddit
			new GetSubredditTask(name, getClient(), callback, this).start();
		}
	}

	public String getSubredditIcon(Subreddit subreddit) {
		return subreddit.data(mContext.getString(R.string.subreddit_icon_url));
	}

	public String getSubredditKeyColor(Subreddit subreddit) {
		return subreddit.data(mContext.getString(R.string.subreddit_key_color));
	}

	public Subreddit getCachedSubreddit(String name) {
		return cachedSubreddits.get(name.toLowerCase());
	}

	@Override
	public void onTaskCompleted(Subreddit result) {
		// Subreddit was fetched. Cache it.
		cachedSubreddits.put(result.getDisplayName().toLowerCase(), result);
	}

	private RedditClient getClient() {
		return AuthenticationManager.get().getRedditClient();
	}

}
