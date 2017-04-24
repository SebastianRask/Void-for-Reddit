package net.nrask.atmos.ui.span;

import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

import net.dean.jraw.models.Subreddit;

/**
 * Created by Sebastian Rask on 23-04-2017.
 */

public class SubredditClickableSpan extends ClickableSpan {
	private Subreddit subreddit;

	public SubredditClickableSpan(Subreddit subreddit) {
		this.subreddit = subreddit;
	}

	@Override
	public void onClick(View view) {
		//view.getContext().startActivity();
	}

	public void updateDrawState(TextPaint ds) {
		// Remove underline
		ds.setUnderlineText(false);
	}
}