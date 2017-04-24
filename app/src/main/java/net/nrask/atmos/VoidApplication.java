package net.nrask.atmos;

import android.app.Application;

/**
 * Created by Sebastian Rask on 22-04-2017.
 */

public class VoidApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		RedditManager.getInstance().init(this);
	}
}
