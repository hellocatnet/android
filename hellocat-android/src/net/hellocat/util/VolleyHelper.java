package net.hellocat.util;

import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Helper class that is used to provide references to initialized RequestQueue(s) and ImageLoader(s)
 */
public class VolleyHelper {
	
	private static final int MAX_IMAGE_CACHE_ENTIRES = 100;
	private static RequestQueue mRequestQueue;
	private static ImageLoader mImageLoader;

	private VolleyHelper() {
		
	}

	public static void init(Context context) {
		mRequestQueue = Volley.newRequestQueue(context);
		mImageLoader = new ImageLoader(mRequestQueue, new BitmapLruCache(MAX_IMAGE_CACHE_ENTIRES));
	}

	public static RequestQueue getRequestQueue() {
		if (mRequestQueue != null) {
			return mRequestQueue;
		} else {
			throw new IllegalStateException("RequestQueue not initialized");
		}
	}

	/**
	 * Returns instance of ImageLoader initialized with {@see FakeImageCache} which effectively means that no memory caching is used. This
	 * is useful for images that you know that will be show only once.
	 */
	public static ImageLoader getImageLoader() {
		if (mImageLoader != null) {
			return mImageLoader;
		} else {
			throw new IllegalStateException("ImageLoader not initialized");
		}
	}
}