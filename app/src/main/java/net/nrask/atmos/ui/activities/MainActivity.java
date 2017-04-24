package net.nrask.atmos.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.paginators.SubredditPaginator;
import net.nrask.atmos.R;
import net.nrask.atmos.RedditManager;
import net.nrask.atmos.data.GetSubredditSubmissionsTask;
import net.nrask.atmos.data.RedditAsyncTask;
import net.nrask.atmos.ui.adapters.RedditSubmissionAdapter;
import net.nrask.atmos.ui.viewholders.RedditSubmissionViewHolder;
import net.nrask.srjneeds.SRJAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements RedditAsyncTask.SimpleCallback<Listing<Submission>>{
	@BindView(R.id.wallpapers_list)
	protected RecyclerView mRecyclerView;

	protected RedditSubmissionAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		setupRecyclerView(mRecyclerView, mAdapter = new RedditSubmissionAdapter());
		mAdapter.setItemCallback(new SRJAdapter.ItemCallback<RedditSubmissionViewHolder>() {
			@Override
			public void onItemClicked(View clickedView, RedditSubmissionViewHolder viewHolder) {
				showPreview(viewHolder.getThumbnailView(), viewHolder.getThumbnailUrl());
			}
		});

		SubredditPaginator subredditPaginator = RedditManager.getInstance().getSubredditFeed(this, "popular");
	}

	private void setupRecyclerView(RecyclerView recyclerView, RecyclerView.Adapter adapter) {
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setItemAnimator(new DefaultItemAnimator());
		recyclerView.setAdapter(adapter);
	}

	@SuppressWarnings("unchecked")
	private void showPreview(ImageView sharedView, String url) {
		Pair<View, String> backgroundPreview = new Pair<>((View) sharedView, getString(R.string.transition_background_preview));
		Pair<View, String> imagePreview = new Pair<>((View) sharedView, getString(R.string.transition_image_preview));
		ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
				this,
				imagePreview,
				backgroundPreview
		);
		Intent intent = new Intent(this, ContentPreviewActivity.class);
		intent.putExtra(getString(R.string.shared_image_url), url);
		startActivityForResult(intent, -1);
	}

	@Override
	public void onTaskCompleted(Listing<Submission> result) {
		mAdapter.addItems(result.getChildren());
	}
}
