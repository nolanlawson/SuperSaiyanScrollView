package com.nolanlawson.supersaiyan.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;
import android.widget.SectionIndexer;

import com.nolanlawson.supersaiyan.OverlaySizeScheme;
import com.nolanlawson.supersaiyan.R;
import com.nolanlawson.supersaiyan.util.StringUtil;
import com.nolanlawson.supersaiyan.util.UtilLogger;


/**
 * Fast Scroll View that allows for arbitrary sizing of the overlay.  Based on 
 * http://nolanlawson.com/2012/03/19/spruce-up-your-listview-by-dividing-it-into-sections/
 * 
 * @author nolan
 * 
 */
public class SuperSaiyanScrollView extends FrameLayout 
        implements OnScrollListener, OnHierarchyChangeListener {

    private static UtilLogger log = new UtilLogger(SuperSaiyanScrollView.class);
    
	// how much transparency to use for the fast scroll thumb
    private static final int ALPHA_MAX = 255;
    
    // how long before the fast scroll thumb disappears
    private static final long FADE_DURATION = 200;
	
    private static final int THUMB_DRAWABLE = R.drawable.fastscroll_thumb_holo;
    private static final int OVERLAY_DRAWABLE = R.drawable.popup_full_bright;
    private static final int THUMB_DRAWABLE_PRESSED = R.drawable.fastscroll_thumb_pressed_holo;
    
    private static final int[] STATE_PRESSED = new int[] {android.R.attr.state_pressed};
    private static final int[] STATE_UNPRESSED = new int[]{};
    
    private Drawable mCurrentThumb;
    private Drawable mOverlayDrawable;

    private int mThumbH;
    private int mThumbW;
    private int mThumbY;

    private RectF mOverlayPos;

    // custom values I defined
    private int mOverlayWidth;
    private int mOverlayHeight;
    private float mOverlayTextSize;

    private boolean mDragging;
    private ListView mList;
    private boolean mScrollCompleted;
    private boolean mThumbVisible;
    private int mVisibleItem;
    private Paint mPaint;
    private int mListOffset;
    private int mOverlayTextColor;

    private Object [] mSections;
    private String mSectionText;
    private boolean mDrawOverlay;
    private ScrollFade mScrollFade;

    private Handler mHandler = new Handler();

    private BaseAdapter mListAdapter;

    private boolean mChangedBounds;

    private boolean shouldRedrawThumb;

    private int mLastW;

    private int mLastH;

    private int mLastOldw;

    private int mLastOldh;

    public SuperSaiyanScrollView(Context context) {
        super(context);

        init(context, null);
    }


    public SuperSaiyanScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context, attrs);
    }

    public SuperSaiyanScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        init(context, attrs);
    }

    private void useThumbDrawable(Drawable drawable) {
        mCurrentThumb = drawable;
        mThumbW = mCurrentThumb.getIntrinsicWidth();
        mThumbH = mCurrentThumb.getIntrinsicHeight();
        mChangedBounds = true;
    }

    private void init(Context context, AttributeSet attrs) {

        // set all attributes from xml
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs,
                    R.styleable.SuperSaiyanScrollView);
            mOverlayHeight = typedArray.getDimensionPixelSize(
                    R.styleable.SuperSaiyanScrollView_ssjn_overlayHeight, 
                    context.getResources().getDimensionPixelSize(R.dimen.ssjn__overlay_height));
            mOverlayWidth = typedArray.getDimensionPixelSize(
                    R.styleable.SuperSaiyanScrollView_ssjn_overlayWidth, 
                    context.getResources().getDimensionPixelSize(R.dimen.ssjn__overlay_width_normal));
            mOverlayTextSize = typedArray.getDimensionPixelSize(
                    R.styleable.SuperSaiyanScrollView_ssjn_overlayTextSize, 
                    context.getResources().getDimensionPixelSize(R.dimen.ssjn__overlay_text_size_normal));
            mOverlayTextColor = typedArray.getColor(R.styleable.SuperSaiyanScrollView_ssjn_overlayTextColor, 
                    context.getResources().getColor(R.color.ssjn__emphasis));
            
            int schemeIndex = typedArray.getInt(R.styleable.SuperSaiyanScrollView_ssjn_overlaySizeScheme, -1);
            
            if (schemeIndex != -1) {
                // use the built-in size schemes
                OverlaySizeScheme scheme = OverlaySizeScheme.values()[schemeIndex];
                mOverlayTextSize = getContext().getResources().getDimensionPixelSize(scheme.getTextSize());
                mOverlayWidth = getContext().getResources().getDimensionPixelSize(scheme.getWidth());
            }
            
            typedArray.recycle();
        } else {
        	mOverlayHeight = context.getResources().getDimensionPixelSize(R.dimen.ssjn__overlay_height);
        	mOverlayWidth = context.getResources().getDimensionPixelSize(R.dimen.ssjn__overlay_width_normal);
            mOverlayTextSize = context.getResources().getDimensionPixelSize(R.dimen.ssjn__overlay_text_size_normal);
            mOverlayTextColor = context.getResources().getColor(R.color.ssjn__emphasis);
        }
        
        log.d("Initialized with mOverlayHeight: %s, mOverlayWidth: %s, mOverlayTextSize: %s, mOverlayTextColor: %s",
                mOverlayHeight, mOverlayWidth, mOverlayTextSize, mOverlayTextColor);

        // Get both the scrollbar states drawables
        final Resources res = context.getResources();
        StateListDrawable thumbDrawable = new StateListDrawable();
        //This for pressed true 
        thumbDrawable.addState(STATE_PRESSED,
                res.getDrawable(THUMB_DRAWABLE_PRESSED));
        //This for pressed false
        thumbDrawable.addState(STATE_UNPRESSED,
                res.getDrawable(THUMB_DRAWABLE));
        useThumbDrawable(thumbDrawable);

        mOverlayDrawable = res.getDrawable(OVERLAY_DRAWABLE);

        mScrollCompleted = true;
        setWillNotDraw(false);

        // Need to know when the ListView is added
        setOnHierarchyChangeListener(this);

        mOverlayPos = new RectF();
        mScrollFade = new ScrollFade();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(mOverlayTextSize);
        mPaint.setColor(mOverlayTextColor);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    private void removeThumb() {
        mThumbVisible = false;
        // Draw one last time to remove thumb
        invalidate();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        if (!mThumbVisible) {
            // No need to draw the rest
            return;
        }

        final int y = mThumbY;
        final int viewWidth = getWidth();
        final SuperSaiyanScrollView.ScrollFade scrollFade = mScrollFade;

        int alpha = -1;
        if (scrollFade.mStarted) {
            alpha = scrollFade.getAlpha();
            if (alpha < ALPHA_MAX / 2) {
                mCurrentThumb.setAlpha(alpha * 2);
            }
            int left = viewWidth - (mThumbW * alpha) / ALPHA_MAX;
            mCurrentThumb.setBounds(left, 0, viewWidth, mThumbH);
            mChangedBounds = true;
        }

        canvas.translate(0, y);
        mCurrentThumb.draw(canvas);
        canvas.translate(0, -y);

        // If user is dragging the scroll bar, draw the alphabet overlay
        if (mDragging && mDrawOverlay) {
            mCurrentThumb.setState(STATE_PRESSED);
            mOverlayDrawable.draw(canvas);
            final Paint paint = mPaint;
            float descent = paint.descent();
            final RectF rectF = mOverlayPos;
            
            if (mSectionText.indexOf('\n') != -1) { // two lines
                float textY = (int) (rectF.bottom + rectF.top) / 2 + descent - (paint.getTextSize() / 2);
                for (String substring : StringUtil.split(mSectionText, '\n')) {
                    canvas.drawText(substring, (int) (rectF.left + rectF.right) / 2, textY, paint);
                    textY += (descent + paint.getTextSize());
                }
            } else { // one line
                canvas.drawText(mSectionText, (int) (rectF.left + rectF.right) / 2, (int) (rectF.bottom + rectF.top) / 2 + descent, paint);
            }
            
        } else if (alpha == 0) {
            mCurrentThumb.setState(STATE_UNPRESSED);
            scrollFade.mStarted = false;
            removeThumb();
        } else {
            mCurrentThumb.setState(STATE_UNPRESSED);
            invalidate(viewWidth - mThumbW, y, viewWidth, y + mThumbH);            
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mCurrentThumb != null) {
            mCurrentThumb.setBounds(w - mThumbW, 0, w, mThumbH);
        }
        
        updateOverlaySize(w, h, oldw, oldh);
        mLastW = w;
        mLastH = h;
        mLastOldw = oldw;
        mLastOldh = oldh;
    }

    private void updateOverlaySize(int w, int h, int oldw, int oldh) {
        final RectF pos = mOverlayPos;
        pos.left = (w - mOverlayWidth) / 2;
        pos.right = pos.left + mOverlayWidth;
        pos.top = h / 10; // 10% from top
        pos.bottom = pos.top + mOverlayHeight;
        mOverlayDrawable.setBounds((int) pos.left, (int) pos.top,
                (int) pos.right, (int) pos.bottom);        
    }


    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            // ensure that the thumb gets redrawn, even if the user is only fling/touch scrolling
            shouldRedrawThumb = true;
        }
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, 
            int totalItemCount) {
        
        if (shouldRedrawThumb) {
            super.invalidate(); // force a redraw of the thumb
            shouldRedrawThumb = false;
        }
        
        if (totalItemCount - visibleItemCount > 0 && !mDragging) {
            mThumbY = ((getHeight() - mThumbH) * firstVisibleItem) / (totalItemCount - visibleItemCount);
            if (mChangedBounds) {
                final int viewWidth = getWidth();
                mCurrentThumb.setBounds(viewWidth - mThumbW, 0, viewWidth, mThumbH);
                mChangedBounds = false;
            }
        }
        mScrollCompleted = true;
        if (firstVisibleItem == mVisibleItem) {
            return;
        }
        mVisibleItem = firstVisibleItem;
        if (!mThumbVisible || mScrollFade.mStarted) {
            mThumbVisible = true;
            mCurrentThumb.setAlpha(ALPHA_MAX);
        }
        mHandler.removeCallbacks(mScrollFade);
        mScrollFade.mStarted = false;
        if (!mDragging) {
            mHandler.postDelayed(mScrollFade, 1500);
        }
    }


    private void getSections() {
        Adapter adapter = mList.getAdapter();
        if (adapter instanceof HeaderViewListAdapter) {
            mListOffset = ((HeaderViewListAdapter)adapter).getHeadersCount();
            adapter = ((HeaderViewListAdapter)adapter).getWrappedAdapter();
        }
        if (adapter instanceof SectionIndexer) {
            mListAdapter = (BaseAdapter) adapter;
            mSections = ((SectionIndexer) mListAdapter).getSections();
        }
    }

    public void onChildViewAdded(View parent, View child) {
        if (child instanceof ListView) {
            mList = (ListView)child;

            mList.setOnScrollListener(this);
            getSections();
        }
    }

    public void onChildViewRemoved(View parent, View child) {
        if (child == mList) {
            mList = null;
            mListAdapter = null;
            mSections = null;
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mThumbVisible && ev.getAction() == MotionEvent.ACTION_DOWN) {
            if (ev.getX() > getWidth() - mThumbW && ev.getY() >= mThumbY &&
                    ev.getY() <= mThumbY + mThumbH) {
                mDragging = true;
                return true;
            }            
        }
        return false;
    }

    private void scrollTo(float position) {
        int count = mList.getCount();
        mScrollCompleted = false;
        final Object[] sections = mSections;
        int sectionIndex;
        if (sections != null && sections.length > 1) {
            final int nSections = sections.length;

            int section = (int) (position * nSections);
            if (section >= nSections) {
                section = nSections - 1;
            }
            sectionIndex = section;
            final SectionIndexer baseAdapter = (SectionIndexer) mListAdapter;
            int index = baseAdapter.getPositionForSection(section);

            // Given the expected section and index, the following code will
            // try to account for missing sections (no names starting with..)
            // It will compute the scroll space of surrounding empty sections
            // and interpolate the currently visible letter's range across the
            // available space, so that there is always some list movement while
            // the user moves the thumb.
            int nextIndex = count;
            int prevIndex = index;
            int prevSection = section;
            int nextSection = section + 1;
            // Assume the next section is unique
            if (section < nSections - 1) {
                nextIndex = baseAdapter.getPositionForSection(section + 1);
            }

            // Find the previous index if we're slicing the previous section
            if (nextIndex == index) {
                // Non-existent letter
                while (section > 0) {
                    section--;
                     prevIndex = baseAdapter.getPositionForSection(section);
                     if (prevIndex != index) {
                         prevSection = section;
                         sectionIndex = section;
                         break;
                     }
                }
            }
            // Find the next index, in case the assumed next index is not
            // unique. For instance, if there is no P, then request for P's 
            // position actually returns Q's. So we need to look ahead to make
            // sure that there is really a Q at Q's position. If not, move 
            // further down...
            int nextNextSection = nextSection + 1;
            while (nextNextSection < nSections &&
                    baseAdapter.getPositionForSection(nextNextSection) == nextIndex) {
                nextNextSection++;
                nextSection++;
            }
            // Compute the beginning and ending scroll range percentage of the
            // currently visible letter. This could be equal to or greater than
            // (1 / nSections). 
            float fPrev = (float) prevSection / nSections;
            float fNext = (float) nextSection / nSections;
            index = prevIndex + (int) ((nextIndex - prevIndex) * (position - fPrev) 
                    / (fNext - fPrev));
            // Don't overflow
            if (index > count - 1) index = count - 1;

            mList.setSelectionFromTop(index + mListOffset, 0);
        } else {
            int index = (int) (position * count);
            mList.setSelectionFromTop(index + mListOffset, 0);
            sectionIndex = -1;
        }

        if (sectionIndex >= 0) {
            String text = mSectionText = sections[sectionIndex].toString();
            mDrawOverlay = (text.length() != 1 || text.charAt(0) != ' ') &&
                    sectionIndex < sections.length;
        } else {
            mDrawOverlay = false;
        }
    }

    private void cancelFling() {
        // Cancel the list fling
        MotionEvent cancelFling = MotionEvent.obtain(0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0);
        mList.onTouchEvent(cancelFling);
        cancelFling.recycle();
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        if (me.getAction() == MotionEvent.ACTION_DOWN) {
            if (me.getX() > getWidth() - mThumbW
                    && me.getY() >= mThumbY 
                    && me.getY() <= mThumbY + mThumbH) {

                mDragging = true;
                if (mListAdapter == null && mList != null) {
                    getSections();
                }

                cancelFling();
                return true;
            }
        } else if (me.getAction() == MotionEvent.ACTION_UP) {
            if (mDragging) {
                mDragging = false;
                final Handler handler = mHandler;
                handler.removeCallbacks(mScrollFade);
                handler.postDelayed(mScrollFade, 1000);
                return true;
            }
        } else if (me.getAction() == MotionEvent.ACTION_MOVE) {
            if (mDragging) {
                final int viewHeight = getHeight();
                mThumbY = (int) me.getY() - mThumbH + 10;
                if (mThumbY < 0) {
                    mThumbY = 0;
                } else if (mThumbY + mThumbH > viewHeight) {
                    mThumbY = viewHeight - mThumbH;
                }
                // If the previous scrollTo is still pending
                if (mScrollCompleted) {
                    scrollTo((float) mThumbY / (viewHeight - mThumbH));
                }
                return true;
            }
        }

        return super.onTouchEvent(me);
    }

    public class ScrollFade implements Runnable {

        long mStartTime;
        long mFadeDuration;
        boolean mStarted;

        void startFade() {
            mFadeDuration = FADE_DURATION;
            mStartTime = SystemClock.uptimeMillis();
            mStarted = true;
        }

        int getAlpha() {
            if (!mStarted) {
                return ALPHA_MAX;
            }
            int alpha;
            long now = SystemClock.uptimeMillis();
            if (now > mStartTime + mFadeDuration) {
                alpha = 0;
            } else {
                alpha = (int) (ALPHA_MAX - ((now - mStartTime) * ALPHA_MAX) / mFadeDuration); 
            }
            return alpha;
        }

        public void run() {
            if (!mStarted) {
                startFade();
                invalidate();
            }

            if (getAlpha() > 0) {
                final int y = mThumbY;
                final int viewWidth = getWidth();
                invalidate(viewWidth - mThumbW, y, viewWidth, y + mThumbH);
            } else {
                mStarted = false;
                removeThumb();
            }
        }
    }
    
    /**
     * Call this whenever the data set changes, in order to update the overlay drawable.
     */
	public void refresh() {
		getSections();
		mDrawOverlay = false; // avoids temporary content flash of previous sections
	}

    public void setOverlayWidth(int overlayWidth) {
        mOverlayWidth = overlayWidth;
        updateOverlaySize(mLastW, mLastH, mLastOldw, mLastOldh);
    }


    public void setOverlayTextSize(float overlayTextSize) {
        mOverlayTextSize = overlayTextSize;
        if (mPaint != null) {
            mPaint.setTextSize(mOverlayTextSize);
        }
        updateOverlaySize(mLastW, mLastH, mLastOldw, mLastOldh);
    }
    
    /**
     * Set a size scheme for the overlay that's shown.  This will modify both the width of the overlay window
     * and the text size.
     * @param scheme
     */
    public void setOverlaySizeScheme(OverlaySizeScheme scheme) {
        mOverlayTextSize = getContext().getResources().getDimensionPixelSize(scheme.getTextSize());
        if (mPaint != null) {
            mPaint.setTextSize(mOverlayTextSize);
        }
        mOverlayWidth = getContext().getResources().getDimensionPixelSize(scheme.getWidth());
        updateOverlaySize(mLastW, mLastH, mLastOldw, mLastOldh);
    }
}