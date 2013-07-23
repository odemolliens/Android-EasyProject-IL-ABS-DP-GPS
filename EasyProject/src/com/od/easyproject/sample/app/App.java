package com.od.easyproject.sample.app;

import java.util.Locale;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

/**
 * 
 * @author odemolliens
 *
 */
public class App extends Application  {

	//App
	private static Context context;

	protected ImageLoader imageLoader = ImageLoader.getInstance();

	@Override
	public void onCreate() {
		App.context = getApplicationContext();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	public static Context getAppContext() {
		return App.context;
	}

	public static boolean checkInternetConnection(Context c) {

		if(c != null){
			ConnectivityManager connectivity = (ConnectivityManager) c
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (connectivity != null) {
				NetworkInfo[] info = connectivity.getAllNetworkInfo();
				if (info != null) {
					for (int i = 0; i < info.length; i++) {
						if (info[i].getState() == NetworkInfo.State.CONNECTED) {
							return true;
						}
					}
				}
			}
			return false;
		}else{
			//Junit implement (no activity linked)
			return true;
		}

	}

	public static String getCurrentLocalLanguage()
	{
		return Locale.getDefault().getDisplayLanguage().toLowerCase(Locale.getDefault()).substring(0, 2);
	}

	public static void initImageLoader(Context context) {
		int memoryCacheSize;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
			int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
			memoryCacheSize = (memClass / 8) * 1024 * 1024; // 1/8 of app memory limit 
		} else {
			memoryCacheSize = 2 * 1024 * 1024;
		}

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
		.discCacheSize(50 * 1024 * 1024)
		.threadPoolSize(3)
		.memoryCacheSize(memoryCacheSize)
		.tasksProcessingOrder(QueueProcessingType.FIFO)
		.threadPriority(Thread.NORM_PRIORITY -5)
		.denyCacheImageMultipleSizesInMemory()
		.discCacheFileNameGenerator(new HashCodeFileNameGenerator())
		.build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
	}

	public static ImageLoader getImageLoader() {
		return ImageLoader.getInstance();
	}

}
