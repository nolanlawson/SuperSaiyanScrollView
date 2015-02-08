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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.OnHierarchyChangeListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;

import com.nolanlawson.supersaiyan.OverlaySizeScheme;
import com.nolanlawson.supersaiyan.R;
import com.nolanlawson.supersaiyan.util.StringUtil;
import com.nolanlawson.supersaiyan.util.UtilLogger;

import java.util.ArrayList;
import java.util.List;


/**
 * Fast Scroll View that allows for arbitrary sizing of the overlay.  Based on
 * http://nolanlawson.com/2012/03/19/spruce-up-your-listview-by-dividing-it-into-sections/
 *
 * @author nolan
 *
 */
public class SuperSaiyanRecyclerView extends FrameLayout implements OnHierarchyChangeListener {

    // Assumes all rows have equal height
    private RecyclerView mRecyclerView = null;
    private LinearLayoutManager mLinearLayoutManager = null;
    private ScrollListener mScrollListener = new ScrollListener();
    private int   mRowHeightPx = 0;
    private float mVisibleItemCount = 0;
    private int   mRecyclerViewHeight = 0;
    private final Interpolator mDecelerateInterpolator = new DecelerateInterpolator();
    private List<String> mSections = new ArrayList<>();
    private List<Integer> mSectionPositions = new ArrayList<>();

    private class ScrollListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrollStateChanged( RecyclerView view, int scrollState ) {
            if ( scrollState != RecyclerView.SCROLL_STATE_IDLE ) {
                // ensure that the thumb gets redrawn, even if the user is only fling/touch scrolling
                mScrollFader.tickle();
            }
        }

        @Override
        public void onScrolled( RecyclerView view, int dx, int dy ) {
            if ( mRecyclerViewHeight == 0 ) {
                mRecyclerViewHeight = view.getHeight();
            }
            if ( mLinearLayoutManager == null ) {
                if ( view.getLayoutManager() instanceof LinearLayoutManager )
                    mLinearLayoutManager = (LinearLayoutManager)view.getLayoutManager();
                else
                    throw new IllegalStateException( "RecyclerView must have a LinearLayoutManager" );
            }
            int firstVisibleItem    = mLinearLayoutManager.findFirstVisibleItemPosition();
            int lastVisibleItem     = mLinearLayoutManager.findLastVisibleItemPosition();
            int totalItemCount      = mLinearLayoutManager.getItemCount();

            // Compute the offset of the first visible item from the top of the view
            View firstVisibleItemView = mLinearLayoutManager.findViewByPosition(firstVisibleItem);
            if ( mRowHeightPx == 0 )
                mRowHeightPx = firstVisibleItemView.getHeight();
            float fractionOfFirstViewVisible = Math.abs( firstVisibleItemView.getTop() / (float) mRowHeightPx );
            View lastVisibleItemView = mLinearLayoutManager.findViewByPosition(lastVisibleItem);
            float fractionOfLastViewVisible = Math.abs( (mRecyclerViewHeight - lastVisibleItemView.getTop()) / (float) mRowHeightPx );
            if ( mVisibleItemCount == 0 ) // This is constant as long as layout doesn't change
                mVisibleItemCount = (lastVisibleItem + fractionOfLastViewVisible) - (firstVisibleItem + fractionOfFirstViewVisible);

            if ( totalItemCount - mVisibleItemCount > 0  &&  ! mDragging ) {
                mThumbY = (int)((mRecyclerViewHeight - mThumbH) * (firstVisibleItem + fractionOfFirstViewVisible) / (totalItemCount - mVisibleItemCount) );
                if ( mChangedBounds ) {
                    final int viewWidth = getWidth();
                    mCurrentThumb.setBounds( viewWidth - mThumbW, 0, viewWidth, mThumbH );
                    mChangedBounds = false;
                }
            }
            mScrollFader.tickle();
        }
    }

    private static UtilLogger log = new UtilLogger( SuperSaiyanRecyclerView.class );

    // how much transparency to use for the fast scroll thumb
    private static final int ALPHA_MAX = 255;

    // how long before the fast scroll thumb disappears
    private static final long FADE_DURATION_IN  = 200;
    private static final long FADE_DURATION_OUT = 200;
    private static final long FADE_DELAY = 3000;

    private static final int THUMB_DRAWABLE = R.drawable.fastscroll_thumb_holo;

    private static final int THUMB_DRAWABLE_PRESSED = R.drawable.fastscroll_thumb_pressed_holo;

    private static final int[] STATE_PRESSED = new int[]{android.R.attr.state_pressed};

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

    private Paint mPaint;

    private int mOverlayTextColor;

    private int mOverlayDrawableResId;

    private String mSectionText;

    private boolean mDrawOverlay;

    private ScrollFader mScrollFader;

    private Handler mHandler = new Handler();

    private boolean mChangedBounds;

    private int mLastW;

    private int mLastH;

    private int mLastOldw;

    private int mLastOldh;

    public SuperSaiyanRecyclerView( Context context ) {
        super( context );

        init( context, null );
    }


    public SuperSaiyanRecyclerView( Context context, AttributeSet attrs ) {
        super( context, attrs );

        init( context, attrs );
    }

    public SuperSaiyanRecyclerView( Context context, AttributeSet attrs, int defStyle ) {
        super( context, attrs, defStyle );

        init( context, attrs );
    }

    private void useThumbDrawable( Drawable drawable ) {
        mCurrentThumb = drawable;
        mThumbW = mCurrentThumb.getIntrinsicWidth();
        mThumbH = mCurrentThumb.getIntrinsicHeight();
        mChangedBounds = true;
    }

    private void init( Context context, AttributeSet attrs ) {

        // set all attributes from xml
        if ( attrs != null ) {
            TypedArray typedArray = context.obtainStyledAttributes( attrs,
                    R.styleable.SuperSaiyanScrollView );
            mOverlayHeight = typedArray.getDimensionPixelSize(
                    R.styleable.SuperSaiyanScrollView_ssjn_overlayHeight,
                    context.getResources().getDimensionPixelSize( R.dimen.ssjn__overlay_height ) );
            mOverlayWidth = typedArray.getDimensionPixelSize(
                    R.styleable.SuperSaiyanScrollView_ssjn_overlayWidth,
                    context.getResources().getDimensionPixelSize( R.dimen.ssjn__overlay_width_normal ) );
            mOverlayTextSize = typedArray.getDimensionPixelSize(
                    R.styleable.SuperSaiyanScrollView_ssjn_overlayTextSize,
                    context.getResources().getDimensionPixelSize( R.dimen.ssjn__overlay_text_size_normal ) );
            mOverlayTextColor = typedArray.getColor( R.styleable.SuperSaiyanScrollView_ssjn_overlayTextColor,
                    context.getResources().getColor( R.color.ssjn__emphasis ) );

            int overlayTheme = typedArray.getInt( R.styleable.SuperSaiyanScrollView_ssjn_overlayTheme, 1 );

            if ( overlayTheme == 0 ) {
                // dark
                mOverlayDrawableResId = R.drawable.popup_full_dark;
            }
            else {
                // light
                mOverlayDrawableResId = R.drawable.popup_full_bright;
            }

            int schemeIndex = typedArray.getInt( R.styleable.SuperSaiyanScrollView_ssjn_overlaySizeScheme, -1 );

            if ( schemeIndex != -1 ) {
                // use the built-in size schemes
                OverlaySizeScheme scheme = OverlaySizeScheme.values()[schemeIndex];
                mOverlayTextSize = getContext().getResources().getDimensionPixelSize( scheme.getTextSize() );
                mOverlayWidth = getContext().getResources().getDimensionPixelSize( scheme.getWidth() );
            }

            typedArray.recycle();
        }
        else {
            // no attrs, so initialize with defaults
            mOverlayHeight = context.getResources().getDimensionPixelSize( R.dimen.ssjn__overlay_height );
            mOverlayWidth = context.getResources().getDimensionPixelSize( R.dimen.ssjn__overlay_width_normal );
            mOverlayTextSize = context.getResources().getDimensionPixelSize( R.dimen.ssjn__overlay_text_size_normal );
            mOverlayTextColor = context.getResources().getColor( R.color.ssjn__emphasis );
            mOverlayDrawableResId = R.drawable.popup_full_bright;
        }

        log.d( "Initialized with mOverlayHeight: %s, mOverlayWidth: %s, mOverlayTextSize: %s, mOverlayTextColor: %s",
                mOverlayHeight, mOverlayWidth, mOverlayTextSize, mOverlayTextColor );

        // Get both the scrollbar states drawables
        final Resources res = context.getResources();
        StateListDrawable thumbDrawable = new StateListDrawable();
        //This for pressed true
        thumbDrawable.addState( STATE_PRESSED, res.getDrawable( THUMB_DRAWABLE_PRESSED ) );
        //This for pressed false
        thumbDrawable.addState( STATE_UNPRESSED, res.getDrawable( THUMB_DRAWABLE ) );
        useThumbDrawable( thumbDrawable );

        mOverlayDrawable = res.getDrawable( mOverlayDrawableResId );

        setWillNotDraw( false );

        // Need to know when the RecyclerView is added
        setOnHierarchyChangeListener( this );

        mOverlayPos = new RectF();
        mScrollFader = new ScrollFader();
        mPaint = new Paint();
        mPaint.setAntiAlias( true );
        mPaint.setTextAlign( Paint.Align.CENTER );
        mPaint.setTextSize( mOverlayTextSize );
        mPaint.setColor( mOverlayTextColor );
        mPaint.setStyle( Paint.Style.FILL_AND_STROKE );
    }

    @Override
    public void draw( Canvas canvas ) {
        super.draw( canvas );

        if ( mScrollFader.mFaderState == FaderState.GONE ) { // Nothing to do
            return;
        }

        // If user is dragging the scroll bar, draw the alphabet overlay
        if ( mDragging ) {
            mCurrentThumb.setState( STATE_PRESSED );
        }
        else
            mCurrentThumb.setState( STATE_UNPRESSED );
        if ( mDragging  &&  mDrawOverlay ) {
            mOverlayDrawable.draw( canvas );
            final Paint paint = mPaint;
            float descent = paint.descent();
            final RectF rectF = mOverlayPos;

            if ( mSectionText.indexOf( '\n' ) != -1 ) { // two lines
                float textY = (int) (rectF.bottom + rectF.top) / 2 + descent - (paint.getTextSize() / 2);
                for ( String substring : StringUtil.split( mSectionText, '\n' ) ) {
                    canvas.drawText( substring, (int) (rectF.left + rectF.right) / 2, textY, paint );
                    textY += (descent + paint.getTextSize());
                }
            }
            else { // one line
                canvas.drawText( mSectionText, (int) (rectF.left + rectF.right) / 2,
                        (int) (rectF.bottom + rectF.top) / 2 + descent, paint );
            }
        }

        final int y = mThumbY;
        final int viewWidth = getWidth();

        int alpha = mScrollFader.getAlpha();
        if ( alpha < ALPHA_MAX / 2 ) {
            mCurrentThumb.setAlpha( alpha * 2 );
        }

        // Make the slider accelerate/decelerate into view
        float interpolatedPosition= mDecelerateInterpolator.getInterpolation( alpha * 1f / ALPHA_MAX );
        int left = (int)(viewWidth - mThumbW * interpolatedPosition);
        mCurrentThumb.setBounds( left, 0, viewWidth, mThumbH );
        mChangedBounds = true;

        canvas.translate( 0, y );
        mCurrentThumb.draw( canvas );
        canvas.translate( 0, -y );

        if ( mScrollFader.mFaderState != FaderState.WAITING )
            invalidateRect();
    }

    @Override
    protected void onSizeChanged( int w, int h, int oldw, int oldh ) {
        super.onSizeChanged( w, h, oldw, oldh );
        if ( mCurrentThumb != null ) {
            mCurrentThumb.setBounds( w - mThumbW, 0, w, mThumbH );
        }

        updateOverlaySize( w, h, oldw, oldh );
        mLastW = w;
        mLastH = h;
        mLastOldw = oldw;
        mLastOldh = oldh;
    }

    private void updateOverlaySize( int w, int h, int oldw, int oldh ) {
        final RectF pos = mOverlayPos;
        pos.left = (w - mOverlayWidth) / 2;
        pos.right = pos.left + mOverlayWidth;
        pos.top = h / 10; // 10% from top
        pos.bottom = pos.top + mOverlayHeight;
        mOverlayDrawable.setBounds( (int) pos.left, (int) pos.top,
                (int) pos.right, (int) pos.bottom );
    }

    public void setSections( List<String> sectionNames, List<Integer> sectionPositions ) {
        mSections = sectionNames;
        mSectionPositions = sectionPositions;
        if ( mRecyclerView.getAdapter() != null )
            mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    private int getSectionForPosition( int position ) {
        if ( mSectionPositions.size() == 0 )
            return -1;

        for ( int i = 0; i < mSectionPositions.size(); ++i ) {
            if ( mSectionPositions.get(i) > position ) {
                return Math.max(0, i-1);
            }
        }
        return Math.max(0, mSectionPositions.size() - 1);
    }

    public void onChildViewAdded( View parent, View child ) {
        if ( child instanceof RecyclerView ) {
            mRecyclerView = (RecyclerView) child;
            mRecyclerView.setOnScrollListener( mScrollListener );
        }
    }

    public void onChildViewRemoved( View parent, View child ) {
        if ( child == mRecyclerView ) {
            mRecyclerView = null;
            mLinearLayoutManager = null;
            mScrollListener = null;
            mSections.clear();
            mSectionPositions.clear();
        }
    }

    @Override
    public boolean onInterceptTouchEvent( MotionEvent ev ) {
        if ( mScrollFader.mFaderState != FaderState.GONE  &&  ev.getAction() == MotionEvent.ACTION_DOWN ) {
            if ( ev.getX() > getWidth() - mThumbW  &&  ev.getY() >= mThumbY  &&  ev.getY() <= mThumbY + mThumbH ) {
                mDragging = true;
                return true;
            }
        }
        return false;
    }

    private void scrollTo( float position ) { // Range of 0 ... 1
        int count = mRecyclerView.getAdapter().getItemCount();
        int index = (int)( position*(count- mVisibleItemCount) );
        int offset = -(int)((position*(count- mVisibleItemCount) - index) * mRowHeightPx);
        int sectionIndex = getSectionForPosition( index );
        ((LinearLayoutManager) mRecyclerView.getLayoutManager()).scrollToPositionWithOffset( index, offset );

        if ( sectionIndex >= 0 ) {
            String text = mSectionText = mSections.get(sectionIndex).toString();
            mDrawOverlay = (text.length() != 1 || text.charAt( 0 ) != ' ');
        }
        else {
            mDrawOverlay = false;
        }
    }

    private void cancelFling() {
        // Cancel the list fling
        MotionEvent cancelFling = MotionEvent.obtain( 0, 0, MotionEvent.ACTION_CANCEL, 0, 0, 0 );
        mRecyclerView.onTouchEvent( cancelFling );
        cancelFling.recycle();
    }

    @Override
    public boolean onTouchEvent( MotionEvent me ) {
        if ( me.getAction() == MotionEvent.ACTION_DOWN ) {
            if ( me.getX() > getWidth() - mThumbW  &&  me.getY() >= mThumbY  &&  me.getY() <= mThumbY + mThumbH ) {
                mDragging = true;
                mScrollFader.tickle();
                cancelFling();
                return true;
            }
        }
        else
        if ( me.getAction() == MotionEvent.ACTION_UP ) {
            if ( mDragging ) {
                mDragging = false;
                mScrollFader.tickle();
                invalidate(); // clear the overlay
                return true;
            }
        }
        else
        if ( me.getAction() == MotionEvent.ACTION_MOVE ) {
            mScrollFader.tickle();

            if ( mDragging ) {
                final int viewHeight = getHeight();
                mThumbY = (int) me.getY() - mThumbH + 10;

                if ( mThumbY < 0 ) {
                    mThumbY = 0;
                }
                else
                if ( mThumbY + mThumbH > viewHeight ) {
                    mThumbY = viewHeight - mThumbH;
                }
                scrollTo( (float) mThumbY / (viewHeight - mThumbH) );
                return true;
            }
        }

        return super.onTouchEvent( me );
    }

    private enum FaderState {
        FADING_IN,
        WAITING,
        FADING_OUT,
        GONE;
    }

    private void invalidateRect() {
        final int y = mThumbY;
        final int viewWidth = getWidth();
        invalidate( viewWidth - mThumbW, y, viewWidth, y + mThumbH );
    }

    public class ScrollFader implements Runnable {
        long mStartTime;
        private FaderState mFaderState = FaderState.GONE;
        int mAlpha = 0;

        void tickle() {
            if ( mFaderState == FaderState.FADING_OUT ) {
                mFaderState = FaderState.FADING_IN;
                // Pick up the current state
                mStartTime = SystemClock.uptimeMillis() - (long)( mAlpha * 1f / ALPHA_MAX * FADE_DURATION_IN);
            }
            else
            if ( mFaderState == FaderState.FADING_IN ) {
                // NO-OP
            }
            else
            if ( mFaderState == FaderState.WAITING ) {
                // Reset the delay
                mHandler.removeCallbacks( mScrollFader );
                mHandler.postDelayed( mScrollFader, FADE_DELAY );
            }
            else
            if ( mFaderState == FaderState.GONE ) {
                mFaderState = FaderState.FADING_IN;
                mStartTime = SystemClock.uptimeMillis();
            }
            invalidateRect();
        }

        int getAlpha() {
            if ( mFaderState == FaderState.GONE )
                mAlpha = 0;
            else
            if ( mFaderState == FaderState.WAITING )
                mAlpha = ALPHA_MAX;
            else
            if ( mFaderState == FaderState.FADING_IN ) {
                long now = SystemClock.uptimeMillis();
                if ( now > mStartTime + FADE_DURATION_IN ) {
                    mAlpha = ALPHA_MAX;
                    mFaderState = FaderState.WAITING;
                    mHandler.removeCallbacks( mScrollFader );
                    mHandler.postDelayed( mScrollFader, FADE_DELAY );
                }
                else {
                    mAlpha = (int)(((now - mStartTime) * ALPHA_MAX * 1f) / FADE_DURATION_IN);
                }
            }
            else
            if ( mFaderState == FaderState.FADING_OUT ) {
                long now = SystemClock.uptimeMillis();
                if ( now > mStartTime + FADE_DURATION_OUT ) {
                    mAlpha = 0;
                    mFaderState = FaderState.GONE;
                }
                else {
                    mAlpha = (int) (ALPHA_MAX - ((now - mStartTime) * ALPHA_MAX * 1f) / FADE_DURATION_OUT);
                };
            }
            return mAlpha;
        }

        // Start fading out
        public void run() {
            mFaderState = FaderState.FADING_OUT;
            mStartTime = SystemClock.uptimeMillis();
            invalidate();
        }
    }

    /**
     * Call this whenever the data set changes, in order to update the overlay drawable.
     */
    public void refresh() {
        mDrawOverlay = false; // avoids temporary content flash of previous sections
    }

    public void setOverlayWidth( int overlayWidth ) {
        mOverlayWidth = overlayWidth;
        updateOverlaySize( mLastW, mLastH, mLastOldw, mLastOldh );
    }


    public void setOverlayTextSize( float overlayTextSize ) {
        mOverlayTextSize = overlayTextSize;
        if ( mPaint != null ) {
            mPaint.setTextSize( mOverlayTextSize );
        }
        updateOverlaySize( mLastW, mLastH, mLastOldw, mLastOldh );
    }

    /**
     * Set a size scheme for the overlay that's shown.  This will modify both the width of the overlay window and the
     * text size.
     */
    public void setOverlaySizeScheme( OverlaySizeScheme scheme ) {
        mOverlayTextSize = getContext().getResources().getDimensionPixelSize( scheme.getTextSize() );
        if ( mPaint != null ) {
            mPaint.setTextSize( mOverlayTextSize );
        }
        mOverlayWidth = getContext().getResources().getDimensionPixelSize( scheme.getWidth() );
        updateOverlaySize( mLastW, mLastH, mLastOldw, mLastOldh );
    }
}