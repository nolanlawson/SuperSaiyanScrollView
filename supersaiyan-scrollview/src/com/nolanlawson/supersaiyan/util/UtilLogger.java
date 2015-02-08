package com.nolanlawson.supersaiyan.util;

import java.util.Arrays;

import android.util.Log;

/**
 * Easier way to interact with logcat.
 * @author nolan
 */
public class UtilLogger {

	public static final boolean DEBUG_MODE = false;
	
	private String tag;
	
	public UtilLogger(String tag) {
		this.tag = tag;
	}
	
	public UtilLogger(Class<?> clazz) {
		this.tag = clazz.getSimpleName();
	}
	
	public void i(String format, Object... more) {
		Log.i(tag, String.format(format, more));
	}
	
	public void i(Exception e, String format, Object... more) {
		Log.i(tag, String.format(format, more), e);
	}
	
	public void w(Exception e, String format, Object... more) {
		Log.w(tag, String.format(format, more), e);
	}
	
	public void w(String format, Object... more) {
		Log.w(tag, String.format(format, more));
	}	
	
	public void e(String format, Object... more) {
		Log.e(tag, String.format(format, more));
	}	
	
	public void e(Exception e, String format, Object... more) {
		Log.e(tag, String.format(format, more), e);
	}

	public void v(String format, Object... more) {	
		v(null, format, more);
	}	
	
	public void v(Exception e, String format, Object... more) {
		if (DEBUG_MODE) {
			formatArgs(more);
			if (e != null) {
				Log.v(tag, String.format(format, more), e);
			} else {
				Log.v(tag, String.format(format, more));
			}
		}
	}		
	
	public void d(String format, Object... more) {	
		d(null, format, more);
	}	
	
	public void d(Exception e, String format, Object... more) {
		if (DEBUG_MODE) {
			formatArgs(more);
			if (e != null) {
				Log.d(tag, String.format(format, more), e);
			} else {
				Log.d(tag, String.format(format, more));
			}
		}
	}	
	
	private void formatArgs(Object[] more) {
		for (int i = 0; i < more.length; i++) {
			if (more[i] instanceof int[]) {
				more[i] = Arrays.toString((int[])more[i]);
			} else if (more[i] instanceof Object[]) {
				more[i] = Arrays.toString((Object[])(more[i]));
			}
		}
	}
}
