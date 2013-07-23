package com.od.easyproject.sample.persist;

import org.droidparts.inject.AbstractDependencyProvider;

import android.content.Context;

/**
 * 
 * @author odemolliens
 *
 */
public class DependencyProvider extends AbstractDependencyProvider {

	private DBOpenHelper dbOpenHelper;

	public DependencyProvider(Context ctx) {
		super(ctx);
		dbOpenHelper = new DBOpenHelper(ctx);
	}

	@Override
	public DBOpenHelper getDBOpenHelper() {
		return dbOpenHelper;
	}

}
