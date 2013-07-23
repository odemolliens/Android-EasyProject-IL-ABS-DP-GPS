package com.od.easyproject.sample.abs;

import android.content.Context;

import com.actionbarsherlock.app.ActionBar;
import com.slidingmenu.lib.app.SlidingFragmentActivity;

/**
 * 
 * @author odemolliens
 *
 */
public class AbstractSlidingFragmentActivity extends SlidingFragmentActivity
{
	//Context
	private Context context;
	
	//NavigationBar
	private ActionBar actionBarFragmentActivity;


	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public ActionBar getActionBarFragmentActivity() {
		return actionBarFragmentActivity;
	}

	public void setActionBarFragmentActivity(ActionBar actionBarFragmentActivity) {
		this.actionBarFragmentActivity = actionBarFragmentActivity;
	}
}


