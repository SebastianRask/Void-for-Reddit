package net.nrask.atmos;

import android.graphics.Bitmap;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.load.resource.gif.GifDrawable;

/**
 * Created by Sebastian Rask on 23-04-2017.
 */

public class MiscUtil {
	public static Bitmap getBitmap(GlideDrawable glideDrawable) {
		if (glideDrawable instanceof GlideBitmapDrawable) {
			return ((GlideBitmapDrawable) glideDrawable).getBitmap();
		} else if (glideDrawable instanceof GifDrawable) {
			return ((GifDrawable) glideDrawable).getFirstFrame();
		}
		return null;
	}
}
