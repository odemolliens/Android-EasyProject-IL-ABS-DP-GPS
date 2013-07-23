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
package org.droidparts.util.net;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.net.ConnectivityManager.TYPE_WIFI;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectivityUtils {

	private ConnectivityManager connectivityManager;

	public ConnectivityUtils(Context ctx) {
		connectivityManager = (ConnectivityManager) ctx
				.getSystemService(CONNECTIVITY_SERVICE);
	}

	public boolean backgroundDataEnabled() {
		return connectivityManager.getBackgroundDataSetting();
	}

	public boolean connected() {
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return networkInfo != null && networkInfo.isConnected();
	}

	public boolean onWiFi() {
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		return connected() && networkInfo.getType() == TYPE_WIFI;
	}

}
