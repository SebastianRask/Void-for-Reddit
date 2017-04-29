package net.nrask.redditvoid.ui.activities;

import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;

import com.google.gson.Gson;

import net.dean.jraw.models.Submission;
import net.nrask.redditvoid.RedditManager;

import java.io.IOException;

/**
 * Created by Sebastian Rask on 25-04-2017.
 */

public class BaseActivity extends AppCompatActivity {
	protected RedditManager manager = RedditManager.getInstance();

	protected Submission getSubmissionExtra(@StringRes int objectKey) throws IOException {
		return manager.deserializeSubmission(getIntent().getStringExtra(getString(objectKey)));
	}

	protected <T> T getIntentExtraObject(@StringRes int objectKey, Class<T> classOfObject) {
		return new Gson().fromJson(
				getIntent().getStringExtra(getString(objectKey)),
				classOfObject
		);
	}
}
