package com.od.easyproject.sample.fragmentactivities;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.actionbarsherlock.app.ActionBar;
import com.neopixl.logger.NPLog;
import com.od.easyproject.sample.R;
import com.od.easyproject.sample.abs.AbstractSlidingFragmentActivity;
import com.slidingmenu.lib.SlidingMenu;

/**
 * 
 * @author odemolliens
 *
 */
public class SlidingActivity extends AbstractSlidingFragmentActivity{

	//Result
	public static final int REFRESH = 0000000001;

	//NavigationBar - button
	private ImageButton imgBtnLeftMenu;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{

		//Trick for compability API < 11
		setTheme(com.actionbarsherlock.R.style.Theme_Sherlock_Light);
		//

		super.onCreate(savedInstanceState);

		setContext(this);

		//Sliding menu init
		setContentView(R.layout.fragment_middle_view);
		setBehindContentView(R.layout.slidingmenu_right_frame);

		getSlidingMenu().setAboveOffset(50);
		getSlidingMenu().setMode(SlidingMenu.LEFT);
		getSlidingMenu().setShadowWidthRes(R.dimen.shadow_width);
		getSlidingMenu().setShadowDrawable(R.drawable.shadowleft);
		getSlidingMenu().setBehindOffsetRes(R.dimen.slidingmenu_offset); 
		getSlidingMenu().setBehindScrollScale(0.25f);
		getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		//

		//Show custom ActionBar (without R.menu.x) but directly with R.layout.x
		setActionBarFragmentActivity(getSupportActionBar());
		getActionBarFragmentActivity().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

		LayoutInflater inflator = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflator.inflate(R.layout.slidingmenu_action_bar, null);
		getActionBarFragmentActivity().setCustomView(v);

		
		////Now add click listener
		imgBtnLeftMenu = (ImageButton)v.findViewById(R.id.btnLeft);
		imgBtnLeftMenu.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				toggle();
			}
		});
	}



	@Override
	public void finish() {
		//Force kill for refresh and restart at event list view
		super.finish();
	}

	@Override
	public void toggle() {
		super.toggle();
	}

	public void rightMenu()
	{

	}


	@Override
	protected void onRestart() {
		super.onRestart();
	}

	public static int getContentFragmentFrame()
	{
		return R.id.fragment_content_frame;
	}

	public static int getRightContentFragmentFrame()
	{
		return R.id.menu_frame;
	}


}
