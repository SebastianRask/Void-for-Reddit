package net.nrask.atmos.ui.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import net.nrask.atmos.R;

public class NoNonsenseActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_no_nonsense);

		ImageView mPreview = (ImageView) findViewById(R.id.preview);

		String wallpaperUrl = getIntent().getExtras().getString(getString(R.string.shared_image_url), "http://i.telegraph.co.uk/multimedia/archive/02830/cat_2830677b.jpg");
		Glide.with(this)
				.load(wallpaperUrl)
				.diskCacheStrategy(DiskCacheStrategy.ALL)
				.into(mPreview);
	}
}
