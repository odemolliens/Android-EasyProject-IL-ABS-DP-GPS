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

import android.widget.ImageView;

public interface ImageFetchListener {

	void onTaskAdded(ImageView imageView);

	void onDownloadProgressChanged(ImageView imageView, int kBTotal,
			int kBReceived);

	void onDownloadFailed(ImageView imageView, Exception e);

	void onTaskCompleted(ImageView imageView, ImageFetcher imageFetcher);

	void onTaskAlreadyInCache(String urlImage);

}
