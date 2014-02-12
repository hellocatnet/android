package net.hellocat.util;

import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.toolbox.ImageLoader.ImageCache;

public class BitmapLruCache extends LruCache<String, Bitmap> implements ImageCache {
	public BitmapLruCache(int maxSize) {
		super(maxSize);
	}

	// Fix thanks to Steven's comment: sizeOf method should not be overriden,
	// when we are passing max image cache entries in another place of the code
	// @Override
	// protected int sizeOf(String key, Bitmap value) {
	// return value.getRowBytes() * value.getHeight();
	// }

	@Override
	public Bitmap getBitmap(String url) {
		return get(url);
	}

	@Override
	public void putBitmap(String url, Bitmap bitmap) {
		put(url, bitmap);
	}
}