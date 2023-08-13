class GridViewSpecial extends View {
    @SuppressWarnings("unused")
    private static final String TAG = "GridViewSpecial";
    private static final float MAX_FLING_VELOCITY = 2500;
    public static interface Listener {
        public void onImageClicked(int index);
        public void onImageTapped(int index);
        public void onLayoutComplete(boolean changed);
        public void onScroll(float scrollPosition);
    }
    public static interface DrawAdapter {
        public void drawImage(Canvas canvas, IImage image,
                Bitmap b, int xPos, int yPos, int w, int h);
        public void drawDecoration(Canvas canvas, IImage image,
                int xPos, int yPos, int w, int h);
        public boolean needsDecoration();
    }
    public static final int INDEX_NONE = -1;
    static class LayoutSpec {
        LayoutSpec(int w, int h, int intercellSpacing, int leftEdgePadding,
                DisplayMetrics metrics) {
            mCellWidth = dpToPx(w, metrics);
            mCellHeight = dpToPx(h, metrics);
            mCellSpacing = dpToPx(intercellSpacing, metrics);
            mLeftEdgePadding = dpToPx(leftEdgePadding, metrics);
        }
        int mCellWidth, mCellHeight;
        int mCellSpacing;
        int mLeftEdgePadding;
    }
    private LayoutSpec [] mCellSizeChoices;
    private void initCellSize() {
        Activity a = (Activity) getContext();
        DisplayMetrics metrics = new DisplayMetrics();
        a.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mCellSizeChoices = new LayoutSpec[] {
            new LayoutSpec(67, 67, 8, 0, metrics),
            new LayoutSpec(92, 92, 8, 0, metrics),
        };
    }
    private static int dpToPx(int dp, DisplayMetrics metrics) {
        return (int) (metrics.density * dp);
    }
    private final Handler mHandler = new Handler();
    private GestureDetector mGestureDetector;
    private ImageBlockManager mImageBlockManager;
    private ImageLoader mLoader;
    private Listener mListener = null;
    private DrawAdapter mDrawAdapter = null;
    private IImageList mAllImages = ImageManager.makeEmptyImageList();
    private int mSizeChoice = 1;  
    private LayoutSpec mSpec;
    private int mColumns;
    private int mMaxScrollY;
    private boolean mLayoutComplete = false;
    private int mCurrentSelection = INDEX_NONE;
    private int mCurrentPressState = 0;
    private static final int TAPPING_FLAG = 1;
    private static final int CLICKING_FLAG = 2;
    private int mCount;  
    private int mRows;  
    private int mBlockHeight; 
    private boolean mRunning = false;
    private Scroller mScroller = null;
    public GridViewSpecial(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }
    private void init(Context context) {
        setVerticalScrollBarEnabled(true);
        initializeScrollbars(context.obtainStyledAttributes(
                android.R.styleable.View));
        mGestureDetector = new GestureDetector(context,
                new MyGestureDetector());
        setFocusableInTouchMode(true);
        initCellSize();
    }
    private final Runnable mRedrawCallback = new Runnable() {
                public void run() {
                    invalidate();
                }
            };
    public void setLoader(ImageLoader loader) {
        Assert(mRunning == false);
        mLoader = loader;
    }
    public void setListener(Listener listener) {
        Assert(mRunning == false);
        mListener = listener;
    }
    public void setDrawAdapter(DrawAdapter adapter) {
        Assert(mRunning == false);
        mDrawAdapter = adapter;
    }
    public void setImageList(IImageList list) {
        Assert(mRunning == false);
        mAllImages = list;
        mCount = mAllImages.getCount();
    }
    public void setSizeChoice(int choice) {
        Assert(mRunning == false);
        if (mSizeChoice == choice) return;
        mSizeChoice = choice;
    }
    @Override
    public void onLayout(boolean changed, int left, int top,
                         int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (!mRunning) {
            return;
        }
        mSpec = mCellSizeChoices[mSizeChoice];
        int width = right - left;
        mColumns = 1 + (width - mSpec.mCellWidth)
                / (mSpec.mCellWidth + mSpec.mCellSpacing);
        mSpec.mLeftEdgePadding = (width
                - ((mColumns - 1) * mSpec.mCellSpacing)
                - (mColumns * mSpec.mCellWidth)) / 2;
        mRows = (mCount + mColumns - 1) / mColumns;
        mBlockHeight = mSpec.mCellSpacing + mSpec.mCellHeight;
        mMaxScrollY = mSpec.mCellSpacing + (mRows * mBlockHeight)
                - (bottom - top);
        mScrollY = Math.max(0, Math.min(mMaxScrollY, mScrollY));
        generateOutlineBitmap();
        if (mImageBlockManager != null) {
            mImageBlockManager.recycle();
        }
        mImageBlockManager = new ImageBlockManager(mHandler, mRedrawCallback,
                mAllImages, mLoader, mDrawAdapter, mSpec, mColumns, width,
                mOutline[OUTLINE_EMPTY]);
        mListener.onLayoutComplete(changed);
        moveDataWindow();
        mLayoutComplete = true;
    }
    @Override
    protected int computeVerticalScrollRange() {
        return mMaxScrollY + getHeight();
    }
    public static final int OUTLINE_EMPTY = 0;
    public static final int OUTLINE_PRESSED = 1;
    public static final int OUTLINE_SELECTED = 2;
    public Bitmap mOutline[] = new Bitmap[3];
    private void generateOutlineBitmap() {
        int w = mSpec.mCellWidth;
        int h = mSpec.mCellHeight;
        for (int i = 0; i < mOutline.length; i++) {
            mOutline[i] = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        }
        Drawable cellOutline;
        cellOutline = GridViewSpecial.this.getResources()
                .getDrawable(android.R.drawable.gallery_thumb);
        cellOutline.setBounds(0, 0, w, h);
        Canvas canvas = new Canvas();
        canvas.setBitmap(mOutline[OUTLINE_EMPTY]);
        cellOutline.setState(EMPTY_STATE_SET);
        cellOutline.draw(canvas);
        canvas.setBitmap(mOutline[OUTLINE_PRESSED]);
        cellOutline.setState(
                PRESSED_ENABLED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET);
        cellOutline.draw(canvas);
        canvas.setBitmap(mOutline[OUTLINE_SELECTED]);
        cellOutline.setState(ENABLED_FOCUSED_SELECTED_WINDOW_FOCUSED_STATE_SET);
        cellOutline.draw(canvas);
    }
    private void moveDataWindow() {
        int startRow = (mScrollY - mSpec.mCellSpacing) / mBlockHeight;
        int endRow = (mScrollY + getHeight() - mSpec.mCellSpacing - 1)
                / mBlockHeight + 1;
        startRow = Math.max(Math.min(startRow, mRows - 1), 0);
        endRow = Math.max(Math.min(endRow, mRows), 0);
        mImageBlockManager.setVisibleRows(startRow, endRow);
    }
    private class MyGestureDetector extends SimpleOnGestureListener {
        private AudioManager mAudioManager;
        @Override
        public boolean onDown(MotionEvent e) {
            if (!canHandleEvent()) return false;
            if (mScroller != null && !mScroller.isFinished()) {
                mScroller.forceFinished(true);
                return false;
            }
            int index = computeSelectedIndex(e.getX(), e.getY());
            if (index >= 0 && index < mCount) {
                setSelectedIndex(index);
            } else {
                setSelectedIndex(INDEX_NONE);
            }
            return true;
        }
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2,
                float velocityX, float velocityY) {
            if (!canHandleEvent()) return false;
            if (velocityY > MAX_FLING_VELOCITY) {
                velocityY = MAX_FLING_VELOCITY;
            } else if (velocityY < -MAX_FLING_VELOCITY) {
                velocityY = -MAX_FLING_VELOCITY;
            }
            setSelectedIndex(INDEX_NONE);
            mScroller = new Scroller(getContext());
            mScroller.fling(0, mScrollY, 0, -(int) velocityY, 0, 0, 0,
                    mMaxScrollY);
            computeScroll();
            return true;
        }
        @Override
        public void onLongPress(MotionEvent e) {
            if (!canHandleEvent()) return;
            performLongClick();
        }
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            if (!canHandleEvent()) return false;
            setSelectedIndex(INDEX_NONE);
            scrollBy(0, (int) distanceY);
            invalidate();
            return true;
        }
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (!canHandleEvent()) return false;
            int index = computeSelectedIndex(e.getX(), e.getY());
            if (index >= 0 && index < mCount) {
                if (mAudioManager == null) {
                    mAudioManager = (AudioManager) getContext()
                            .getSystemService(Context.AUDIO_SERVICE);
                }
                mAudioManager.playSoundEffect(AudioManager.FX_KEY_CLICK);
                mListener.onImageTapped(index);
                return true;
            }
            return false;
        }
    }
    public int getCurrentSelection() {
        return mCurrentSelection;
    }
    public void invalidateImage(int index) {
        if (index != INDEX_NONE) {
            mImageBlockManager.invalidateImage(index);
        }
    }
    public void setSelectedIndex(int index) {
        if (mCurrentSelection == index) {
            return;
        }
        mCurrentSelection = Math.min(index, mCount - 1);
        if (mCurrentSelection != INDEX_NONE) {
            ensureVisible(mCurrentSelection);
        }
        invalidate();
    }
    public void scrollToImage(int index) {
        Rect r = getRectForPosition(index);
        scrollTo(0, r.top);
    }
    public void scrollToVisible(int index) {
        Rect r = getRectForPosition(index);
        int top = getScrollY();
        int bottom = getScrollY() + getHeight();
        if (r.bottom > bottom) {
            scrollTo(0, r.bottom - getHeight());
        } else if (r.top < top) {
            scrollTo(0, r.top);
        }
    }
    private void ensureVisible(int pos) {
        Rect r = getRectForPosition(pos);
        int top = getScrollY();
        int bot = top + getHeight();
        if (r.bottom > bot) {
            mScroller = new Scroller(getContext());
            mScroller.startScroll(mScrollX, mScrollY, 0,
                    r.bottom - getHeight() - mScrollY, 200);
            computeScroll();
        } else if (r.top < top) {
            mScroller = new Scroller(getContext());
            mScroller.startScroll(mScrollX, mScrollY, 0, r.top - mScrollY, 200);
            computeScroll();
        }
    }
    public void start() {
        Assert(mLoader != null);
        Assert(mListener != null);
        Assert(mDrawAdapter != null);
        mRunning = true;
        requestLayout();
    }
    public void stop() {
        mHandler.removeCallbacks(mLongPressCallback);
        mScroller = null;
        if (mImageBlockManager != null) {
            mImageBlockManager.recycle();
            mImageBlockManager = null;
        }
        mRunning = false;
        mCurrentSelection = INDEX_NONE;
    }
    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!canHandleEvent()) return;
        mImageBlockManager.doDraw(canvas, getWidth(), getHeight(), mScrollY);
        paintDecoration(canvas);
        paintSelection(canvas);
        moveDataWindow();
    }
    @Override
    public void computeScroll() {
        if (mScroller != null) {
            boolean more = mScroller.computeScrollOffset();
            scrollTo(0, mScroller.getCurrY());
            if (more) {
                invalidate();  
            } else {
                mScroller = null;
            }
        } else {
            super.computeScroll();
        }
    }
    Rect getRectForPosition(int pos) {
        int row = pos / mColumns;
        int col = pos - (row * mColumns);
        int left = mSpec.mLeftEdgePadding
                + (col * (mSpec.mCellWidth + mSpec.mCellSpacing));
        int top = row * mBlockHeight;
        return new Rect(left, top,
                left + mSpec.mCellWidth + mSpec.mCellSpacing,
                top + mSpec.mCellHeight + mSpec.mCellSpacing);
    }
    int computeSelectedIndex(float xFloat, float yFloat) {
        int x = (int) xFloat;
        int y = (int) yFloat;
        int spacing = mSpec.mCellSpacing;
        int leftSpacing = mSpec.mLeftEdgePadding;
        int row = (mScrollY + y - spacing) / (mSpec.mCellHeight + spacing);
        int col = Math.min(mColumns - 1,
                (x - leftSpacing) / (mSpec.mCellWidth + spacing));
        return (row * mColumns) + col;
    }
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (!canHandleEvent()) {
            return false;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mCurrentPressState |= TAPPING_FLAG;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                mCurrentPressState &= ~TAPPING_FLAG;
                invalidate();
                break;
        }
        mGestureDetector.onTouchEvent(ev);
        return true;
    }
    @Override
    public void scrollBy(int x, int y) {
        scrollTo(mScrollX + x, mScrollY + y);
    }
    public void scrollTo(float scrollPosition) {
        scrollTo(0, Math.round(scrollPosition * mMaxScrollY));
    }
    @Override
    public void scrollTo(int x, int y) {
        y = Math.max(0, Math.min(mMaxScrollY, y));
        if (mSpec != null) {
            mListener.onScroll((float) mScrollY / mMaxScrollY);
        }
        super.scrollTo(x, y);
    }
    private boolean canHandleEvent() {
        return mRunning && mLayoutComplete;
    }
    private final Runnable mLongPressCallback = new Runnable() {
        public void run() {
            mCurrentPressState &= ~CLICKING_FLAG;
            showContextMenu();
        }
    };
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (!canHandleEvent()) return false;
        int sel = mCurrentSelection;
        if (sel != INDEX_NONE) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    if (sel != mCount - 1 && (sel % mColumns < mColumns - 1)) {
                        sel += 1;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    if (sel > 0 && (sel % mColumns != 0)) {
                        sel -= 1;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    if (sel >= mColumns) {
                        sel -= mColumns;
                    }
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    sel = Math.min(mCount - 1, sel + mColumns);
                    break;
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    if (event.getRepeatCount() == 0) {
                        mCurrentPressState |= CLICKING_FLAG;
                        mHandler.postDelayed(mLongPressCallback,
                                ViewConfiguration.getLongPressTimeout());
                    }
                    break;
                default:
                    return super.onKeyDown(keyCode, event);
            }
        } else {
            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                case KeyEvent.KEYCODE_DPAD_LEFT:
                case KeyEvent.KEYCODE_DPAD_UP:
                case KeyEvent.KEYCODE_DPAD_DOWN:
                        int startRow =
                                (mScrollY - mSpec.mCellSpacing) / mBlockHeight;
                        int topPos = startRow * mColumns;
                        Rect r = getRectForPosition(topPos);
                        if (r.top < getScrollY()) {
                            topPos += mColumns;
                        }
                        topPos = Math.min(mCount - 1, topPos);
                        sel = topPos;
                    break;
                default:
                    return super.onKeyDown(keyCode, event);
            }
        }
        setSelectedIndex(sel);
        return true;
    }
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!canHandleEvent()) return false;
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            mCurrentPressState &= ~CLICKING_FLAG;
            invalidate();
            mHandler.removeCallbacks(mLongPressCallback);
            mListener.onImageClicked(mCurrentSelection);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
    private void paintDecoration(Canvas canvas) {
        if (!mDrawAdapter.needsDecoration()) return;
        int startRow = (mScrollY - mSpec.mCellSpacing) / mBlockHeight;
        int endRow = (mScrollY + getHeight() - mSpec.mCellSpacing - 1)
                / mBlockHeight + 1;
        startRow = Math.max(Math.min(startRow, mRows - 1), 0);
        endRow = Math.max(Math.min(endRow, mRows), 0);
        int startIndex = startRow * mColumns;
        int endIndex = Math.min(endRow * mColumns, mCount);
        int xPos = mSpec.mLeftEdgePadding;
        int yPos = mSpec.mCellSpacing + startRow * mBlockHeight;
        int off = 0;
        for (int i = startIndex; i < endIndex; i++) {
            IImage image = mAllImages.getImageAt(i);
            mDrawAdapter.drawDecoration(canvas, image, xPos, yPos,
                    mSpec.mCellWidth, mSpec.mCellHeight);
            off += 1;
            if (off == mColumns) {
                xPos = mSpec.mLeftEdgePadding;
                yPos += mBlockHeight;
                off = 0;
            } else {
                xPos += mSpec.mCellWidth + mSpec.mCellSpacing;
            }
        }
    }
    private void paintSelection(Canvas canvas) {
        if (mCurrentSelection == INDEX_NONE) return;
        int row = mCurrentSelection / mColumns;
        int col = mCurrentSelection - (row * mColumns);
        int spacing = mSpec.mCellSpacing;
        int leftSpacing = mSpec.mLeftEdgePadding;
        int xPos = leftSpacing + (col * (mSpec.mCellWidth + spacing));
        int yTop = spacing + (row * mBlockHeight);
        int type = OUTLINE_SELECTED;
        if (mCurrentPressState != 0) {
            type = OUTLINE_PRESSED;
        }
        canvas.drawBitmap(mOutline[type], xPos, yTop, null);
    }
}
class ImageBlockManager {
    @SuppressWarnings("unused")
    private static final String TAG = "ImageBlockManager";
    private static final int CACHE_ROWS = 30;
    private final HashMap<Integer, ImageBlock> mCache;
    private final Handler mHandler;
    private final Runnable mRedrawCallback;  
    private final IImageList mImageList;
    private final ImageLoader mLoader;
    private final GridViewSpecial.DrawAdapter mDrawAdapter;
    private final GridViewSpecial.LayoutSpec mSpec;
    private final int mColumns;  
    private final int mBlockWidth;  
    private final Bitmap mOutline;  
    private final int mCount;  
    private final int mRows;  
    private final int mBlockHeight;  
    private int mStartRow = 0;
    private int mEndRow = 0;
    ImageBlockManager(Handler handler, Runnable redrawCallback,
            IImageList imageList, ImageLoader loader,
            GridViewSpecial.DrawAdapter adapter,
            GridViewSpecial.LayoutSpec spec,
            int columns, int blockWidth, Bitmap outline) {
        mHandler = handler;
        mRedrawCallback = redrawCallback;
        mImageList = imageList;
        mLoader = loader;
        mDrawAdapter = adapter;
        mSpec = spec;
        mColumns = columns;
        mBlockWidth = blockWidth;
        mOutline = outline;
        mBlockHeight = mSpec.mCellSpacing + mSpec.mCellHeight;
        mCount = imageList.getCount();
        mRows = (mCount + mColumns - 1) / mColumns;
        mCache = new HashMap<Integer, ImageBlock>();
        mPendingRequest = 0;
        initGraphics();
    }
    public void setVisibleRows(int startRow, int endRow) {
        if (startRow != mStartRow || endRow != mEndRow) {
            mStartRow = startRow;
            mEndRow = endRow;
            startLoading();
        }
    }
    int mPendingRequest;  
    static final int REQUESTS_LOW = 3;
    static final int REQUESTS_HIGH = 6;
    private void startLoading() {
        clearLoaderQueue();
        continueLoading();
    }
    private void clearLoaderQueue() {
        int[] tags = mLoader.clearQueue();
        for (int pos : tags) {
            int row = pos / mColumns;
            int col = pos - row * mColumns;
            ImageBlock blk = mCache.get(row);
            Assert(blk != null);  
            blk.cancelRequest(col);
        }
    }
    private void continueLoading() {
        if (mPendingRequest >= REQUESTS_LOW) return;
        for (int i = mStartRow; i < mEndRow; i++) {
            if (scanOne(i)) return;
        }
        int range = (CACHE_ROWS - (mEndRow - mStartRow)) / 2;
        for (int d = 1; d <= range; d++) {
            int after = mEndRow - 1 + d;
            int before = mStartRow - d;
            if (after >= mRows && before < 0) {
                break;  
            }
            if (after < mRows && scanOne(after)) return;
            if (before >= 0 && scanOne(before)) return;
        }
    }
    private boolean scanOne(int i) {
        mPendingRequest += tryToLoad(i);
        return mPendingRequest >= REQUESTS_HIGH;
    }
    private int tryToLoad(int row) {
        Assert(row >= 0 && row < mRows);
        ImageBlock blk = mCache.get(row);
        if (blk == null) {
            blk = getEmptyBlock();
            blk.setRow(row);
            blk.invalidate();
            mCache.put(row, blk);
        }
        return blk.loadImages();
    }
    private ImageBlock getEmptyBlock() {
        if (mCache.size() < CACHE_ROWS) {
            return new ImageBlock();
        }
        int bestDistance = -1;
        int bestIndex = -1;
        for (int index : mCache.keySet()) {
            if (mCache.get(index).hasPendingRequests()) {
                continue;
            }
            int dist = 0;
            if (index >= mEndRow) {
                dist = index - mEndRow + 1;
            } else if (index < mStartRow) {
                dist = mStartRow - index;
            } else {
                continue;
            }
            if (dist > bestDistance) {
                bestDistance = dist;
                bestIndex = index;
            }
        }
        return mCache.remove(bestIndex);
    }
    public void invalidateImage(int index) {
        int row = index / mColumns;
        int col = index - (row * mColumns);
        ImageBlock blk = mCache.get(row);
        if (blk == null) return;
        if ((blk.mCompletedMask & (1 << col)) != 0) {
            blk.mCompletedMask &= ~(1 << col);
        }
        startLoading();
    }
    public void recycle() {
        for (ImageBlock blk : mCache.values()) {
            blk.recycle();
        }
        mCache.clear();
        mEmptyBitmap.recycle();
    }
    public void doDraw(Canvas canvas, int thisWidth, int thisHeight,
            int scrollPos) {
        final int height = mBlockHeight;
        int currentBlock = (scrollPos < 0)
                ? ((scrollPos - height + 1) / height)
                : (scrollPos / height);
        while (true) {
            final int yPos = currentBlock * height;
            if (yPos >= scrollPos + thisHeight) {
                break;
            }
            ImageBlock blk = mCache.get(currentBlock);
            if (blk != null) {
                blk.doDraw(canvas, 0, yPos);
            } else {
                drawEmptyBlock(canvas, 0, yPos, currentBlock);
            }
            currentBlock += 1;
        }
    }
    private int numColumns(int row) {
        return Math.min(mColumns, mCount - row * mColumns);
    }
    private void drawEmptyBlock(Canvas canvas, int xPos, int yPos, int row) {
        canvas.drawRect(xPos, yPos, xPos + mBlockWidth, yPos + mBlockHeight,
                mBackgroundPaint);
        int x = xPos + mSpec.mLeftEdgePadding;
        int y = yPos + mSpec.mCellSpacing;
        int cols = numColumns(row);
        for (int i = 0; i < cols; i++) {
            canvas.drawBitmap(mEmptyBitmap, x, y, null);
            x += (mSpec.mCellWidth + mSpec.mCellSpacing);
        }
    }
    Paint mBackgroundPaint;
    private Bitmap mEmptyBitmap;
    private void initGraphics() {
        mBackgroundPaint = new Paint();
        mBackgroundPaint.setStyle(Paint.Style.FILL);
        mBackgroundPaint.setColor(0xFF000000);  
        mEmptyBitmap = Bitmap.createBitmap(mSpec.mCellWidth, mSpec.mCellHeight,
                Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(mEmptyBitmap);
        canvas.drawRGB(0xDD, 0xDD, 0xDD);
        canvas.drawBitmap(mOutline, 0, 0, null);
    }
    private class ImageBlock {
        private Bitmap mBitmap;
        private final Canvas mCanvas;
        private int mRequestedMask;
        private int mCompletedMask;
        private int mRow;
        public ImageBlock() {
            mBitmap = Bitmap.createBitmap(mBlockWidth, mBlockHeight,
                    Bitmap.Config.RGB_565);
            mCanvas = new Canvas(mBitmap);
            mRow = -1;
        }
        public void setRow(int row) {
            mRow = row;
        }
        public void invalidate() {
            mCompletedMask = 0;
        }
        public void recycle() {
            cancelAllRequests();
            mBitmap.recycle();
            mBitmap = null;
        }
        private boolean isVisible() {
            return mRow >= mStartRow && mRow < mEndRow;
        }
        public int loadImages() {
            Assert(mRow != -1);
            int columns = numColumns(mRow);
            int needMask = ((1 << columns) - 1)
                    & ~(mCompletedMask | mRequestedMask);
            if (needMask == 0) {
                return 0;
            }
            int retVal = 0;
            int base = mRow * mColumns;
            for (int col = 0; col < columns; col++) {
                if ((needMask & (1 << col)) == 0) {
                    continue;
                }
                int pos = base + col;
                final IImage image = mImageList.getImageAt(pos);
                if (image != null) {
                    final int colFinal = col;
                    ImageLoader.LoadedCallback cb =
                            new ImageLoader.LoadedCallback() {
                                    public void run(final Bitmap b) {
                                        mHandler.post(new Runnable() {
                                            public void run() {
                                                loadImageDone(image, b,
                                                        colFinal);
                                            }
                                        });
                                    }
                                };
                    mLoader.getBitmap(image, cb, pos);
                    mRequestedMask |= (1 << col);
                    retVal += 1;
                }
            }
            return retVal;
        }
        public boolean hasPendingRequests() {
            return mRequestedMask != 0;
        }
        private void loadImageDone(IImage image, Bitmap b,
                int col) {
            if (mBitmap == null) return;  
            int spacing = mSpec.mCellSpacing;
            int leftSpacing = mSpec.mLeftEdgePadding;
            final int yPos = spacing;
            final int xPos = leftSpacing
                    + (col * (mSpec.mCellWidth + spacing));
            drawBitmap(image, b, xPos, yPos);
            if (b != null) {
                b.recycle();
            }
            int mask = (1 << col);
            Assert((mCompletedMask & mask) == 0);
            Assert((mRequestedMask & mask) != 0);
            mRequestedMask &= ~mask;
            mCompletedMask |= mask;
            mPendingRequest--;
            if (isVisible()) {
                mRedrawCallback.run();
            }
            continueLoading();
        }
        private void drawBitmap(
                IImage image, Bitmap b, int xPos, int yPos) {
            mDrawAdapter.drawImage(mCanvas, image, b, xPos, yPos,
                    mSpec.mCellWidth, mSpec.mCellHeight);
            mCanvas.drawBitmap(mOutline, xPos, yPos, null);
        }
        public void doDraw(Canvas canvas, int xPos, int yPos) {
            int cols = numColumns(mRow);
            if (cols == mColumns) {
                canvas.drawBitmap(mBitmap, xPos, yPos, null);
            } else {
                canvas.drawRect(xPos, yPos, xPos + mBlockWidth,
                        yPos + mBlockHeight, mBackgroundPaint);
                int w = mSpec.mLeftEdgePadding
                        + cols * (mSpec.mCellWidth + mSpec.mCellSpacing);
                Rect srcRect = new Rect(0, 0, w, mBlockHeight);
                Rect dstRect = new Rect(srcRect);
                dstRect.offset(xPos, yPos);
                canvas.drawBitmap(mBitmap, srcRect, dstRect, null);
            }
            int isEmpty = ((1 << cols) - 1) & ~mCompletedMask;
            if (isEmpty != 0) {
                int x = xPos + mSpec.mLeftEdgePadding;
                int y = yPos + mSpec.mCellSpacing;
                for (int i = 0; i < cols; i++) {
                    if ((isEmpty & (1 << i)) != 0) {
                        canvas.drawBitmap(mEmptyBitmap, x, y, null);
                    }
                    x += (mSpec.mCellWidth + mSpec.mCellSpacing);
                }
            }
        }
        public void cancelRequest(int col) {
            int mask = (1 << col);
            Assert((mRequestedMask & mask) != 0);
            mRequestedMask &= ~mask;
            mPendingRequest--;
        }
        private void cancelAllRequests() {
            for (int i = 0; i < mColumns; i++) {
                int mask = (1 << i);
                if ((mRequestedMask & mask) != 0) {
                    int pos = (mRow * mColumns) + i;
                    if (mLoader.cancel(mImageList.getImageAt(pos))) {
                        mRequestedMask &= ~mask;
                        mPendingRequest--;
                    }
                }
            }
        }
    }
}
