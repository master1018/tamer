public class Slideshow extends SurfaceView implements SurfaceHolder.Callback {
    public Slideshow(Context context) {
        super(context);
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
    }
    public static final int SLIDESHOW_DURATION = 2000;
    public interface DataSource {
        Bitmap getBitmapForIndex(Context context, int currentSlideshowCounter);
    }
    private final Handler mHandler = new Handler();
    private final Runnable mDrawFrame = new Runnable() {
        public void run() {
            drawFrame();
        }
    };
    private static final Paint sPaint = new Paint();
    static {
        sPaint.setFilterBitmap(true);
        sPaint.setDither(true);
    }
    private boolean mVisible = true;
    private DataSource mSource;
    private int mCurrentSlideshowCounter;
    private Bitmap mBitmap;
    private Rect mRect;
    private RectF mFrameRect;
    private static final Vector3f sGrow = new Vector3f();
    private Bitmap mQueuedBitmap;
    private Rect mQueuedRect;
    private RectF mQueuedFrameRect;
    private static final Vector3f sQueuedGrow = new Vector3f();
    private long mPrevTime;
    private long mTimeElapsed;
    public void setDataSource(DataSource source) {
        mSource = source;
    }
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mHandler.post(mDrawFrame);
        if (mBitmap != null) {
            mRect = getRectToFitBitmap(mBitmap.getWidth(), mBitmap.getHeight(), width, height);
            mFrameRect.right = width;
            mFrameRect.bottom = height;
        }
        if (mQueuedBitmap != null) {
            mQueuedRect = getRectToFitBitmap(mQueuedBitmap.getWidth(), mQueuedBitmap.getHeight(), width, height);
            mQueuedFrameRect.right = width;
            mQueuedFrameRect.bottom = height;
        }
    }
    public void surfaceCreated(SurfaceHolder holder) {
        mHandler.post(mDrawFrame);
    }
    public void surfaceDestroyed(SurfaceHolder holder) {
    }
    public void drawFrame() {
        final SurfaceHolder holder = getHolder();
        Rect frame = holder.getSurfaceFrame();
        final Paint paint = sPaint;
        Canvas c = null;
        try {
            c = holder.lockCanvas();
            if (c != null) {
                long now = SystemClock.uptimeMillis();
                long delta = now - mPrevTime;
                if (delta > 50)
                    delta = 50;
                mTimeElapsed += delta;
                mPrevTime = now;
                performSetup(frame.width(), frame.height());
                if (mBitmap != null) {
                    if (mTimeElapsed > SLIDESHOW_DURATION) {
                        float alpha = ((float) (mTimeElapsed - SLIDESHOW_DURATION)) / 2000.0f;
                        paint.setColorFilter(null);
                        if (alpha < 1.0f) {
                        }
                        c.drawBitmap(mBitmap, mRect, mFrameRect, paint);
                        if (alpha < 1.0f) {
                            int val = (int) (255 * alpha);
                            int srcColor = Color.argb(val, 0, 0, 0);
                            PorterDuffColorFilter colorFilter = new PorterDuffColorFilter(srcColor, Mode.DST_IN);
                            paint.setColorFilter(colorFilter);
                        }
                        c.drawBitmap(mQueuedBitmap, mQueuedRect, mQueuedFrameRect, paint);
                        performUpdate(mQueuedFrameRect, sQueuedGrow, delta);
                        if (alpha >= 1.0f) {
                            mRect = mQueuedRect;
                            mBitmap = mQueuedBitmap;
                            mFrameRect = mQueuedFrameRect;
                            sGrow.set(sQueuedGrow);
                            mQueuedBitmap = null;
                            mQueuedRect = null;
                            mQueuedFrameRect = null;
                            mTimeElapsed = 0;
                        }
                    } else {
                        paint.setColorFilter(null);
                        c.drawBitmap(mBitmap, mRect, mFrameRect, paint);
                    }
                    performUpdate(mFrameRect, sGrow, delta);
                }
            }
        } finally {
            if (c != null)
                holder.unlockCanvasAndPost(c);
        }
        mHandler.removeCallbacks(mDrawFrame);
        if (mVisible) {
            mHandler.postDelayed(mDrawFrame, 20);
        }
    }
    private void performUpdate(RectF rect, Vector3f grow, long delta) {
        float timeElapsed = ((float) (delta)) / 1000.0f;
        float amountToGrowX = timeElapsed * (rect.width() / 15.0f);
        float amountToGrowY = amountToGrowX * (rect.height() / rect.width());
        rect.top -= amountToGrowY * grow.x;
        rect.left -= amountToGrowX * grow.y;
        rect.bottom += amountToGrowY * (1 - grow.x);
        rect.right += amountToGrowX * (1 - grow.y);
    }
    private void performSetup(int viewWidth, int viewHeight) {
        if (mBitmap == null) {
            mBitmap = getRandomBitmap();
            if (mBitmap != null) {
                mRect = getRectToFitBitmap(mBitmap.getWidth(), mBitmap.getHeight(), viewWidth, viewHeight);
                mFrameRect = new RectF();
                mFrameRect.right = viewWidth;
                mFrameRect.bottom = viewHeight;
                sGrow.set((float) Math.random(), (float) Math.random(), 0);
            }
        }
        if (mQueuedBitmap == null) {
            mQueuedBitmap = getRandomBitmap();
            if (mQueuedBitmap == null) {
                mQueuedBitmap = mBitmap;
            }
            if (mQueuedBitmap != null) {
                mQueuedRect = getRectToFitBitmap(mQueuedBitmap.getWidth(), mQueuedBitmap.getHeight(), viewWidth, viewHeight);
                mQueuedFrameRect = new RectF();
                mQueuedFrameRect.right = viewWidth;
                mQueuedFrameRect.bottom = viewHeight;
                sQueuedGrow.set((float) Math.random(), (float) Math.random(), 0);
            }
        }
    }
    private Rect getRectToFitBitmap(int bitmapWidth, int bitmapHeight, int viewWidth, int viewHeight) {
        Rect rect = new Rect();
        float viewAspect = (float) viewHeight / viewWidth;
        float newWidth = bitmapWidth * viewAspect;
        if (bitmapHeight < newWidth) {
            newWidth = bitmapHeight / viewAspect;
            rect.set((int) (bitmapWidth / 2 - newWidth / 2), 0, (int) (bitmapWidth / 2 + newWidth / 2), bitmapHeight);
        } else {
            float newHeight = bitmapWidth * viewAspect;
            rect.set(0, (int) (bitmapHeight / 2 - newHeight / 2), bitmapWidth, (int) (bitmapHeight / 2 + newHeight / 2));
        }
        return rect;
    }
    private Bitmap getRandomBitmap() {
        if (mSource != null) {
            return mSource.getBitmapForIndex(getContext(), mCurrentSlideshowCounter++);
        }
        return null;
    }
    public void onVisibilityChanged(boolean visible) {
        mVisible = visible;
        if (!visible) {
            mHandler.removeCallbacks(mDrawFrame);
        }
        drawFrame();
    }
}
