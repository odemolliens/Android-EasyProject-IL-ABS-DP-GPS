/**
 * Copyright 2013 Alex Yanchenko
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */
package org.droidparts.net;

import static android.graphics.Color.TRANSPARENT;
import static org.droidparts.contract.Constants.BUFFER_SIZE;
import static org.droidparts.util.io.IOUtils.silentlyClose;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import org.droidparts.http.RESTClient;
import org.droidparts.net.cache.BitmapDiskCache;
import org.droidparts.net.cache.BitmapMemoryCache;
import org.droidparts.util.L;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Pair;
import android.widget.ImageView;

public class ImageFetcher {

	private final RESTClient restClient;

	private final BitmapMemoryCache memoryCache;
	private final BitmapDiskCache diskCache;

	final ThreadPoolExecutor cacheExecutor;
	final ThreadPoolExecutor fetchExecutor;

	final ConcurrentHashMap<ImageView, Long> wip = new ConcurrentHashMap<ImageView, Long>();
	private Handler handler;

	ImageFetchListener fetchListener;
	private ImageReshaper reshaper;
	int crossFadeMillis = 0;
	private boolean finish;
	private int width = -1;
	private int height = -1;

	public ImageFetcher(Context ctx) {
		this(ctx, (ThreadPoolExecutor) Executors.newFixedThreadPool(1),
				new RESTClient(ctx), BitmapMemoryCache.getDefaultInstance(ctx),
				BitmapDiskCache.getDefaultInstance(ctx));
		finish = false;
	}

	protected ImageFetcher(Context ctx, ThreadPoolExecutor fetchExecutor,
			RESTClient restClient, BitmapMemoryCache memoryCache,
			BitmapDiskCache diskCache) {
		this.fetchExecutor = fetchExecutor;
		this.restClient = restClient;
		this.memoryCache = memoryCache;
		this.diskCache = diskCache;
		handler = new Handler(Looper.getMainLooper());
		cacheExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
		finish = false;
	}

	public void setFetchListener(ImageFetchListener fetchListener) {
		wip.clear();
		this.fetchListener = fetchListener;
	}

	public void setReshaper(ImageReshaper reshaper) {
		wip.clear();
		this.reshaper = reshaper;
	}

	public void setCrossFadeDuration(int millisec) {
		wip.clear();
		this.crossFadeMillis = millisec;
	}

	//

	public void attachImage(ImageView imageView, String imgUrl) {
		if (fetchListener != null) {
			fetchListener.onTaskAdded(imageView);
		}
		long submitted = System.nanoTime();
		wip.put(imageView, submitted);

		Runnable r = new ReadFromCacheRunnable(this, imageView, imgUrl,
				submitted, this.getHeight(), this.getWidth());
		cacheExecutor.remove(r);
		fetchExecutor.remove(r);
		cacheExecutor.execute(r);
	}

	public void attachImage(ImageView imageView, String imgUrl, int width,
			int height) {
		this.width = width;
		this.height = height;
		if (fetchListener != null) {
			fetchListener.onTaskAdded(imageView);
		}
		long submitted = System.nanoTime();
		wip.put(imageView, submitted);

		Runnable r = new ReadFromCacheRunnable(this, imageView, imgUrl,
				submitted, this.height, this.width);
		cacheExecutor.remove(r);
		fetchExecutor.remove(r);
		cacheExecutor.execute(r);
	}

	public void purgeRunnable() {
		cacheExecutor.purge();
	}

	public Boolean urlIsInCache(String imgUrl) {
		Bitmap bm = getCached(imgUrl);

		if (bm == null) {
			return false;
		} else {
			return true;
		}
	}

	public Bitmap getImage(String imgUrl) {
		Bitmap bm = getCached(imgUrl);
		if (bm == null) {
			bm = fetch(null, imgUrl);
		} else {
			finish = true;
			if (fetchListener != null) {
				fetchListener.onTaskAlreadyInCache(imgUrl);
			}
		}
		if (bm != null) {
			bm = reshapeAndCache(imgUrl, bm);
		}
		return bm;
	}

	public void loadImageIfDontExist(String imgUrl) {
		boolean isCached = getCachedBool(imgUrl);
		if (!isCached) {
			fetch(imgUrl);
		}
	}

	public void clearCacheOlderThan(int hours) {
		if (diskCache != null) {
			final long timestamp = System.currentTimeMillis() - hours * 60 * 60
					* 1000;
			cacheExecutor.execute(new Runnable() {

				@Override
				public void run() {
					diskCache.purgeFilesAccessedBefore(timestamp);
				}
			});
		}
	}

	//

	void fetch(final String imgUrl) {
		int bytesReadTotal = 0;
		byte[] buffer = new byte[BUFFER_SIZE];
		BufferedInputStream bis = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			Pair<Integer, BufferedInputStream> resp = restClient
					.getInputStream(imgUrl);
			final int kBTotal = resp.first / 1024;
			bis = resp.second;
			int bytesRead;
			while ((bytesRead = bis.read(buffer)) != -1) {
				baos.write(buffer, 0, bytesRead);
				bytesReadTotal += bytesRead;
				if (fetchListener != null) {
					final int kBReceived = bytesReadTotal / 1024;
					fetchListener.onDownloadProgressChanged(null, kBTotal,
							kBReceived);
				}
			}

			finish = true;
			if (fetchListener != null) {
				L.w("Finish to fetch " + imgUrl);
				fetchListener.onTaskCompleted(null, this);
			}

		} catch (final Exception e) {
			L.w("Failed to fetch " + imgUrl);
			L.d(e);
			finish = true;
			if (fetchListener != null) {
				fetchListener.onDownloadFailed(null, e);

			}
		} finally {
			silentlyClose(bis, baos);
		}
	}

	Bitmap fetch(final ImageView imageView, final String imgUrl) {
		int bytesReadTotal = 0;
		byte[] buffer = new byte[BUFFER_SIZE];
		BufferedInputStream bis = null;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			Pair<Integer, BufferedInputStream> resp = restClient
					.getInputStream(imgUrl);
			final int kBTotal = resp.first / 1024;
			bis = resp.second;
			int bytesRead;
			while ((bytesRead = bis.read(buffer)) != -1) {
				baos.write(buffer, 0, bytesRead);
				bytesReadTotal += bytesRead;
				if (fetchListener != null) {
					final int kBReceived = bytesReadTotal / 1024;
					fetchListener.onDownloadProgressChanged(imageView, kBTotal,
							kBReceived);
				}

			}

			finish = true;
			if (fetchListener != null) {
				fetchListener.onTaskCompleted(imageView, this);
			}
			byte[] data = baos.toByteArray();
			Bitmap bm = BitmapFactory.decodeByteArray(data, 0, data.length);
			return bm;
		} catch (final Exception e) {
			L.d(e);
			finish = true;
			if (fetchListener != null) {
				fetchListener.onDownloadFailed(imageView, e);
			}
			return null;
		} finally {
			silentlyClose(bis, baos);
		}
	}

	boolean getCachedBool(String imgUrl) {
		String key = getCacheKey(imgUrl);

		if (memoryCache != null) {
			return memoryCache.getAvaibility(key);
		} else {

		}

		return false;

	}

	Bitmap getCached(String imgUrl) {
		String key = getCacheKey(imgUrl);
		Bitmap bm = null;
		if (reshaper != null) {
			if (memoryCache != null) {
				bm = memoryCache.get(key);
			}
			if (bm == null) {
				if (diskCache != null) {
					bm = diskCache.get(key);
				}
				if (bm != null) {
					if (memoryCache != null) {
						memoryCache.put(key, bm);
					}
				}
			}
		}
		if (bm == null) {
			if (memoryCache != null) {
				bm = memoryCache.get(key);
			}
			if (bm == null) {
				if (diskCache != null) {
					bm = diskCache.get(imgUrl);
				}
				if (bm != null) {
					if (reshaper != null) {
						bm = reshaper.reshape(bm);
					}
					if (memoryCache != null) {
						memoryCache.put(key, bm);
					}
				}
			}
		}
		return bm;
	}

	Bitmap reshapeAndCache(String imgUrl, Bitmap bm) {
		if (reshaper != null) {
			bm = reshaper.reshape(bm);
		}
		String key = getCacheKey(imgUrl);
		if (memoryCache != null) {
			memoryCache.put(key, bm);
		}
		if (diskCache != null) {
			diskCache.put(key, bm);
		}
		return bm;
	}

	private String getCacheKey(String imgUrl) {
		return (reshaper == null) ? imgUrl : (imgUrl + reshaper.getId());
	}

	private void runOnUiThread(Runnable r) {
		boolean success = handler.post(r);
		// a hack
		while (!success) {
			handler = new Handler(Looper.getMainLooper());
			success = handler.post(r);
		}
	}

	//

	static abstract class ImageViewRunnable implements Runnable {

		protected final ImageFetcher imageFetcher;
		protected final ImageView imageView;

		public ImageViewRunnable(ImageFetcher imageFetcher, ImageView imageView) {
			this.imageFetcher = imageFetcher;
			this.imageView = imageView;
		}

		@Override
		public boolean equals(Object o) {
			boolean eq = false;
			if (this == o) {
				eq = true;
			} else if (o instanceof ImageViewRunnable) {
				eq = imageView.equals(((ImageViewRunnable) o).imageView);
			}
			return eq;
		}

		@Override
		public int hashCode() {
			return imageView.hashCode();
		}
	}

	static class ReadFromCacheRunnable extends ImageViewRunnable {

		protected final String imgUrl;
		protected final long submitted;
		private ImageFetcher imgFetch;
		private int width;
		private int height;

		public ReadFromCacheRunnable(ImageFetcher imageFetcher,
				ImageView imageView, String imgUrl, long submitted, int height,
				int width) {
			super(imageFetcher, imageView);
			this.imgFetch = imageFetcher;
			this.imgUrl = imgUrl;
			this.submitted = submitted;
			this.height = height;
			this.width = width;
		}

		@Override
		public void run() {
			Bitmap bm = imageFetcher.getCached(imgUrl);
			if (bm == null) {
				FetchAndCacheRunnable r = new FetchAndCacheRunnable(
						imageFetcher, imageView, imgUrl, submitted,
						imageFetcher.getHeight(), imageFetcher.getWidth());
				imageFetcher.fetchExecutor.execute(r);
			} else {
				imageFetcher.wip.remove(imageView);
				SetBitmapRunnable r = new SetBitmapRunnable(this.imgFetch,
						imageView, bm, imageFetcher.crossFadeMillis,
						this.height, this.width);
				imageFetcher.runOnUiThread(r);
			}
		}
	}

	static class FetchAndCacheRunnable extends ReadFromCacheRunnable {

		private int width;
		private int height;

		public FetchAndCacheRunnable(ImageFetcher imageFetcher,
				ImageView imageView, String imgUrl, long submitted, int height,
				int width) {
			super(imageFetcher, imageView, imgUrl, submitted, height, width);
			this.height = height;
			this.width = width;
		}

		@Override
		public void run() {
			Bitmap bm = imageFetcher.fetch(imageView, imgUrl);
			if (bm != null) {
				bm = imageFetcher.reshapeAndCache(imgUrl, bm);
				//
				Long timestamp = imageFetcher.wip.get(imageView);
				if (timestamp != null && timestamp == submitted) {
					imageFetcher.wip.remove(imageView);
					SetBitmapRunnable r = new SetBitmapRunnable(imageFetcher,
							imageView, bm, imageFetcher.crossFadeMillis,
							this.height, this.width);
					imageFetcher.runOnUiThread(r);
				}
			}
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + ": " + imgUrl;
		}

	}

	static class SetBitmapRunnable extends ImageViewRunnable {

		private final Bitmap bitmap;
		private final int crossFadeMillis;
		private ImageFetcher imageFetcher;

		private int width;
		private int height;

		public SetBitmapRunnable(ImageFetcher imageFetcher,
				ImageView imageView, Bitmap bitmap, int crossFadeMillis,
				int height, int width) {
			super(imageFetcher, imageView);
			this.bitmap = bitmap;
			this.crossFadeMillis = 0;
			this.imageFetcher = imageFetcher;
			this.height = height;
			this.width = width;
		}

		@Override
		public void run() {
			if (imageFetcher.fetchListener != null) {
				imageFetcher.fetchListener.onTaskCompleted(imageView,
						imageFetcher);
			}
			if (crossFadeMillis > 0) {

				Drawable prevDrawable = imageView.getDrawable();
				if (prevDrawable == null) {
					prevDrawable = new ColorDrawable(TRANSPARENT);
				}
				Drawable nextDrawable = new BitmapDrawable(
						imageView.getResources(), bitmap);
				TransitionDrawable transitionDrawable = new TransitionDrawable(
						new Drawable[] { prevDrawable, nextDrawable });
				imageView.setImageDrawable(transitionDrawable);
				transitionDrawable.startTransition(crossFadeMillis);
			} else {

				if (this.height != -1 || this.width != -1) {
					imageView.setImageBitmap(Bitmap.createScaledBitmap(bitmap,
							this.width, this.height, false));
				} else {
					imageView.setImageBitmap(bitmap);
				}

			}
		}

	}

	public boolean isFinish() {
		return finish;
	}

	public void setFinish(boolean finish) {
		this.finish = finish;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

}