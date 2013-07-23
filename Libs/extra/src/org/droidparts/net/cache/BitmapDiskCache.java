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
package org.droidparts.net.cache;

import static android.graphics.Bitmap.CompressFormat.PNG;
import static org.droidparts.contract.Constants.BUFFER_SIZE;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.droidparts.util.AppUtils;
import org.droidparts.util.L;
import org.droidparts.util.crypto.HashCalc;
import org.droidparts.util.io.IOUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

public class BitmapDiskCache {

	private static final String DEFAULT_DIR = "img";

	private static BitmapDiskCache instance;

	public static BitmapDiskCache getDefaultInstance(Context ctx) {

		File cacheDir;
		if (instance == null) {
			cacheDir = new AppUtils(ctx).getExternalCacheDir();
			if (cacheDir != null) {
				instance = new BitmapDiskCache(new File(cacheDir, DEFAULT_DIR));
			} else {

				// NO SDCARD
				cacheDir = new AppUtils(ctx).getCacheDir();

				if (cacheDir != null) {
					instance = new BitmapDiskCache(new File(cacheDir,
							DEFAULT_DIR));
				} else {
					Log.e("BitmapDiskCache",
							"External cache dir null. Lacking 'android.permission.WRITE_EXTERNAL_STORAGE' permission?");
				}

			}
		}
		return instance;
	}

	private final File cacheDir;

	public BitmapDiskCache(File cacheDir) {
		this.cacheDir = cacheDir;
		cacheDir.mkdirs();
	}

	public boolean put(String key, Bitmap bm) {
		File file = getCachedFile(key);
		BufferedOutputStream bos = null;
		try {
			bos = new BufferedOutputStream(new FileOutputStream(file),
					BUFFER_SIZE);
			bm.compress(PNG, 100, bos);
			return true;
		} catch (Exception e) {
			L.w(e);
			return false;
		} finally {
			IOUtils.silentlyClose(bos);
		}
	}

	public Bitmap get(String key) {
		Bitmap bm = null;
		File file = getCachedFile(key);
		if (file.exists()) {
			BufferedInputStream bis = null;
			try {
				bis = new BufferedInputStream(new FileInputStream(file),
						BUFFER_SIZE);
				bm = BitmapFactory.decodeStream(bis);
				// only after successful restore
				file.setLastModified(System.currentTimeMillis());
			} catch (Exception e) {
				L.w(e);
			} finally {
				IOUtils.silentlyClose(bis);
			}
		}
		if (bm == null) {
			L.i("Cache miss for " + key);
		}
		return bm;
	}

	public void purgeFilesAccessedBefore(long timestamp) {
		for (File f : IOUtils.getFileList(cacheDir)) {
			if (f.lastModified() < timestamp) {
				f.delete();
			}
		}
	}

	private File getCachedFile(String key) {
		return new File(cacheDir, HashCalc.getMD5(key));
	}

}