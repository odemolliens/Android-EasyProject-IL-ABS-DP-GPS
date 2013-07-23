package com.od.easyproject.sample.persist;

import org.droidparts.model.Entity;
import org.droidparts.persist.sql.AbstractDBOpenHelper2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.od.easyproject.sample.Conf;

/**
 * 
 * @author odemolliens
 *
 */
public class DBOpenHelper extends AbstractDBOpenHelper2 implements DB {

	public DBOpenHelper(Context ctx) {
		super(ctx, Conf.DATABASE_NAME, Conf.DATABASE_VERSION);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected Class<? extends Entity>[] getEntityClasses() {
		return new Class[] {
			
		};
	}

	@Override
	protected void onCreateExtra(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		dropTables(db);
		onCreate(db);
	}

}
