package com.nolanlawson.supersaiyan.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ListView;

import com.nolanlawson.supersaiyan.util.UtilLogger;

/**
 * When using the SeparatedListAdapter, there are random ArrayIndexOutOfBounds exceptions.
 * I haven't figured out what's causing it yet, but I know I can fix it by just having a list view
 * that ignores the errors.
 * 
 * @author nolan
 *
 */
public class SuperSaiyanListView extends ListView {

	UtilLogger log = new UtilLogger(SuperSaiyanListView.class);
	
	public SuperSaiyanListView(Context context) {
		super(context);
	}

	public SuperSaiyanListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}

	public SuperSaiyanListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		// XXX: for some reson, this avoids an IndexOutOfBoundsException altogether with the progressView
		// footers.  No idea why.
		try {
			super.dispatchDraw(canvas);
		} catch (IndexOutOfBoundsException e) {
			log.e(e, "listview exception");
		}
	}
	
}
