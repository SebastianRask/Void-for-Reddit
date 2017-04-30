package net.nrask.redditvoid.ui.activities;

import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.fasterxml.jackson.core.JsonProcessingException;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.paginators.SubredditPaginator;
import net.nrask.redditvoid.R;
import net.nrask.redditvoid.RedditManager;
import net.nrask.redditvoid.data.RedditAsyncTask;
import net.nrask.redditvoid.ui.adapters.RedditSubmissionAdapter;
import net.nrask.redditvoid.ui.viewholders.RedditSubmissionViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements RedditAsyncTask.SimpleCallback<Listing<Submission>>, RedditSubmissionAdapter.SubmissionClickListener {
	@BindView(R.id.wallpapers_list)
	protected RecyclerView mRecyclerView;

	protected RedditSubmissionAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		setupRecyclerView(mRecyclerView, mAdapter = new RedditSubmissionAdapter());
		mAdapter.setSubmissionClickListener(this);

		SubredditPaginator subredditPaginator = RedditManager.getInstance().getSubredditFeed(this, "popular");
	}

	@Override
	public void onTaskCompleted(Listing<Submission> result) {
		mAdapter.addItems(result.getChildren());
	}

	@Override
	public void onPreviewClicked(View clickedView, RedditSubmissionViewHolder viewHolder) {
		showSubmission(viewHolder, true);
	}

	@Override
	public void onItemClicked(View clickedView, RedditSubmissionViewHolder viewHolder) {
		showSubmission(viewHolder, false);
	}

	private void showSubmission(RedditSubmissionViewHolder viewHolder, boolean showPreview) {
		Bundle options = showPreview ? constructSharedPreviewOptions(viewHolder) : constructSharedHeaderOptions(viewHolder);
		try {
			startActivityForResult(ContentPreviewActivity.createStartIntent(this, viewHolder.getData(), showPreview), -1, options);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			//ToDo: Notify user.
		}
	}

	@SuppressWarnings("unchecked")
	private Bundle constructSharedPreviewOptions(RedditSubmissionViewHolder viewHolder) {
		View sharedPreviewView = viewHolder.getThumbnailView();
		View sharedTitleView = viewHolder.getTitleView();

		Pair<View, String> backgroundPreview = new Pair<>(sharedPreviewView, getString(R.string.transition_background_preview));
		Pair<View, String> imagePreview = new Pair<>(sharedPreviewView, getString(R.string.transition_image_preview));

		ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
				this,
				imagePreview,
				backgroundPreview
		);

		return options.toBundle();
	}

	@SuppressWarnings("unchecked")
	private Bundle constructSharedHeaderOptions(RedditSubmissionViewHolder viewHolder) {
		View sharedTitleView = viewHolder.getTitleView();
		View sharedPreviewView = viewHolder.getThumbnailView();

		Pair<View, String> backgroundPreview = new Pair<>(sharedPreviewView, getString(R.string.transition_background_preview));
		Pair<View, String> imagePreview = new Pair<>(sharedPreviewView, getString(R.string.transition_image_preview));
		Pair<View, String> title = new Pair<>(sharedTitleView, getString(R.string.transition_title));

		ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
				this,
				imagePreview,
				backgroundPreview,
				title
		);

		return options.toBundle();
	}

	private void setupRecyclerView(RecyclerView recyclerView, RecyclerView.Adapter adapter) {
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		recyclerView.setAdapter(adapter);
	}
}
