public class LockPatternView extends View {
    private static final int ASPECT_SQUARE = 0; 
    private static final int ASPECT_LOCK_WIDTH = 1; 
    private static final int ASPECT_LOCK_HEIGHT = 2; 
    private static final long[] DEFAULT_VIBE_PATTERN = {0, 1, 40, 41};
    private static final boolean PROFILE_DRAWING = false;
    private boolean mDrawingProfilingStarted = false;
    private Paint mPaint = new Paint();
    private Paint mPathPaint = new Paint();
    static final int STATUS_BAR_HEIGHT = 25;
    private static final int MILLIS_PER_CIRCLE_ANIMATING = 700;
    private OnPatternListener mOnPatternListener;
    private ArrayList<Cell> mPattern = new ArrayList<Cell>(9);
    private boolean[][] mPatternDrawLookup = new boolean[3][3];
    private float mInProgressX = -1;
    private float mInProgressY = -1;
    private long mAnimatingPeriodStart;
    private DisplayMode mPatternDisplayMode = DisplayMode.Correct;
    private boolean mInputEnabled = true;
    private boolean mInStealthMode = false;
    private boolean mTactileFeedbackEnabled = true;
    private boolean mPatternInProgress = false;
    private float mDiameterFactor = 0.5f;
    private float mHitFactor = 0.6f;
    private float mSquareWidth;
    private float mSquareHeight;
    private Bitmap mBitmapBtnDefault;
    private Bitmap mBitmapBtnTouched;
    private Bitmap mBitmapCircleDefault;
    private Bitmap mBitmapCircleGreen;
    private Bitmap mBitmapCircleRed;
    private Bitmap mBitmapArrowGreenUp;
    private Bitmap mBitmapArrowRedUp;
    private final Path mCurrentPath = new Path();
    private final Rect mInvalidate = new Rect();
    private int mBitmapWidth;
    private int mBitmapHeight;
    private Vibrator vibe; 
    private long[] mVibePattern;
    private int mAspect;
    public static class Cell {
        int row;
        int column;
        static Cell[][] sCells = new Cell[3][3];
        static {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    sCells[i][j] = new Cell(i, j);
                }
            }
        }
        private Cell(int row, int column) {
            checkRange(row, column);
            this.row = row;
            this.column = column;
        }
        public int getRow() {
            return row;
        }
        public int getColumn() {
            return column;
        }
        public static synchronized Cell of(int row, int column) {
            checkRange(row, column);
            return sCells[row][column];
        }
        private static void checkRange(int row, int column) {
            if (row < 0 || row > 2) {
                throw new IllegalArgumentException("row must be in range 0-2");
            }
            if (column < 0 || column > 2) {
                throw new IllegalArgumentException("column must be in range 0-2");
            }
        }
        public String toString() {
            return "(row=" + row + ",clmn=" + column + ")";
        }
    }
    public enum DisplayMode {
        Correct,
        Animate,
        Wrong
    }
    public static interface OnPatternListener {
        void onPatternStart();
        void onPatternCleared();
        void onPatternCellAdded(List<Cell> pattern);
        void onPatternDetected(List<Cell> pattern);
    }
    public LockPatternView(Context context) {
        this(context, null);
    }
    public LockPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        vibe = new Vibrator();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LockPatternView);
        final String aspect = a.getString(R.styleable.LockPatternView_aspect);
        if ("square".equals(aspect)) {
            mAspect = ASPECT_SQUARE;
        } else if ("lock_width".equals(aspect)) {
            mAspect = ASPECT_LOCK_WIDTH;
        } else if ("lock_height".equals(aspect)) {
            mAspect = ASPECT_LOCK_HEIGHT;
        } else {
            mAspect = ASPECT_SQUARE;
        }
        setClickable(true);
        mPathPaint.setAntiAlias(true);
        mPathPaint.setDither(true);
        mPathPaint.setColor(Color.WHITE);   
        mPathPaint.setAlpha(128);
        mPathPaint.setStyle(Paint.Style.STROKE);
        mPathPaint.setStrokeJoin(Paint.Join.ROUND);
        mPathPaint.setStrokeCap(Paint.Cap.ROUND);
        mBitmapBtnDefault = getBitmapFor(R.drawable.btn_code_lock_default);
        mBitmapBtnTouched = getBitmapFor(R.drawable.btn_code_lock_touched);
        mBitmapCircleDefault = getBitmapFor(R.drawable.indicator_code_lock_point_area_default);
        mBitmapCircleGreen = getBitmapFor(R.drawable.indicator_code_lock_point_area_green);
        mBitmapCircleRed = getBitmapFor(R.drawable.indicator_code_lock_point_area_red);
        mBitmapArrowGreenUp = getBitmapFor(R.drawable.indicator_code_lock_drag_direction_green_up);
        mBitmapArrowRedUp = getBitmapFor(R.drawable.indicator_code_lock_drag_direction_red_up);
        mBitmapWidth = mBitmapBtnDefault.getWidth();
        mBitmapHeight = mBitmapBtnDefault.getHeight();
        mVibePattern = loadVibratePattern(com.android.internal.R.array.config_virtualKeyVibePattern);
    }
    private long[] loadVibratePattern(int id) {
        int[] pattern = null;
        try {
            pattern = getResources().getIntArray(id);
        } catch (Resources.NotFoundException e) {
            Log.e("LockPatternView", "Vibrate pattern missing, using default", e);
        }
        if (pattern == null) {
            return DEFAULT_VIBE_PATTERN;
        }
        long[] tmpPattern = new long[pattern.length];
        for (int i = 0; i < pattern.length; i++) {
            tmpPattern[i] = pattern[i];
        }
        return tmpPattern;
    }
    private Bitmap getBitmapFor(int resId) {
        return BitmapFactory.decodeResource(getContext().getResources(), resId);
    }
    public boolean isInStealthMode() {
        return mInStealthMode;
    }
    public boolean isTactileFeedbackEnabled() {
        return mTactileFeedbackEnabled;
    }
    public void setInStealthMode(boolean inStealthMode) {
        mInStealthMode = inStealthMode;
    }
    public void setTactileFeedbackEnabled(boolean tactileFeedbackEnabled) {
        mTactileFeedbackEnabled = tactileFeedbackEnabled;
    }
    public void setOnPatternListener(
            OnPatternListener onPatternListener) {
        mOnPatternListener = onPatternListener;
    }
    public void setPattern(DisplayMode displayMode, List<Cell> pattern) {
        mPattern.clear();
        mPattern.addAll(pattern);
        clearPatternDrawLookup();
        for (Cell cell : pattern) {
            mPatternDrawLookup[cell.getRow()][cell.getColumn()] = true;
        }
        setDisplayMode(displayMode);
    }
    public void setDisplayMode(DisplayMode displayMode) {
        mPatternDisplayMode = displayMode;
        if (displayMode == DisplayMode.Animate) {
            if (mPattern.size() == 0) {
                throw new IllegalStateException("you must have a pattern to "
                        + "animate if you want to set the display mode to animate");
            }
            mAnimatingPeriodStart = SystemClock.elapsedRealtime();
            final Cell first = mPattern.get(0);
            mInProgressX = getCenterXForColumn(first.getColumn());
            mInProgressY = getCenterYForRow(first.getRow());
            clearPatternDrawLookup();
        }
        invalidate();
    }
    public void clearPattern() {
        resetPattern();
    }
    private void resetPattern() {
        mPattern.clear();
        clearPatternDrawLookup();
        mPatternDisplayMode = DisplayMode.Correct;
        invalidate();
    }
    private void clearPatternDrawLookup() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                mPatternDrawLookup[i][j] = false;
            }
        }
    }
    public void disableInput() {
        mInputEnabled = false;
    }
    public void enableInput() {
        mInputEnabled = true;
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        final int width = w - mPaddingLeft - mPaddingRight;
        mSquareWidth = width / 3.0f;
        final int height = h - mPaddingTop - mPaddingBottom;
        mSquareHeight = height / 3.0f;
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int height = MeasureSpec.getSize(heightMeasureSpec);
        int viewWidth = width;
        int viewHeight = height;
        switch (mAspect) {
            case ASPECT_SQUARE:
                viewWidth = viewHeight = Math.min(width, height);
                break;
            case ASPECT_LOCK_WIDTH:
                viewWidth = width;
                viewHeight = Math.min(width, height);
                break;
            case ASPECT_LOCK_HEIGHT:
                viewWidth = Math.min(width, height);
                viewHeight = height;
                break;
        }
        setMeasuredDimension(viewWidth, viewHeight);
    }
    private Cell detectAndAddHit(float x, float y) {
        final Cell cell = checkForNewHit(x, y);
        if (cell != null) {
            Cell fillInGapCell = null;
            final ArrayList<Cell> pattern = mPattern;
            if (!pattern.isEmpty()) {
                final Cell lastCell = pattern.get(pattern.size() - 1);
                int dRow = cell.row - lastCell.row;
                int dColumn = cell.column - lastCell.column;
                int fillInRow = lastCell.row;
                int fillInColumn = lastCell.column;
                if (Math.abs(dRow) == 2 && Math.abs(dColumn) != 1) {
                    fillInRow = lastCell.row + ((dRow > 0) ? 1 : -1);
                }
                if (Math.abs(dColumn) == 2 && Math.abs(dRow) != 1) {
                    fillInColumn = lastCell.column + ((dColumn > 0) ? 1 : -1);
                }
                fillInGapCell = Cell.of(fillInRow, fillInColumn);
            }
            if (fillInGapCell != null &&
                    !mPatternDrawLookup[fillInGapCell.row][fillInGapCell.column]) {
                addCellToPattern(fillInGapCell);
            }
            addCellToPattern(cell);
            if (mTactileFeedbackEnabled){
                vibe.vibrate(mVibePattern, -1); 
            }
            return cell;
        }
        return null;
    }
    private void addCellToPattern(Cell newCell) {
        mPatternDrawLookup[newCell.getRow()][newCell.getColumn()] = true;
        mPattern.add(newCell);
        if (mOnPatternListener != null) {
            mOnPatternListener.onPatternCellAdded(mPattern);
        }
    }
    private Cell checkForNewHit(float x, float y) {
        final int rowHit = getRowHit(y);
        if (rowHit < 0) {
            return null;
        }
        final int columnHit = getColumnHit(x);
        if (columnHit < 0) {
            return null;
        }
        if (mPatternDrawLookup[rowHit][columnHit]) {
            return null;
        }
        return Cell.of(rowHit, columnHit);
    }
    private int getRowHit(float y) {
        final float squareHeight = mSquareHeight;
        float hitSize = squareHeight * mHitFactor;
        float offset = mPaddingTop + (squareHeight - hitSize) / 2f;
        for (int i = 0; i < 3; i++) {
            final float hitTop = offset + squareHeight * i;
            if (y >= hitTop && y <= hitTop + hitSize) {
                return i;
            }
        }
        return -1;
    }
    private int getColumnHit(float x) {
        final float squareWidth = mSquareWidth;
        float hitSize = squareWidth * mHitFactor;
        float offset = mPaddingLeft + (squareWidth - hitSize) / 2f;
        for (int i = 0; i < 3; i++) {
            final float hitLeft = offset + squareWidth * i;
            if (x >= hitLeft && x <= hitLeft + hitSize) {
                return i;
            }
        }
        return -1;
    }
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!mInputEnabled || !isEnabled()) {
            return false;
        }
        final float x = motionEvent.getX();
        final float y = motionEvent.getY();
        Cell hitCell;
        switch(motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                resetPattern();
                hitCell = detectAndAddHit(x, y);
                if (hitCell != null && mOnPatternListener != null) {
                    mPatternInProgress = true;
                    mPatternDisplayMode = DisplayMode.Correct;
                    mOnPatternListener.onPatternStart();
                } else if (mOnPatternListener != null) {
                    mPatternInProgress = false;
                    mOnPatternListener.onPatternCleared();
                }
                if (hitCell != null) {
                    final float startX = getCenterXForColumn(hitCell.column);
                    final float startY = getCenterYForRow(hitCell.row);
                    final float widthOffset = mSquareWidth / 2f;
                    final float heightOffset = mSquareHeight / 2f;
                    invalidate((int) (startX - widthOffset), (int) (startY - heightOffset),
                            (int) (startX + widthOffset), (int) (startY + heightOffset));
                }
                mInProgressX = x;
                mInProgressY = y;
                if (PROFILE_DRAWING) {
                    if (!mDrawingProfilingStarted) {
                        Debug.startMethodTracing("LockPatternDrawing");
                        mDrawingProfilingStarted = true;
                    }
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (!mPattern.isEmpty() && mOnPatternListener != null) {
                    mPatternInProgress = false;
                    mOnPatternListener.onPatternDetected(mPattern);
                    invalidate();
                }
                if (PROFILE_DRAWING) {
                    if (mDrawingProfilingStarted) {
                        Debug.stopMethodTracing();
                        mDrawingProfilingStarted = false;
                    }
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                final int patternSizePreHitDetect = mPattern.size();
                hitCell = detectAndAddHit(x, y);
                final int patternSize = mPattern.size();
                if (hitCell != null && (mOnPatternListener != null) && (patternSize == 1)) {
                    mPatternInProgress = true;
                    mOnPatternListener.onPatternStart();
                }
                final float dx = Math.abs(x - mInProgressX);
                final float dy = Math.abs(y - mInProgressY);
                if (dx + dy > mSquareWidth * 0.01f) {
                    float oldX = mInProgressX;
                    float oldY = mInProgressY;
                    mInProgressX = x;
                    mInProgressY = y;
                    if (mPatternInProgress && patternSize > 0) {
                        final ArrayList<Cell> pattern = mPattern;
                        final float radius = mSquareWidth * mDiameterFactor * 0.5f;
                        final Cell lastCell = pattern.get(patternSize - 1);
                        float startX = getCenterXForColumn(lastCell.column);
                        float startY = getCenterYForRow(lastCell.row);
                        float left;
                        float top;
                        float right;
                        float bottom;
                        final Rect invalidateRect = mInvalidate;
                        if (startX < x) {
                            left = startX;
                            right = x;
                        } else {
                            left = x;
                            right = startX;
                        }
                        if (startY < y) {
                            top = startY;
                            bottom = y;
                        } else {
                            top = y;
                            bottom = startY;
                        }
                        invalidateRect.set((int) (left - radius), (int) (top - radius),
                                (int) (right + radius), (int) (bottom + radius));
                        if (startX < oldX) {
                            left = startX;
                            right = oldX;
                        } else {
                            left = oldX;
                            right = startX;
                        }
                        if (startY < oldY) {
                            top = startY;
                            bottom = oldY;
                        } else {
                            top = oldY;
                            bottom = startY;
                        }
                        invalidateRect.union((int) (left - radius), (int) (top - radius),
                                (int) (right + radius), (int) (bottom + radius));
                        if (hitCell != null) {
                            startX = getCenterXForColumn(hitCell.column);
                            startY = getCenterYForRow(hitCell.row);
                            if (patternSize >= 2) {
                                hitCell = pattern.get(patternSize - 1 - (patternSize - patternSizePreHitDetect));
                                oldX = getCenterXForColumn(hitCell.column);
                                oldY = getCenterYForRow(hitCell.row);
                                if (startX < oldX) {
                                    left = startX;
                                    right = oldX;
                                } else {
                                    left = oldX;
                                    right = startX;
                                }
                                if (startY < oldY) {
                                    top = startY;
                                    bottom = oldY;
                                } else {
                                    top = oldY;
                                    bottom = startY;
                                }
                            } else {
                                left = right = startX;
                                top = bottom = startY;
                            }
                            final float widthOffset = mSquareWidth / 2f;
                            final float heightOffset = mSquareHeight / 2f;
                            invalidateRect.set((int) (left - widthOffset),
                                    (int) (top - heightOffset), (int) (right + widthOffset),
                                    (int) (bottom + heightOffset));
                        }
                        invalidate(invalidateRect);
                    } else {
                        invalidate();
                    }
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                resetPattern();
                if (mOnPatternListener != null) {
                    mPatternInProgress = false;
                    mOnPatternListener.onPatternCleared();
                }
                if (PROFILE_DRAWING) {
                    if (mDrawingProfilingStarted) {
                        Debug.stopMethodTracing();
                        mDrawingProfilingStarted = false;
                    }
                }
                return true;
        }
        return false;
    }
    private float getCenterXForColumn(int column) {
        return mPaddingLeft + column * mSquareWidth + mSquareWidth / 2f;
    }
    private float getCenterYForRow(int row) {
        return mPaddingTop + row * mSquareHeight + mSquareHeight / 2f;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        final ArrayList<Cell> pattern = mPattern;
        final int count = pattern.size();
        final boolean[][] drawLookup = mPatternDrawLookup;
        if (mPatternDisplayMode == DisplayMode.Animate) {
            final int oneCycle = (count + 1) * MILLIS_PER_CIRCLE_ANIMATING;
            final int spotInCycle = (int) (SystemClock.elapsedRealtime() -
                    mAnimatingPeriodStart) % oneCycle;
            final int numCircles = spotInCycle / MILLIS_PER_CIRCLE_ANIMATING;
            clearPatternDrawLookup();
            for (int i = 0; i < numCircles; i++) {
                final Cell cell = pattern.get(i);
                drawLookup[cell.getRow()][cell.getColumn()] = true;
            }
            final boolean needToUpdateInProgressPoint = numCircles > 0
                    && numCircles < count;
            if (needToUpdateInProgressPoint) {
                final float percentageOfNextCircle =
                        ((float) (spotInCycle % MILLIS_PER_CIRCLE_ANIMATING)) /
                                MILLIS_PER_CIRCLE_ANIMATING;
                final Cell currentCell = pattern.get(numCircles - 1);
                final float centerX = getCenterXForColumn(currentCell.column);
                final float centerY = getCenterYForRow(currentCell.row);
                final Cell nextCell = pattern.get(numCircles);
                final float dx = percentageOfNextCircle *
                        (getCenterXForColumn(nextCell.column) - centerX);
                final float dy = percentageOfNextCircle *
                        (getCenterYForRow(nextCell.row) - centerY);
                mInProgressX = centerX + dx;
                mInProgressY = centerY + dy;
            }
            invalidate();
        }
        final float squareWidth = mSquareWidth;
        final float squareHeight = mSquareHeight;
        float radius = (squareWidth * mDiameterFactor * 0.5f);
        mPathPaint.setStrokeWidth(radius);
        final Path currentPath = mCurrentPath;
        currentPath.rewind();
        final boolean drawPath = (!mInStealthMode || mPatternDisplayMode == DisplayMode.Wrong);
        if (drawPath) {
            boolean anyCircles = false;
            for (int i = 0; i < count; i++) {
                Cell cell = pattern.get(i);
                if (!drawLookup[cell.row][cell.column]) {
                    break;
                }
                anyCircles = true;
                float centerX = getCenterXForColumn(cell.column);
                float centerY = getCenterYForRow(cell.row);
                if (i == 0) {
                    currentPath.moveTo(centerX, centerY);
                } else {
                    currentPath.lineTo(centerX, centerY);
                }
            }
            if ((mPatternInProgress || mPatternDisplayMode == DisplayMode.Animate)
                    && anyCircles) {
                currentPath.lineTo(mInProgressX, mInProgressY);
            }
            canvas.drawPath(currentPath, mPathPaint);
        }
        final int paddingTop = mPaddingTop;
        final int paddingLeft = mPaddingLeft;
        for (int i = 0; i < 3; i++) {
            float topY = paddingTop + i * squareHeight;
            for (int j = 0; j < 3; j++) {
                float leftX = paddingLeft + j * squareWidth;
                drawCircle(canvas, (int) leftX, (int) topY, drawLookup[i][j]);
            }
        }
        boolean oldFlag = (mPaint.getFlags() & Paint.FILTER_BITMAP_FLAG) != 0;
        mPaint.setFilterBitmap(true); 
        if (drawPath) {
            for (int i = 0; i < count - 1; i++) {
                Cell cell = pattern.get(i);
                Cell next = pattern.get(i + 1);
                if (!drawLookup[next.row][next.column]) {
                    break;
                }
                float leftX = paddingLeft + cell.column * squareWidth;
                float topY = paddingTop + cell.row * squareHeight;
                drawArrow(canvas, leftX, topY, cell, next);
            }
        }
        mPaint.setFilterBitmap(oldFlag); 
    }
    private void drawArrow(Canvas canvas, float leftX, float topY, Cell start, Cell end) {
        boolean green = mPatternDisplayMode != DisplayMode.Wrong;
        final int endRow = end.row;
        final int startRow = start.row;
        final int endColumn = end.column;
        final int startColumn = start.column;
        final int offsetX = ((int) mSquareWidth - mBitmapWidth) / 2;
        final int offsetY = ((int) mSquareHeight - mBitmapHeight) / 2;
        Bitmap arrow = green ? mBitmapArrowGreenUp : mBitmapArrowRedUp;
        Matrix matrix = new Matrix();
        final int cellWidth = mBitmapCircleDefault.getWidth();
        final int cellHeight = mBitmapCircleDefault.getHeight();
        final float theta = (float) Math.atan2(
                (double) (endRow - startRow), (double) (endColumn - startColumn));
        final float angle = (float) Math.toDegrees(theta) + 90.0f;
        matrix.setTranslate(leftX + offsetX, topY + offsetY); 
        matrix.preRotate(angle, cellWidth / 2.0f, cellHeight / 2.0f);  
        matrix.preTranslate((cellWidth - arrow.getWidth()) / 2.0f, 0.0f); 
        canvas.drawBitmap(arrow, matrix, mPaint);
    }
    private void drawCircle(Canvas canvas, int leftX, int topY, boolean partOfPattern) {
        Bitmap outerCircle;
        Bitmap innerCircle;
        if (!partOfPattern || (mInStealthMode && mPatternDisplayMode != DisplayMode.Wrong)) {
            outerCircle = mBitmapCircleDefault;
            innerCircle = mBitmapBtnDefault;
        } else if (mPatternInProgress) {
            outerCircle = mBitmapCircleGreen;
            innerCircle = mBitmapBtnTouched;
        } else if (mPatternDisplayMode == DisplayMode.Wrong) {
            outerCircle = mBitmapCircleRed;
            innerCircle = mBitmapBtnDefault;
        } else if (mPatternDisplayMode == DisplayMode.Correct ||
                mPatternDisplayMode == DisplayMode.Animate) {
            outerCircle = mBitmapCircleGreen;
            innerCircle = mBitmapBtnDefault;
        } else {
            throw new IllegalStateException("unknown display mode " + mPatternDisplayMode);
        }
        final int width = mBitmapWidth;
        final int height = mBitmapHeight;
        final float squareWidth = mSquareWidth;
        final float squareHeight = mSquareHeight;
        int offsetX = (int) ((squareWidth - width) / 2f);
        int offsetY = (int) ((squareHeight - height) / 2f);
        canvas.drawBitmap(outerCircle, leftX + offsetX, topY + offsetY, mPaint);
        canvas.drawBitmap(innerCircle, leftX + offsetX, topY + offsetY, mPaint);
    }
    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        return new SavedState(superState,
                LockPatternUtils.patternToString(mPattern),
                mPatternDisplayMode.ordinal(),
                mInputEnabled, mInStealthMode, mTactileFeedbackEnabled);
    }
    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        final SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setPattern(
                DisplayMode.Correct,
                LockPatternUtils.stringToPattern(ss.getSerializedPattern()));
        mPatternDisplayMode = DisplayMode.values()[ss.getDisplayMode()];
        mInputEnabled = ss.isInputEnabled();
        mInStealthMode = ss.isInStealthMode();
        mTactileFeedbackEnabled = ss.isTactileFeedbackEnabled();
    }
    private static class SavedState extends BaseSavedState {
        private final String mSerializedPattern;
        private final int mDisplayMode;
        private final boolean mInputEnabled;
        private final boolean mInStealthMode;
        private final boolean mTactileFeedbackEnabled;
        private SavedState(Parcelable superState, String serializedPattern, int displayMode,
                boolean inputEnabled, boolean inStealthMode, boolean tactileFeedbackEnabled) {
            super(superState);
            mSerializedPattern = serializedPattern;
            mDisplayMode = displayMode;
            mInputEnabled = inputEnabled;
            mInStealthMode = inStealthMode;
            mTactileFeedbackEnabled = tactileFeedbackEnabled;
        }
        private SavedState(Parcel in) {
            super(in);
            mSerializedPattern = in.readString();
            mDisplayMode = in.readInt();
            mInputEnabled = (Boolean) in.readValue(null);
            mInStealthMode = (Boolean) in.readValue(null);
            mTactileFeedbackEnabled = (Boolean) in.readValue(null);
        }
        public String getSerializedPattern() {
            return mSerializedPattern;
        }
        public int getDisplayMode() {
            return mDisplayMode;
        }
        public boolean isInputEnabled() {
            return mInputEnabled;
        }
        public boolean isInStealthMode() {
            return mInStealthMode;
        }
        public boolean isTactileFeedbackEnabled(){
            return mTactileFeedbackEnabled;
        }
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(mSerializedPattern);
            dest.writeInt(mDisplayMode);
            dest.writeValue(mInputEnabled);
            dest.writeValue(mInStealthMode);
            dest.writeValue(mTactileFeedbackEnabled);
        }
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
